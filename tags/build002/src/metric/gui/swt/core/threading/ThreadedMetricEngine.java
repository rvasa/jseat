package metric.gui.swt.core.threading;

import java.io.IOException;
import java.util.Collection;
import java.util.Observer;

import metric.core.MetricEngine;
import metric.core.model.HistoryMetricData;
import metric.core.model.MetricData;
import metric.core.model.VersionMetricData;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;

/**
 * Creates a <code>MetricEngine</code> on its own Thread and updates the
 * specified list with the data returned from the MetricEngine.
 * 
 * Can be interrupted to cancel the MetricEngine from building <code>MetricData</code>.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class ThreadedMetricEngine extends Thread
{
	private final String filename;
	private HistoryMetricData hmd;
	private List versionList;
	private MetricEngine me;
	private Observer o;

	/**
     * 
     * @param o The Observer that should be notified on the update status of the
     *            MetricEngine.
     * @param versionList The List where all <code>VersionMetricData</code>
     *            should be added once the <code>MetricEngine</code> has
     *            created the appropriate <code>MetricData</code>.
     * @param filename The filename of the *.versions file the MetricEngine
     *            should process.
     */
	public ThreadedMetricEngine(Observer o, List versionList, String filename)
	{
		this.filename = filename;
		this.o = o;
		this.versionList = versionList;
		setName("MetricEngineThread");
	}

	/**
     * This is a blocking call. It will block the calling thread until the
     * MetricData has been produced. Don't call this if it is likely the
     * MetricData will not be produced quickly.
     * 
     * @return The processed MetricData.
     */
	public MetricData get()
	{
		while (hmd == null)
		{
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return hmd;
	}

	@Override
	public void run()
	{
		me = new MetricEngine(true, 3);
		me.addObserver(o);

		try
		{
			this.hmd = me.process(filename);
			me = null; // Finished with MetricEngine.
			final Collection<VersionMetricData> c = hmd.getVersionList();
			o.update(null, hmd);

			Runnable toRun = new Runnable()
			{
				public void run()
				{
					versionList.removeAll();
					versionList.setData(hmd);
					for (VersionMetricData vmd : c)
					{
						versionList.add(vmd.toString());
					}
				}
			};
			if (!isInterrupted())
				Display.getDefault().asyncExec(toRun);
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (InterruptedException e)
		{
		} // handle. We have overriden interrupt() to notify the MetricEngine
		// ourselves.
	}

	@Override
	/**
     * Interrupts the <code>MetricEngine</code>, notifying it that it should
     * stop what it is doing and clean up.
     */
	public void interrupt()
	{
		super.interrupt();
		me.interruptModelBuilder(true);
	}
}
