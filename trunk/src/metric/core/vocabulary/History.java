package metric.core.vocabulary;

public enum History {
	VERSIONS,

	// Non metrics.
	NAME, SHORTNAME;

	public String toString()
	{
		return name().toLowerCase();
	}

	public static int getNumberOfMetrics()
	{
		// We exclude non metric related enums
		int size = values().length - 2;
		return size;
	}

	public static History parse(String toParse)
	{
		for (History h : values())
		{
			if (toParse.equals(h.toString()))
				return h;
		}
		return null;
	}
}
