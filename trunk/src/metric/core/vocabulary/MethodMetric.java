package metric.core.vocabulary;

public enum MethodMetric {
	SCOPE,

	// Type related.
	IS_FINAL, IS_STATIC, IS_ABSTRACT, IS_SYNCHRONIZED,

	// Branching related.
	TRY_CATCH_BLOCK_COUNT, BRANCH_COUNT, METHOD_CALL_COUNT, THROW_COUNT,

	// Instruction related
	TYPE_INSN_COUNT, ZERO_OP_INSN_COUNT, CONSTANT_LOAD_COUNT, INCREMENT_OP_COUNT, ILOAD_COUNT,
	ISTORE_COUNT, REF_LOAD_OP_COUNT, REF_STORE_OP_COUNT,
	
	TYPE_CONSTRUCTION_COUNT, INSTANCE_OF_COUNT, CHECK_CAST_COUNT, NEW_COUNT, NEW_ARRAY_COUNT,

	// Method related.
	IN_METHOD_CALL_COUNT, EX_METHOD_CALL_COUNT,

	// Field related
	STORE_FIELD_COUNT, LOAD_FIELD_COUNT, LOCAL_VAR_COUNT;


	public String toString()
	{
		return name().toLowerCase();
	}

	public static MethodMetric parse(String toParse)
	{
		for (MethodMetric m : values())
		{
			if (toParse.equals(m.toString()))
				return m;
		}
		return null;
	}
}
