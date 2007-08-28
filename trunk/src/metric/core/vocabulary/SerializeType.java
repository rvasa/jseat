package metric.core.vocabulary;

public enum SerializeType {
	CLASSES(".cme"),
	METHODS(".mme"),
	DEPENDENCIES(".dep");
	
	String ext;

	private SerializeType(String ext)
	{
		this.ext = ext;
	}
	
	public String getExt()
	{
		return ext;
	}
}
