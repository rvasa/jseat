package metric.core.report.decorator;

import metric.core.exception.ReportException;
import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.MethodMetricMap;
import metric.core.model.VersionMetricData;
import metric.core.report.visitor.ReportVisitor;
import metric.core.util.MetricTable;

/**
 * An abstract decorator for a <code>ReportVisitor</code>. This should be
 * extended to add new visual representations for a ReportVisitor. For example,
 * a TextDecorator could provide a textural representation of a ReportVisitor or
 * a ChartVisitor could draw a chart of the ReportVisitor's underlying
 * <code>MetricTable</code>.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public abstract class ReportDecorator implements ReportVisitor
{
	protected ReportVisitor decoratedReport;

	public ReportDecorator(ReportVisitor decoratedReport)
	{
		this.decoratedReport = decoratedReport;
	}

	public void visit(HistoryMetricData hmd) throws ReportException
	{
		decoratedReport.visit(hmd);
		display();
	}

	public void visit(VersionMetricData vmd) throws ReportException
	{
		decoratedReport.visit(vmd);
		display();
	}

	public void visit(ClassMetricData cmd) throws ReportException
	{
		decoratedReport.visit(cmd);
		display();
	}

	public void visit(MethodMetricMap mmp) throws ReportException
	{
		decoratedReport.visit(mmp);
		display();
	}

	public abstract void display();

	/**
     * @return The MetricTable generated by the underling
     *         <code>ReportVisitor</code>.
     */
	public MetricTable getTable()
	{
		return decoratedReport.getTable();
	}
}