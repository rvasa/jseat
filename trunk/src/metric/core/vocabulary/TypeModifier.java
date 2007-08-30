package metric.core.vocabulary;

public enum TypeModifier {
	PUBLIC, PRIVATE, PROTECTED, STATIC, SYCHNRONIZED, FINAL, ABSTRACT;

	public String toString()
	{
		return name().toLowerCase().toString();
	}

	public static TypeModifier parse(String toParse)
	{
		for (TypeModifier s : values())
		{
			if (toParse.equals(s.toString()))
				return s;
		}
		return null;
	}
}
