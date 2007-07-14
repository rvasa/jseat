package metric.core.vocabulary;

public enum Version 
{
	ID, /** property **/
	RSN, /** metric **/
	NAME,
	REPORT_PROGRESS(5),
	GUI_CLASS_COUNT,
	CLASS_COUNT,
	// Complex Metrics below
	ALPHA,
	BETA,
	RELATIVE_SIZE_CHANGE,
	ISUM,
	PRED,
	PRED_ERROR;

	private int value;
	
	private Version(int value) { this.value = value; }
	
	private Version() { this(0); }
	
	public String toString()
	{
		return name().toLowerCase();
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
	
	public final int getValue() { return value;}
}
