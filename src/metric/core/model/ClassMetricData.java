package metric.core.model;

import java.util.HashSet;
import java.util.Set;

import metric.core.exception.ReportException;
import metric.core.report.visitor.ReportVisitor;
import metric.core.util.StatUtils;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Evolution;

/**
 * 
 * The ClassMetricData represents a Java class, storing its metrics,
 * dependendices, methods etc.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007/n/t rvasa
 * 
 */
public class ClassMetricData extends MetricData<ClassMetric> implements Comparable<ClassMetricData>
{
	public Set<String> dependencies = new HashSet<String>();
	public Set<String> users = new HashSet<String>();
	public Set<String> internalDeps = new HashSet<String>();
	public MethodMetricMap methods;

	public long lastModified;

	/**
     * Initialise metrics, properties etc. This is private because it is only
     * intended to be called internally
     */
	public ClassMetricData()
	{
		// Dependency related
		properties.put(ClassMetric.SUPER_CLASS_NAME, "java/lang/Object");
		setSimpleMetric(ClassMetric.SUPER_CLASS_COUNT, 0);
		setSimpleMetric(ClassMetric.INTERFACE_COUNT, 0);
		setSimpleMetric(ClassMetric.FAN_OUT_COUNT, 0);
		setSimpleMetric(ClassMetric.FAN_IN_COUNT, 0);
		setSimpleMetric(ClassMetric.INTERNAL_FAN_OUT_COUNT, 0);
		setSimpleMetric(ClassMetric.BRANCH_COUNT, 0);
		setSimpleMetric(ClassMetric.NORMALIZED_BRANCH_COUNT, 0);
		setSimpleMetric(ClassMetric.DATE, 999);

		// Fields
		setSimpleMetric(ClassMetric.FIELD_COUNT, 0);
		setSimpleMetric(ClassMetric.PUBLIC_FIELD_COUNT, 0);
		setSimpleMetric(ClassMetric.PRIVATE_FIELD_COUNT, 0);
		setSimpleMetric(ClassMetric.PROTECTED_FIELD_COUNT, 0);
		setSimpleMetric(ClassMetric.STATIC_FIELD_COUNT, 0);
		setSimpleMetric(ClassMetric.FINAL_FIELD_COUNT, 0);
		setSimpleMetric(ClassMetric.INNER_CLASS_COUNT, 0);

		// Type
		setSimpleMetric(ClassMetric.IS_IO_CLASS, 0);
		setSimpleMetric(ClassMetric.IS_INTERFACE, 0);
		setSimpleMetric(ClassMetric.IS_ABSTRACT, 0);
		// Public by default.
		setSimpleMetric(ClassMetric.IS_PUBLIC, 1);
		setSimpleMetric(ClassMetric.IS_PRIVATE, 1);
		setSimpleMetric(ClassMetric.IS_PROTECTED, 0);
		setSimpleMetric(ClassMetric.IS_EXCEPTION, 0);

		// Methods
		setSimpleMetric(ClassMetric.EX_METHOD_CALL_COUNT, 0);
		setSimpleMetric(ClassMetric.IN_METHOD_CALL_COUNT, 0);
		setSimpleMetric(ClassMetric.METHOD_CALL_COUNT, 0);
		setSimpleMetric(ClassMetric.METHOD_COUNT, 0);
		setSimpleMetric(ClassMetric.PUBLIC_METHOD_COUNT, 0);
		setSimpleMetric(ClassMetric.PRIVATE_METHOD_COUNT, 0);
		setSimpleMetric(ClassMetric.PROTECTED_METHOD_COUNT, 0);
		setSimpleMetric(ClassMetric.FINAL_METHOD_COUNT, 0);
		setSimpleMetric(ClassMetric.ABSTRACT_METHOD_COUNT, 0);
		setSimpleMetric(ClassMetric.STATIC_METHOD_COUNT, 0);
		setSimpleMetric(ClassMetric.SYNCHRONIZED_METHOD_COUNT, 0);

		setSimpleMetric(ClassMetric.THROW_COUNT, 0);
		setSimpleMetric(ClassMetric.REF_LOAD_OP_COUNT, 0);
		setSimpleMetric(ClassMetric.REF_STORE_OP_COUNT, 0);
		setSimpleMetric(ClassMetric.LOAD_FIELD_COUNT, 0);
		setSimpleMetric(ClassMetric.STORE_FIELD_COUNT, 0);

		setSimpleMetric(ClassMetric.TRY_CATCH_BLOCK_COUNT, 0);
		setSimpleMetric(ClassMetric.LOCAL_VAR_COUNT, 0);

		// Instruction counts
		setSimpleMetric(ClassMetric.CONSTANT_LOAD_COUNT, 0);
		setSimpleMetric(ClassMetric.INCREMENT_OP_COUNT, 0);
		setSimpleMetric(ClassMetric.LOAD_COUNT, 0);
		setSimpleMetric(ClassMetric.STORE_COUNT, 0);
		setSimpleMetric(ClassMetric.ILOAD_COUNT, 0);
		setSimpleMetric(ClassMetric.ISTORE_COUNT, 0);
		setSimpleMetric(ClassMetric.TYPE_INSN_COUNT, 0);
		setSimpleMetric(ClassMetric.ZERO_OP_INSN_COUNT, 0);

		// Evolution
		setSimpleMetric(ClassMetric.IS_DELETED, 0);
		setSimpleMetric(ClassMetric.IS_MODIFIED, 0);
		setSimpleMetric(ClassMetric.EVOLUTION_STATUS, Evolution.UNCHANGED.getValue());
		setSimpleMetric(ClassMetric.EVOLUTION_DISTANCE, Evolution.UNCHANGED.getValue());
		setSimpleMetric(ClassMetric.NEXT_VERSION_STATUS, Evolution.UNCHANGED.getValue());

		// Distance related.
		setSimpleMetric(ClassMetric.GUI_DISTANCE, 0);
		setSimpleMetric(ClassMetric.COMPUTED_DISTANCE, -1);

		// Start off young
		setSimpleMetric(ClassMetric.AGE, 1);
		// lets assume it was born in first version.
		setSimpleMetric(ClassMetric.BORN_RSN, 1);
	}

	public ClassMetricData(String productName)
	{
		properties.put(ClassMetric.PRODUCT_NAME, productName);
	}

	public ClassMetricData(String productName, int[] metrics)
	{
		properties.put(ClassMetric.PRODUCT_NAME, productName);
		this.metrics = metrics;
	}

	/**
     * Computes the distance of the class from this. This is used to check how
     * far a class has evolved or to check if it is in the same neighbourhood.
     */
	public double distanceFrom(ClassMetricData cm)
	{
		ClassMetric[] metrics = { ClassMetric.BRANCH_COUNT, ClassMetric.SUPER_CLASS_COUNT,
				ClassMetric.CONSTANT_LOAD_COUNT, ClassMetric.ILOAD_COUNT, ClassMetric.ISTORE_COUNT,
				ClassMetric.LOAD_FIELD_COUNT, ClassMetric.STORE_FIELD_COUNT, ClassMetric.REF_LOAD_OP_COUNT,
				ClassMetric.REF_STORE_OP_COUNT, ClassMetric.FAN_OUT_COUNT, ClassMetric.INTERFACE_COUNT,
				ClassMetric.METHOD_COUNT, ClassMetric.TRY_CATCH_BLOCK_COUNT, ClassMetric.TYPE_INSN_COUNT,
				ClassMetric.ZERO_OP_INSN_COUNT, ClassMetric.LOCAL_VAR_COUNT, ClassMetric.IN_METHOD_CALL_COUNT,
				ClassMetric.EX_METHOD_CALL_COUNT, ClassMetric.INTERNAL_FAN_OUT_COUNT, ClassMetric.INCREMENT_OP_COUNT

		};
		double d = 0.0;
		for (ClassMetric cmetric : metrics)
		{
			try
			{
				d += StatUtils.sqr(cm.getSimpleMetric(cmetric) - getSimpleMetric(cmetric));
				// if (change > 0) modifiedVectors++;
				// d += change;
			} catch (Exception e)
			{
				e.printStackTrace();
			} // lets hope it does not get to this
		}
		return Math.sqrt(d);
	}

	/** Checks if it is a clone, i.e. similar to another CM */
	public boolean isExactMatch(ClassMetricData c)
	{
		if (!c.get(ClassMetric.NAME).equals(this.get(ClassMetric.NAME)))
			return false;
		if (!c.get(ClassMetric.SUPER_CLASS_NAME).equals(this.get(ClassMetric.SUPER_CLASS_NAME)))
			return false;

		// Check if dependencies have changed
		for (String dep : this.dependencies)
		{
			if (!c.dependencies.contains(dep))
			{
				return false;
			}
		}

		// TODO Should check fields here
		// TODO Should check methods here.

		return equals(c);
	}

	/**
     * Merges the metric data from the specified inner class into this
     * ClassMetricData.
     * 
     * @param innerClass The class being merged.
     */
	public void mergeInnerClass(ClassMetricData innerClass)
	{
		incrementMetric(ClassMetric.INNER_CLASS_COUNT, innerClass.getSimpleMetric(ClassMetric.INNER_CLASS_COUNT));
		incrementMetric(ClassMetric.INTERFACE_COUNT, innerClass.getSimpleMetric(ClassMetric.INTERFACE_COUNT));
		incrementMetric(ClassMetric.FIELD_COUNT, innerClass.getSimpleMetric(ClassMetric.FIELD_COUNT));
		incrementMetric(ClassMetric.METHOD_COUNT, innerClass.getSimpleMetric(ClassMetric.METHOD_COUNT));

		incrementMetric(ClassMetric.PUBLIC_FIELD_COUNT, innerClass.getSimpleMetric(ClassMetric.PUBLIC_FIELD_COUNT));
		incrementMetric(ClassMetric.PRIVATE_FIELD_COUNT, innerClass.getSimpleMetric(ClassMetric.PRIVATE_FIELD_COUNT));
		incrementMetric(ClassMetric.PROTECTED_FIELD_COUNT, innerClass
				.getSimpleMetric(ClassMetric.PROTECTED_FIELD_COUNT));

		incrementMetric(ClassMetric.STATIC_FIELD_COUNT, innerClass.getSimpleMetric(ClassMetric.STATIC_FIELD_COUNT));
		incrementMetric(ClassMetric.FINAL_FIELD_COUNT, innerClass.getSimpleMetric(ClassMetric.FINAL_FIELD_COUNT));

		incrementMetric(ClassMetric.SYNCHRONIZED_METHOD_COUNT, innerClass
				.getSimpleMetric(ClassMetric.SYNCHRONIZED_METHOD_COUNT));
		incrementMetric(ClassMetric.STATIC_METHOD_COUNT, innerClass.getSimpleMetric(ClassMetric.STATIC_METHOD_COUNT));
		incrementMetric(ClassMetric.FINAL_METHOD_COUNT, innerClass.getSimpleMetric(ClassMetric.FINAL_METHOD_COUNT));
		incrementMetric(ClassMetric.ABSTRACT_METHOD_COUNT, innerClass
				.getSimpleMetric(ClassMetric.ABSTRACT_METHOD_COUNT));
		incrementMetric(ClassMetric.PUBLIC_METHOD_COUNT, innerClass.getSimpleMetric(ClassMetric.PUBLIC_METHOD_COUNT));
		incrementMetric(ClassMetric.PRIVATE_METHOD_COUNT, innerClass.getSimpleMetric(ClassMetric.PRIVATE_METHOD_COUNT));
		incrementMetric(ClassMetric.PROTECTED_METHOD_COUNT, innerClass
				.getSimpleMetric(ClassMetric.PROTECTED_METHOD_COUNT));

		incrementMetric(ClassMetric.METHOD_CALL_COUNT, innerClass.getSimpleMetric(ClassMetric.METHOD_CALL_COUNT));
		incrementMetric(ClassMetric.IN_METHOD_CALL_COUNT, innerClass.getSimpleMetric(ClassMetric.IN_METHOD_CALL_COUNT));
		incrementMetric(ClassMetric.EX_METHOD_CALL_COUNT, innerClass.getSimpleMetric(ClassMetric.EX_METHOD_CALL_COUNT));

		incrementMetric(ClassMetric.BRANCH_COUNT, innerClass.getSimpleMetric(ClassMetric.BRANCH_COUNT));
		incrementMetric(ClassMetric.CONSTANT_LOAD_COUNT, innerClass.getSimpleMetric(ClassMetric.CONSTANT_LOAD_COUNT));
		incrementMetric(ClassMetric.INCREMENT_OP_COUNT, innerClass.getSimpleMetric(ClassMetric.INCREMENT_OP_COUNT));
		incrementMetric(ClassMetric.ISTORE_COUNT, innerClass.getSimpleMetric(ClassMetric.ISTORE_COUNT));
		incrementMetric(ClassMetric.ILOAD_COUNT, innerClass.getSimpleMetric(ClassMetric.ILOAD_COUNT));
		incrementMetric(ClassMetric.LOCAL_VAR_COUNT, innerClass.getSimpleMetric(ClassMetric.LOCAL_VAR_COUNT));
		incrementMetric(ClassMetric.REF_LOAD_OP_COUNT, innerClass.getSimpleMetric(ClassMetric.REF_LOAD_OP_COUNT));
		incrementMetric(ClassMetric.REF_STORE_OP_COUNT, innerClass.getSimpleMetric(ClassMetric.REF_STORE_OP_COUNT));
		incrementMetric(ClassMetric.LOAD_FIELD_COUNT, innerClass.getSimpleMetric(ClassMetric.LOAD_FIELD_COUNT));
		incrementMetric(ClassMetric.STORE_FIELD_COUNT, innerClass.getSimpleMetric(ClassMetric.STORE_FIELD_COUNT));
		incrementMetric(ClassMetric.THROW_COUNT, innerClass.getSimpleMetric(ClassMetric.THROW_COUNT));
		incrementMetric(ClassMetric.TRY_CATCH_BLOCK_COUNT, innerClass
				.getSimpleMetric(ClassMetric.TRY_CATCH_BLOCK_COUNT));
		incrementMetric(ClassMetric.TYPE_INSN_COUNT, innerClass.getSimpleMetric(ClassMetric.TYPE_INSN_COUNT));
		incrementMetric(ClassMetric.ZERO_OP_INSN_COUNT, innerClass.getSimpleMetric(ClassMetric.ZERO_OP_INSN_COUNT));

		// Add inner class dependencies.
		for (String dep : innerClass.dependencies)
			if (!dependencies.contains(dep))
				dependencies.add(dep);
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof ClassMetricData))
			return false;

		ClassMetricData other = (ClassMetricData) o;

		// Compare metrics between other and this class.

		int[] otherMetrics = other.getMetrics();
		for (int i = 0; i < ClassMetric.getNumberOfComparativeMetrics(); i++)
		{
			if (otherMetrics[i] != getMetrics()[i])
			{
				return false;
			}
		}

		// TODO Should check fields here
		// TODO Should check methods here.
		return true;
	}

	// TODO Could probably give a better textual representation of a
	// ClassMetricData here.
	public String toString()
	{
		String classType = getClassType();

		return String.format(
				"%3d, %3d, %3d, %3d, %3d, %3d, %3d, %4d, %3d, %3d," + " %3d, %4d, %4d, %3d, %3d, %s, %s",
				getSimpleMetric(ClassMetric.FAN_IN_COUNT),
				getSimpleMetric(ClassMetric.FAN_OUT_COUNT),
				getSimpleMetric(ClassMetric.LOAD_COUNT),
				getSimpleMetric(ClassMetric.STORE_COUNT),
				getSimpleMetric(ClassMetric.BRANCH_COUNT),
				getSimpleMetric(ClassMetric.METHOD_COUNT),
				getSimpleMetric(ClassMetric.FIELD_COUNT),
				getSimpleMetric(ClassMetric.SUPER_CLASS_COUNT),
				getSimpleMetric(ClassMetric.INTERFACE_COUNT),
				getSimpleMetric(ClassMetric.LOCAL_VAR_COUNT),
				getSimpleMetric(ClassMetric.TYPE_INSN_COUNT),
				getSimpleMetric(ClassMetric.ZERO_OP_INSN_COUNT),
				getSimpleMetric(ClassMetric.IN_METHOD_CALL_COUNT),
				getSimpleMetric(ClassMetric.EX_METHOD_CALL_COUNT),
				getSimpleMetric(ClassMetric.AGE),
				classType,
				get(ClassMetric.NAME)); // ,
		// superClassName);
	}

	public String getClassType()
	{
		String classType = "C";
		if (getSimpleMetric(ClassMetric.IS_ABSTRACT) == 1)
			classType = "A";
		if (getSimpleMetric(ClassMetric.IS_INTERFACE) == 1)
			classType = "I";
		if (getSimpleMetric(ClassMetric.GUI_DISTANCE) == 1)
			classType += "-GUI";
		if (getSimpleMetric(ClassMetric.IS_IO_CLASS) == 1)
			classType += "-IO";
		if (getSimpleMetric(ClassMetric.IS_EXCEPTION) == 1)
			classType += "-EX";
		return classType;
	}

	// Used for sorting by distance
	public int compareTo(ClassMetricData cm)
	{
		return Integer.valueOf(cm.getSimpleMetric(ClassMetric.COMPUTED_DISTANCE)).compareTo(
				getSimpleMetric(ClassMetric.COMPUTED_DISTANCE));
	}

	@Override
	public int hashCode()
	{
		return get(ClassMetric.NAME).hashCode();
	}

	public void accept(ReportVisitor visitor) throws ReportException
	{
		visitor.visit(this);
	}
}
