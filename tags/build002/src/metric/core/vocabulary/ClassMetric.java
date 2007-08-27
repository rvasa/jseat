package metric.core.vocabulary;

public enum ClassMetric{
	// Class related.
	NAME,
	PRODUCT_NAME,
	INNER_CLASS_COUNT,
	SUPER_CLASS_NAME,
	SUPER_CLASS_COUNT, // will be set to 1, if it is not java/lang/Object
	INTERFACE_COUNT, // Number of implemented interfaces
	LOCAL_VAR_COUNT,
	GUI_DISTANCE,
	BORN_RSN,
	COMPUTED_DISTANCE, // used to store distance once computed
	INSTABILITY,
	LAYER, // Currenly only 4 layers are extracted [top/Mid/bottom and free]
	AGE, // Start off young
	RAW_SIZE_COUNT,
	
	// Dependency related.
	FAN_OUT_COUNT,     // Non-primitive dependencies, including library deps
	FAN_IN_COUNT,
	
	// Method related
	EX_METHOD_CALL_COUNT,
	IN_METHOD_CALL_COUNT,
	METHOD_CALL_COUNT,
	METHOD_COUNT,    //TODO Make this a complex metric.
	PUBLIC_METHOD_COUNT,
	PRIVATE_METHOD_COUNT,
	PROTECTED_METHOD_COUNT,
	FINAL_METHOD_COUNT,
	SYNCHRONIZED_METHOD_COUNT,
	ABSTRACT_METHOD_COUNT,
	STATIC_METHOD_COUNT,
	
	// Field related.
	FIELD_COUNT, // TODO Make this a complex metric.
	PUBLIC_FIELD_COUNT,
	PRIVATE_FIELD_COUNT,
	PROTECTED_FIELD_COUNT,
	STATIC_FIELD_COUNT,
	FINAL_FIELD_COUNT,
	
	// Type related
	IS_INTERFACE, 
	IS_ABSTRACT,
	IS_PUBLIC,
	IS_PRIVATE,
	IS_PROTECTED,
	IS_EXCEPTION,
	IS_IO_CLASS,

	// Instruction related.
	CONSTANT_LOAD_COUNT,
	INCREMENT_OP_COUNT,
	REF_LOAD_OP_COUNT, // Number of reference loads
	REF_STORE_OP_COUNT, // Number of reference stores
	LOAD_COUNT,
	STORE_COUNT,
	ILOAD_COUNT,
	ISTORE_COUNT,
	TYPE_INSN_COUNT,
	LOAD_FIELD_COUNT, // Number of times a field was loaded
	STORE_FIELD_COUNT, // Number of times a field was stored
	ZERO_OP_INSN_COUNT,
	
	// Inner class related.
	INNER_LOAD_COUNT,
	INNER_STORE_COUNT,
	INTERNAL_FAN_OUT_COUNT,
	
	// Brachning related.
	THROW_COUNT,
	TRY_CATCH_BLOCK_COUNT, // Number of try-catch blocks
	NORMALIZED_BRANCH_COUNT,
	BRANCH_COUNT,     // Total number of branch instructions
	
	// Evolution related.
	EVOLUTION, 
	NEXT_VERSION_STATUS,
	EVOLUTION_DISTANCE,
	EVOLUTION_STATUS,
	IS_DELETED,
	IS_MODIFIED; // records if this class will be modified in the next version
	
	public String toString()
	{
		return name().toLowerCase();
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
