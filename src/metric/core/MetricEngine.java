package metric.core;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import metric.core.model.HistoryMetricData;
import metric.core.model.VersionMetricData;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.History;

/**
 * Provides a Facade interface to the <code>MetricModelBuilder</code>.
 * Generally speaking, you should use this class instead of the
 * MetricModelBuilder.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class MetricEngine extends Observable implements Observer
{
	private boolean showProcessing;
	private Logger logger = Logger.getLogger(getClass().getSimpleName());
	private MetricModelBuilder mmb;

	private int completion;
	private int numThreads;
	public boolean interrupt;

	/**
     * Initialises a MetricEngine.
     * 
     * @param numThreads The number of threads to scale Version processing
     *            across.
     * @param showProcessing Whether or not to display processing information.
     */
	public MetricEngine(boolean showProcessing, int numThreads)
	{
		this.showProcessing = showProcessing;
		LogOrganiser.addLogger(logger);
		logger.setLevel(Level.ALL);
		this.numThreads = numThreads;
	}
	
	/**
     * Initialises a MetricEngine.
     * 
     * @param showProcessing Whether or not to display processing information.
     */
	public MetricEngine(boolean showProcessing)
	{
		this (showProcessing, 1);
	}

	/**
     * Processes the
     * 
     * @param filename
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
	public HistoryMetricData process(String filename) throws IOException,
			InterruptedException
	{
		HistoryMetricData h = null;
		VersionMetricData.showProcessing = showProcessing;
		if (showProcessing)
			logger.fine("Processing: " + filename);
		long startTime = System.currentTimeMillis();
		mmb = new MetricModelBuilder(filename, numThreads);
		mmb.addObserver(this);

		h = mmb.build();

		long bytesProcessed = mmb.getBytesProcessed();
		long mbProcessed = bytesProcessed >> 20;
		long endTime = System.currentTimeMillis();
		if (showProcessing)
		{
			{
				StringBuffer buffer = new StringBuffer();
				buffer
						.append("(Load time: " + mmb.getLoadTime() / 1000
								+ "s) ");
				buffer.append("(Extract time: " + mmb.getExtractTime() / 1000
						+ "s) ");
				buffer.append("(Pre-Processing time: " + mmb.getProcessTime()
						/ 1000 + "s)\n ");
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
		return mmb.getBytesProcessed();
	}

	/**
     * @return time spent loading loading jar files into memory.
     */
	public final float getLoadTime()
	{
		return mmb.getLoadTime();
	}

	/**
     * @return time spent pre-processing and computing metric data.
     */
	public final float getPreProcessingTime()
	{
		return mmb.getProcessTime();
	}

	/**
     * @return time spent extracting metrics data.
     */
	public final float getExtractTime()
	{
		return mmb.getExtractTime();
	}

	public void update(Observable observerable, Object o)
	{
		if (observerable instanceof MetricModelBuilder)
		{
			MetricModelBuilder mmb = (MetricModelBuilder) observerable;
			this.completion = mmb.getCompletion();
			System.out.println(this.completion);
			setChanged();
			notifyObservers();
		}
	}

	public void interruptModelBuilder(boolean interrupt)
	{
		mmb.interrupted = interrupt;
	}

	/**
     * @return the completion
     */
	public final int getCompletion()
	{
		return completion;
	}
}
