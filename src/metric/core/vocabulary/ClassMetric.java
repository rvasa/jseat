package metric.core.vocabulary;

public enum ClassMetric {
	INNER_CLASS_COUNT,
	SUPER_CLASS_COUNT,
	INTERFACE_COUNT,
	LOCAL_VAR_COUNT,
	INSTABILITY(MetricType.NOT_COMPARABLE),
	LAYER(MetricType.NOT_COMPARABLE),
	RAW_SIZE_COUNT(MetricType.NOT_COMPARABLE),

	// Dependency related.
	FAN_OUT_COUNT,
	FAN_IN_COUNT,

	// Method related
	EX_METHOD_CALL_COUNT,
	IN_METHOD_CALL_COUNT,
	METHOD_CALL_COUNT,
	METHOD_COUNT,
	PUBLIC_METHOD_COUNT,
	PRIVATE_METHOD_COUNT,
	PROTECTED_METHOD_COUNT,
	FINAL_METHOD_COUNT(MetricType.COMPARATIVE),
	SYNCHRONIZED_METHOD_COUNT(MetricType.COMPARATIVE),
	ABSTRACT_METHOD_COUNT,
	STATIC_METHOD_COUNT(MetricType.COMPARATIVE),

	// Field related.
	FIELD_COUNT, // TODO Make this a complex metric.
	PUBLIC_FIELD_COUNT,
	PRIVATE_FIELD_COUNT,
	PROTECTED_FIELD_COUNT,
	STATIC_FIELD_COUNT(MetricType.COMPARATIVE),
	FINAL_FIELD_COUNT(MetricType.COMPARATIVE),

	// Type related
	IS_INTERFACE(MetricType.COMPARATIVE), IS_ABSTRACT(MetricType.COMPARATIVE),
	IS_PUBLIC(MetricType.COMPARATIVE),
	IS_PRIVATE(MetricType.COMPARATIVE),
	IS_PROTECTED(MetricType.COMPARATIVE),
	IS_EXCEPTION(MetricType.COMPARATIVE),
	IS_IO_CLASS,

	// Instruction related.
	CONSTANT_LOAD_COUNT, INCREMENT_OP_COUNT, REF_LOAD_OP_COUNT, REF_STORE_OP_COUNT, LOAD_COUNT, STORE_COUNT,
	ILOAD_COUNT, ISTORE_COUNT, TYPE_INSN_COUNT, LOAD_FIELD_COUNT, STORE_FIELD_COUNT, ZERO_OP_INSN_COUNT, LOAD_RATIO(
			MetricType.NOT_COMPARABLE),

	TYPE_CONSTRUCTION_COUNT(MetricType.NOT_COMPARABLE), INSTANCE_OF_COUNT(MetricType.NOT_COMPARABLE),
	CHECK_CAST_COUNT(MetricType.NOT_COMPARABLE),
	NEW_COUNT(MetricType.NOT_COMPARABLE),
	NEW_ARRAY_COUNT(MetricType.NOT_COMPARABLE),

	// Inner class related.
	INTERNAL_FAN_OUT_COUNT,

	// Brachning related.
	THROW_COUNT, TRY_CATCH_BLOCK_COUNT, NORMALIZED_BRANCH_COUNT, BRANCH_COUNT, MODIFICATION_STATUS_SINCE_BIRTH(
			MetricType.NOT_COMPARABLE), MODIFIED_METRIC_COUNT_SINCE_BIRTH(MetricType.NOT_COMPARABLE),
	MODIFIED_METRIC_COUNT(MetricType.NOT_COMPARABLE), MODIFICATION_FREQUENCY(MetricType.NOT_COMPARABLE),

	// These are not comparive measures for class equality checks.
	GUI_DISTANCE(MetricType.NOT_COMPARABLE), COMPUTED_DISTANCE(MetricType.NOT_COMPARABLE), DISTANCE_MOVED_SINCE_BIRTH(
			MetricType.NOT_COMPARABLE), NEXT_VERSION_STATUS(MetricType.NOT_COMPARABLE), EVOLUTION_DISTANCE(
			MetricType.NOT_COMPARABLE), EVOLUTION_STATUS(MetricType.NOT_COMPARABLE), IS_DELETED(
			MetricType.NOT_COMPARABLE), IS_MODIFIED(MetricType.NOT_COMPARABLE), DATE(MetricType.NOT_COMPARABLE),
	BORN_RSN(MetricType.NOT_COMPARABLE), AGE(MetricType.NOT_COMPARABLE),
	// Non metrics.
	NAME(MetricType.NOT_COMPARABLE), PRODUCT_NAME(MetricType.NOT_COMPARABLE), SUPER_CLASS_NAME(MetricType.NOT_COMPARABLE), EVOLUTION(MetricType.NOT_COMPARABLE);

	MetricType type;

	// Default
	private ClassMetric()
	{
		this.type = MetricType.ANY;
	}

	private ClassMetric(MetricType type)
	{
		this.type = type;
	}

	public MetricType type()
	{
		return type;
	}

	public String toString()
	{
		return name().toLowerCase();
	}

	public static int getNumberOfMetrics()
	{
		// We exclude non metric related enums
		return values().length - 4;
	}

	public static int getNumberOfComparativeMetrics()
	{
		// We exclude non metric related enums
		// as well as non comparitive measures.
		return values().length - 14;
	}

	/**
     * @return A string representation of each enum value.
     */
	public static String[] toStrings()
	{
		String[] values = new String[values().length];
		int index = 0;
		for (ClassMetric cm : values())
		{
			values[index++] = cm.toString();
		}
		return values;
	}

	public static ClassMetric parse(String toParse)
	{
		for (ClassMetric c : values())
		{
			if (toParse.equals(c.toString()))
				return c;
		}
		return null;
	}
}
