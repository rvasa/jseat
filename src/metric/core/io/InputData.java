package metric.core.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

public class InputData
{
	private final InputStream stream;
	private final long lastModified;
	private final String name;
	
	public InputData(File file) throws FileNotFoundException
	{
		this.stream = new BufferedInputStream(new FileInputStream(file));
		this.lastModified = file.lastModified();
		this.name = file.getName();
	}
	
	public InputData(ZipEntry entry, InputStream stream) throws FileNotFoundException
	{
		this.stream = stream;
		this.lastModified = entry.getTime();
		this.name = entry.getName();
	} 
	
	public InputStream getInputStream()
	{
		return stream;
	}
	
	public long getLastModifiedTime()
	{
		return lastModified;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
