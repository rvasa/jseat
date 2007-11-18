package metric.core.model;

import java.lang.reflect.Method;
import java.util.HashMap;

import metric.core.report.visitor.VisitableReport;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.History;
import metric.core.vocabulary.Version;

public abstract class MetricData<T extends Enum<T>> implements VisitableReport
{
	// The name of the product or project this metric data belongs to.
	// This is necessary so that when looking at a particular version for
	// instance,
	// you can still tell what product it belongs to.
	public String shortName;

	// Should be used to store all class related String based properties
	protected HashMap<T, String> properties;

	// Should be used to store all class related int based metrics
	protected int[] metrics;

	// Should be used to store all class related double based metrics
	protected HashMap<T, Method> complexMetrics;

	// Should be used to store all class related dependencies
	// protected HashMap<T, List<String>> dependencies;

	public MetricData()
	{
		properties = new HashMap<T, String>();

		complexMetrics = new HashMap<T, Method>();

		if (getClass() == HistoryMetricData.class)
		{
			metrics = new int[History.getNumberOfMetrics()];
		} else if (getClass() == VersionMetricData.class)
		{
			metrics = new int[Version.getNumberOfMetrics()];
		} else if (getClass() == ClassMetricData.class)
		{
			metrics = new int[ClassMetric.getNumberOfMetrics()];
		}
	}

	/**
     * Smart get method for retrieving any metric, property or complex metric as
     * a string. If not performing calculations, use this instead of getMetric.
     */
	public String get(T prop)
	{
		int metricPos = -1;
		if (prop.getDeclaringClass().equals(History.class))
		{
			metricPos = History.getNumberOfMetrics();
		} else if (prop.getDeclaringClass().equals(Version.class))
		{
			metricPos = Version.getNumberOfMetrics();
		} else if (prop.getDeclaringClass().equals(ClassMetric.class))
		{
			metricPos = ClassMetric.getNumberOfMetrics();
		}

		if (prop.ordinal() < metricPos)
		{
			return String.valueOf(metrics[prop.ordinal()]);
		}
		// if (simpleMetrics.containsKey(prop))
		// return String.valueOf(simpleMetrics.get(prop));
		else if (properties.containsKey(prop))
		{
			return properties.get(prop);
		}

		// Didn't find it.
		return null;
	}

	/**
     * @return The value of the provided metric.
     */
	public int getSimpleMetric(T prop)
	{
		return metrics[prop.ordinal()];
	}

	/**
     * Sets the specified property to the specified value
     * 
     * @param prop The property type to set.
     * @param property The property value to set it to.
     */
	public void setProperty(T prop, String property)
	{
		properties.put(prop, property);
	}

	/**
     * Sets the value of the specified metric.
     * 
     * @param prop The metric to set
     * @param value The value to set it to.
     */
	public void setSimpleMetric(T prop, int value)
	{
		metrics[prop.ordinal()] = value;
	}

	/**
     * Increments the specified metric.
     * 
     * @param metric The metric to increment.
     */
	public void incrementMetric(T metric)
	{
		metrics[metric.ordinal()]++;
	}

	/**
     * Increments the specified metric by the specified amount.
     * 
     * @param metric The metric to increment.
     * @param toAdd The amount to increment the metric by.
     */
	public void incrementMetric(T metric, int toAdd)
	{
		metrics[metric.ordinal()] += toAdd;
	}

	/**
     * Decrements the specified metric.
     * 
     * @param metric The metric to decrement.
     */
	public void decrementMetric(T metric)
	{
		metrics[metric.ordinal()]--;
	}

	public final int[] getMetrics()
	{
		return metrics;
	};

	public final HashMap<T, String> getProperties()
	{
		return properties;
	};

}
