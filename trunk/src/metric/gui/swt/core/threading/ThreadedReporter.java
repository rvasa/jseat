package metric.gui.swt.core.threading;

import metric.core.exception.ReportException;
import metric.core.model.HistoryMetricData;
import metric.core.report.visitor.ReportVisitor;

/**
 * Runs the specified <code>ReportDecorator</code> (and subsequent
 * <code>ReportVisitor</code>) on the specified
 * <code>HistoryMetricData<code>.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class ThreadedReporter extends Thread
{
	private HistoryMetricData hmd;
	private ReportVisitor visitor;
	private Object lock;
	private boolean running;

	public ThreadedReporter(HistoryMetricData hmd, ReportVisitor visitor)
	{
		this.hmd = hmd;
		this.visitor = visitor;
		running = true;
		lock = new Object();
	}
	
	public boolean isRunning() throws InterruptedException
	{
		synchronized (lock) {
			while (running)
			{
				yield();
				Thread.sleep(100);
			}
		}
		
		
		return true;
		
	}

	@Override
	public void run()
	{
		try
		{
			System.out.println(Thread.currentThread().getName() + "running report...");
			hmd.accept(visitor);
			System.out.println(Thread.currentThread().getName() + "finished report...");
			synchronized (lock)
			{
				running = false;
			}
			notifyAll();
		} catch (ReportException e)
		{
			// FIXME This should be logged instead.
			e.printStackTrace();
		}
	}
}
