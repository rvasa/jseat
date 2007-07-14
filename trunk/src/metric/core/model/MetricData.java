package metric.core.model;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import metric.core.report.visitor.VisitableReport;

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
	protected HashMap<T, Integer> metrics;

	// Should be used to store all class related double based metrics
	protected HashMap<T, Method> complexMetrics;

	// Should be used to store all class related dependencies
	protected HashMap<T, List<String>> dependencies;

	public MetricData()
	{
		properties = new HashMap<T, String>();
		metrics = new HashMap<T, Integer>();
		complexMetrics = new HashMap<T, Method>();
		dependencies = new HashMap<T, List<String>>();
	}

	/**
     * Smart get method for retrieving any metric, property or complex metric as
     * a string. If not performing calculations, use this instead of getMetric.
     */
	public String get(T prop)
	{
		// check metrics first
		if (metrics.containsKey(prop))
			return metrics.get(prop).toString();
		// if not found, check complex metrics
//		else if (complexMetrics.containsKey(prop))
//			return complexMetrics.get(prop).toString();
		// if not found, check properties
		else if (properties.containsKey(prop))
			return properties.get(prop);

		// Didn't find it.
		return null;
	}

	/**
     * @return The value of the provided metric.
     */
	public int getSimpleMetric(T prop)
	{
		return metrics.get(prop);
	}
	
	/**
	 * Sets the specified property to the specified value
	 * @param prop The property type to set.
	 * @param property The property value to set it to.
	 */
	public void setProperty(T prop, String property)
	{
		properties.put(prop, property);
	}

	/**
     * Sets the value of the specified metric.
     * @param prop The metric to set
     * @param value The value to set it to.
     */
	public void setSimpleMetric(T prop, int value)
	{
		metrics.put(prop, value);
	}

	/**
     * Increments the specified metric.
     * @param metric The metric to increment.
     */
	public void incrementMetric(T metric)
	{
		int value = metrics.get(metric);
		metrics.put(metric, ++value);
	}

	/**
     * Increments the specified metric by the specified amount.
     * @param metric The metric to increment.
     * @param toAdd The amount to increment the metric by.
     */
	public void incrementMetric(T metric, int toAdd)
	{
		int value = metrics.get(metric);
		metrics.put(metric, value += toAdd);
	}

	/**
     * Decrements the specified metric.
     * @param metric The metric to decrement.
     */
	public void decrementMetric(T metric)
	{
		int value = metrics.get(metric);
		metrics.put(metric, --value);
	}
	
	public Set simpleMetricSet() { return metrics.entrySet(); }
	
	public Set complexMetricSet() { return complexMetrics.entrySet(); }
	
	public Set propertySet() { return properties.entrySet(); }
}
