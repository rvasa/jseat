package metric.gui.swt.core.threading;

import java.util.Iterator;
import java.util.Observer;
import java.util.Map.Entry;

import metric.core.model.MetricData;
import metric.core.model.HistoryMetricData;
import metric.core.persistence.MetricDataConverter;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;

/**
 * Serializes or de-serializes <code>MetricData</code> on its own Thread
 * according to which constructor is used to construct this ThreadedConverter.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class ThreadedMetricDataConverter extends Thread
{
	private MetricDataConverter mc;
	private MetricData md;
	private String path;
	private List lVersions;
	private Observer observer;

	private ThreadedMetricDataConverter(MetricDataConverter mc, String path)
	{
		this.mc = mc;
		this.path = path;
	}

	/**
     * This constructor is intended to be used when MetricData is being
     * de-serialized.
     * 
     * @param mc The MetricConverter to use.
     * @param path The path to de-serialize from.
     * @param lVersions The List that should be populated with versions once
     *            processing has finished.
     * @param Observer The observer that should be notified and passed a
     *            reference ot the proessed HistoryMetriData.
     */
	public ThreadedMetricDataConverter(MetricDataConverter mc, String path,
			List lVersions, Observer observer)
	{
		this(mc, path);
		this.lVersions = lVersions;
		this.observer = observer;
	}

	/**
     * This constructor is intended to be used when serialising MetricData.
     * 
     * @param mc The MetricConverter to use
     * @param md The MetricData to serialize.
     * @param path The path the MetricData should be serialized to.
     */
	public ThreadedMetricDataConverter(MetricDataConverter mc, MetricData md, String path)
	{
		this(mc, path);
		this.md = md;
	}

	@Override
	public void run()
	{
//		try
//		{
//			// Serialising
////			if (md != null)
////				mc.serialize(md, path);
////			else
////			{
////				FileReader fr = new FileReader(path);
////				md = mc.deSerialize(fr);
////				fr.close();
////
////				updateVersionList();
////			}
////			mc.close();
//		} catch (IOException e)
//		{ // FIXME errors are suppressed here. Nee to send back some other
//			// indication of error.
//			e.printStackTrace();
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//		}
	}

	private void updateVersionList()
	{
		final HistoryMetricData hmd = (HistoryMetricData) md;
		Runnable toRun = new Runnable()
		{
			public void run()
			{
//				Collection<VersionMetricData> c = hmd.getVersionList();
//				Collection<String> c = hmd.getVersionList();
				Iterator<Entry<Integer, String>> iterator = hmd.getVersions().iterator();

				lVersions.removeAll();
				lVersions.setData(hmd);
//				Iterator<String> it = c.iterator();
				while (iterator.hasNext())
				{
//					String version = it.next();
//					String vers
					Entry<Integer, String> entry = iterator.next();
					lVersions.add(entry.getValue());
					lVersions.setData(entry.getValue(), entry.getKey());
				}
//				for (VersionMetricData vmd : c)
//				{
//					lVersions.add(vmd.toString());
//					lVersions.setData(vmd.toString(), vmd);
//				}
			}
		};
		Display.getDefault().asyncExec(toRun);
		observer.update(null, hmd);
	}

	/**
     * This is a blocking call. It will block the calling thread until the
     * MetricData has been processed. Don't call this if it is likely the
     * MetricData will not be produced quickly.
     * 
     * @return The processed MetricData.
     */
	public MetricData get()
	{
		while (md == null)
		{
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return md;
	}
}
