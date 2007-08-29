package metric.core.report.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import metric.core.vocabulary.Reporting;
import metric.core.vocabulary.Version;

/**
 * Acts as a visitor to HistoryMetricData. When it visits HistoryMetricData it
 * will perform one of its methods as part of its visit.
 * 
 * The method invoked, is defined by attributes stored in
 * <code>MetricDefinition</code>.
 * 
 * @author Joshua Hayes, rvasa
 * 
 */
public class SurvivorReportVisitor extends Report
{
	private int survivorThreshold;
	private boolean relative;

	public SurvivorReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

	@Override
	protected void processArgs() throws ReportException
	{
		try
		{
			survivorThreshold = (Integer) rd.args[0];
			relative = (Boolean) rd.args[1];
		} catch (ArrayIndexOutOfBoundsException e)
		{
			throw new ReportException();
		}
	}

	@Override
	public HashMap<String, Object> getArguments()
	{
		HashMap<String, Object> h = new HashMap<String, Object>();
		// String[] constrainedValues = ClassMetric.toStrings();
		h.put("Threshold", survivorThreshold);
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
			if (e.getKey().equals("Threshold"))
				survivorThreshold = (Integer) e.getValue();
			else if (e.getKey().equals("Relative"))
				relative = (Boolean) e.getValue();
		}
	}

	@Override
	public void visit(HistoryMetricData hmd) throws ReportException
	{
		printSurvivors(hmd, survivorThreshold, relative);
	}

	@Override
	public void visit(VersionMetricData vmd) throws ReportException
	{
		getSurvivorClassNames(vmd, false, survivorThreshold);
	}

	/** Calculates the number of classes that did not change between versions */
	public void printSurvivors(HistoryMetricData hmd, int survivorThreshold,
			boolean relative)
	{
		if (hmd.versions.size() < 2)
			return;
		int numRows = hmd.versions.size();
		ArrayList<String[]> rows = new ArrayList<String[]>(numRows);
		for (int i = 2; i <= hmd.versions.size(); i++)
		{
			VersionMetricData v1 = hmd.getVersion(i-1);
			updateProgress(i-1, hmd.getVersions().size(), v1);
			VersionMetricData v2 = hmd.getVersion(i);
			updateProgress(i, hmd.getVersions().size(), v2);

			int count = ((Set<String>) getSurvivorClassNames(v2, true, 1))
					.size();

			// double survivors = (double) count
			// / v2.getMetric(Version.CLASS_COUNT);

			String[] row = {
					v1.get(Version.NAME),
					v1.get(Version.RSN) + "-" + v2.get(Version.RSN),
					v1.get(Version.ID),
					v1.get(Version.CLASS_COUNT),
					v2.get(Version.ID),
					v2.get(Version.CLASS_COUNT),
					getSurvivorNumber(count, v2.getSimpleMetric(Version.CLASS_COUNT),
							relative) };
			// count + "", StatUtils.toFixedDecPlaces(survivors, 3) + "" };

			rows.add(row);
		}

		// Setup headings.
		Enum[] headings = { Version.NAME, Version.RSN, Version.ID,
				Version.CLASS_COUNT, Version.ID, Version.CLASS_COUNT,
				Reporting.SURVIVORS };

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				StringUtils.asStrings(headings), rd.description);
		et.addRows(rows);
		et.setColumnPadding(1);
		et.setDisplayTitle(true);
		setTable(et);
	}

	/** Calculates the number of classes that did not change between versions */
	public void printSurvivorsFromBirth(HistoryMetricData hmd,
			int survivorThreshold, boolean relative)
	{
		if (hmd.versions.size() < 2)
			return;
		int numRows = hmd.versions.size();
		ArrayList<String[]> rows = new ArrayList<String[]>(numRows);
		for (int i = 1; i <= hmd.versions.size(); i++)
		{
			// VersionMetricData v1 = hmd.versions.get(i - 1);
			VersionMetricData v2 = hmd.getVersion(i);
			updateProgress(i, hmd.getVersions().size(), v2);

			int count = ((Set<String>) getSurvivorClassNames(v2, true,
					survivorThreshold)).size();

			// double survivors = (double) count
			// / v2.getMetric(Version.CLASS_COUNT);

			String[] row = {
					v2.get(Version.NAME),
					v2.get(Version.RSN),
					v2.get(Version.ID),
					v2.get(Version.CLASS_COUNT),
					getSurvivorNumber(count, v2.getSimpleMetric(Version.CLASS_COUNT),
							relative) };
			// count + "", StatUtils.toFixedDecPlaces(survivors, 3) + "" };

			rows.add(row);
		}

		// Setup headings.
		Enum[] headings = { Version.NAME, Version.RSN, Version.ID,
				Version.CLASS_COUNT, Reporting.SURVIVORS };

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				StringUtils.asStrings(headings), rd.description);
		et.addRows(rows);
		et.setColumnPadding(1);
		et.setDisplayTitle(true);
		setTable(et);
	}

	/** If it has an age >= threshold, than it is a survivor */
	// TODO update to make more useful.
	public Set<String> getSurvivorClassNames(VersionMetricData vmd,
			boolean silent, int threshold)
	{
		Set<String> survivors = new HashSet<String>();
		for (ClassMetricData cm : vmd.metricData.values())
		{
			if (cm.getSimpleMetric(ClassMetric.BORN_RSN) >= threshold)
				survivors.add(cm.get(ClassMetric.NAME));
		}
		return survivors;
	}

	private String getSurvivorNumber(int count, int totalCount, boolean relative)
	{
		if (relative)
		{
			double survivors = (double) count / totalCount;
			return String.valueOf(StatUtils.toFixedDecPlaces(survivors, 3));
		} else
			return String.valueOf(count);
	}
}
