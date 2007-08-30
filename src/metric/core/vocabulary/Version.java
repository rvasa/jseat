package metric.core.vocabulary;

public enum Version {
	CLASS_COUNT, RSN, GUI_CLASS_COUNT,

	// Non metrics.
	ID, /** property * */
	REPORT_PROGRESS(5), NAME,
	// Complex Metrics below
	ALPHA, BETA, RELATIVE_SIZE_CHANGE, ISUM, PRED, PRED_ERROR;

	private int value;

	private Version(int value)
	{
		this.value = value;
	}

	private Version()
	{
		this(0);
	}

	public String toString()
	{
		return name().toLowerCase();
	}

	public static int getNumberOfMetrics()
	{
		// We exclude non metric related enums
		int size = values().length - 9;
		return size;
	}

	public static Version parse(String toParse)
	{
		for (Version v : values())
		{
			if (toParse.equals(v.toString()))
				return v;
		}
		return null;
	}

	public final int getValue()
	{
		return value;
	}
}
