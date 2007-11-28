package metric.core.report.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;

import metric.core.ReportDefinition;
import metric.core.exception.ReportException;
import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.VersionMetricData;
import metric.core.report.Report;
import metric.core.util.MetricTable;
import metric.core.util.StatUtils;
import metric.core.util.StringUtils;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Evolution;
import metric.core.vocabulary.Version;

/**
 * Acts as a visitor to HistoryMetricData. When it visits HistoryMetricData it
 * will perform one of its methods as part of its visit. The method invoked, is
 * defined by attributes stored in <code>MetricDefinition</code>.
 * 
 * @author Joshua Hayes, rvasa
 */
public class RawDistanceReportVisitor extends Report
{
	private String type;
	private int max;

	private String[] distanceHeading = { "rsn", Version.RSN.toString(), "name", "distance" };
//	public ClassMetric[] m = { ClassMetric.METHOD_COUNT, ClassMetric.FAN_OUT_COUNT, ClassMetric.FAN_IN_COUNT,
//			ClassMetric.LOAD_COUNT, ClassMetric.STORE_COUNT, ClassMetric.BRANCH_COUNT, ClassMetric.TYPE_CONSTRUCTION_COUNT,
//			ClassMetric.FIELD_COUNT, ClassMetric.SUPER_CLASS_COUNT, ClassMetric.INNER_CLASS_COUNT };

	public RawDistanceReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

	@Override
	protected void processArgs() throws ReportException
	{
		try
		{
			type = (String) rd.args[0];
			max = (Integer) rd.args[1];
		} catch (ArrayIndexOutOfBoundsException e)
		{
			throw new ReportException();
		}
	}

	@Override
	public HashMap<String, Object> getArguments()
	{
		HashMap<String, Object> h = new HashMap<String, Object>();
		h.put("Metric", StringUtils.sort(ClassMetric.toStrings(), type));
		h.put("MaxValue", max);
		return h;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setArguments(HashMap<String, Object> args)
	{
		Set<Entry<String, Object>> set = args.entrySet();
		Iterator it = set.iterator();
		while (it.hasNext())
		{
			Entry<String, Object> e = (Entry<String, Object>) it.next();
			if (e.getKey().equals("Metric"))
				type = ((ArrayList<String>) e.getValue()).get(0);
			else if (e.getKey().equals("MaxValue"))
				max = (Integer) e.getValue();
		}
	}

	@SuppressWarnings("unchecked")
	public void visit(HistoryMetricData hmd) throws ReportException
	{
		ArrayList<String[]> rows = printDistances(hmd, type, max);
		MetricTable et = new MetricTable<String, String>(distanceHeading, rd.description);
		et.addRows(rows);
		et.setDisplayTitle(true);
		setTable(et);
	}

	private ArrayList<String[]> printDistances(HistoryMetricData hmd, String field, int max) throws ReportException
	{
		ArrayList<String[]> rows = new ArrayList<String[]>();

		if (hmd.versions.size() < 2)
			throw new ReportException("Insufficient versions to compute distances");

		for (int i = 2; i <= hmd.versions.size(); i++)
		{
			VersionMetricData v1 = hmd.getVersion(i - 1);
			updateProgress(i - 1, hmd.getVersions().size(), v1);
			VersionMetricData v2 = hmd.getVersion(i);
			updateProgress(i, hmd.getVersions().size(), v2);

			rows.addAll(getDistancesBetweenVersions(v1, v2));
		}

		sortClasses(hmd, ClassMetric.parse(field), max);
		return rows;
	}

	private ArrayList<String[]> getDistancesBetweenVersions(VersionMetricData vmd, VersionMetricData vmd2)
	{
		ArrayList<String[]> tmp = new ArrayList<String[]>();

		for (ClassMetricData cmd : vmd.metricData.values())
		{
			if (cmd.getSimpleMetric(ClassMetric.NEXT_VERSION_STATUS) == Evolution.MODIFIED.getValue())
			{				
				double d = 0.0d;
				for (ClassMetric metric : DistanceReportVisitor.METRICS)
				{
					d += StatUtils.sqr(vmd2.metricData.get(cmd.get(ClassMetric.NAME)).getSimpleMetric(metric)
							- vmd.metricData.get(cmd.get(ClassMetric.NAME)).getSimpleMetric(metric));
				}

				String[] row = { vmd.get(Version.RSN), vmd.get(Version.NAME), cmd.get(ClassMetric.NAME),
						String.valueOf(d) };
				tmp.add(row);
			}
		}
		return tmp;
	}
	
	private void sortClasses(HistoryMetricData hmd, ClassMetric metric, int maxValue)
	{
		ArrayList<String[]> rows = new ArrayList<String[]>();
		int rowNum = maxValue, colNum = 17;

		int[][] d = new int[rowNum][colNum];

		for (int i = 2; i <= hmd.versions.size(); i++)
		{
			VersionMetricData v1 = hmd.getVersion(i - 1);
			updateProgress(i - 1, hmd.getVersions().size(), v1);
			VersionMetricData v2 = hmd.getVersion(i);
			updateProgress(i, hmd.getVersions().size(), v2);

			for (ClassMetricData cmd : v1.metricData.values())
			{
				if (cmd.getSimpleMetric(ClassMetric.NEXT_VERSION_STATUS) == Evolution.MODIFIED.getValue())
				{
					// Natural log distance.
					double dist = 0.0d;
					for (ClassMetric theMetric : DistanceReportVisitor.METRICS)
					{
						dist += StatUtils.sqr(v2.metricData.get(cmd.get(ClassMetric.NAME)).getSimpleMetric(theMetric)
								- v1.metricData.get(cmd.get(ClassMetric.NAME)).getSimpleMetric(theMetric));
					}
					double nDistance = Math.log(dist);
					if (nDistance >= 0)
					{
						int rowIndex = cmd.getSimpleMetric(metric);

						if (rowIndex >= rowNum)
						{
							rowIndex = rowNum - 1;
						}
						if (nDistance >= colNum)
						{
							nDistance = colNum - 1;
						} else if (nDistance < 1.0d)
						{
							nDistance = 0;
						}

						d[rowIndex][(int) nDistance]++;
					}
				}
			}
		}

		for (int[] r : d)
		{
			for (int c : r)
			{
				System.out.print(c + ",");
			}
			System.out.println();
		}

		// Always sort by distance across first.
		// Sort by other metric.
	}

}
