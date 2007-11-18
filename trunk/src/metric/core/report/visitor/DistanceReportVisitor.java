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
public class DistanceReportVisitor extends Report
{
	private String type;
	private int max;

	private String[] distanceHeading = { "name", Version.RSN.toString(),
			Version.ID.toString(), Version.RSN.toString(),
			Version.ID.toString(), "raw_distance", "beta_delta", "delta_size",
			"histogram_diff", "histogram_int" };

	public DistanceReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

	@Override
	protected void processArgs() throws ReportException
	{
		try
		{
			type = (String) rd.args[0];
			max = (Integer) rd.args[1];
		} catch (ArrayIndexOutOfBoundsException e)
		{
			throw new ReportException();
		}
	}

	@Override
	public HashMap<String, Object> getArguments()
	{
		HashMap<String, Object> h = new HashMap<String, Object>();
		h.put("Metric", StringUtils.sort(ClassMetric.toStrings(), type));
		h.put("MaxValue", max);
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
				type = ((ArrayList<String>) e.getValue()).get(0);
			else if (e.getKey().equals("MaxValue"))
				max = (Integer) e.getValue();
		}
	}

	@SuppressWarnings("unchecked")
	public void visit(HistoryMetricData hmd) throws ReportException
	{
		ArrayList<String[]> rows = printDistances(hmd, type, max);
		MetricTable et = new MetricTable<String, String>(distanceHeading,
				rd.description);
		et.addRows(rows);
		et.setDisplayTitle(true);
		setTable(et);
	}

	private ArrayList<String[]> printDistances(HistoryMetricData hmd,
			String field, int max) throws ReportException
	{
		ArrayList<String[]> rows = new ArrayList<String[]>();

		if (hmd.versions.size() < 2)
			throw new ReportException(
					"Insufficient versions to compute distances");

		for (int i = 2; i <= hmd.versions.size(); i++)
		{
			VersionMetricData v1 = hmd.getVersion(i-1);
			updateProgress(i-1, hmd.getVersions().size(), v1);
			VersionMetricData v2 = hmd.getVersion(i);
			updateProgress(i, hmd.getVersions().size(), v2);
			// int deltaSize = v2.getClassCount() - v1.getClassCount();
			double relDelta = ((double) v2.getSimpleMetric(Version.CLASS_COUNT) - v1
					.getSimpleMetric(Version.CLASS_COUNT))
					/ v1.getSimpleMetric(Version.CLASS_COUNT);
			double rawDistance = getRawCountDistanceFrom(v1, v2);
			double deltaBeta = v2.getComplexMetric(Version.BETA, ClassMetric
					.parse(field))
					- v1.getComplexMetric(Version.BETA, ClassMetric
							.parse(field));
			double histDiff = histogramDiffDist(v1, v2, field, max);
			double histInter = histogramIntersectionDist(v1, v2, field, max);

			String[] row = { v1.get(Version.NAME), v1.get(Version.RSN),
					v1.get(Version.ID), v2.get(Version.RSN),
					v2.get(Version.ID),
					String.valueOf(StatUtils.toFixedDecPlaces(rawDistance, 4)),
					String.valueOf(StatUtils.toFixedDecPlaces(deltaBeta, 4)),
					String.valueOf(StatUtils.toFixedDecPlaces(relDelta, 4)),
					String.valueOf(StatUtils.toFixedDecPlaces(histDiff, 4)),
					String.valueOf(StatUtils.toFixedDecPlaces(histInter, 4)) };
			rows.add(row);
		}
		return rows;
	}

	protected double getRawCountDistanceFrom(VersionMetricData v1,
			VersionMetricData v2)
	{
		double d = 0.0;
		d += StatUtils.sqr(v1.getComplexMetric(
				Version.ISUM,
				ClassMetric.METHOD_COUNT)
				- v2.getComplexMetric(Version.ISUM, ClassMetric.METHOD_COUNT));
		d += StatUtils.sqr(v1.getComplexMetric(
				Version.ISUM,
				ClassMetric.FAN_OUT_COUNT)
				- v2.getComplexMetric(Version.ISUM, ClassMetric.FAN_OUT_COUNT));
		d += StatUtils.sqr(v1.getComplexMetric(
				Version.ISUM,
				ClassMetric.METHOD_CALL_COUNT)
				- v2.getComplexMetric(
						Version.ISUM,
						ClassMetric.METHOD_CALL_COUNT));
		d += StatUtils.sqr(v1.getComplexMetric(
				Version.ISUM,
				ClassMetric.LOAD_COUNT)
				- v2.getComplexMetric(Version.ISUM, ClassMetric.LOAD_COUNT));
		d += StatUtils.sqr(v1.getComplexMetric(
				Version.ISUM,
				ClassMetric.STORE_COUNT)
				- v2.getComplexMetric(Version.ISUM, ClassMetric.STORE_COUNT));
		d += StatUtils.sqr(v1.getComplexMetric(
				Version.ISUM,
				ClassMetric.BRANCH_COUNT)
				- v2.getComplexMetric(Version.ISUM, ClassMetric.BRANCH_COUNT));
		d = Math.sqrt(d); // v.getMetric(Version.CLASS_COUNT);
		return d;
	}

	/**
     * Computes intersection using absolute histogram distribution. Will build
     * the histogram for bin range 0 - 100. Field has to store an int value in
     * ClassMetric
     */
	protected double histogramIntersectionDist(VersionMetricData v1,
			VersionMetricData v2, String field, int maxValue)
	{
		int[] v1HistRange = v1.getMetricRange(ClassMetric.parse(field));
		int[] v2HistRange = v1.getMetricRange(ClassMetric.parse(field));

		int[] v1Hist = StatUtils.createFreqTable(v1HistRange, maxValue);
		int[] v2Hist = StatUtils.createFreqTable(v2HistRange, maxValue);
		// assert v1Hist.length and v2Hist.length are the same
		double sum = 0.0;
		for (int i = 0; i < v1Hist.length; i++)
		{
			if (v1Hist[i] == v2Hist[i])
				continue; // ignore similarities
			double min = Math.min(v1Hist[i], v2Hist[i]);
			double max = Math.max(v1Hist[i], v2Hist[i]);
			if (max != 0.0)
				sum += (1 - min / max); // avoid div-by-zero
		}
		return sum;
	}

	/**
     * Computes difference using the relative histogram distribution. Will build
     * the histogram for bin range 0 - 100. Field has to store an int value in
     * ClassMetric
     */
	protected double histogramDiffDist(VersionMetricData v1,
			VersionMetricData v2, String field, int maxValue)
	{
		int[] range1 = v1.getMetricRange(ClassMetric.parse(field));
		int[] range2 = v2.getMetricRange(ClassMetric.parse(field));

		double[] v1Hist = StatUtils.createRelFreqTable(range1, maxValue);
		double[] v2Hist = StatUtils.createRelFreqTable(range2, maxValue);

		// assert v1RelHist.length and v2RelHist.length are the same
		double sum = 0.0;
		for (int i = 0; i < v1Hist.length; i++)
		{
			// bhattacharya distance measure B-distance
			sum += Math.sqrt(v1Hist[i]) * Math.sqrt(v2Hist[i]);

			// bhattacharya cosine angle measure
			// sum += Math.sqrt(v1Hist[i]*v2Hist[i]);

			// matusita measure
			// (Math.sqrt(v2Hist[i]) - Math.sqrt(v1Hist[i]))^2
		}
		return sum;// /(2*v1Hist.length);
		// return (-1.0*Math.log(sum));
	}

}
