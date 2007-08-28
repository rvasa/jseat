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
public class PredictionReportVisitor extends Report
{
	private String[] fields;

	private ArrayList<String> heading;

	public PredictionReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
		heading = new ArrayList<String>();
		heading.add("name");
		heading.add(Version.RSN.toString());
		heading.add(Version.ID.toString());
		heading.add(Version.CLASS_COUNT.toString() + "_v1");
		heading.add(Version.CLASS_COUNT.toString() + "_v2");
		heading.add(Version.RELATIVE_SIZE_CHANGE.toString());
	}

	protected void processArgs() throws ReportException
	{
		try
		{
			try
			{
				fields = StringUtils.asStrings((Object[]) rd.args[0]);
			} catch (ClassCastException e)
			{
				fields = new String[] { (String) rd.args[0] };
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
			{
				ArrayList<String> list = (ArrayList<String>) e.getValue();
				fields = StringUtils.asStrings(list);
			}
		}
	}

	public void visit(HistoryMetricData hmd) throws ReportException
	{
		// Update heading
		for (int i = 0; i < fields.length; i++)
		{
			// Add to heading.
			heading.add(fields[i] + "_" + Version.ISUM.toString());
			heading.add(fields[i] + "_" + Version.PRED.toString());
			heading.add(fields[i] + "_" + Version.PRED_ERROR.toString());
		}

		ArrayList<String[]> rows = getPredictions(hmd, fields);

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				StringUtils.asStrings(heading), rd.description);
		et.addRows(rows);
		et.setColumnPadding(1);
		et.setDisplayTitle(true);
		setTable(et);
	}

	public ArrayList<String[]> getPredictions(HistoryMetricData hmd,
			String[] fields) throws ReportException
	{
		if (hmd.versions.size() < 2)
		{
			throw new ReportException(
					"Insufficient versions to compute predictions");
		}
		ArrayList<String[]> rows = new ArrayList<String[]>();
		for (int i = 2; i <= hmd.versions.size(); i++)
		{
			VersionMetricData v1 = hmd.getVersion(i-1);
			VersionMetricData v2 = hmd.getVersion(i);

			// Get isum, pred and pred_error for user specified fields.
			ArrayList<String> tmpRow = new ArrayList<String>();
			for (String string : fields)
			{
				ClassMetric field = ClassMetric.parse(string);
				tmpRow.add(v2.get(Version.ISUM, field));
				tmpRow.add(v2.get(Version.PRED, field, v1));
				tmpRow.add(v2.get(Version.PRED_ERROR, field, v1));
			}

			// All fields in final constructed row.
			ArrayList<String> finalRow = new ArrayList<String>();
			finalRow.add(v1.get(Version.NAME));
			finalRow.add(v2.get(Version.RSN));
			finalRow.add(v2.get(Version.ID));
			finalRow.add(v1.get(Version.CLASS_COUNT));
			finalRow.add(v2.get(Version.CLASS_COUNT));
			finalRow.add(v2.get(Version.RELATIVE_SIZE_CHANGE, v1));
			// add on dynamic variables we created before.
			for (String str : tmpRow)
				finalRow.add(str);

			String[] finalRowArray = new String[finalRow.size()];
			finalRow.toArray(finalRowArray);
			rows.add(finalRowArray);
		}
		return rows;
	}
}