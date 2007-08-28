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
import metric.core.util.CSVUtil;
import metric.core.util.MetricTable;
import metric.core.util.StatUtils;
import metric.core.util.StringUtils;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Version;

public class FreqReportVisitor extends Report
{
	private String[] headings = { "name", Version.RSN.toString(), Version.ID.toString(),
			Version.CLASS_COUNT.toString(), "freq" };

	private String metric;
	private int maxValue;
	private boolean relative;

	public FreqReportVisitor(ReportDefinition md) throws ReportException
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
			else if (e.getKey().equals("MaxValue"))
				maxValue = (Integer) e.getValue();
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
			VersionMetricData vmd = hmd.getVersion(i);
			String[] row = getFreqRow(vmd, metric, maxValue, relative);
			rows.add(row);
			updateProgress(i, total, vmd);
		}

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				headings, rd.description);
		et.setColumnPadding(1);
		et.addRows(rows);
		et.setDisplayTitle(true);
		setTable(et);

	}

	@Override
	public void visit(VersionMetricData vmd) throws ReportException
	{
		String[] row = getFreqRow(vmd, metric, maxValue, relative);

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				headings, rd.description);
		et.setColumnPadding(1);
		et.addRow(row);
		et.setDisplayTitle(true);
		setTable(et);
	}

	/**
     * Computes frequency and relative frequency.
     */
	public String[] getFreqRow(VersionMetricData vmd, String field,
			int maxValue, boolean relative)
	{
		int[] range = vmd.getMetricRange(ClassMetric.parse(field));
		int[] freqTable = StatUtils.createFreqTable(range, maxValue);
		if (relative)
		{
			return new String[] {
					vmd.get(Version.NAME),
					vmd.get(Version.RSN),
					vmd.get(Version.ID),
					vmd.get(Version.CLASS_COUNT),
					CSVUtil.toCSVString(StatUtils
							.toRelativeFreqTable(freqTable)) };
		} else
			return new String[] { vmd.get(Version.NAME),
					vmd.get(Version.RSN), vmd.get(Version.ID),
					vmd.get(Version.CLASS_COUNT),
					CSVUtil.toCSVString(freqTable, true) };
	}

}
