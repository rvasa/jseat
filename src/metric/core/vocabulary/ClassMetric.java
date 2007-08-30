package metric.core.vocabulary;

public enum ClassMetric {
	// Class related.

	INNER_CLASS_COUNT,
	SUPER_CLASS_COUNT, // will be set to 1, if it is not
	// java/lang/Object
	INTERFACE_COUNT, // Number of implemented interfaces
	LOCAL_VAR_COUNT,

	INSTABILITY,
	LAYER, // Currenly only 4 layers are extracted [top/Mid/bottom
	// and free]
	RAW_SIZE_COUNT,

	// Dependency related.
	FAN_OUT_COUNT, // Non-primitive dependencies, including library deps
	FAN_IN_COUNT,

	// Method related
	EX_METHOD_CALL_COUNT, IN_METHOD_CALL_COUNT, METHOD_CALL_COUNT, METHOD_COUNT, PUBLIC_METHOD_COUNT,
	PRIVATE_METHOD_COUNT, PROTECTED_METHOD_COUNT, FINAL_METHOD_COUNT, SYNCHRONIZED_METHOD_COUNT,
	ABSTRACT_METHOD_COUNT,
	STATIC_METHOD_COUNT,

	// Field related.
	FIELD_COUNT, // TODO Make this a complex metric.
	PUBLIC_FIELD_COUNT, PRIVATE_FIELD_COUNT, PROTECTED_FIELD_COUNT, STATIC_FIELD_COUNT, FINAL_FIELD_COUNT,

	// Type related
	IS_INTERFACE, IS_ABSTRACT, IS_PUBLIC, IS_PRIVATE, IS_PROTECTED, IS_EXCEPTION, IS_IO_CLASS,

	// Instruction related.
	CONSTANT_LOAD_COUNT, INCREMENT_OP_COUNT, REF_LOAD_OP_COUNT, REF_STORE_OP_COUNT, LOAD_COUNT, STORE_COUNT,
	ILOAD_COUNT, ISTORE_COUNT, TYPE_INSN_COUNT, LOAD_FIELD_COUNT, STORE_FIELD_COUNT, ZERO_OP_INSN_COUNT, LOAD_RATIO,
	
	TYPE_CONSTRUCTION_COUNT, INSTANCE_OF_COUNT, CHECK_CAST_COUNT, NEW_COUNT, NEW_ARRAY_COUNT,

	// Inner class related.
	INTERNAL_FAN_OUT_COUNT,

	// Brachning related.
	THROW_COUNT, TRY_CATCH_BLOCK_COUNT, // Number of try-catch blocks
	NORMALIZED_BRANCH_COUNT, BRANCH_COUNT, // Total number of branch
	// instructions
	
	MODIFICATION_STATUS_SINCE_BIRTH,
	MODIFIED_METRIC_COUNT_SINCE_BIRTH,

	// These are not comparive measures for class equality checks.
	GUI_DISTANCE, COMPUTED_DISTANCE, DISTANCE_MOVED_SINCE_BIRTH,// used to store distance once computed
	NEXT_VERSION_STATUS, EVOLUTION_DISTANCE, EVOLUTION_STATUS, IS_DELETED, IS_MODIFIED, DATE, BORN_RSN, AGE,
	// Non metrics.
	NAME, PRODUCT_NAME, SUPER_CLASS_NAME, EVOLUTION;

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
