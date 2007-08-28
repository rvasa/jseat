package metric.core.util;

import java.text.DecimalFormat;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * A collection of number and statistical utilties. As this is a utility class,
 * all methods are intended to be invoked statically.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007, rvasa
 * 
 */
public class StatUtils
{
	/** Sum the given integer field in the ClassMetric */
	public static int getISum(int[] values)
	{
		int sum = 0;
		try
		{
			for (int i = 0; i < values.length; i++)
				sum += values[i];
		} catch (Exception e)
		{
			e.printStackTrace();
		} // ignore exception
		return sum;
	}

	/**
     * Creates a frequency table over the range of values specified and max
     * upper limit.
     * 
     * @param range The range of values.
     * @param maxValue The maxium value.
     * @return frequency table.
     */
	public static int[] createFreqTable(int range[], int maxValue)
	{
		int[] freq = new int[maxValue];

		for (int i : range)
		{
			if (i >= freq.length)
				freq[freq.length - 1]++;
			else
				freq[i]++;
		}
		return freq;
	}

	/**
     * Creates a relative frequency table over the range of values specified and
     * max upper limit.
     * 
     * @param values a range of values
     * @param maxValue the maximum value.
     * @return relative frequency table.
     */
	public static double[] createRelFreqTable(int range[], int maxValue)
	{
		return StatUtils.toRelativeFreqTable(StatUtils.createFreqTable(range,
				maxValue));
	}

	/**
     * Computes the relative change between numbers m1 and m2
     */
	public static double getRelativeChange(int m1, int m2)
	{
		return ((double) (m2 - m1)) / m1;
	}

	/** Square of i */
	public static double sqr(double i)
	{
		return i * i;
	}

	/**
     * Formats the double specified to a fixed set of decimal places.
     * 
     * @param value the value to format.
     * @param places the number of places after the decimal point.
     * @return the fromatted double.
     */
	public static double toFixedDecPlaces(double value, int places)
	{
		try
		{
			StringBuffer format = new StringBuffer("0.0");
			if (places > 1)
			{
				for (int i = 0; i < places - 1; i++)
					format.append("0");
			}
			DecimalFormat threeDec = new DecimalFormat(format.toString());
			// threeDec.setGroupingUsed(false);
			return Double.parseDouble(threeDec.format(value));
		} catch (NumberFormatException e)
		{
			return 0.0d;
		}
	}

	/** Converts a given frequency table into a relative freq table */
	public static double[] toRelativeFreqTable(int[] freq)
	{
		double total = 0.0;
		for (int i : freq)
			total += i;
		double[] relFreq = new double[freq.length];
		for (int j = 0; j < freq.length; j++)
			relFreq[j] = freq[j] / total;
		return relFreq;
	}

	/** Converts a given freq. table into a cummulative table */
	public static double[] toCummulFreqTable(int[] freq)
	{
		double[] cf = StatUtils.toRelativeFreqTable(freq);
		for (int i = 0; i < cf.length - 1; i++)
		{
			cf[i + 1] = cf[i + 1] + cf[i];
		}
		return cf;
	}

	/**
     * Applies the specified set of constraints to a frequency range, returning
     * a constrained frequency table.
     */
	public static int[] toConstrainedFreqTable(int[] fRange, int[] cRange,
			int cMin, int cMax, int max)
	{
		int[] newFreq = new int[max];
		for (int i = 0; i < fRange.length; i++)
		{
			int c = cRange[i];
			if ((c >= cMin) && (c <= cMax))
			{
				int m = fRange[i];
				if (m >= newFreq.length)
					newFreq[newFreq.length - 1]++;
				else
					newFreq[m]++;
			}
		}
		return newFreq;
	}

	/**
     * @return the specified frequency table as a csv string.
     */
	public static String toCSVString(int[] freq)
	{
		int sum = 0;
		for (int f : freq)
		{
			sum += f;
		}
		return sum + ", \t" + CSVUtil.toCSVString(freq, true);
	}

	/**
     * @return the specified frequency table as a csv string.
     */
	public static String toCSVString(double[] freq)
	{
		int sum = 0;
		for (double f : freq)
		{
			sum += f;
		}
		return sum + ", \t" + CSVUtil.toCSVString(freq);
	}

	public static int scaleDoubleMetric(double metricValue, int scaleMax,
			double cutOffMax)
	{
		double mv = metricValue;
		if (metricValue > cutOffMax)
			mv = cutOffMax;
		Double d = new Double((scaleMax * mv) / cutOffMax);
		return d.intValue();
	}

	public static double calcCorrelation(DoubleArrayList m1, DoubleArrayList m2)
	{
		double sdm1 = Descriptive.standardDeviation(Descriptive.variance(m1
				.size(), Descriptive.sum(m1), Descriptive.sumOfSquares(m1)));
		double sdm2 = Descriptive.standardDeviation(Descriptive.variance(m2
				.size(), Descriptive.sum(m2), Descriptive.sumOfSquares(m2)));
		return Descriptive.correlation(m1, sdm1, m2, sdm2);
	}
}
