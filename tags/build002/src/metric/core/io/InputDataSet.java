package metric.core.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InputDataSet implements Iterable<InputStream>
{
	private HashSet<File> files = new HashSet<File>();
	private HashMap<String, InputStream> streams;

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
     * @param name the name of the stream to retrieve.
     * @return the corresponding input data stream.
     */
	public InputStream getInputStream(String name)
	{
		return streams.get(name);
	}

	public long sizeInBytes()
	{
		return fileSize;
	}

	public void addInputFile(File f)
	{
		if (FileUtil.isClassFile(f.toString(), true)
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
	public void inflate()
	{
		if (streams == null)
			streams = new HashMap<String, InputStream>();

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
					if (FileUtil.isInnerClassFile(ze.getName()))
					{
						streams.put(ze.getName(), zipFile.getInputStream(ze));
						haveOpenStreams = true;
					}
				}
//				zipFile.close();
			} catch (IOException e) // shouldn't happen.
			{
				e.printStackTrace();
			}
		}
	}

	/**
     * This closes any files streams that were opened earlier during inflation.
     * @throws IOException
     */
	public void deflate() throws IOException
	{
		if (streams != null)
		{
			Iterator it = streams.values().iterator();
			while (it.hasNext())
			{
				InputStream is = (InputStream) it.next();
				is.close();
				is = null;
			}
			streams = null;
		}
	}

	/**
     * Closes any file streams that may still be open.
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

	public Iterator<InputStream> iterator()
	{
		return new DataSetIterator();
	}

	class DataSetIterator implements Iterator<InputStream>
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
			// nextFile = setIter.next();
		}

		public boolean hasNext()
		{
			if (processingArchive && zipIter.hasNext())
				return true;
			processingArchive = false;
			return setIter.hasNext();
		}

		/** Return the next file in the input data set */
		public InputStream next()
		{
			if (processingArchive)
				return zipIter.next();

			InputStream ret = null;
			try
			{
				nextFile = setIter.next();
				if (FileUtil.isArchive(nextFile.toString()))
				{
					zipFile = new ZipFile(nextFile);
					zipIter = new ZipFileIterator(zipFile);

					ret = zipIter.next();
					processingArchive = true;
				} else
				{
					ret = new BufferedInputStream(new FileInputStream(nextFile));
					// nextFile.toString();
					processingArchive = false;
				}
			} catch (Exception e)
			{
				System.err.println("Unexpected exception: " + e);
			}

			return ret;
		}

		public void remove()
		{
		} // Ignore -- no action for this
	}

	/**
     * Test harness
     * @throws FileNotFoundException
     */
	public static void main(String[] args) throws IOException
	{
		InputDataSet input = new InputDataSet();
		input.addInputFile("B:\\workspace\\builds\\asm\\asm-1.4.2.jar");
		System.out.println("Files in input data set: " + input.size());
		int items = 0;

		// Iterate over streams
		for (InputStream s : input)
		{
			System.out.println(s);
			items++;
		}

		// Selectively pick stream.
		System.out.println(input
				.getInputStream("org/objectweb/asm/ClassReader.class"));
		System.out.println(input
				.getInputStream("org/objectweb/asm/Constants.class"));
	}
}
