package metric.core.io;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * ZipFileIterator iterates over a Zip/Jar archive giving access to the 
 * inputstream of the entires in the archive via an iterator
 * @author rvasa
 */
public class ZipFileIterator implements Iterator<InputStream>
{
	Enumeration<? extends ZipEntry> zipIter = null;
	InputStream entry = null;
    ZipFile zipFile;

	public ZipFileIterator(ZipFile zf)
	{
        zipFile = zf;
		zipIter = zf.entries();
		entry = findNext(); // find the initial entry
	}

	public boolean hasNext()
	{
		return (entry != null); // there is one viable class file
	}

	private InputStream findNext()
	{
		while (zipIter.hasMoreElements())
		{
            try
            {
                ZipEntry ze = zipIter.nextElement();
    			if (FileUtil.isClassFile(ze.getName(), true))
                    return zipFile.getInputStream(ze);
            } catch (IOException ioex)
            {
                continue; // go to the next element if there is an exception
            }
		}
		return null;
	}

	public InputStream next()
	{
		InputStream retEntry = entry;
		entry = findNext(); // prepare the next entry for future calls
		return retEntry;
	}

	public void remove()
	{
	} // not used -- ignore
}