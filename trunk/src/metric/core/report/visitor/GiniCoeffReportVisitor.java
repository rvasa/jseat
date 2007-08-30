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
import metric.core.util.StatUtils;
import metric.core.util.StringUtils;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Version;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * Acts as a visitor to HistoryMetricData. When it visits HistoryMetricData it
 * will perform one of its methods as part of its visit. The method invoked, is
 * defined by attributes stored in <code>MetricDefinition</code>.
 * 
 * @author Joshua Hayes, rvasa
 */
public class GiniCoeffReportVisitor extends Report
{
	private int maxValue;
	private String[] fields;

	public GiniCoeffReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

	@Override
	protected void processArgs() throws ReportException
	{
		try
		{
			maxValue = (Integer) rd.args[0];
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
		String[] constrainedValues = ClassMetric.toStrings();
		h.put("Metric", StringUtils.sortAndExpand(fields, constrainedValues));
		h.put("MaxValue", maxValue);
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
			if (e.getKey().equals("Max_Value"))
				maxValue = (Integer) e.getValue();
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
		ArrayList<String[]> rows = new ArrayList<String[]>(hmd.versions.size());

		int total = hmd.versions.size();
		for (int i = 1; i <= total; i++)
		{
			VersionMetricData vmd = hmd.getVersion(i);
			updateProgress(i, total, vmd);
			rows.add(getGiniCoeffRow(vmd, maxValue, fields));
		}

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(getHeading(fields), rd.description);
		et.addRows(rows);
		et.setColumnPadding(2);
		et.setDisplayTitle(true);
		setTable(et);
	}

	@Override
	public void visit(VersionMetricData vmd) throws ReportException
	{
		String[] row = getGiniCoeffRow(vmd, maxValue, fields);

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(getHeading(fields), rd.description);
		et.addRow(row);
		et.setColumnPadding(2);
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
		for (String str : fields)
			heading.add(ClassMetric.parse(str).toString());

		// Setup final heading array to add to table.
		// Object[] headings = new Object[heading.size()];
		// heading.toArray(
		// headings);
		return StringUtils.asStrings(heading);
	}

	public String[] getGiniCoeffRow(VersionMetricData vmd, int maxValue, String[] fields)
	{
		ArrayList<String> tmpRow = new ArrayList<String>();
		tmpRow.add(vmd.get(Version.NAME));
		tmpRow.add(vmd.get(Version.RSN));
		tmpRow.add(vmd.get(Version.ID));

		for (String metric : fields)
			tmpRow.add(StatUtils.toFixedDecPlaces(getGiniCoeff(vmd, metric, maxValue), 3) + "");
		String[] row = new String[tmpRow.size()];
		tmpRow.toArray(row);
		return row;
	}

	private double getGiniCoeff(VersionMetricData vmd, String field, int maxValue)
	{
		int[] data = vmd.getMetricRange(ClassMetric.parse(field));
		data = StatUtils.createFreqTable(data, maxValue);

		DoubleArrayList d = new DoubleArrayList();
		for (int i : data)
			d.add(i);
		double relVars = 0;
		for (int i = 0; i < d.size(); i++)
		{
			for (int j = 0; j < d.size(); j++)
			{
				if (i == j)
					continue;
				relVars += (Math.abs(d.get(i) - d.get(j))) / Descriptive.mean(d);
			}
		}
		return (relVars / (2 * Math.pow(d.size(), 2)));
	}

}
