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

public class CumFreqReportVisitor extends Report
{
	private String[] headings = { "name", Version.RSN.toString(), Version.ID.toString(),
			Version.CLASS_COUNT.toString(), "freq" };

	private String metric;
	private int maxValue;

	public CumFreqReportVisitor(ReportDefinition md) throws ReportException
	{
		super(md);
	}

	protected void processArgs() throws ReportException
	{
		try
		{
			metric = (String) rd.args[0];
			maxValue = (Integer) rd.args[1];
		} catch (ArrayIndexOutOfBoundsException e)
		{
			throw new ReportException();
		}
	}

	@Override
	public HashMap<String, Object> getArguments()
	{
		HashMap<String, Object> h = new HashMap<String, Object>();
		h.put("MaxValue", maxValue);
		h.put("Metric", StringUtils.sort(ClassMetric.toStrings(), metric));
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
			if (e.getKey().equals("MaxValue"))
				maxValue = (Integer) e.getValue();
			else if (e.getKey().equals("Metric"))
				metric = ((ArrayList<String>) e.getValue()).get(0);
		}
	}

	public void visit(HistoryMetricData hmd) throws ReportException
	{
		int total = hmd.versions.size();
		ArrayList<String[]> rows = new ArrayList<String[]>(total);

		for (int i = 1; i <= hmd.versions.size(); i++)
		{
			VersionMetricData vmd = hmd.versions.get(i);
			String[] row = getCumFreqRow(vmd, maxValue, metric);
			rows.add(row);
			updateProgress(i, total);
		}

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				headings, rd.description);
		et.setColumnPadding(2);
		et.addRows(rows);
		et.setDisplayTitle(true);
		setTable(et);
	}

	public void visit(VersionMetricData vmd) throws ReportException
	{
		String[] row = getCumFreqRow(vmd, maxValue, metric);

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				headings, rd.description);
		et.setColumnPadding(2);
		et.addRow(row);
		setTable(et);
	}

	private String[] getCumFreqRow(VersionMetricData vmd, int maxValue,
			String field)
	{
		int[] range = vmd.getMetricRange(ClassMetric.parse(field));
		int[] freqTable = StatUtils.createFreqTable(range, maxValue);
		double[] cumlTable = StatUtils.toCummulFreqTable(freqTable);

		String[] row = { vmd.get(Version.NAME), vmd.get(Version.RSN),
				vmd.get(Version.ID), vmd.get(Version.CLASS_COUNT),
				StringUtils.toCSVString(cumlTable) };
		return row;
	}
}
