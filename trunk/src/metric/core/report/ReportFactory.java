package metric.core.report;

import java.lang.reflect.Constructor;

import metric.core.ReportDefinition;
import metric.core.ReportDefinitionRepository;
import metric.core.exception.MalformedReportDefinition;
import metric.core.exception.ReportException;
import metric.core.report.visitor.ReportVisitor;

public class ReportFactory
{
	private static final String PACKAGE_NAME = "metric.core.report.visitor.";

	public static ReportVisitor getReport(ReportDefinition definition) throws ReportException
	{
		Constructor con = null;
		try
		{
			// Instantiate concrete reporting visitor
			try
			{
				Class[] consArgs = new Class[] { ReportDefinition.class };

				con = Class.forName(PACKAGE_NAME + definition.className).getConstructor(consArgs);
			} catch (ClassNotFoundException e)
			{
				throw new ReportException("Unable to instantiate report: " + definition.description);
			}
			return (Report) con.newInstance(definition);
		} catch (Exception e)
		{
			throw new ReportException("Unable to instantiate report: " + definition.description);
		}
	}

	public static ReportVisitor getReport(String line) throws ReportException, MalformedReportDefinition
	{
		return getReport(ReportDefinitionRepository.parseDefinition(line));
	}

	// public static ReportVisitor getReport(String[] lines) throws
	// ReportException, MalformedReportDefinition
	// {
	// CompositeMetricDataVisitor cr = new CompositeMetricDataVisitor();
	//
	// for (String line : lines)
	// {
	// cr.add(getReport(ReportDefinitionRepository.parseDefinition(line)));
	// }
	// return cr;
	// }

	// public static ReportVisitor getReport(ReportDefinition[] definitions)
	// throws ReportException
	// {
	// CompositeMetricDataVisitor cr = new CompositeMetricDataVisitor();
	//
	// for (ReportDefinition def : definitions)
	// {
	// cr.add(getReport(def));
	// }
	// return cr;
	// }

}
