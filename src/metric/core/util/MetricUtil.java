package metric.core.util;

import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.MetricType;

/**
 * Basic utility methods comparing differences between metric arrays.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class MetricUtil
{
	/**
     * Compares the entire contents of the two metric arrays for equality.
     * 
     * @param metrics The metrics to compare against metrics2
     * @param metrics2 The metrics to be compared against.
     * @return Whether or not the two metric arrays are equal.
     */
	public static boolean equal(int[] metrics, int[] metrics2)
	{
		for (int i = 0; i < metrics.length; i++)
		{
			if (metrics[i] != metrics2[i])
				return false;
		}
		return true;
	}

	/**
     * Compares only the metrics that satisfy the type specified for equality.
     * 
     * @param metrics The metrics to compare against metrics2
     * @param metrics2 The metrics to be compared against.
     * @param type The type of metrics to be included in the comparison.
     * @return Whether or not the two metric arrays are equal.
     */
	public static boolean equal(int[] metrics, int[] metrics2, MetricType type)
	{
		for (int i = 0; i < metrics.length; i++)
		{
			ClassMetric enumMetric = ClassMetric.values()[i];

			// Skip non comparable metrics.
			if (enumMetric.type() == MetricType.NOT_COMPARABLE)
				continue;
			else if (enumMetric.type() == type || enumMetric.type() == MetricType.ANY)
				if (metrics[i] != metrics2[i])
					return false;
		}
		return true;
	}

	/**
     * Computes the distance between two metric arrays.
     * 
     * @param metrics The metrics to compare against metrics2
     * @param metrics2 The metrics to be compared against.
     * @return The distance between the two metric arrays.
     */
	public static double distanceFrom(int[] metrics, int[] metrics2)
	{
		double d = 0.0;
		for (int i = 0; i < metrics.length; i++)
		{
			try
			{
				d += StatUtils.sqr(metrics[i] - metrics2[i]);
			} catch (Exception e)
			{
				e.printStackTrace();
			} // lets hope it does not get to this
		}
		return Math.sqrt(d);
	}

	/**
     * Computes the distance between two metric arrays, using only metrics that
     * satisfy the specified MetricType.
     * 
     * @param metrics The metrics to compare against metrics2
     * @param metrics2 The metrics to be compared against.
     * @param type The type of metrics to be included in the comparison.
     * @return The distance between the two metric arrays.
     */
	public static double distanceFrom(int[] metrics, int[] metrics2, MetricType type)
	{
		double d = 0.0;
		for (int i = 0; i < metrics.length; i++)
		{
			ClassMetric enumMetric = ClassMetric.values()[i];

			// Skip non comparable metrics.
			if (enumMetric.type() == MetricType.NOT_COMPARABLE)
				continue;
			else if (enumMetric.type() == type || enumMetric.type() == MetricType.ANY)
			{
				try
				{
					d += StatUtils.sqr(metrics[i] - metrics2[i]);
				} catch (Exception e)
				{
					e.printStackTrace();
				} // lets hope it does not get to this
			}
		}
		return Math.sqrt(d);
	}

	/**
     * Computes the number of modified metrics between two metric arrays.
     * 
     * @param metrics The metrics to compare against metrics2
     * @param metrics2 The metrics to be compared against.
     * @return The number of modified metrics.
     */
	public static int modifiedMetrics(int[] metrics, int[] metrics2)
	{
		int modified = 0;
		for (int i = 0; i < metrics.length; i++)
		{
			if (metrics[i] != metrics2[i])
				modified++;
		}
		return modified;
	}

	/**
     * Computes the number of modified metrics between two metric arrays, using
     * only metrics that satisfy the specified MetricType.
     * 
     * @param metrics The metrics to compare against metrics2
     * @param metrics2 The metrics to be compared against.
     * @param type The type of metrics to be included in the comparison.
     * @return The distance between the two metric arrays.
     */
	public static int modifiedMetrics(int[] metrics, int[] metrics2, MetricType type)
	{
		int modified = 0;
		for (int i = 0; i < metrics.length; i++)
		{
			ClassMetric enumMetric = ClassMetric.values()[i];

			// Skip non comparable metrics.
			if (enumMetric.type() == MetricType.NOT_COMPARABLE)
				continue;
			else if (enumMetric.type() == type || enumMetric.type() == MetricType.ANY)
				if (metrics[i] != metrics2[i])
					modified++;
		}
		return modified;
	}
}
