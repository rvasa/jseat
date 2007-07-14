package metric.core.vocabulary;

public enum History {
	NAME,
	SHORTNAME,
	VERSIONS;
	
	public String toString()
	{
		return name().toLowerCase();
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
