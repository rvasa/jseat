package metric.core.vocabulary;

/**
 * All supported file types.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public enum JSeatFileType {
	ALL_SUPPORTED("*.ver;*.rep;*.jpf", "Supported File Types (*.ver;*.rep;*.jpf)"), VERSION("*.ver",
			"JSeat Version File (*.ver)"), REPORT("*.rep", "JSeat Report Configuration File (*.rep)"), PROJECT("*.jpf",
			"JSeat Project File (*.jpf)");

	private String ext;
	private String extName;

	private JSeatFileType(String ext, String extName)
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
