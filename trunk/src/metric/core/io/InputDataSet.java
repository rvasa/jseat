package metric.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InputDataSet implements Iterable<InputData>
{
	private Logger logger = Logger.getLogger(getClass().getSimpleName());
	private HashSet<File> files = new HashSet<File>();
	private HashMap<String, InputData> idata;

	private boolean processingArchive = false;
	private long fileSize = 0;

	public String jarFileName, versionId, productName, shortName;
	public int RSN;

	// streams open outside of the DataSetIterator.
	private boolean haveOpenStreams;

	public InputDataSet()
	{
	}

	public InputDataSet(String jarFileName, String versionId, int RSN,
			String productName, String shortName)
	{
		this.jarFileName = jarFileName;
		this.versionId = versionId.trim();
		this.RSN = RSN;
		this.productName = productName;
		this.shortName = shortName;
		haveOpenStreams = false;
	}

	/**
     * Returns the InputStream corresponding to the name specified if it exists
     * in this <code>InputDataSet</code>.
     * 
     * @param name the name of the stream to retrieve.
     * @return the corresponding input data stream.
     */
	public InputData getInputData(String name)
	{
		return idata.get(name);
	}

	public long sizeInBytes()
	{
		return fileSize;
	}

	public void addInputFile(File f)
	{
		if (FileUtil.isClassFile(f.toString())
				|| FileUtil.isArchive(f.toString()))
		{
			files.add(f);
			fileSize += f.length();
		}
	}

	/**
     * Inflates all inner class files as these will need to be referenced later.
     * Only inner classes are inflated to keep memory requirements minimal.
     */
	public void inflate(boolean onlyInnerClasses)
	{
		if (idata == null)
			idata = new HashMap<String, InputData>();

		Iterator it = files.iterator();
		while (it.hasNext())
		{
			File f = (File) it.next();
			try
			{ // This maps each inputstream to the classname.
				ZipFile zipFile = new ZipFile(f);
				Enumeration e = zipFile.entries();
				while (e.hasMoreElements())
				{
					ZipEntry ze = (ZipEntry) e.nextElement();
					if (onlyInnerClasses
							&& FileUtil.isInnerClassFile(ze.getName()))
					{
						InputData id = new InputData(ze, zipFile
								.getInputStream(ze));
						idata.put(ze.getName(), id);
						haveOpenStreams = true;
					} else
					{
						InputData id = new InputData(ze, zipFile
								.getInputStream(ze));
						idata.put(ze.getName(), id);
						haveOpenStreams = true;
					}

				}
			} catch (IOException e) // shouldn't happen.
			{
				e.printStackTrace();
			}
		}
	}

	/**
     * This closes any files streams that were opened earlier during inflation.
     * 
     * @throws IOException
     */
	public void deflate() throws IOException
	{
		if (idata != null)
		{
			Iterator<InputData> it = idata.values().iterator();
			while (it.hasNext())
			{
				InputStream is = it.next().getInputStream();
				is.close();
				is = null;
			}
			idata = null;
		}
	}

	/**
     * Closes any file streams that may still be open.
     * 
     * @throws IOException
     */
	public void close() throws IOException
	{
		if (haveOpenStreams)
			deflate();
	}

	/**
     * Accepts valid JAR, .class and .ZIP files. Will throw exception if the
     * input data is not found or is not of the correct format
     * 
     * @throws FileNotFoundException
     */
	public void addInputFile(String fileName) throws IOException
	{
		addInputFile(FileUtil.getFileHandle(fileName));
	}

	public void addInputDir(String dirName, boolean recursive)
			throws IOException
	{
		Set<File> dirFiles = FileUtil.getFiles(dirName, recursive);
		for (File f : dirFiles)
			addInputFile(f);
	}

	public int size()
	{
		return files.size();
	}

	public Iterator<InputData> iterator()
	{
		return new DataSetIterator();
	}

	class DataSetIterator implements Iterator<InputData>
	{
		File nextFile;
		ZipFile zipFile = null;
		ZipFileIterator zipIter = null;
		Iterator<File> setIter;

		public DataSetIterator()
		{
			setIter = files.iterator();
			if (!setIter.hasNext())
				throw new IllegalArgumentException();
		}

		public boolean hasNext()
		{
			if (processingArchive && zipIter.hasNext())
				return true;
			processingArchive = false;
			return setIter.hasNext();
		}

		/** Return the next file in the input data set */
		public InputData next()
		{
			InputData ret = null;

			while (ret == null)
			{
				ZipEntry ze = null;
				try
				{
					if (processingArchive)
					{
						ze = zipIter.next();
						ret = new InputData(ze, zipFile.getInputStream(ze));
					} else
					{
						nextFile = setIter.next();
						if (FileUtil.isArchive(nextFile.toString()))
						{
							zipFile = new ZipFile(nextFile);
							zipIter = new ZipFileIterator(zipFile);
							ze = zipIter.next();
							ret = new InputData(ze, zipFile.getInputStream(ze));
							processingArchive = true;
						} else
						{
							ret = new InputData(nextFile);
							processingArchive = false;
						}
					}
				} catch (Exception e)
				{
					if (ze != null) // Print the name of the bad file if we can.
						logger.log(Level.WARNING, "Skipping " + ze.getName() +": Could not retrieve file from input data set.");
					else // Just report there was a bad file that could not be retrieved.
						logger.log(Level.WARNING, "Skipping bad file. Could not retrieve file from input data set.");
				}
			}

			return ret;
		}

		public void remove()
		{
		} // Ignore -- no action for this
	}

	/**
     * Test harness
     * 
     * @throws FileNotFoundException
     */
	public static void main(String[] args) throws IOException
	{
		InputDataSet input = new InputDataSet();
		input.addInputFile("B:\\workspace\\builds\\asm\\asm-1.4.2.jar");
		System.out.println("Files in input data set: " + input.size());
		int items = 0;

		// Iterate over streams
		for (InputData id : input)
		{
			System.out.println(id);
			items++;
		}
		System.out.println("Files iterated over: " + items + "\n");

		// Selectively pick stream.
		input.inflate(false); // Must inflate data set into memory.
		System.out.println(input
				.getInputData("org/objectweb/asm/ClassReader.class"));

		System.out.println(input
				.getInputData("org/objectweb/asm/Constants.class"));
	}
}
