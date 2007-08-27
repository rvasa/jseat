package metric.core.vocabulary;

public enum Evolution {
	UNCHANGED(0), MODIFIED(1), DELETED(2), NEW(3);
	
	private int value;
	
	private Evolution(int value)
	{
		this.value = value;
	}
	
	public int getValue()
	{
		return value;
	}
	
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
		for (Evolution e : values())
		{
			values[index++] = e.toString();
		}
		return values;
	}
	
	public static Evolution parse(String toParse)
	{
		for (Evolution e : values())
		{
			if (toParse.equals(e.toString()))
				return e;
		}
		return null;
	}
}
