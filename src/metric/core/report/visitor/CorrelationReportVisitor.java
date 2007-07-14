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
import metric.core.util.StatUtils;
import metric.core.util.StringUtils;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Version;
import cern.colt.list.DoubleArrayList;

/**
 * Acts as a visitor to HistoryMetricData. When it visits HistoryMetricData it
 * will perform one of its methods as part of its visit. The method invoked, is
 * defined by attributes stored in <code>MetricDefinition</code>.
 * 
 * @author Joshua Hayes, rvasa
 */
public class CorrelationReportVisitor extends Report
{
	private String[] fields;
	private String baseMetric;

	public CorrelationReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

	@Override
	protected void processArgs() throws ReportException
	{
		try
		{
			baseMetric = (String) rd.args[0];
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
		h.put("BaseMetric", StringUtils.sort(ClassMetric.toStrings(),
				baseMetric));

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
			if (e.getKey().equals("BaseMetric"))
				baseMetric = ((ArrayList<String>) e.getValue()).get(0);
			else if (e.getKey().equals("Metric"))
			{
				ArrayList<String> list = (ArrayList<String>) e.getValue();
				fields = StringUtils.asStrings(list);
			}
		}
	}

	public void visit(HistoryMetricData hmd) throws ReportException
	{
		ArrayList<String[]> rows;

		rows = getRowCorrelations(hmd, baseMetric, fields);

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
		for (String str : fields)
			heading.add(str);

		// Setup final heading array to add to table.
//		Object[] headings = new String[heading.size()];
//		heading.toArray(headings);
		return StringUtils.asStrings(heading);
	}

	private ArrayList<String[]> getRowCorrelations(HistoryMetricData hmd,
			String baseMetric, String[] otherMetrics) throws ReportException
	{
		if (hmd.versions.size() < 2)
			throw new ReportException(
					"Insufficient number of versions for correlation.");

		ArrayList<String[]> rows = new ArrayList<String[]>();
		for (int i = 2; i < hmd.versions.size(); i++)
		{
			VersionMetricData v = hmd.versions.get(i);
			ArrayList<String> row = new ArrayList<String>();
			try
			{
				// double[] correls = v.getComplexMetric(Version.CORRELATION,
				// ClassMetric.parse(baseMetric), otherMetrics);
				double[] correls = correlation(v,
						ClassMetric.parse(baseMetric), otherMetrics);
				row.add(v.get(Version.NAME));
				row.add(v.get(Version.RSN));
				row.add(v.get(Version.ID));
				for (double c : correls)
					row.add(String.valueOf(StatUtils.toFixedDecPlaces(c, 3)));
			} catch (Exception e)
			{
				e.printStackTrace();
				throw new ReportException(e.getMessage());
			}

			rows.add(StringUtils.asStrings(row));
			updateProgress(i, hmd.versions.size());
		}
		return rows;
	}

	/** Correlate survivors with instability metric and others as needed */
	public double[] correlation(VersionMetricData vmd, ClassMetric baseMetric,
			String[] otherMetrics) throws Exception
	{
		double[] correls = new double[otherMetrics.length];
		for (int i = 0; i < otherMetrics.length; i++)
		{
			DoubleArrayList m1 = new DoubleArrayList();
			DoubleArrayList m2 = new DoubleArrayList();
			for (ClassMetricData cm : vmd.metricData.values())
			{
				if (cm.getSimpleMetric(ClassMetric.IS_INTERFACE) == 1)
					continue; // ignore interfaces

				m1.add(cm.getSimpleMetric(baseMetric));
				m2.add(cm.getSimpleMetric(ClassMetric.parse(otherMetrics[i])));
			}
			correls[i] = StatUtils.calcCorrelation(m1, m2);
		}
		return correls;
	}
}
