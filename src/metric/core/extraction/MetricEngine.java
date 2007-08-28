package metric.core.extraction;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import metric.core.WorkStatus;
import metric.core.io.InputDataSet;
import metric.core.io.TextFile;
import metric.core.model.HistoryMetricData;
import metric.core.model.VersionMetricData;
import metric.core.util.SimpleWorkTimer;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.History;
import metric.core.vocabulary.LoadType;
import metric.gui.swt.core.threading.ProcessingReport;

/**
 * Creates a project for the wrapped *.ver (version file).
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class MetricEngine extends Observable implements Observer
{
	private boolean showProcessing;
	private Logger logger = Logger.getLogger(getClass().getSimpleName());

	private LinkedList<InputDataSet> dataSetList;
	private BlockingQueue<String> inputQueue;
	private BlockingQueue<VersionMetricData> outputQueue;
	private Map<Integer, String[]> history;

	private String productName, shortName;
	private String inputFileName, outputFilename, dataFolder;

	private long bytesProcessed, loadTime, extractTime, processTime;

	private int workDone, workToProcess, completion, numVersions;
	private Object workLock;
	public boolean interrupted;

	private final int NUM_OF_STAGES = 2;
	private final int numThreads;

	/**
     * Initialises the MetricEngine with the specified versions file. Version
     * processing is scaled across the specified number of threads.
     * 
     * I.E. 5 Threads will process 5 versions concurrently.
     * 
     * @param inputFileName The file containing a list of version information.
     * @param outputFileName The path to output metric data to.
     * @param numThreads The number of threads to use when processing.
     */
	public MetricEngine(String inputFileName, String outputFileName,
			int numThreads, boolean showProcessing)
	{
		this.inputFileName = inputFileName;
		this.outputFilename = outputFileName;
		inputQueue = new LinkedBlockingQueue<String>();
		outputQueue = new LinkedBlockingQueue<VersionMetricData>();
		history = new HashMap<Integer, String[]>();
		dataSetList = new LinkedList<InputDataSet>();
		bytesProcessed = 0l;
		workToProcess = workDone = 0;
		workLock = new Object();

		// Log organiser should not add this if it has already been registered.
		// on a previous construct and not removed.
		LogOrganiser.addLogger(logger);
		logger.setLevel(Level.ALL);
		this.numThreads = numThreads;
	}

	/**
     * Initialises a MetricEngine.
     * 
     * @param showProcessing Whether or not to display processing information.
     */
	public MetricEngine(String inputFileName, String outputFilename,
			boolean showProcessing)
	{
		this(inputFileName, outputFilename, 1, showProcessing);
	}

	/**
     * Processes the specified file.
     * 
     * @param inputFileName
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
	public HistoryMetricData process() throws IOException, InterruptedException
	{
		HistoryMetricData h = null;
		VersionMetricData.showProcessing = showProcessing;
		if (showProcessing)
			logger.fine("Processing: " + inputFileName);
		long startTime = System.currentTimeMillis();

		h = build();

		long bytesProcessed = getBytesProcessed();
		long mbProcessed = bytesProcessed >> 20;
		long endTime = System.currentTimeMillis();
		if (showProcessing)
		{
			{
				StringBuffer buffer = new StringBuffer();
				buffer.append("(Load time: " + getLoadTime() / 1000 + "s) ");
				buffer.append("(Extract time: " + getExtractTime() / 1000
						+ "s) ");
				// buffer.append("(Pre-Processing time: " + getProcessTime()
				// / 1000 + "s)\n ");
				buffer.append("Processing: " + h.getVersionList().size() + " ");
				buffer.append(h.get(History.NAME) + " version(s), "
						+ mbProcessed);
				buffer.append("MB in " + (endTime - startTime) / 1000.0);
				buffer.append("s");
				logger.fine(buffer.toString());
			}
		}
		// Garbage collect.
		System.gc();
		return h;
	}

	/**
     * Parses the version (*.ver) and loads the names off the dataset to be
     * loaded.
     * 
     * @throws IOException
     */
	private void loadDataSet() throws IOException
	{
		TextFile f = new TextFile(inputFileName);

		// Setup the data folder that we will be writing to.
		setupDataFolder();

		// InputDataSet input
		for (String line : f)
		{

			if (line.trim().length() == 0)
				continue; // skip empty lines
			if (line.trim().startsWith("#"))
				continue; // skip comments
			if (line.trim().startsWith("$")) // set product name and move to
			{
				this.productName = (line.replace('$', ' ').trim()); // remove
				// store the first word as the shortName
				int spacePos = productName.indexOf(" ");
				if (spacePos > 0)
					this.shortName = productName.substring(
							0,
							productName.indexOf(" ")).trim();
				else
					this.shortName = productName;
			}

			String[] cols = line.split(",");
			if (cols.length != 3)
				continue; // bad data -- skip line

			inputQueue.offer(line.trim() + "," + shortName);
			numVersions++;

			int version = Integer.parseInt(cols[0].trim());
			String file = dataFolder + File.separator + version;

			String[] aVersion = { shortName + "-" + cols[1].trim(), file };
			history.put(version, aVersion);
		}
		f.close();
		workToProcess = history.size() * NUM_OF_STAGES;
	}

	private void setupDataFolder()
	{
		// Setup proper path to write data.
		// Will always be a data folder under the directory where the project
		// file is being written.
		File dataFolder = new File(outputFilename);
		dataFolder = new File(dataFolder.getParent() + File.separator + "data");
		if (!dataFolder.exists())
			dataFolder.mkdir();

		this.dataFolder = dataFolder.getAbsolutePath();
	}

	private void startExtractors()
	{
		File versionFile = new File(inputFileName);
		for (int i = 0; i < numThreads; i++)
		{
			VersionExtractor extractor = new VersionExtractor(inputQueue,
					outputQueue, versionFile.getParent());
			extractor.addObserver(this);
			extractor.start();
		}
	}

	private VersionPersister startPersister() throws InterruptedException
	{
		// Persist to disk as we process.
		VersionPersister persister = new VersionPersister(outputQueue,
				dataFolder);
		// persister.addObserver(this);
		persister.start();

		// Wait for persister to finish...
		// int persisted = 0, checkPersisted;
		while (persister.getProcessingDone() != numVersions)
		{
			Thread.sleep(100);
			// // if (checkPersisted > persisted)
			// // {
			// // synchronized (workLock)
			// // {
			// // workDone++;
			// // System.out.println("Initial persisting workDone: "
			// // + workDone);
			// // this.completion = (int) (((double) workDone / (double)
			// // workToProcess) * 100);
			// // }
			// // persisted = checkPersisted;
			// // }
		}
		return persister;
	}

	public void performPostProcessing(HistoryMetricData hmd,
			VersionPersister persister) throws InterruptedException
	{
		VersionPostProcessor postProcessor = new VersionPostProcessor(hmd,
				outputQueue);
		postProcessor.addObserver(this);
		postProcessor.process();

		// int persisted = 0, checkPersisted = 0;
		// At the moment this will not be executed concurrently because the
		// thread performing the post processing is this thead. So it it not
		// really needed, but left in here in case post-processing is shifted to
		// another thread.

		// Wait for persister to finish...
		while (persister.getProcessingDone() != numVersions)
		{
			Thread.sleep(100);
			System.out.println("Waiting for post to finish.");
			// // if (checkPersisted > persisted)
			// // {
			// // // synchronized (workLock)
			// // // {
			// // // workDone++;
			// // // System.out.println("Post processing workDone: " +
			// workDone);
			// // // this.completion = (int) (((double) workDone / (double)
			// // workToProcess) * 100);
			// // // }
			// // persisted = checkPersisted;
			// // }
		}
		persister.stop();
	}

	private HistoryMetricData build() throws IOException, InterruptedException
	{
		loadDataSet();

		// Start the extractors.
		startExtractors();

		// Start the VersionPersister and get a reference to it.
		VersionPersister persister = startPersister();

		// Need to use maximal data loading for post procesing.
		HistoryMetricData hmd = new HistoryMetricData(productName, history,
				LoadType.MAXIMAL);
		hmd.setSimpleMetric(History.VERSIONS, numVersions);

		// Don't stop persister, but reset it so we can use it again to
		// re-persist after post processing.

		persister.reset();
//		persister.stop();
		// Start the post-processing. The post-processing will put processed
		// work back onto the same queue the persister takes work from, so it
		// will be automatically re-persisted to file.
		performPostProcessing(hmd, persister);

		return hmd;
	}

	/**
     * @param showProcessing whether or not to show processing related
     *            information.
     */
	public final void showProcessing(boolean showProcessing)
	{
		this.showProcessing = showProcessing;
	}

	/**
     * @return the amount of bytes processed.
     */
	public final long getBytesProcessed()
	{
		return bytesProcessed;
	}

	/**
     * @return time spent loading loading jar files into memory.
     */
	public final float getLoadTime()
	{
		return loadTime;
	}

	/**
     * @return time spent pre-processing and computing metric data.
     */
	public final float getPreProcessingTime()
	{
		return processTime;
	}

	/**
     * @return time spent extracting metrics data.
     */
	public final float getExtractTime()
	{
		return extractTime;
	}

	/**
     * @return the completion
     */
	public final int getCompletion()
	{
		synchronized (workLock)
		{
			return completion;
		}
	}
	
	// This is only intended to receive updates from the VersionExtractor and
	// VersionPostProcessor. The work calculations are based on this. If extra
	// objects are sending update messages then this should be updated to
	// reflect the extra work so the user knows :)
	public void update(Observable o, Object data)
	{
		// if (o instanceof ProcessingReport)
		// {
		// ProcessingReport pr = (ProcessingReport) o;
		// System.err.println("processing: " + pr.getProcessingDone());
		// }
		synchronized (workLock)
		{
			workDone++;
			this.completion = (int) (((double) workDone / (double) workToProcess) * 100);
		}
		setChanged();
		notifyObservers(data);
	}
}
