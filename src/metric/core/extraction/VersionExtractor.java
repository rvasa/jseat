package metric.core.extraction;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import metric.core.ActiveObject;
import metric.core.io.InputData;
import metric.core.io.InputDataSet;
import metric.core.model.ClassMetricData;
import metric.core.model.VersionMetricData;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.ClassMetric;
import metric.gui.swt.core.threading.ProcessingReport;

public class VersionExtractor extends ActiveObject<String> implements ProcessingReport
{
	private Logger logger = Logger.getLogger(getClass().getSimpleName());
	private BlockingQueue<String> versionNames;
	private BlockingQueue<VersionMetricData> versions;
	private String productName, versionPath;

	private static Object processingLock = new Object();
	private static int totalProcessed;
	private static int uniqueProcessed;

	public VersionExtractor(String name, BlockingQueue<String> versionNames, BlockingQueue<VersionMetricData> versions,
			String versionPath)
	{
		super(name);
		this.versionNames = versionNames;
		this.versions = versions;
		this.versionPath = versionPath;
		LogOrganiser.addLogger(logger);
		logger.setLevel(Level.ALL);
	}

	@Override
	public void doWork(String toDo)
	{
		if (toDo != null)
		{
			String msg = "Extracting - " + toDo;

			synchronized (processingLock)
			{
				totalProcessed++;
				uniqueProcessed++;
			}

			String[] cols = toDo.split(",");

			int rsn = Integer.parseInt(cols[0].trim());
			String versionId = cols[1];
			String jarFileName = new File(versionPath, cols[2].trim()).toString();

			InputDataSet input = new InputDataSet(jarFileName, versionId, rsn, productName, cols[3]);

			try
			{
				/** Add input data from either a JAR file or a directory */
				if ((new File(jarFileName)).isDirectory())
					input.addInputDir(jarFileName, true); // recursive
				else
					input.addInputFile(jarFileName);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			logger.log(Level.ALL, msg);
			setChanged();
			notifyObservers(msg);
			VersionMetricData vmd = createVersion(input);

			// Put onto completed channel.
			versions.offer(vmd);
		}
	}

	private VersionMetricData createVersion(InputDataSet input)
	{
		VersionMetricData vmd = new VersionMetricData(input.RSN, input.versionId, input.shortName);

		if (input.size() == 0)
			return null; // nothing to extract from

		vmd.metricData.clear();

		// Inflate inner class files in this input set.
		// Inner class files need to be available in memory whilst
		// processing class files.
		input.inflate(true);

		// Create ClassMetricData set for the given input data set.
		for (InputData idata : input)
		{
			try
			{
				// For each InputData (class file), create ClassMetricData.
				ClassMetricExtractor cme = new ClassMetricExtractor(idata, input);
				// Create a new ClassMetricData
				ClassMetricData cmd = cme.extract();
				cmd.setProperty(ClassMetric.PRODUCT_NAME, vmd.shortName);

				// close input stream.
				idata.getInputStream().close();
				cme = null;

				// Add this class metric data to its version.
				vmd.metricData.put(cmd.get(ClassMetric.NAME), cmd);
			} catch (Exception e)
			{
				logger.log(Level.WARNING, "Skipping bad input file during. Could not get data stream. ");
			}
		}
		// bytesProcessed += input.sizeInBytes();

		// Drop all streams we opened during processing this DataInputSet
		// (Version/jar)
		// This is important to free up memory.
		try
		{
			input.deflate();
			input.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		input = null;

		return vmd;
	}

	@Override
	public String getWork()
	{
		try
		{
			return versionNames.take();
		} catch (InterruptedException e)
		{
			logger.log(Level.WARNING, "Interrupted. Getting ready to finish up.");
			return null;
		}
	}

	@Override
	protected void cleanup()
	{
		logger.log(Level.ALL, getName() + " stopped.");
	}

	public int getProcessingDone()
	{
		synchronized (processingLock)
		{
			return uniqueProcessed++;
		}
	}

	public int getTotalProcessingDone()
	{
		synchronized (processingLock)
		{
			return totalProcessed++;
		}
	}

}
