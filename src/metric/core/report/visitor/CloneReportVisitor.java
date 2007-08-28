package metric.core.report.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import metric.core.ReportDefinition;
import metric.core.exception.ReportException;
import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.VersionMetricData;
import metric.core.report.Report;
import metric.core.util.MetricTable;
import metric.core.vocabulary.Version;

/**
 * Acts as a visitor to HistoryMetricData. When it visits HistoryMetricData it
 * will perform one of its methods as part of its visit. The method invoked, is
 * defined by attributes stored in <code>MetricDefinition</code>.
 * 
 * @author Joshua Hayes, rvasa
 */
public class CloneReportVisitor extends Report
{
	// Setup headings.
	private String[] headings = { Version.NAME.toString(),
			Version.RSN.toString(), Version.ID.toString(), "Clusters" };

	public CloneReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

	@Override
	protected void processArgs() throws ReportException
	{
	} // Has no arguments.

	@Override
	public HashMap<String, Object> getArguments()
	{
		return null;
	} // Cannot be configured.

	@Override
	public void setArguments(HashMap<String, Object> args)
	{
	} // Cannot be configured.

	@Override
	public void visit(HistoryMetricData hmd) throws ReportException
	{
		int total = hmd.versions.size();
		ArrayList<String[]> rows = new ArrayList<String[]>(total);

		for (int i = 1; i <= total; i++)
		{
			VersionMetricData vmd = hmd.getVersion(i);
			updateProgress(i, total, vmd);
			rows.add(getCloneClusters(vmd));
		}

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				headings, rd.description);
		et.setColumnPadding(2);
		et.addRows(rows);
		et.setDisplayTitle(true);
		setTable(et);
	}

	@Override
	public void visit(VersionMetricData vmd) throws ReportException
	{
		String[] row = getCloneClusters(vmd);

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				headings, rd.description);
		et.setColumnPadding(2);
		et.setDisplayTitle(true);
		et.addRow(row);
		setTable(et);

	}

	/** Print the clusters in the data, i.e. those that are clones */
	public String[] getCloneClusters(VersionMetricData vmd)
	{
		List<ClassMetricData> data = new LinkedList<ClassMetricData>(
				vmd.metricData.values());
		Collections.sort(data);
		int clusters = 0;
		LinkedList<ClassMetricData> list = new LinkedList<ClassMetricData>();
		for (ClassMetricData cm : data)
		{
			if (list.size() == 0)
			{
				list.addFirst(cm);
				continue;
			}
			if (cm.equals(list.getFirst()))
				list.addFirst(cm);
			else
			{
				if (list.size() == 1)
				{
					list.clear();
					continue;
				}

				// iterate over the stack and dump it to screen
				// if (list.get(0).getMetric(ClassMetric.IS_INTERFACE) != 1)
				// clusters++;
				for (ClassMetricData c : list)
					System.out.println(c);
				System.out.println("--- " + list.size() + " items ---\n");
				list.clear();
			}
		}
		clusters = list.size();
		String[] row = { vmd.get(Version.NAME), vmd.get(Version.RSN),
				vmd.get(Version.ID), clusters + "" };
		return row;
	}

}
