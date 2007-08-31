package io;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipFile;


public class InputDataSet implements Iterable<InputStream>
{
	private HashSet<File> files = new HashSet<File>();
	private boolean processingArchive = false;
    private long fileSize = 0;
    
    public long sizeInBytes()
    {
        return fileSize;
    }
	
	public void addInputFile(File f)
	{
		if (FileUtil.isClassFile(f.toString()) || FileUtil.isArchive(f.toString()))
        {
			//System.out.println(f);
			files.add(f);
            fileSize += f.length();
        }
	}
	
	/** Accepts valid JAR, .class and .ZIP files. 
	 * Will throw exception if the input data is not found or is not
	 * of the correct format
	 * @throws FileNotFoundException 
	 */
	public void addInputFile(String fileName) throws IOException
	{
		addInputFile(FileUtil.getFileHandle(fileName));
	}
	
	public void addInputDir(String dirName, boolean recursive) throws IOException
	{
		Set<File> dirFiles = FileUtil.getFiles(dirName, recursive);
		for (File f : dirFiles) addInputFile(f);
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
			if (!setIter.hasNext()) throw new IllegalArgumentException();
			//nextFile = setIter.next();
		}
		
		public boolean hasNext()
		{
			//if (zipIter == null) System.out.println("Hmm .. this is not good");
			if (processingArchive && (zipIter != null) && (zipIter.hasNext())) return true;
			processingArchive = false;
			return setIter.hasNext();
		}
		
		/** Return the next file in the input data set */ 
		public InputStream next()
		{
			if (processingArchive) return zipIter.next();

			InputStream ret = null;           
            try
            {
    			nextFile = setIter.next(); 
    			if (FileUtil.isArchive(nextFile.toString()))
    			{
    				//System.out.println("Processing archive: "+nextFile);
    				zipFile = new ZipFile(nextFile);
    				zipIter = new ZipFileIterator(zipFile);
    				ret = zipIter.next();
    				processingArchive = true;
    			}
    			else
    			{
    				//System.out.println("Processing data set "+nextFile);
    				ret = new BufferedInputStream(new FileInputStream(nextFile));
                        //nextFile.toString();
    				processingArchive = false;
    			}
            } catch (Exception e)
            {
                System.err.println("Unexpected exception: "+e);
            }
			
			return ret;
		}

		public void remove() {} // Ignore -- no action for this 
	}

	/** Test harness 
	 * @throws FileNotFoundException */
	public static void main(String[] args) throws IOException
	{
		InputDataSet input = new InputDataSet();
		input.addInputDir("C:\\workspace\\asm-2.2\\lib", false);
		System.out.println("Files in input data set: "+input.size());
		int items = 0;
		for (InputStream s : input)
		{
			System.out.println(s);
			items++;			
		}
		System.out.println("Items processed: "+items);
	}
}
