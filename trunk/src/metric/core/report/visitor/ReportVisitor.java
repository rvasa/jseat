package metric.core.report.visitor;

import java.util.Observer;

import metric.core.exception.ReportException;
import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.MethodMetricMap;
import metric.core.model.VersionMetricData;
import metric.core.util.MetricTable;

public interface ReportVisitor
{
	public void visit(HistoryMetricData hmd) throws ReportException;
	
	public void visit(VersionMetricData vmd) throws ReportException;
	
	public void visit(ClassMetricData cmd) throws ReportException;
	
	public void visit(MethodMetricMap mmp) throws ReportException;
	
	public void addObserver(Observer observer);
	
	public MetricTable getTable();
}
