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

public class ConstrainedFreqReportVisitor extends Report
{
	private String metric;
	private String cMetric;
	private int maxValue;
	private int cMin;
	private int cMax;
	private boolean relative;

	public ConstrainedFreqReportVisitor(ReportDefinition md)
			throws ReportException
	{
		super(md);
	}

	@Override
	protected void processArgs() throws ReportException
	{
		try
		{
			metric = (String) rd.args[0];
			maxValue = (Integer) rd.args[1];
			relative = (Boolean) rd.args[2];
			cMetric = (String) rd.args[3];
			cMin = (Integer) rd.args[4];
			cMax = (Integer) rd.args[5];
		} catch (ArrayIndexOutOfBoundsException e)
		{
			throw new ReportException();
		}
	}

	@Override
	public HashMap<String, Object> getArguments()
	{
		HashMap<String, Object> h = new HashMap<String, Object>();
		h.put("Metric", StringUtils.sort(ClassMetric.toStrings(), metric));
		h.put("MaxValue", maxValue);
		h.put("Relative", relative);
		h.put("ConstraintMetric", StringUtils.sort(ClassMetric.toStrings(),
				cMetric));
		h.put("ConstraintMin", cMin);
		h.put("ConstraintMax", cMax);
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
				metric = ((ArrayList<String>) e.getValue()).get(0);
			else if (e.getKey().equals("ConstraintMetric"))
				cMetric = ((ArrayList<String>) e.getValue()).get(0);
			else if (e.getKey().equals("MaxValue"))
				maxValue = (Integer) e.getValue();
			else if (e.getKey().equals("ConstraintMin"))
				cMin = (Integer) e.getValue();
			else if (e.getKey().equals("ConstraintMax"))
				cMax = (Integer) e.getValue();
			else if (e.getKey().equals("Relative"))
				relative = (Boolean) e.getValue();
		}
	}

	@Override
	public void visit(HistoryMetricData hmd) throws ReportException
	{
		int total = hmd.versions.size();
		ArrayList<String[]> rows = new ArrayList<String[]>(total);

		for (int i = 1; i <= hmd.versions.size(); i++)
		{
			VersionMetricData vmd = hmd.versions.get(i);

			String[] row = getConstrainedFreq(vmd, metric, maxValue, relative,
					cMetric, cMin, cMax);
			rows.add(row);
			updateProgress(i, total);
		}

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				getHeading(maxValue), rd.description);

		et.addRows(rows);
		et.setDisplayTitle(true);
		setTable(et);
	}

	@Override
	public void visit(VersionMetricData vmd) throws ReportException
	{
		String[] row = getConstrainedFreq(vmd, metric, maxValue, relative,
				cMetric, cMin, cMax);

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				getHeading(maxValue), rd.description);
		
		et.setDisplayTitle(true);
		et.addRow(row);
		setTable(et);
	}

	public String[] getConstrainedFreq(VersionMetricData vmd, String field,
			int max, boolean relative, String cField, int cMin, int cMax)
	{
		int[] range = vmd.getMetricRange(ClassMetric.parse(field));
		int[] cRange = vmd.getMetricRange(ClassMetric.parse(cField));

		int[] constrainedFreq = StatUtils.toConstrainedFreqTable(range, cRange,
				cMin, cMax, max);

		ArrayList<String> row = new ArrayList<String>();
		row.add(vmd.get(Version.NAME));
		row.add(vmd.get(Version.RSN));
		row.add(vmd.get(Version.ID));
		row.add(vmd.get(Version.CLASS_COUNT));
		if (relative)
		{
			for (double freq : StatUtils.toRelativeFreqTable(constrainedFreq))
				row.add(String.valueOf(StatUtils.toFixedDecPlaces(freq, 3)));
		} else
		{
			for (int freq : constrainedFreq)
				row.add(String.valueOf(freq));
		}
		return StringUtils.asStrings(row);
	}

	protected String[] getHeading(int cols)
	{
		// Have to construct heading dynamically
		ArrayList<String> heading = new ArrayList<String>();
		heading.add("name");
		heading.add(Version.RSN.toString());
		heading.add(Version.ID.toString());
		heading.add(Version.CLASS_COUNT.toString());
		for (int i = 1; i <= cols; i++)
		{
			heading.add("F" + i);
		}

		// Setup final heading array to add to table.
		Object[] headings = new Object[heading.size()];
		heading.toArray(headings);
		return StringUtils.asStrings(heading);
	}
}
