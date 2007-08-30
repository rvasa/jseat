package metric.core.vocabulary;

/**
 * All supported file types.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public enum SupportedFileType {
	ALL_SUPPORTED("*.ver;*.mmd;*.xmd", "Supported File Types (*.ver;*.mmd;*.xmd)"), CSV("*.mmd",
			"CSV Model Data (*.mmd)"), XML("*.xmd", "XML Model Data (*.xmd)"),
	VERSION("*.ver", "Version File (*.ver)"), REPORT("*.rep", "Report Configuration File (*.rep)"), CONFIG("*.conf",
			"JSeat Configuration File (*.conf)");

	private String ext;
	private String extName;

	private SupportedFileType(String ext, String extName)
	{
		this.ext = ext;
		this.extName = extName;
	}

	public String getExtension()
	{
		return ext;
	}

	public String getExtensionName()
	{
		return extName;
	}

	public static String[] getExtensions()
	{
		String[] extensions = new String[values().length];
		for (int i = 0; i < extensions.length; i++)
			extensions[i] = values()[i].getExtension();
		return extensions;
	}

	public static String[] getExtensionNames()
	{
		String[] extensionNames = new String[values().length];
		for (int i = 0; i < extensionNames.length; i++)
			extensionNames[i] = values()[i].getExtensionName();
		return extensionNames;
	}

	@Override
	/**
     * @return The extension of the FileType.
     */
	public String toString()
	{
		// remove * at beginning.
		return ext.substring(1, ext.length());
	}

}
