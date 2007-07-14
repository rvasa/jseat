package metric.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import metric.core.io.InputDataSet;
import metric.core.io.TextFile;
import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.VersionMetricData;
import metric.core.util.StatUtils;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Evolution;
import metric.core.vocabulary.Version;

/**
 * Responsible for processing a *.versions file and returning a representive
 * metric model in the form of <code>HistoryMetricData</code>. The main
 * method invocation for this class is the build() method, which builds a metric
 * model for the filename the <code>MetricModelBuilder</code> was constructed
 * with.
 * 
 * Alternatively, a static method is provided for updating a versions map. Given
 * a HashMap of Versions, it will recompute dependencie related information.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class MetricModelBuilder extends Observable
{
	private static Logger logger = Logger.getLogger(MetricModelBuilder.class
			.getSimpleName());

	private File filename;
	private LinkedList<InputDataSet> dataSetList;

	private BlockingQueue<VersionMetricData> versions;

	private String productName;
	private long bytesProcessed;
	private long loadTime;
	private long extractTime;
	private long processTime;

	private int workDone;
	private int workToProcess;
	private int completion;

	private ThreadPoolExecutor extractionWorkers;
	private BlockingQueue<Runnable> workQueue;

	public boolean interrupted;

	// Loading, extracting, processing. But we count extracting as 3,
	// Because on average, it takes twice as long.
	private final int NUM_OF_STAGES = 5;

	/**
     * Initialises the MetricModelBuilder with the specified versions file.
     * Version processing is scaled across the specified number of threads.
     * 
     * I.E. 5 Threads will process 5 versions concurrently.
     * 
     * @param filename The file containing a list of version information.
     * @param numThreads The number of threads to use when processing.
     */
	public MetricModelBuilder(String filename, int numThreads)
	{
		this.filename = new File(filename);
		versions = new LinkedBlockingQueue<VersionMetricData>();
		dataSetList = new LinkedList<InputDataSet>();
		bytesProcessed = 0l;
		workToProcess = workDone = 0;

		// Log organiser should not add this if it has already been registered.
		// on a previous construct and not removed.
		LogOrganiser.addLogger(logger);
		logger.setLevel(Level.ALL);
		
		// TimeUnit.SECONDS
		workQueue = new LinkedBlockingQueue<Runnable>();
		extractionWorkers = new ThreadPoolExecutor(numThreads, numThreads,
				30, TimeUnit.SECONDS, workQueue);
	}

	public MetricModelBuilder(String filename)
	{
		this(filename, 1);
	}

	/**
     * Builds a metric model for the specified versions file.
     * 
     * @param filename The file containing a list of version information.
     */
	public MetricModelBuilder(String baseDir, String filename)
	{
		this(baseDir + filename, 1);
	}

	public MetricModelBuilder(String baseDir, String filename, int numThreads)
	{
		this(baseDir + filename, numThreads);
	}

	private synchronized void addWorkDone(int workDone)
	{
		this.workDone += workDone;
	}

	private void loadDataSet() throws IOException
	{
		TextFile f = new TextFile(filename);
		String shortName = "";

		// InputDataSet input
		for (String line : f)
		{
			if (line.trim().length() == 0)
				continue; // skip empty lines
			if (line.trim().startsWith("#"))
				continue; // skip comments
			if (line.trim().startsWith("$")) // set product name and move to
			// next line
			{
				this.productName = (line.replace('$', ' ').trim()); // remove
				// the $
				// store the first word as the shortName
				int spacePos = productName.indexOf(" ");
				if (spacePos > 0)
					shortName = productName.substring(0, productName
							.indexOf(" "));
				else
					shortName = productName;
				// continue;
			}

			String[] cols = line.split(",");
			if (cols.length != 3)
				continue; // bad data -- skip line

			String jarFileName = new File(filename.getParent(), cols[2].trim())
					.toString();
			String versionId = cols[1];
			int rsn = Integer.parseInt(cols[0].trim());

			InputDataSet input = new InputDataSet(jarFileName, versionId, rsn,
					productName, shortName);

			/** Add input data from either a JAR file or a directory */
			if ((new File(jarFileName)).isDirectory())
				input.addInputDir(jarFileName, false); // no recursive support
			else
				input.addInputFile(jarFileName);

			dataSetList.add(input);
		}
		f.close();
		workToProcess = dataSetList.size() * NUM_OF_STAGES;
	}

	/**
     * Extracts metrics for each ClassMetricData in each version
     */
	private void extractMetrics()
	{
		// Pre queue up InputDataSet's for processing.
		queueWork();
		// Schedule the ThreadPool to run.
		beginProcessing();

		/**
         * Initiates an orderly shutdown in which previously submitted tasks are
         * executed, but no new tasks will be accepted. Invocation has no
         * additional effect if already shut down.
         */

		extractionWorkers.shutdown();
		while (!extractionWorkers.isTerminated())
		{
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
//				 We have been interrupted. Probably to stop processing.
//				// In most cases, this is expected if the user intentionally
//				// interrupts.
//				// Perform an immediate shutdown.
				logger.log(Level.WARNING,
						"Was interrupted during version processing.");
				extractionWorkers.shutdownNow();
			}
		}
//		try
//		{
//			boolean terminationStatus = false;
//
//			do
//			{
//				terminationStatus = extractionWorkers.awaitTermination(10,
//						TimeUnit.SECONDS);
//				// TODO Should probably add a variable here to update
//				// the amount of times we have waited. Or time spent
//				// waiting.....and abort upon a user specified amount
//				// of 'waiting'.
//			} while (!terminationStatus);
//
//		} catch (InterruptedException interruptedException)
//		{
//			// We have been interrupted. Probably to stop processing.
//			// In most cases, this is expected if the user intentionally
//			// interrupts.
//			// Perform an immediate shutdown.
//			logger.log(Level.WARNING,
//					"Was interrupted during version processing.");
//			extractionWorkers.shutdownNow();
//		}
	}

	private void queueWork()
	{
		// For each dataset, create a version and extract subsequent class
		// metric files.
		for (final InputDataSet data : dataSetList)
		{
			if (interrupted)
				return;
			Runnable runnableVersion = new Runnable()
			{
				public void run()
				{
					VersionMetricData vmd = createVersion(data);
					try
					{
						versions.offer(vmd);
						// workDone += 4; // weighting of 3 units + 1 for loading
						addWorkDone(4);
						// stage.
						completion = (int) getProcesingDone(workDone, workToProcess);
						setChanged();
						notifyObservers();
					} catch (NullPointerException e)
					{
						// Handle. This will occur if the ThreadPool was
						// interrupted.
					}
				}
			};
			// Put runnable onto work queue.
			workQueue.offer(runnableVersion);
		}
	}

	private void beginProcessing()
	{
		// Queue up all the Versions for extraction.
		int workQueueSize = workQueue.size();
		for (int i = 0; i < workQueueSize; i++)
		{
			try
			{
				extractionWorkers.execute(workQueue.take());
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void updateMetrics(Map<Integer, VersionMetricData> versions)
	{

		for (VersionMetricData vmd : versions.values())
		{
			computeDependencies(vmd);
			computeLayers(vmd);
			computeGUIAndIOClasses(vmd);
			computeGUIClassCount(vmd);

			vmd.setSimpleMetric(Version.CLASS_COUNT, vmd.metricData.size());
		}
		scanAndMarkSurvivors(versions);
	}

	public HistoryMetricData build() throws IOException, InterruptedException
	{
		long start = System.currentTimeMillis();
		if (!interrupted)
			loadDataSet();
		long finish = System.currentTimeMillis();
		loadTime = finish - start;

		start = finish;
		if (!interrupted)
		{
			// Runnable runnable
			extractMetrics();
		}
		finish = System.currentTimeMillis();
		extractTime = finish - start;

		HashMap<Integer, VersionMetricData> vHashMap = new HashMap<Integer, VersionMetricData>();
		if (!interrupted)
		{
			processMetrics(versions);
			for (VersionMetricData vmd : versions)
			{
				vHashMap.put(vmd.getSimpleMetric(Version.RSN), vmd);
			}
			scanAndMarkSurvivors(vHashMap);
		}
		processTime = System.currentTimeMillis() - finish;
		dataSetList = null; // Each data set should have already been

		if (interrupted)
		{
			dataSetList = null;
			versions.clear();
			versions = null;
			throw new InterruptedException();
		}

		return new HistoryMetricData(productName, vHashMap);
	}

	public void processMetrics(BlockingQueue<VersionMetricData> versions)
	{
		for (VersionMetricData vmd : versions)
		{
			computeDependencies(vmd);
			computeLayers(vmd);
			computeGUIAndIOClasses(vmd);
			computeGUIClassCount(vmd);

			vmd.setSimpleMetric(Version.CLASS_COUNT, vmd.metricData.size());

			workDone++;
			completion = (int) getProcesingDone(workDone, workToProcess);
			setChanged();
			notifyObservers();
		}
	}

	// public void processMetrics(Map<Integer, VersionMetricData> versions)
	// {
	//
	// for (VersionMetricData vmd : versions.values())
	// {
	// computeDependencies(vmd);
	// computeLayers(vmd);
	// computeGUIAndIOClasses(vmd);
	// computeGUIClassCount(vmd);
	//
	// vmd.setSimpleMetric(Version.CLASS_COUNT, vmd.metricData.size());
	//
	// workDone++;
	// completion = (int) getProcesingDone(workDone, workToProcess);
	// setChanged();
	// notifyObservers();
	//
	// }
	// scanAndMarkSurvivors(versions);
	// }

	private VersionMetricData createVersion(InputDataSet input)
	{
		VersionMetricData vmd = new VersionMetricData(input.RSN,
				input.versionId, input.shortName);

		if (input.size() == 0)
			return null; // nothing to extract from
		// TODO Throw exception here instead.

		vmd.metricData.clear();
		try
		{
			// Inflate inner class files in this input set.
			// Inner class files need to be available in memory whilst
			// processing class files.
			input.inflate();

			// Create ClassMetricData set for the given input data set.
			for (InputStream is : input)
			{
				// For each InputStream (class file), create ClassMetricData.
				ClassMetricExtractor cme = new ClassMetricExtractor(is, input);
				// Create a new ClassMetricData
				ClassMetricData cmd = cme.extract();
				cmd.setProperty(ClassMetric.PRODUCT_NAME, vmd.shortName);

				// close input stream.
				is.close();
				cme = null;

				// Add this class metric data to its version.
				vmd.metricData.put(cmd.get(ClassMetric.NAME), cmd);
			}
			bytesProcessed += input.sizeInBytes();

			// Drop all streams we opened during processing this DataInputSet
			// (Version/jar)
			// This is important to free up memory.
			input.deflate();
			input = null;

		} catch (IOException iox)
		{
			System.err.println(iox.getMessage());
		}
		return vmd;
	}

	// TODO: Fixme, urgent. Could be losing precision here.
	// Check whether this really does need to be of type double.
	private static void computeGUIClassCount(VersionMetricData vmd)
	{
		double sum = 0.0;

		for (ClassMetricData c : vmd.metricData.values())
		{
			sum += c.getSimpleMetric(ClassMetric.GUI_DISTANCE);
		}
		vmd.setSimpleMetric(Version.GUI_CLASS_COUNT, (int) sum);
	}

	/**
     * Should be called only after metric data has been extracted Fanin will be
     * updated directly on the ClassMetric object Internal Fanout value will
     * also be calculated and updated
     */
	private static void computeDependencies(VersionMetricData vmd)
	{
		for (ClassMetricData cm : vmd.metricData.values())
		{
			int storeCount = cm.getSimpleMetric(ClassMetric.ISTORE_COUNT)
					+ cm.getSimpleMetric(ClassMetric.STORE_FIELD_COUNT)
					+ cm.getSimpleMetric(ClassMetric.REF_STORE_OP_COUNT);

			cm.setSimpleMetric(ClassMetric.STORE_COUNT, storeCount);

			int loadCount = cm.getSimpleMetric(ClassMetric.ILOAD_COUNT)
					+ cm.getSimpleMetric(ClassMetric.LOAD_FIELD_COUNT)
					+ cm.getSimpleMetric(ClassMetric.REF_LOAD_OP_COUNT)
					+ cm.getSimpleMetric(ClassMetric.CONSTANT_LOAD_COUNT);

			cm.setSimpleMetric(ClassMetric.LOAD_COUNT, loadCount);
			for (String name : cm.dependencies)
			{
				if (name.equals(cm))
					continue; // ignore self-dependencies
				ClassMetricData fanInNode = vmd.metricData.get(name);
				if (fanInNode != null)
				{
					cm.internalDeps.add(name);
					fanInNode.users.add(cm.get(ClassMetric.NAME));
				}
			}
		}

		// Set fan-in counts and compute distance
		for (ClassMetricData cm : vmd.metricData.values())
		{
			cm.setSimpleMetric(ClassMetric.FAN_IN_COUNT, cm.users.size());
			cm.setSimpleMetric(ClassMetric.INTERNAL_FAN_OUT_COUNT,
					cm.internalDeps.size());

			computeDistance(cm); // compute distance

			// Compute metrics.
			cm.setSimpleMetric(ClassMetric.RAW_SIZE_COUNT, cm
					.getSimpleMetric(ClassMetric.LOAD_COUNT)
					+ cm.getSimpleMetric(ClassMetric.STORE_COUNT)
					+ cm.getSimpleMetric(ClassMetric.BRANCH_COUNT)
					+ cm.getSimpleMetric(ClassMetric.FAN_OUT_COUNT)
					+ cm.getSimpleMetric(ClassMetric.INTERFACE_COUNT));

			cm.incrementMetric(ClassMetric.RAW_SIZE_COUNT, cm
					.getSimpleMetric(ClassMetric.METHOD_COUNT)
					+ cm.getSimpleMetric(ClassMetric.FIELD_COUNT)
					+ cm.getSimpleMetric(ClassMetric.METHOD_CALL_COUNT));
			cm.setSimpleMetric(ClassMetric.NORMALIZED_BRANCH_COUNT,
					(int) (((double) cm
							.getSimpleMetric(ClassMetric.BRANCH_COUNT) / cm
							.getSimpleMetric(ClassMetric.RAW_SIZE_COUNT)) * 100.0));
		}
	}

	/** Flag all classes that depend on a GUI class as GUI as well */
	private static void computeGUIAndIOClasses(VersionMetricData vmd)
	{
		Set<String> flagged = vmd.metricData.keySet();
		while (flagged.size() > 0)
			flagged = flagUsersAsGUI(vmd, flagged);
		// Now flag all IO classes like we did GUI
		flagged = vmd.metricData.keySet();
		while (flagged.size() > 0)
			flagged = flagUsersAsIO(vmd, flagged);
	}

	/** Computes the layers and instability metrics */
	private static void computeLayers(VersionMetricData vmd)
	{
		// Now update the fanin and instability value for each class
		for (ClassMetricData cmd : vmd.metricData.values())
		{
			cmd.setSimpleMetric(ClassMetric.INSTABILITY, (int) (((double) cmd
					.getSimpleMetric(ClassMetric.FAN_OUT_COUNT) / (cmd
					.getSimpleMetric(ClassMetric.FAN_OUT_COUNT) + cmd
					.getSimpleMetric(ClassMetric.FAN_IN_COUNT))) * 1000));

			int fanIn = cmd.getSimpleMetric(ClassMetric.FAN_IN_COUNT);
			int internalFanOut = cmd
					.getSimpleMetric(ClassMetric.INTERNAL_FAN_OUT_COUNT);

			if ((fanIn > 0) && (internalFanOut > 0))
				cmd.setSimpleMetric(ClassMetric.LAYER, 1); // mid
			else if ((fanIn == 0) && (internalFanOut > 0))
				cmd.setSimpleMetric(ClassMetric.LAYER, 2); // top
			else if ((fanIn > 0) && (internalFanOut == 0))
				cmd.setSimpleMetric(ClassMetric.LAYER, 0); // foundation
			else if ((fanIn == 0) && (internalFanOut == 0))
				cmd.setSimpleMetric(ClassMetric.LAYER, 3); // free
			else
				cmd.setSimpleMetric(ClassMetric.LAYER, 4);
			; // there should be no classes here, hopefully

			// Compute distance for each ClassMetricData
			// computeDistance(cmd);
		}
	}

	private static void computeDistance(ClassMetricData cmd)
	{
		// If not computed then do it.
		if (cmd.getSimpleMetric(ClassMetric.COMPUTED_DISTANCE) < 0)
		{
			double l = StatUtils.sqr(cmd
					.getSimpleMetric(ClassMetric.METHOD_COUNT))
					+ StatUtils.sqr(cmd
							.getSimpleMetric(ClassMetric.FIELD_COUNT));
			l += StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.BRANCH_COUNT))
					+ StatUtils.sqr(cmd
							.getSimpleMetric(ClassMetric.ZERO_OP_INSN_COUNT))
					+ StatUtils.sqr(cmd
							.getSimpleMetric(ClassMetric.TYPE_INSN_COUNT));
			l += StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.ILOAD_COUNT))
					+ StatUtils.sqr(cmd
							.getSimpleMetric(ClassMetric.ISTORE_COUNT));
			l += StatUtils.sqr(cmd
					.getSimpleMetric(ClassMetric.LOAD_FIELD_COUNT))
					+ StatUtils.sqr(cmd
							.getSimpleMetric(ClassMetric.STORE_FIELD_COUNT));
			l += StatUtils.sqr(cmd
					.getSimpleMetric(ClassMetric.REF_LOAD_OP_COUNT))
					+ StatUtils.sqr(cmd
							.getSimpleMetric(ClassMetric.REF_STORE_OP_COUNT));
			l += StatUtils.sqr(cmd
					.getSimpleMetric(ClassMetric.TRY_CATCH_BLOCK_COUNT))
					+ StatUtils.sqr(cmd
							.getSimpleMetric(ClassMetric.INTERFACE_COUNT))
					+ StatUtils.sqr(cmd
							.getSimpleMetric(ClassMetric.SUPER_CLASS_COUNT));
			l += StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.FAN_OUT_COUNT))
					+ StatUtils.sqr(cmd
							.getSimpleMetric(ClassMetric.EX_METHOD_CALL_COUNT))
					+ StatUtils.sqr(cmd
							.getSimpleMetric(ClassMetric.IN_METHOD_CALL_COUNT));
			l += StatUtils
					.sqr(cmd.getSimpleMetric(ClassMetric.LOCAL_VAR_COUNT))
					+ StatUtils.sqr(cmd
							.getSimpleMetric(ClassMetric.PRIVATE_METHOD_COUNT))
					+ StatUtils.sqr(cmd
							.getSimpleMetric(ClassMetric.CONSTANT_LOAD_COUNT));
			l += StatUtils.sqr(cmd
					.getSimpleMetric(ClassMetric.INCREMENT_OP_COUNT));
			l = Math.sqrt(l);
			cmd.setSimpleMetric(ClassMetric.COMPUTED_DISTANCE, StatUtils
					.scaleDoubleMetric(l, 10, 1000.0));
		}
	}

	/** Given an input set, it will iterate over it and flag all users as IO */
	private static Set<String> flagUsersAsIO(VersionMetricData vmd,
			Set<String> classNameSet)
	{
		Set<String> flagged = new HashSet<String>();
		for (String cn : classNameSet)
		{
			if (vmd.metricData.get(cn).getSimpleMetric(ClassMetric.IS_IO_CLASS) == 1)
			{
				for (String userClassName : vmd.metricData.get(cn).users)
				{
					if (vmd.metricData.get(userClassName).getSimpleMetric(
							ClassMetric.IS_IO_CLASS) == 0)
					{
						vmd.metricData.get(userClassName).setSimpleMetric(
								ClassMetric.IS_IO_CLASS, 1);
						flagged.add(userClassName);
					}
				}
			}
		}
		return flagged;
	}

	/** Given an input set, it will iterate over it and flag all users as GUI */
	private static Set<String> flagUsersAsGUI(VersionMetricData vmd,
			Set<String> classNameSet)
	{
		Set<String> flagged = new HashSet<String>();
		for (String cn : classNameSet)
		{
			double currGUIDistance = vmd.metricData.get(cn).getSimpleMetric(
					ClassMetric.GUI_DISTANCE);
			if (currGUIDistance != 0) // if it is a GUI class
			{
				// Check its immediate users and make them all as GUI classes as
				// well
				for (String userClassName : vmd.metricData.get(cn).users)
				{
					if (vmd.metricData.get(userClassName).getSimpleMetric(
							ClassMetric.GUI_DISTANCE) == 0)
					{
						// FIXME - URGENT: double cast to int as interim step in
						// shitfing to vocab.
						vmd.metricData.get(userClassName).setSimpleMetric(
								ClassMetric.GUI_DISTANCE,
								(int) currGUIDistance / 2);
						flagged.add(userClassName);
					}
				}
			}
		}
		return flagged;
	}

	/**
     * Updates the age for each Class in every Version. Increments age if the
     * class is an exact match from before
     */
	private static void scanAndMarkSurvivors(
			Map<Integer, VersionMetricData> versions)
	{
		if (versions.size() < 2)
			return; // can do nothing with 1 version
		// VersionMetricData[] allVersions = (VersionMetricData[]) versions
		// .values().toArray();

		for (int i = 2; i <= versions.size(); i++) // since we have 1 for
		// base
		{
			VersionMetricData v1 = versions.get(i - 1);
			VersionMetricData v2 = versions.get(i);
			for (ClassMetricData cm2 : v2.metricData.values())
			{
				// Assume NEW or unchanged
				cm2.setSimpleMetric(ClassMetric.EVOLUTION_DISTANCE,
						Evolution.NEW.getValue());
				ClassMetricData cm1 = v1.metricData.get(cm2
						.get(ClassMetric.NAME));
				if (cm1 == null) // cm2 class name not found in previous
				// version
				{
					cm2.setSimpleMetric(ClassMetric.EVOLUTION_STATUS,
							Evolution.NEW.getValue());
					cm2.setSimpleMetric(ClassMetric.BORN_RSN, v2
							.getSimpleMetric(Version.RSN));
					cm2.setSimpleMetric(ClassMetric.AGE, 1);
				} else
				// found in previous version
				{
					cm2.setSimpleMetric(ClassMetric.BORN_RSN, cm1
							.getSimpleMetric(ClassMetric.BORN_RSN));
					if (cm2.isExactMatch(cm1))
					{
						cm2.setSimpleMetric(ClassMetric.AGE, cm1
								.getSimpleMetric(ClassMetric.AGE) + 1); // This
						// class is
						// a
						// survivor
						cm2.setSimpleMetric(ClassMetric.EVOLUTION_STATUS,
								Evolution.UNCHANGED.getValue());
						cm1.setSimpleMetric(ClassMetric.NEXT_VERSION_STATUS,
								Evolution.UNCHANGED.getValue());
						cm2.setSimpleMetric(ClassMetric.EVOLUTION_DISTANCE,
								Evolution.UNCHANGED.getValue());
					} else
					// found, but it is not an exact match
					{
						cm2.setSimpleMetric(ClassMetric.EVOLUTION_STATUS,
								Evolution.MODIFIED.getValue());
						cm1.setSimpleMetric(ClassMetric.IS_MODIFIED, 1);
						cm2.setSimpleMetric(ClassMetric.AGE, 1);
						setEvolutionDistanceFrom(cm1, cm2);
						cm1.setSimpleMetric(ClassMetric.NEXT_VERSION_STATUS,
								Evolution.MODIFIED.getValue());
					}
				}
			}

			// Now look for deleted names and mark them down
			for (ClassMetricData cm1 : v1.metricData.values())
			{
				ClassMetricData cm2 = v2.metricData.get(cm1
						.get(ClassMetric.NAME));
				if (cm2 == null)
				{
					cm1.setSimpleMetric(ClassMetric.IS_DELETED, 1);
					cm1.setSimpleMetric(ClassMetric.NEXT_VERSION_STATUS,
							Evolution.DELETED.getValue());
				}
			}
		}
	}

	/**
     * Sets the evolution distance of cm2 to the distance from cm1 to cm2.
     */
	private static void setEvolutionDistanceFrom(ClassMetricData cm1,
			ClassMetricData cm2)
	{
		double ed = cm2.distanceFrom(cm1);
		double scaleMax = 100.0;
		double metricMax = 1000.0;
		if (ed > metricMax)
			ed = metricMax;
		double scaledValue = (scaleMax * ed) / metricMax;
		cm2.setSimpleMetric(ClassMetric.EVOLUTION_DISTANCE, (int) Math
				.round(scaledValue));
	}

	/**
     * Computes the amount of procressing that has been done as a percentage of
     * the total work to do.
     * 
     * @param workDone How many units of work have been done.
     * @param workToProcess How many units of work this is to do.
     * @return Percentage of work done.
     */
	private double getProcesingDone(int workDone, int workToProcess)
	{
		return (((double) workDone / workToProcess)) * 100;
	}

	/**
     * @return the bytesProcessed
     */
	public final long getBytesProcessed()
	{
		return bytesProcessed;
	}

	/**
     * @return the extractTime
     */
	public final long getExtractTime()
	{
		return extractTime;
	}

	/**
     * @return the loadTime
     */
	public final long getLoadTime()
	{
		return loadTime;
	}

	/**
     * @return the processTime
     */
	public final long getProcessTime()
	{
		return processTime;
	}

	/**
     * @return the completion
     */
	public final int getCompletion()
	{
		return completion;
	}

}
