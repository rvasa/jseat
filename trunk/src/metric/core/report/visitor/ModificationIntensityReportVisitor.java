package metric.core.report.visitor;

import java.util.ArrayList;
import java.util.HashMap;

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
import metric.core.vocabulary.Evolution;

public class ModificationIntensityReportVisitor extends Report
{

	public ModificationIntensityReportVisitor(ReportDefinition rd)
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
		VersionMetricData prev = hmd.versions.get(1);
//		int total = hmd.versions.size();
		ArrayList<String[]> rows = new ArrayList<String[]>(versions);

		for (int i = 1; i <= versions; i++)
		{
			double[] tmp = changeFactor(prev, hmd.versions.get(i), versions);
			String[] newTmp = new String[tmp.length];
			for (int j = 0; j < tmp.length; j++)
				newTmp[j] = String.valueOf(tmp[j]);

			rows.add(newTmp);
			prev = hmd.versions.get(i);
		}
		
//		Iterator<VersionMetricData> it = hmd.versions.values().iterator();
//		prev = it.next(); // set first version.
//		it = hmd.versions.values().iterator(); // reset iterator.
//		
//		while (it.hasNext())
//		{
//			VersionMetricData nextVersion = it.next();
//			double[] tmp = changeFactor(prev, nextVersion, versions);
//			String[] newTmp = new String[tmp.length];
//			for (int j = 0; j < tmp.length; j++)
//				newTmp[j] = String.valueOf(tmp[j]);
//			
//			rows.add(newTmp);
//			prev = nextVersion;
//		}
		
		
		
		
		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(
				getHeading(versions), rd.description);
		et.setColumnPadding(1);
		et.setDisplayTitle(true);
		et.addRows(rows);
		setTable(et);
	}

	protected String[] getHeading(int versions)
	{
		// Have to construct heading dynamically
		ArrayList<String> heading = new ArrayList<String>();
		for (int i = 1; i <= versions; i++)
			heading.add("RSN " + i);
		return StringUtils.asStrings(heading);
	}

	private double[] changeFactor(VersionMetricData v1, VersionMetricData v2,
			int totalVersions)
	{
		int numClasses = v1.metricData.size();
		double[] intensityRange = new double[totalVersions];

		for (ClassMetricData cm : v1.metricData.values())
		{
			if (cm.getSimpleMetric(ClassMetric.NEXT_VERSION_STATUS) == Evolution.MODIFIED
					.getValue())
			{
				int changeNum = 0;
				if (cm.getSimpleMetric(ClassMetric.FAN_IN_COUNT) != v2.metricData
						.get(cm.get(ClassMetric.NAME)).getSimpleMetric(
								ClassMetric.FAN_IN_COUNT))
					changeNum++;
				if (cm.getSimpleMetric(ClassMetric.FAN_OUT_COUNT) != v2.metricData
						.get(cm.get(ClassMetric.NAME)).getSimpleMetric(
								ClassMetric.FAN_OUT_COUNT))
					changeNum++;
				if (cm.getSimpleMetric(ClassMetric.BRANCH_COUNT) != v2.metricData
						.get(cm.get(ClassMetric.NAME)).getSimpleMetric(
								ClassMetric.BRANCH_COUNT))
					changeNum++;
				// Update intensity.
				if (intensityRange[cm.getSimpleMetric(ClassMetric.BORN_RSN)] == -1.0d)
					intensityRange[cm.getSimpleMetric(ClassMetric.BORN_RSN)] = 0.0;
				intensityRange[cm.getSimpleMetric(ClassMetric.BORN_RSN)] += computeIntensity(
						changeNum,
						numClasses);

			} else
			{
				// Zero modification.
				intensityRange[cm.getSimpleMetric(ClassMetric.BORN_RSN)] = -1.0d;
			}
		}

		StringUtils.toCSVString(intensityRange);
		return intensityRange;
	}

	private double computeIntensity(int changeNumber, int numClasses)
	{
		double tmp = 0;
		switch (changeNumber)
		{
			case 1:
			{
				tmp = StatUtils.toFixedDecPlaces(
						((double) (1d / numClasses) * 0.33),
						3);
				break;
			}
			case 2:
			{
				tmp = StatUtils.toFixedDecPlaces(
						((double) (1d / numClasses) * 0.33),
						3);
				break;
			}
			case 3:
			{
				tmp = StatUtils.toFixedDecPlaces(
						((double) (1d / numClasses) * 0.33),
						3);
				break;
			}
			default:
			{
			} // do nothing.
		}
		return tmp;
	}
}
