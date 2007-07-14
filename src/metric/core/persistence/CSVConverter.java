package metric.core.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Observable;
import java.util.Observer;

import metric.core.exception.ConversionException;
import metric.core.model.MetricData;

public class CSVConverter extends Observable implements MetricDataConverter,
		Observer
{
	private static final String FILE_EXTENSION = ".mmd";

	private MetricDataToCSV md2csv;
	private BufferedWriter bw;
	private BufferedReader br;

	private long storeTime = 0, loadTime = 0;
	private int completion;

	public CSVConverter()
	{
		md2csv = new MetricDataToCSV();
		md2csv.addObserver(this);
	}

	public void close() throws IOException
	{
		if (bw != null)
			bw.close();
		if (br != null)
			br.close();
	}

	public MetricData deSerialize(String data) throws ConversionException
	{
		throw new ConversionException("Not yet implemented");
	}

	public MetricData deSerialize(Reader data) throws ConversionException
	{
		long start = System.currentTimeMillis();
		br = new BufferedReader(data);
		MetricData md;
		try
		{
			md = md2csv.fromCSV(br);
		} catch (IOException e)
		{
			throw new ConversionException(e.getMessage());
		}
		loadTime = System.currentTimeMillis() - start;
		return md;
	}

	public void serialize(MetricData md, String path)
			throws ConversionException
	{
		long start = System.currentTimeMillis();
		if (path.indexOf(".") == -1)
			path = path + FILE_EXTENSION;
		try
		{
			bw = new BufferedWriter(new FileWriter(path));
			md2csv.toCSV(md, bw);
			storeTime = System.currentTimeMillis() - start;
		} catch (IOException e)
		{
			throw new ConversionException(e.getMessage());
		}
	}

	public String serialize(MetricData md) throws ConversionException
	{
		StringWriter sw = null;
		try
		{
			if (bw != null)
				bw.close();

			long start = System.currentTimeMillis();
			sw = new StringWriter();
			bw = new BufferedWriter(sw);
			md2csv.toCSV(md, bw);
			storeTime = System.currentTimeMillis() - start;
		} catch (IOException e)
		{
			throw new ConversionException(e.getMessage());
		}
		return sw.toString();
	}

	public void update(Observable observable, Object o)
	{
		MetricDataToCSV md = (MetricDataToCSV) observable;
		completion = md.getCompletion();
		setChanged();
		notifyObservers();
	}

	public String getFileExtension()
	{
		return FILE_EXTENSION;
	}

	public long getLoadTime()
	{
		return loadTime;
	}

	public long getStoreTime()
	{
		return storeTime;
	}

	public int getCompletion()
	{
		return completion;
	}

}
