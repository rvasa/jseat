package metric.core.extraction;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import metric.core.ActiveObject;
import metric.core.exception.ConversionException;
import metric.core.model.ClassMetricData;
import metric.core.model.VersionMetricData;
import metric.core.persistence.CSVConverter;
import metric.core.persistence.DataLoaderFactory;
import metric.core.persistence.MetricDataConverter;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.LoadType;
import metric.core.vocabulary.SerializeType;
import metric.core.vocabulary.Version;
import metric.gui.swt.core.threading.ProcessingReport;

public class VersionPersister extends ActiveObject<VersionMetricData> implements
		ProcessingReport
{
	private Logger logger = Logger.getLogger(getClass().getSimpleName());
	private BlockingQueue<VersionMetricData> versions;
	private MetricDataConverter classConverter, methodConverter, depConverter;
	private String path;

	private Set<Integer> persistedVersionNames;
	private Object processingLock;
	private int totalProcessed;

	// private HashMap<Integer, String> processed

	public VersionPersister(String name, BlockingQueue<VersionMetricData> versions,
			String path)
	{
		super(name);
		this.versions = versions;
		this.path = path;

		// this.converter = converter;
		classConverter = new CSVConverter(SerializeType.CLASSES);
		methodConverter = new CSVConverter(SerializeType.METHODS);
		depConverter = new CSVConverter(SerializeType.DEPENDENCIES);

		LogOrganiser.addLogger(logger);
		logger.setLevel(Level.ALL);

		persistedVersionNames = new HashSet<Integer>();
		processingLock = new Object();
	}

	@Override
	public void doWork(VersionMetricData toDo)
	{
		if (toDo != null)
		{
			String file = path + File.separator + toDo.get(Version.RSN);
			try
			{
				int beforePersist = toDo.metricData.size();
				String msg = "Persisting - " + file
						+ classConverter.getFileExtension();
				logger.log(Level.ALL, msg);
				classConverter.serialize(toDo, file);
				classConverter.close();

				logger.log(Level.ALL, "Persisting - " + file
						+ methodConverter.getFileExtension());
				methodConverter.serialize(toDo, file);
				methodConverter.close();

				logger.log(Level.ALL, "Persisting - " + file
						+ depConverter.getFileExtension());
				depConverter.serialize(toDo, file);
				depConverter.close();

				synchronized (processingLock)
				{
					totalProcessed++;
					if (!persistedVersionNames.contains(toDo
							.getSimpleMetric(Version.RSN)))
						persistedVersionNames.add(toDo
								.getSimpleMetric(Version.RSN));
				}

				// toDo = null; // Null reference.

				// load back up and check
//				System.out.println("Performing check on persisted entity...");
				HashMap<Integer, String[]> toLoad = new HashMap<Integer, String[]>();
				toLoad.put(toDo.getSimpleMetric(Version.RSN), new String[] {
						"", file });
				toDo = DataLoaderFactory.getInstance().getDataLoader(
						toLoad,
						LoadType.MAXIMAL).getVersion(
						toDo.getSimpleMetric(Version.RSN));
				
				if (toDo.metricData.size() != beforePersist)
				{
					System.err.println("Data changed after persisting....");
				}

			} catch (ConversionException e)
			{
				logger.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			} catch (IOException e)
			{
				logger.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void doIntegritCheck()
	{

	}

	@Override
	public VersionMetricData getWork()
	{
		try
		{
			VersionMetricData vmd = versions.take();
			return vmd;
		} catch (InterruptedException e)
		{
			logger.log(
					Level.WARNING,
					"Interrupted. Getting ready to finish up.");
			// Handle. Have been asked to stop.
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
			return persistedVersionNames.size();
		}
	}

	public int getTotalProcessingDone()
	{
		synchronized (processingLock)
		{
			return totalProcessed;
		}
	}

	public synchronized void reset()
	{
		persistedVersionNames.clear();
	}

	public void interrupt()
	{
		Thread.currentThread().interrupt();
	}
}
