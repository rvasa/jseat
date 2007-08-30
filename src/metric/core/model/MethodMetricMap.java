package metric.core.model;

import java.util.HashMap;

import metric.core.exception.ReportException;
import metric.core.report.visitor.ReportVisitor;
import metric.core.report.visitor.VisitableReport;
import metric.core.vocabulary.MethodMetric;

public class MethodMetricMap implements Comparable<MethodMetricMap>, VisitableReport
{
	private HashMap<String, int[]> methods;

	public MethodMetricMap(HashMap<String, int[]> methods)
	{
		this.methods = methods;
	}

	public final HashMap<String, int[]> methods()
	{
		return this.methods;
	}

	/**
     * Returns the specified simple metric for the specified method
     * 
     * @param method The name and description of the method
     * @param metric The metric to return.
     */
	public int getSimpleMetric(String method, MethodMetric metric)
	{
		int[] metrics = methods.get(method);
		return metrics[metric.ordinal()];
	}

	public int compareTo(MethodMetricMap arg0)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
     * @return The number of methods.
     */
	public int size()
	{
		return methods.size();
	}

	public void accept(ReportVisitor visitor) throws ReportException
	{
		visitor.visit(this);
	}
}
