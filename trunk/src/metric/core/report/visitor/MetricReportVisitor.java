package metric.core.report.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import metric.core.ReportDefinition;
import metric.core.exception.ReportException;
import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.VersionMetricData;
import metric.core.report.Report;
import metric.core.util.MetricTable;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Version;

/**
 * Acts as a visitor to HistoryMetricData. When it visits HistoryMetricData it
 * will perform one of its methods as part of its visit.
 * 
 * The method invoked, is defined by attributes stored in
 * <code>MetricDefinition</code>.
 * 
 * @author Joshua Hayes, rvasa
 * 
 */
public class MetricReportVisitor extends Report
{

	private String type;

	private String metricType;

	public MetricReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

	@Override
	protected void processArgs() throws ReportException
	{
		try
		{
			type = (String) rd.args[0];
			// metricType = (String) rd.args[1];
		} catch (ArrayIndexOutOfBoundsException e)
		{
			throw new ReportException();
		}
	}

	@Override
	public HashMap<String, Object> getArguments()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setArguments(HashMap<String, Object> args)
	{
		// TODO Auto-generated method stub

	}

	public void visit(HistoryMetricData hmd) throws ReportException
	{
		int total = hmd.versions.size();
		ArrayList<String[]> rows = new ArrayList<String[]>();

		String[] headings = { "name", Version.RSN.toString(), Version.ID.toString(),
				Version.CLASS_COUNT.toString(), "class_stats" };
		for (int i = 1; i <= total; i++)
		{
			updateProgress(i, total);
			VersionMetricData vmd = hmd.getVersion(i);
			if (type.equals("metric"))
				for (String[] row : getRowsOfMetrics(vmd))
					rows.add(row);
			else if (type.equals("history") && metricType != null)
				printMetricHistory(hmd, metricType);
		}
		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				headings, rd.description);
		et.addRows(rows);
		et.setColumnPadding(1);
		et.setDisplayTitle(true);
		setTable(et);
	}

	public void visit(VersionMetricData vmd) throws ReportException
	{
		getRowsOfMetrics(vmd);
	}

	public ArrayList<String[]> getRowsOfMetrics(VersionMetricData vmd)
	{
		ArrayList<String[]> rows = new ArrayList<String[]>();
		for (ClassMetricData cm : vmd.metricData.values())
		{
			String[] row = { vmd.get(Version.NAME),
					vmd.get(Version.RSN), vmd.get(Version.ID),
					vmd.get(Version.CLASS_COUNT), cm.toString() };
			rows.add(row);
		}
		return rows;
	}

	public void printMetricHistory(HistoryMetricData hmd, String fieldName) // ,
	// String
	// constraintField, int
	// constraintMin, int
	// constraintMax)
	{
		try
		{
			Map<String, Integer[]> hm = createHistoryMap(hmd, fieldName); // ,
			// constraintField,
			// constraintMin,
			// constraintMax);
			printHistoryMap(hm);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
     * This method will create a map of history for the metric stored in
     * fieldName It will compute the metric value for a class name for all
     * versions. If the class does not exist in a version then the value will be
     * null for the metric
     * 
     * @return A name that contains class name and the metric values for each
     *         version
     */
	private Map<String, Integer[]> createHistoryMap(HistoryMetricData hmd,
			String fieldName)
			// ,
			// String
			// constraintField,
			// int
			// constraintMin,
			// int
			// constraintMax)
			throws IllegalArgumentException, SecurityException,
			IllegalAccessException, NoSuchFieldException
	{
		// TODO: This is not smart enough, as it will not work well when we have
		// classes that are removed for a version, then come back again.
		Map<String, Integer[]> historyMap = new HashMap<String, Integer[]>();
		for (int i = 1; i <= hmd.versions.size(); i++)
		{
			VersionMetricData v = hmd.versions.get(i);
			Map<String, Integer> versionMetricMap = getMetricMap(v, fieldName); // ,
			// constraintField,
			// constraintMin,
			// constraintMax);
			for (String className : versionMetricMap.keySet())
			{
				// first time, create the array object
				if (!historyMap.containsKey(className))
				{
					historyMap.put(className, new Integer[hmd.versions.size()]);
				}

				// Store the metric value against the version in array
				historyMap.get(className)[i - 1] = versionMetricMap
						.get(className);
			}
		}
		return historyMap;
	}

	private Map<String, Integer> getMetricMap(VersionMetricData vmd,
			String fieldName) throws IllegalArgumentException,
			SecurityException, IllegalAccessException, NoSuchFieldException
	{
		Map<String, Integer> m = new HashMap<String, Integer>();
		for (ClassMetricData c : vmd.metricData.values())
		{
			m.put(c.get(ClassMetric.NAME), c.getSimpleMetric(ClassMetric
					.parse(fieldName)));
		}
		return m;
	}

	private void printHistoryMap(Map<String, Integer[]> hm)
	{
		if (hm == null)
			return;
		for (String className : hm.keySet())
		{
			String line = className;
			Integer[] values = hm.get(className);
			for (Integer i : values)
			{
				if (i != null)
					line += "," + i;
				else
					line += ",";
			}
			System.out.println(line.substring(0, line.length()));
//			buffer.append(line.substring(0, line.length()));
//			logBuffer();
			// System.out.println(line.substring(0, line.length()));
		}
	}
}
