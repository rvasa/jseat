package metric.core.model;

import java.lang.reflect.Method;
import java.util.HashMap;

import metric.core.exception.ReportException;
import metric.core.report.visitor.ReportVisitor;
import metric.core.util.StatUtils;
import metric.core.util.StringUtils;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Version;

/**
 * Represents a version of data for a particular product Version.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007, rvasa
 */
public class VersionMetricData extends MetricData<Version> implements
		Comparable<VersionMetricData>
{
	private VersionStatsUtil vsu;

	public static boolean showProcessing = true;
	public HashMap<String, ClassMetricData> metricData;

	public VersionMetricData()
	{
		metricData = new HashMap<String, ClassMetricData>();
		vsu = new VersionStatsUtil();
		
		// Initialise complex metric mappings.
		try
		{
			initComplexMetricMappings();
		} catch (SecurityException e)
		{
			e.printStackTrace();
		} catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
	}

	public VersionMetricData(int rsn, String versionID)
	{
		this();
		setSimpleMetric(Version.RSN, rsn);
		properties.put(Version.ID, versionID.trim());
	}

	public VersionMetricData(int rsn, String versionID, String shortName)
	{
		this(rsn, versionID);
		properties.put(Version.NAME, shortName);
		this.shortName = shortName;
	}

	/**
     * Maps complex metrics defined in the helper class
     * <code>VersionStatsUtil</code> to the get() and getComplexMetric()
     * interface of this VersionMetricData. This allows any type of complex
     * metric to be invoked through the same get methods.
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
     */
	public void initComplexMetricMappings() throws SecurityException, NoSuchMethodException
	{
		Class[] cmArg = { ClassMetric.class };
		complexMetrics.put(Version.ALPHA, VersionStatsUtil.class.getMethod(
				"getAlpha",
				cmArg));
		complexMetrics.put(Version.BETA, VersionStatsUtil.class.getMethod(
				"getBeta",
				cmArg));

		complexMetrics.put(Version.ISUM, VersionStatsUtil.class.getMethod(
				"getISum",
				cmArg));

		Class[] relArg = { VersionMetricData.class };
		complexMetrics.put(Version.RELATIVE_SIZE_CHANGE, VersionStatsUtil.class
				.getMethod("getRelativeSizeChange", relArg));

		Class[] predArg = { ClassMetric.class, VersionMetricData.class };
		complexMetrics.put(Version.PRED, VersionStatsUtil.class.getMethod(
				"getPred",
				predArg));

		complexMetrics.put(Version.PRED_ERROR, VersionStatsUtil.class
				.getMethod("getPredErr", predArg));

	}

	public void addClass(ClassMetricData cmd)
	{
		metricData.put(cmd.get(ClassMetric.NAME), cmd);
	}

	public double getComplexMetric(Version prop, Object cm)
	{
		Method m = complexMetrics.get(prop);
		double ret = -1;
		try
		{
			ret = (Double) m.invoke(vsu, cm);
		} catch (Exception e)
		{
			e.printStackTrace(); // Something went wrong.
		} // Format to 3 decimal places
		return StatUtils.toFixedDecPlaces(ret, 3);
	}

	public String get(Version prop, Object cm)
	{
		return String.valueOf(getComplexMetric(prop, cm));
	}

	public double getComplexMetric(Version prop, Object cm1, Object cm2)
	{
		Method m = complexMetrics.get(prop);
		double ret = -1;
		try
		{
			ret = (Double) m.invoke(vsu, cm1, cm2);
		} catch (Exception e)
		{
			e.printStackTrace(); // Something went wrong.
		} // Format to 3 decimal places
		// return null;
		return StatUtils.toFixedDecPlaces(ret, 3);
	}

	/**
     * @return the range of values for the specified metric as a string.
     */
	public String get(Version prop, Object cm1, Object cm2)
	{
		return String.valueOf(getComplexMetric(prop, cm1, cm2));
	}

	/**
     * @return the range of values for the specified metric
     */
	public int[] getMetricRange(ClassMetric prop)
	{
		int[] range = new int[metricData.values().size()];
		int index = 0;
		for (ClassMetricData cmd : metricData.values())
		{
			range[index++] = cmd.getSimpleMetric(prop);
		}
		return range;
	}

	/**
     * @return the range of values for the specified metric formatted as a csv
     *         string.
     */
	public String getRange(ClassMetric prop)
	{
		return StringUtils.toCSVString(getMetricRange(prop), true);
	}

	public int compareTo(VersionMetricData o)
	{
		return o.getSimpleMetric(Version.RSN) - getSimpleMetric(Version.RSN);
	}

	/** Two versions are the same if they have the same RSN */
	@Override
	public boolean equals(Object o)
	{
		return ((VersionMetricData) o).getSimpleMetric(Version.RSN) == getSimpleMetric(Version.RSN);
	}

	/** Construct with a JAR file or a directory of JAR files */
	// public VersionMetricData(int rsn, String versionID, String shortName,
	// String fileName) throws IOException
	// {
	// this(rsn, versionID);
	// addInput(fileName.trim());
	// this.shortName = shortName;
	// }
	// public void addInputDir(String dirName) throws IOException
	// {
	// input.addInputDir(dirName, false); // no recursive support yet
	// }
	//    
	// public void addJARFile(String fileName) throws IOException
	// {
	// input.addInputFile(fileName);
	// }
	// public int getClassCount() {return metricData.size();}
	// public double getGUIClassCount()
	// {
	// double sum = 0.0;
	// for (ClassMetricData c : metricData.values())
	// { sum += c.getMetric(ClassMetric.GUI_DISTANCE);}
	// return sum;
	// }
	@Override
	public String toString()
	{
		return get(Version.NAME) + "-" + get(Version.ID);
	}
	
	public void accept(ReportVisitor visitor) throws ReportException
	{
		visitor.visit(this);
	}

	/**
     * A collection of complex metric types. This class serves as a helper to
     * <code>VersionMetricData</code>. Any complex metrics defined here
     * should be mapped to a Version enum in the VersionMetricData constructor.
     * 
     * All complex metrics here can be invoked through the parent classes
     * 'getComplexMetric(...)' methods (overloaded depending on the number of
     * arguments provided.
     * 
     * e.g. vmd.getComplexMetric(Version.ALPHA, ClassMetric.FIELD_COUNT);
     * 
     * Can also be returned as a string like simple metrics through the
     * overloaded get methods.
     * 
     * e.g. vmd.get(Version.ALPHA, ClassMetric.FIELD_COUNT);
     * 
     * @author Joshua Hayes,Swinburne University (ICT),2007
     * 
     */
	class VersionStatsUtil
	{
		/**
         * Alpha value of the specified ClassMetric.
         * 
         * @param metric The metric from which to obtain alpha.
         * @return The alpha value.
         */
		public double getAlpha(ClassMetric metric)
		{
			return (double) getISum(metric)
					/ getSimpleMetric(Version.CLASS_COUNT);
		}

		/**
         * Beta value of the specified ClassMetric.
         * 
         * @param metric The metric from which to obtain beta.
         * @return The beta value.
         */
		public double getBeta(ClassMetric metric)
		{
			return Math.log(getISum(metric))
					/ Math.log(getSimpleMetric(Version.CLASS_COUNT));
		}

		/**
         * Relative size change between the classes in this version and the
         * previous version specified.
         * 
         * @param prev The previous VersionMetricData.
         * @return The relative size difference.
         */
		public double getRelativeSizeChange(VersionMetricData prev)
		{
			return StatUtils.getRelativeChange(
					prev.getSimpleMetric(Version.CLASS_COUNT),
					getSimpleMetric(Version.CLASS_COUNT));
		}

		/**
         * The sum of values over all classes in this VersionMetricData for the
         * specified ClassMetric.
         * 
         * @param metric The ClassMetric
         * @return ISum of the specified metric for this VersionMetricData.
         */
		public double getISum(ClassMetric metric)
		{
			int sum = 0;
			try
			{
				for (ClassMetricData c : metricData.values())
					sum += c.getSimpleMetric(metric);
			} catch (Exception e)
			{
				e.printStackTrace();
			} // ignore exception
			return sum;
		}

		/**
         * Prediction for the specified metric relative to the previous version
         * specified.
         * 
         * @param metric The ClassMetric.
         * @param prev The previous VersionMetricData.
         * @return The prediction.
         */
		public double getPred(ClassMetric metric, VersionMetricData prev)
		{
			return Math.pow(getSimpleMetric(Version.CLASS_COUNT), prev
					.getComplexMetric(Version.BETA, metric));
		}

		/**
         * Prediction error for the specified metric relative to the previous
         * version specified.
         * 
         * @param metric The ClassMetric.
         * @param prev The previous VersionMetricData.
         * @return The prediction error.
         */
		public double getPredErr(ClassMetric metric, VersionMetricData prev)
		{
			return StatUtils.getRelativeChange(
					(int) vsu.getISum(metric),
					(int) getPred(metric, prev));
		}
	}
}
