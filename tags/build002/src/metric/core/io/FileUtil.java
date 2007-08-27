package metric.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/** A collection of file utilities that dont' seem to fit anywhere else */
public class FileUtil
{
	/** True if file is a JAR or ZIP file */
	public static boolean isArchive(String fileName)
	{
		if (fileName == null)
			return false;
		return ((fileName.endsWith(".jar") || fileName.endsWith(".zip")));
	}

	/** Checks if the input is a class file
	 * @param cf the input to check.
	 * @param includeInner whether or not to ignore innerclasses.
	 * @return
	 */
	public static boolean isClassFile(String cf, boolean ignoreInner)
	{
		if (cf == null)
			return false;
//		else if (ignoreInner)
//			return ((cf.endsWith(".class") && (cf.indexOf("$") == -1)));
		else
			return (cf.endsWith(".class"));
	}
	
	public static boolean isInnerClassFile(String cf)
	{
		if (cf == null)
			return false;
		else
			return ((cf.endsWith(".class") && (cf.indexOf("$") != -1)));
	}

	/**
     * Accepts valid directory, picks up all JAR, .class and ZIP files
     * @param dirName if valid dir. will process, else will do nothing
     * @param recursive when true will process directory tree recursively
     */
	public static Set<File> getFiles(String dirName, boolean recursive)
			throws IOException
	{
		Set<File> fileList = new HashSet<File>();
		File dir = getFileHandle(dirName);
		if (!dir.isDirectory())
			return fileList;// return an empty list

		// The file is directory, Check for files recursively
		File[] files = dir.listFiles();
		for (File f : files)
		{
			if (f.isDirectory() && f.canRead() && recursive)
			{
				fileList.addAll(getFiles(f.getPath(), recursive)); // recursive-call
			} else
			{
				fileList.add(f);
			}
		}
		return fileList;
	}

	/** Returns File is it exists, and has read permission -- exception if not */
	public static File getFileHandle(String fileName) throws IOException
	{
		File f = new File(fileName.trim());
		if (!f.canRead())
			throw new FileNotFoundException(fileName);
		return f;
	}

}
