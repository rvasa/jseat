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
	HistoryMetricData hmd;
	ReportVisitor visitor;

	public ThreadedReporter(HistoryMetricData hmd, ReportVisitor visitor)
	{
		this.hmd = hmd;
		this.visitor = visitor;
	}

	@Override
	public void run()
	{
		try
		{
			hmd.accept(visitor);
		} catch (ReportException e)
		{
			// FIXME This should be logged instead.
			e.printStackTrace();
		}
	}
}
