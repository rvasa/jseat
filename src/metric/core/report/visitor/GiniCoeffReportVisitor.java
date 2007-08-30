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
import cern.jet.stat.Descriptive;

/**
 * Acts as a visitor to HistoryMetricData. When it visits HistoryMetricData it
 * will perform one of its methods as part of its visit. The method invoked, is
 * defined by attributes stored in <code>MetricDefinition</code>.
 * 
 * @author Joshua Hayes, rvasa
 */
public class GiniCoeffReportVisitor extends Report
{
	private String[] fields;

	public GiniCoeffReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

	@Override
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

	@Override
	public void visit(HistoryMetricData hmd) throws ReportException
	{
		ArrayList<String[]> rows = new ArrayList<String[]>(hmd.versions.size());

		int total = hmd.versions.size();
		for (int i = 1; i <= total; i++)
		{
			VersionMetricData vmd = hmd.getVersion(i);
			updateProgress(i, total, vmd);
			rows.add(getGiniCoeffRow(vmd, fields));
		}

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(getHeading(fields), rd.description);
		et.addRows(rows);
		et.setColumnPadding(2);
		et.setDisplayTitle(true);
		setTable(et);
	}

	@Override
	public void visit(VersionMetricData vmd) throws ReportException
	{
		String[] row = getGiniCoeffRow(vmd, fields);

		// Create and set table.
		MetricTable<String, String> et = new MetricTable<String, String>(getHeading(fields), rd.description);
		et.addRow(row);
		et.setColumnPadding(2);
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
			heading.add(ClassMetric.parse(str).toString());

		// Setup final heading array to add to table.
		// Object[] headings = new Object[heading.size()];
		// heading.toArray(
		// headings);
		return StringUtils.asStrings(heading);
	}

	public String[] getGiniCoeffRow(VersionMetricData vmd, String[] fields)
	{
		ArrayList<String> tmpRow = new ArrayList<String>();
		tmpRow.add(vmd.get(Version.NAME));
		tmpRow.add(vmd.get(Version.RSN));
		tmpRow.add(vmd.get(Version.ID));

		for (String metric : fields)
			tmpRow.add(StatUtils.toFixedDecPlaces(getGiniCoeff(vmd, metric), 4) + "");
		String[] row = new String[tmpRow.size()];
		tmpRow.toArray(row);
		return row;
	}

	private double getGiniCoeff(VersionMetricData vmd, String field)
	{
		DoubleArrayList d = new DoubleArrayList();
		for (ClassMetricData cm : vmd.metricData.values())
		{
			if ((field.equals(ClassMetric.LOAD_COUNT.toString()))
					&& (cm.getSimpleMetric(ClassMetric.IS_INTERFACE) == 1))
				continue;
			if ((field.equals(ClassMetric.STORE_COUNT.toString()))
					&& (cm.getSimpleMetric(ClassMetric.IS_INTERFACE) == 1))
				continue;
			if ((field.equals(ClassMetric.BRANCH_COUNT.toString()))
					&& (cm.getSimpleMetric(ClassMetric.IS_INTERFACE) == 1))
				continue;
			if ((field.equals(ClassMetric.FIELD_COUNT.toString()))
					&& (cm.getSimpleMetric(ClassMetric.IS_INTERFACE) == 1))
				continue;
			if ((field.equals(ClassMetric.METHOD_CALL_COUNT.toString()))
					&& (cm.getSimpleMetric(ClassMetric.IS_INTERFACE) == 1))
				continue;
			if ((field.equals(ClassMetric.TYPE_CONSTRUCTION_COUNT.toString()))
					&& (cm.getSimpleMetric(ClassMetric.IS_INTERFACE) == 1))
				continue;
			d.add(cm.getSimpleMetric(ClassMetric.parse(field)));
		}

		double relVars = 0;
		double descMean = Descriptive.mean(d);

		for (int i = 0; i < d.size(); i++)
		{
			for (int j = 0; j < d.size(); j++)
			{
				if (i == j)
					continue;
				relVars += (Math.abs(d.get(i) - d.get(j))); // / descMean;
			}
		}
		relVars = relVars / (2 * d.size() * d.size());
		double gini = relVars / descMean;

		return gini;

	}

	// public void printGiniCoeff(String[] fields) throws Exception
	// {
	// if (fields.length == 0)
	// return;
	// for (int i = 1; i <= versions.size(); i++)
	// {
	// Version v = versions.get(i);
	// double[] giniValues = v.getGiniValues(fields);
	// double distance = 0.0;
	// if (i > 1)
	// distance = Version.calcDistance(giniValues, versions.get(i -
    // 1).getGiniValues(fields));
	// System.out.printf("%10s\t%2d\t%10s\t%5d\t", shortName, v.RSN, v.id,
    // v.getClassCount());
	// for (int j = 0; j < giniValues.length; j++)
	// {
	// System.out.printf("%5.4f\t", giniValues[j]);
	// }
	// System.out.printf("%5.4f\n", distance);
	// }
	// }
	//
	// public double[] getGiniValues(String[] fields) throws Exception
	// {
	// double[] giniValues = new double[fields.length];
	// for (int j = 0; j < fields.length; j++)
	// giniValues[j] = calcGiniCoefficient(fields[j]);
	// return giniValues;
	// }
	//
	// public double calcGiniCoefficient(String field) throws Exception
	// {
	// DoubleArrayList d = new DoubleArrayList();
	// for (ClassMetric cm : metricData.values())
	// {
	// if ((field.equals("loadCount")) && (cm.isInterface == 1))
	// continue;
	// if ((field.equals("storeCount")) && (cm.isInterface == 1))
	// continue;
	// if ((field.equals("branchCount")) && (cm.isInterface == 1))
	// continue;
	// if ((field.equals("fieldCount")) && (cm.isInterface == 1))
	// continue;
	// if ((field.equals("methodCallCount")) && (cm.isInterface == 1))
	// continue;
	// if ((field.equals("typeConstructionCount")) && (cm.isInterface == 1))
	// continue;
	// d.add(cm.getMetricValue(field));
	// }
	//
	// double relVars = 0;
	// double descMean = Descriptive.mean(d);
	//
	// for (int i = 0; i < d.size(); i++)
	// {
	// for (int j = 0; j < d.size(); j++)
	// {
	// if (i == j)
	// continue;
	// relVars += (Math.abs(d.get(i) - d.get(j))); // / descMean;
	// }
	// }
	// relVars = relVars / (2 * d.size() * d.size());
	// double gini = relVars / descMean;
	//
	// // System.out.println("Gini: " + gini);
	// return gini;
	// }

}
