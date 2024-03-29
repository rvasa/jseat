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

/**
 * Acts as a visitor to HistoryMetricData. When it visits HistoryMetricData it
 * will perform one of its methods as part of its visit. The method invoked, is
 * defined by attributes stored in <code>MetricDefinition</code>.
 * 
 * @author Joshua Hayes, rvasa
 */
public class CountReportVisitor extends Report
{
	protected boolean doIsum;
	protected boolean doBeta;
	protected boolean doRelIsum;
	protected String[] fields;

	public CountReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

	@Override
	protected void processArgs() throws ReportException
	{
		try
		{
			doIsum = (Boolean) rd.args[0];
			doRelIsum = (Boolean) rd.args[1];
			doBeta = (Boolean) rd.args[2];
			try
			{
				fields = StringUtils.asStrings((Object[]) rd.args[3]);
			} catch (ClassCastException e)
			{
				fields = new String[] { (String) rd.args[3] };
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
		h.put("ISum", doIsum);
		h.put("RelISum", doRelIsum);
		h.put("Beta", doBeta);

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
			if (e.getKey().equals("ISum"))
				doIsum = (Boolean) e.getValue();
			else if (e.getKey().equals("Beta"))
				doBeta = (Boolean) e.getValue();
			else if (e.getKey().equals("RelISum"))
				doRelIsum = (Boolean) e.getValue();
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
		ArrayList<String[]> rows = new ArrayList<String[]>(total);

		for (int i = 1; i <= total; i++)
		{
			VersionMetricData vmd = hmd.getVersion(i);
			updateProgress(i, total, vmd);
			rows.add(getRowCounts(vmd, doIsum, doRelIsum, doBeta, fields));
		}
		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(getHeading(fields), rd.description);
		et.setColumnPadding(1);
		et.setDisplayTitle(true);
		et.addRows(rows);
		setTable(et);
		
		
	}

	@Override
	public void visit(VersionMetricData vmd) throws ReportException
	{
		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(getHeading(fields), rd.description);
		et.setColumnPadding(1);
		et.addRow(getRowCounts(vmd, doIsum, doRelIsum, doBeta, fields));
		et.setDisplayTitle(true);
		setTable(et);
	}

	protected String[] getHeading(String[] fields)
	{
		// Have to construct heading dynamically
		ArrayList<String> heading = new ArrayList<String>();
		heading.add("name");
		heading.add(Version.RSN.toString());
		heading.add(Version.ID.toString());
		heading.add(Version.CLASS_COUNT.toString());
		for (String str : fields)
		{
			if (doIsum)
				heading.add(str);
			if (doRelIsum)
				heading.add(ClassMetric.parse(str)+"%");
			if (doBeta)
				heading.add("beta_" + ClassMetric.parse(str));
		}

		return StringUtils.asStrings(heading);
	}

	protected String[] getRowCounts(VersionMetricData vmd, boolean isum, boolean relIsum, boolean beta, String[] fields)
	{
		int classCount = vmd.getSimpleMetric(Version.CLASS_COUNT);
		ArrayList<String> row = new ArrayList<String>();
		row.add(vmd.get(Version.NAME));
		row.add(vmd.get(Version.RSN));
		row.add(vmd.get(Version.ID));
		row.add(String.valueOf(classCount));

		for (String field : fields)
		{
			if (isum)
				row.add(vmd.get(Version.ISUM, ClassMetric.parse(field)));
			
			if (relIsum)
			{
				double deletes = vmd.getComplexMetric(Version.ISUM, ClassMetric.parse(field));
				double val = StatUtils.toFixedDecPlaces((deletes/classCount)*100, 3);
				row.add(String.valueOf(val));
			}
			if (beta)
				row.add(vmd.get(Version.BETA, ClassMetric.parse(field)));
		}
		return StringUtils.asStrings(row);
	}

}
