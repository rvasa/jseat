package metric.gui.swt.core.threading;

import java.util.Observer;
import java.util.Set;
import java.util.Map.Entry;

import metric.core.Project;
import metric.core.model.HistoryMetricData;
import metric.core.model.MetricData;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;

/**
 * Creates a <code>Project</code> on its own Thread and updates the specified
 * list with the data returned from the MetricEngine.
 * 
 * Can be interrupted to cancel the MetricEngine from building
 * <code>MetricData</code>.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class ThreadedProjectBuilder extends Thread
{
	private HistoryMetricData hmd;
	private List versionList;
	private Project project;
	/**
     * 
     * @param versionList The List where all <code>VersionMetricData</code>
     *            should be added once the <code>MetricEngine</code> has
     *            created the appropriate <code>MetricData</code>.
     * @param filename The filename of the project file.
     */
	public ThreadedProjectBuilder(List versionList, String projectInput)
	{
		this.versionList = versionList;
		setName("ProjectBuilderThread");
		
		project = new Project(projectInput);
	}

	public ThreadedProjectBuilder(List versions, String projectInput,
			String projectOutput, int concurrentVerThreads)
	{
		this.versionList = versions;
		project = new Project(projectInput, projectOutput, concurrentVerThreads);
	}
	
	public void addObserver(Observer observer)
	{
		project.addObserver(observer);
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
		this.hmd = project.build();

		// Only do this if we have a version list to populate.
		if (versionList != null && hmd != null)
		{
			final Set<Entry<Integer, String>> c = hmd.getVersionList();

			Runnable toRun = new Runnable()
			{
				public void run()
				{
					versionList.removeAll();
					versionList.setData(hmd);

					for (Entry<Integer, String> version : c)
					{
						versionList.add(version.getValue());
						versionList.setData(version.getValue(), version
								.getKey());
					}
				}
			};
			if (!isInterrupted())
				Display.getDefault().asyncExec(toRun);
		} else
		{
			// Handle.....might have been aborted.
		}
	}

	@Override
	/**
     * Interrupts the <code>MetricEngine</code>, notifying it that it should
     * stop what it is doing and clean up.
     */
	public void interrupt()
	{
		project.interrupt();
	}
}
