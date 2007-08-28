package metric.core.report.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import metric.core.ReportDefinition;
import metric.core.exception.ReportException;
import metric.core.model.HistoryMetricData;
import metric.core.model.VersionMetricData;
import metric.core.report.Report;
import metric.core.util.MetricTable;
import metric.core.util.StringUtils;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Version;

/**
 * Acts as a visitor to HistoryMetricData. When it visits HistoryMetricData it
 * will perform one of its methods as part of its visit. The method invoked, is
 * defined by attributes stored in <code>MetricDefinition</code>.
 * 
 * @author Joshua Hayes, rvasa
 */
public class GreekReportVisitor extends Report
{
	private String type;
	private String[] fields;

	public GreekReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

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
		// Constrain 'type' field and sort the value specified in the
		// ReportDefinition to the front.
		String[] constrainedType = { Version.ALPHA.toString(),
				Version.BETA.toString() };
		h.put("Type", StringUtils.sort(constrainedType, type));

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
			if (e.getKey().equals("Type"))
				type = ((ArrayList<String>) e.getValue()).get(0);
			else if (e.getKey().equals("Metric"))
			{
				ArrayList<String> list = (ArrayList<String>) e.getValue();
				fields = StringUtils.asStrings(list);
			}
		}
	}

	public void visit(HistoryMetricData hmd)
	{
		int numRows = hmd.versions.size();
		ArrayList<String[]> rows = new ArrayList<String[]>(numRows);

		Version vType = Version.parse(type);

		for (int i = 1; i <= numRows; i++)
		{
			VersionMetricData vmd = hmd.getVersion(i);
			rows.add(getRow(vmd, vType, fields));
			updateProgress(i, numRows, vmd);
		}

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				getHeading(fields), rd.description);
		et.addRows(rows);
		et.setDisplayTitle(true);
		setTable(et);
	}

	public void visit(VersionMetricData vmd)
	{
		ArrayList<String[]> rows = new ArrayList<String[]>(1);

		Version vType = Version.parse(type);
		rows.add(getRow(vmd, vType, fields));

		updateProgress(1, 1, vmd);
		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				getHeading(fields), rd.description);
		et.addRows(rows);
		et.setDisplayTitle(true);
		setTable(et);
	}

	private String[] getHeading(String[] fields)
	{
		// Have to construct heading dynamically
		ArrayList<String> heading = new ArrayList<String>();
		heading.add("name");
		heading.add(Version.RSN.toString());
		heading.add(Version.ID.toString());
		heading.add(Version.CLASS_COUNT.toString());
		for (String str : fields)
			heading.add(ClassMetric.parse(str).toString());

		return StringUtils.asStrings(heading);
		// Setup final heading array to add to table.
//		String[] headings = new String[heading.size()];
//		heading.toArray(headings);
//		return headings;
	}

	public String[] getRow(VersionMetricData vmd, Version type, String[] fields)
	{
		ArrayList<String> row = new ArrayList<String>();
		row.add(vmd.get(Version.NAME));
		row.add(vmd.get(Version.RSN));
		row.add(vmd.get(Version.ID));
		row.add(vmd.get(Version.CLASS_COUNT));
		for (String field : fields)
			row.add(vmd.get(type, ClassMetric.parse(field)));
		return StringUtils.asStrings(row);
	}
}
