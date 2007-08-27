package metric.core.report.visitor;

import metric.core.exception.ReportException;

public interface VisitableReport
{
	public void accept(ReportVisitor visitor) throws ReportException;
}
