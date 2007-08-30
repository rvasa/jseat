package metric.core.io;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * ZipFileIterator iterates over a Zip/Jar archive giving access to the
 * inputstream of the entires in the archive via an iterator
 * 
 * @author rvasa
 */
public class ZipFileIterator implements Iterator<ZipEntry>
{
	Enumeration<? extends ZipEntry> zipIter = null;
	ZipFile zipFile;
	ZipEntry entry;

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

	private ZipEntry findNext()
	{
		while (zipIter.hasMoreElements())
		{
			ZipEntry ze = zipIter.nextElement();
			if (FileUtil.isClassFile(ze.getName()))
				return ze;
		}
		return null;
	}

	public ZipEntry next()
	{
		ZipEntry current = entry;
		entry = findNext(); // prepare the next entry for future calls
		return current;
	}

	public void remove()
	{
	} // not used -- ignore
}