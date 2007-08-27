package metric.core.vocabulary;

public enum MethodMetric {
//	NAME(0),  // Number indicates position in MethodMetric
	SCOPE(1),
	
	// Type related.
	IS_FINAL(2),
	IS_STATIC(3),
	IS_ABSTRACT(4),
	IS_SYNCHRONIZED(5),
	
	// Branching related.
	TRY_CATCH_BLOCK_COUNT(6),
	BRANCH_COUNT(7),
	METHOD_CALL_COUNT(8),
	THROW_COUNT(9),
	
	// Instruction related
	TYPE_INSN_COUNT(10),
	ZERO_OP_INSN_COUNT(11),
	CONSTANT_LOAD_COUNT(12),
	INCREMENT_OP_COUNT(13),
	ILOAD_COUNT(14),
	ISTORE_COUNT(15),
	REF_LOAD_OP_COUNT(16),
	REF_STORE_OP_COUNT(17),
	
	// Method related.
	IN_METHOD_CALL_COUNT(18),
	EX_METHOD_CALL_COUNT(19),
	
	// Field related
	STORE_FIELD_COUNT(20),
	LOAD_FIELD_COUNT(21),	
	LOCAL_VAR_COUNT(22);
	
	
	private int value;
	
	private MethodMetric(int value)
	{
		this.value = value;
	}
	
	public boolean isString()
	{
		return value == 0;
	}
	
	public boolean isInt()
	{
		return value == 1;
	}

	public String toString()
	{
		return  name().toLowerCase();
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
