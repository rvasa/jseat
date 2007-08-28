package metric.core.report.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import metric.core.ReportDefinition;
import metric.core.exception.ReportException;
import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.VersionMetricData;
import metric.core.report.Report;
import metric.core.util.MetricTable;
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
public class EvolutionReportVisitor extends Report
{
	private String type;
	private String[] fields;

	public EvolutionReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

	@Override
	protected void processArgs() throws ReportException
	{
		try
		{
			type = (String) rd.args[0];
			try
			{
				fields = StringUtils.asStrings((Object[]) rd.args[1]);
			} catch (ClassCastException e)
			{
				fields = new String[] { (String) rd.args[1] };
			}
		} catch (ArrayIndexOutOfBoundsException e)
		{
			throw new ReportException();
		}
	}

	@Override
	public HashMap<String, Object> getArguments()
	{
		HashMap<String, Object> h = new HashMap<String, Object>();

		String[] constrainedEValues = Evolution.toStrings();
		h.put(ClassMetric.EVOLUTION.toString(), StringUtils.sort(
				constrainedEValues, type));

		String[] constrainedValues = ClassMetric.toStrings();
		h.put("Metric", StringUtils.sortAndExpand(fields, constrainedValues));
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
			if (e.getKey().equals(ClassMetric.EVOLUTION.toString()))
				type = ((ArrayList<String>) e.getValue()).get(0);
			else if (e.getKey().equals("Metric"))
			{
				ArrayList<String> list = (ArrayList<String>) e.getValue();
				fields = StringUtils.asStrings(list);
			}
		}
	}

	@Override
	public void visit(HistoryMetricData hmd) throws ReportException
	{
		int total = hmd.versions.size();
		ArrayList<String[]> rows = new ArrayList<String[]>();

		for (int i = 1; i <= total; i++)
		{
			VersionMetricData vmd = hmd.getVersion(i);
			ArrayList<String[]> tmpRows = getClassList(vmd, Evolution
					.parse(type), fields);
			for (String[] row : tmpRows)
				rows.add(row);
			updateProgress(i, total, vmd);
		}

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				getHeading(fields), rd.description);
		et.addRows(rows);
		et.setDisplayTitle(true);
		setTable(et);
	}

	public void visit(VersionMetricData vmd) throws ReportException
	{

		ArrayList<String[]> rows = getClassList(vmd, Evolution.parse(type),
				fields);
		updateProgress(1, 1, vmd);

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				getHeading(fields), rd.description);
		et.addRows(rows);
		et.setDisplayTitle(true);
		setTable(et);
	}

	public ArrayList<String[]> getClassList(VersionMetricData vmd,
			Evolution field, String[] fields)
	{
		int numRows = vmd.metricData.values().size();
		ArrayList<String[]> rows = new ArrayList<String[]>(numRows);
		for (ClassMetricData cm : vmd.metricData.values())
		{
			if (cm.getSimpleMetric(ClassMetric.NEXT_VERSION_STATUS) == field
					.getValue())
			{
				ArrayList<String> tmpRow = new ArrayList<String>();
				tmpRow.add(vmd.get(Version.NAME));
				tmpRow.add(vmd.get(Version.RSN));
				tmpRow.add(vmd.get(Version.ID));

				for (String metric : fields)
					tmpRow.add(cm.get(ClassMetric.parse(metric)));
				String[] row = new String[tmpRow.size()];
				tmpRow.toArray(row);
				rows.add(row);
			}
		}

		return rows;
	}

	private String[] getHeading(String[] fields)
	{
		// Have to construct heading dynamically
		ArrayList<String> heading = new ArrayList<String>();
		heading.add("name");
		heading.add(Version.RSN.toString());
		heading.add(Version.ID.toString());

		for (String str : fields)
			heading.add(ClassMetric.parse(str).toString());

		return StringUtils.asStrings(heading);
	}
}
