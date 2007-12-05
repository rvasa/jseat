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
public class EarthquakeReportVisitor extends Report
{
	private String type;
	private int max;
	private boolean relative;

	private String[] distanceHeading = { "Name", "rsn", "ID", TremorMagnitude.MICRO.toString(), TremorMagnitude.MINOR.toString(),
			TremorMagnitude.LIGHT.toString(),TremorMagnitude.MODERATE.toString(),TremorMagnitude.STRONG.toString(),
			TremorMagnitude.MAJOR.toString(),TremorMagnitude.GREAT.toString(),TremorMagnitude.HUGE.toString(),
			TremorMagnitude.HUGE2.toString(),TremorMagnitude.HUGE3.toString(),TremorMagnitude.HUGE4.toString(),
			TremorMagnitude.HUGE5.toString(),TremorMagnitude.HUGE6.toString(),TremorMagnitude.HUGE7.toString(),
			TremorMagnitude.HUGE8.toString()};
	
	private String[] dataOnlyHeading = {TremorMagnitude.MICRO.toString(), TremorMagnitude.MINOR.toString(),
			TremorMagnitude.LIGHT.toString(),TremorMagnitude.MODERATE.toString(),TremorMagnitude.STRONG.toString(),
			TremorMagnitude.MAJOR.toString(),TremorMagnitude.GREAT.toString(),TremorMagnitude.HUGE.toString(),
			TremorMagnitude.HUGE2.toString(),TremorMagnitude.HUGE3.toString(),TremorMagnitude.HUGE4.toString(),
			TremorMagnitude.HUGE5.toString(),TremorMagnitude.HUGE6.toString(),TremorMagnitude.HUGE7.toString(),
			TremorMagnitude.HUGE8.toString()};
	
	enum TremorMagnitude
	{
		MICRO,
		MINOR,
		LIGHT,
		MODERATE,
		STRONG,
		MAJOR,
		GREAT,
		HUGE,
		HUGE2,
		HUGE3,
		HUGE4,
		HUGE5,
		HUGE6,
		HUGE7,
		HUGE8,
	}

	public EarthquakeReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

	@SuppressWarnings("unchecked")
	public void visit(HistoryMetricData hmd) throws ReportException
	{
		ArrayList<String[]> rows = printDistances(hmd, type, max);
		MetricTable et = null;
		if (!dataOnly())
			et = new MetricTable<String, String>(distanceHeading, rd.description);
		else
			et = new MetricTable<String, String>(dataOnlyHeading, rd.description);
		
		et.addRows(rows);
		if (!dataOnly())
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

			rows.add(getDistancesBetweenVersions(v1, v2));
		}
		return rows;
	}

	private String[] getDistancesBetweenVersions(VersionMetricData vmd, VersionMetricData vmd2)
	{
		HashMap<TremorMagnitude, Double> table = new HashMap<TremorMagnitude, Double>();
		
		for (TremorMagnitude tm : TremorMagnitude.values())
			table.put(tm, 0d);

		for (ClassMetricData cmd : vmd.metricData.values())
		{
			if (cmd.getSimpleMetric(ClassMetric.NEXT_VERSION_STATUS) == Evolution.MODIFIED.getValue())
			{
				double d = 0.0;
				for (ClassMetric metric : DistanceReportVisitor.METRICS)
				{
					d += StatUtils.sqr(vmd2.metricData.get(cmd.get(ClassMetric.NAME)).getSimpleMetric(metric)
							- vmd.metricData.get(cmd.get(ClassMetric.NAME)).getSimpleMetric(metric));
				}

				updateTable(table, StatUtils.toFixedDecPlaces(d, 2));
			}
		}
		if (relative)
		{
			// converts to a percentage. Crude, but works.
			for (TremorMagnitude tm : TremorMagnitude.values())
			{
				table.put(tm, StatUtils.toFixedDecPlaces(((double)table.get(tm)/vmd.metricData.size())*100, 2));
			}
		}
		
		if (!dataOnly())
		{
			String[] row = { vmd.get(Version.NAME), vmd.get(Version.RSN) + "-" + vmd2.get(Version.RSN), vmd.get(Version.ID), table.get(TremorMagnitude.MICRO).toString(),
					table.get(TremorMagnitude.MINOR).toString(),table.get(TremorMagnitude.LIGHT).toString(),
					table.get(TremorMagnitude.MODERATE).toString(), table.get(TremorMagnitude.STRONG).toString(),
					table.get(TremorMagnitude.MAJOR).toString(), table.get(TremorMagnitude.GREAT).toString(),
					table.get(TremorMagnitude.HUGE).toString(), table.get(TremorMagnitude.HUGE2).toString(),
					table.get(TremorMagnitude.HUGE3).toString(), table.get(TremorMagnitude.HUGE4).toString(),
					table.get(TremorMagnitude.HUGE5).toString(), table.get(TremorMagnitude.HUGE6).toString(),
					table.get(TremorMagnitude.HUGE7).toString(), table.get(TremorMagnitude.HUGE8).toString()};
			
			return row;
		}
		else
		{
			String[] row = {table.get(TremorMagnitude.MICRO).toString(),
					table.get(TremorMagnitude.MINOR).toString(),table.get(TremorMagnitude.LIGHT).toString(),
					table.get(TremorMagnitude.MODERATE).toString(), table.get(TremorMagnitude.STRONG).toString(),
					table.get(TremorMagnitude.MAJOR).toString(), table.get(TremorMagnitude.GREAT).toString(),
					table.get(TremorMagnitude.HUGE).toString(), table.get(TremorMagnitude.HUGE2).toString(),
					table.get(TremorMagnitude.HUGE3).toString(), table.get(TremorMagnitude.HUGE4).toString(),
					table.get(TremorMagnitude.HUGE5).toString(), table.get(TremorMagnitude.HUGE6).toString(),
					table.get(TremorMagnitude.HUGE7).toString(), table.get(TremorMagnitude.HUGE8).toString()};
			
			return row;
		}
	}
	
	private void updateTable(HashMap<TremorMagnitude, Double> table, double rawDist)
	{
		double logDist = Math.log(rawDist);
		if (logDist < 2)
			table.put(TremorMagnitude.MICRO, (table.get(TremorMagnitude.MICRO)+1));
		else if (logDist > 2 && logDist < 3.9)
			table.put(TremorMagnitude.MINOR, (table.get(TremorMagnitude.MINOR)+1));
		else if (logDist > 3.9 && logDist < 4.9)
			table.put(TremorMagnitude.LIGHT, (table.get(TremorMagnitude.LIGHT)+1));
		else if (logDist > 4.9 && logDist < 5.9)
			table.put(TremorMagnitude.MODERATE, (table.get(TremorMagnitude.MODERATE)+1));
		else if (logDist > 5.9 && logDist < 6.9)
			table.put(TremorMagnitude.STRONG, (table.get(TremorMagnitude.STRONG)+1));
		else if (logDist > 6.9 && logDist < 7.9)
			table.put(TremorMagnitude.MAJOR, (table.get(TremorMagnitude.MAJOR)+1));
		else if (logDist > 7.9 && logDist < 8.9)
			table.put(TremorMagnitude.GREAT, (table.get(TremorMagnitude.GREAT)+1));
		else if (logDist > 9.0 && logDist < 9.9)
			table.put(TremorMagnitude.HUGE, (table.get(TremorMagnitude.HUGE)+1));
		else if (logDist > 10.0  && logDist < 10.9)
			table.put(TremorMagnitude.HUGE2, (table.get(TremorMagnitude.HUGE2)+1));
		else if (logDist > 11.0  && logDist < 11.9)
			table.put(TremorMagnitude.HUGE3, (table.get(TremorMagnitude.HUGE3)+1));
		else if (logDist > 12.0 && logDist < 12.9)
			table.put(TremorMagnitude.HUGE4, (table.get(TremorMagnitude.HUGE4)+1));
		else if (logDist > 13.0 && logDist < 13.9)
			table.put(TremorMagnitude.HUGE5, (table.get(TremorMagnitude.HUGE5)+1));
		else if (logDist > 14.0 && logDist < 14.9)
			table.put(TremorMagnitude.HUGE6, (table.get(TremorMagnitude.HUGE6)+1));
		else if (logDist > 15.0 && logDist < 15.9)
			table.put(TremorMagnitude.HUGE7, (table.get(TremorMagnitude.HUGE7)+1));
		else if (logDist > 16.0 && logDist < 16.9)
			table.put(TremorMagnitude.HUGE8, (table.get(TremorMagnitude.HUGE8)+1));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setArguments(HashMap<String, Object> args)
	{
		Set<Entry<String, Object>> set = args.entrySet();
		Iterator it = set.iterator();
		while (it.hasNext())
		{
			Entry<String, Object> e = (Entry<String, Object>) it.next();
			if (e.getKey().equals("Relative"))
				relative = (Boolean) e.getValue();
		}
	}

	@Override
	public HashMap<String, Object> getArguments()
	{
		HashMap<String, Object> h = new HashMap<String, Object>();
		h.put("Relative", relative);
		return h;
	}

	@Override
	protected void processArgs() throws ReportException
	{
		try
		{
			relative = (Boolean) rd.args[0];
		} catch (ArrayIndexOutOfBoundsException e)
		{
			throw new ReportException();
		}
	}
}
