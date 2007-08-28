package metric.core.report.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import metric.core.ReportDefinition;
import metric.core.exception.ReportException;
import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.MethodMetricMap;
import metric.core.model.VersionMetricData;
import metric.core.report.Report;
import metric.core.util.MetricTable;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Version;

public class CyclomaticComplexityReportVisitor extends Report
{
	String[] heading = { Version.NAME.toString(), Version.RSN.toString(),
			Version.ID.toString(), "Cyclomatic Complexity" };

	public CyclomaticComplexityReportVisitor(ReportDefinition rd)
			throws ReportException
	{
		super(rd);
	}

	@Override
	public HashMap<String, Object> getArguments()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void processArgs() throws ReportException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setArguments(HashMap<String, Object> args)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(HistoryMetricData hmd) throws ReportException
	{
		int versions = hmd.versions.values().size();
		ArrayList<String[]> rows = new ArrayList<String[]>(versions);

		for (int i = 1; i <= versions; i++)
		{
			rows.add(getCycloComplexity(hmd.getVersion(i)));
			
		}

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				heading, rd.description);
		et.setColumnPadding(1);
		et.setDisplayTitle(true);
		et.addRows(rows);
		setTable(et);
	}

	// protected String[] getHeading(int versions)
	// {
	// // Have to construct heading dynamically
	// ArrayList<String> heading = new ArrayList<String>();
	// for (int i = 1; i <= versions; i++)
	// heading.add("RSN " + i);
	// return StringUtils.asStrings(heading);
	// }

	private String[] getCycloComplexity(VersionMetricData vmd)
	{
		// int[]
		int[] branchRange = vmd.getMetricRange(ClassMetric.BRANCH_COUNT);
		int branchAvg = (int) vmd.getComplexMetric(
				Version.ISUM,
				ClassMetric.BRANCH_COUNT)
				/ vmd.metricData.size();
		System.out.println("Average cyclomatic complexity: " + branchAvg);

		int branch = 0;
		for (ClassMetricData cmd : vmd.metricData.values())
		{
			MethodMetricMap mmm = cmd.methods;
			Set<String> methodNames = mmm.methods().keySet();
			// int classBranchCount = cmd.g;

			// for (String methodName : methodNames)
			// {
			// int branchCount = mmm.getSimpleMetric(methodName,
			// MethodMetric.BRANCH_COUNT);
			// if (branchCount == 0)
			// branchCount = 1;
			// classBranchCount += branchCount;
			// }
			// if (classBranchCount > 0 && methodNames.size() > 0)
			// classBranchCount = classBranchCount/methodNames.size();

			// System.out.println("avg class branch: " + classBranchCount);
			// branch += classBranchCount;
		}
		// branch = (branch/vmd.metricData.values().size());
		System.out.println("manual cc: " + branch);
		return new String[] { vmd.get(Version.NAME), vmd.get(Version.RSN),
				vmd.get(Version.ID), String.valueOf(branch) };
	}
}
