package metric.core.persistence;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.Observable;
import java.util.Observer;

import metric.core.exception.ConversionException;
import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.MethodMetricMap;
import metric.core.model.MetricData;
import metric.core.model.VersionMetricData;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.History;
import metric.core.vocabulary.MethodMetric;
import metric.core.vocabulary.Version;

import com.thoughtworks.xstream.XStream;

/**
 * The XMLConverter provides an XStream XML interface for converting XML files.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class XMLConverter extends Observable implements MetricDataConverter,
		Observer
{
	private static final String FILE_EXTENSION = ".xmd";
	private XStream xStream;
	private FileOutputStream out;
	private MetricDataToXML md2xml;
	private long storeTime;
	private long loadTime;

	private int completion;

	public XMLConverter()
	{
		xStream = new XStream();
		xStream.alias("ClassMetricData", ClassMetricData.class);
		xStream.alias("HistoryMetricData", HistoryMetricData.class);
		xStream.alias("VersionMetricData", VersionMetricData.class);
		 xStream.alias("MethodMetricData", MethodMetricMap.class);
		xStream.alias("History", History.class);
		xStream.alias("Version", Version.class);
		xStream.alias("ClassMetric", ClassMetric.class);
		xStream.alias("Method", MethodMetric.class);
		registerConverter();
	}

	/**
     * Registers the converter used with XStream.
     */
	private void registerConverter()
	{
		md2xml = new MetricDataToXML();
		md2xml.addObserver(this);
		xStream.registerConverter(md2xml);
	}

	/**
     * Serializes the specified component to xml, allowing a path to be
     * specified.
     * 
     * @param component - The IComponent to serialize.
     * @param path - The path to use when serialize the specified application.
     */
	public void serialize(MetricData component, String path) throws ConversionException
	{
		long start = System.currentTimeMillis();
		if (component != null)
		{
			if (path.indexOf(".") == -1)
				path = path + FILE_EXTENSION;
			try
			{
				out = new FileOutputStream(path);
			} catch (FileNotFoundException e)
			{
				throw new ConversionException(e.getMessage());
			}
			xStream.toXML(component, out);
		}
		storeTime = System.currentTimeMillis() - start;

	}

	/**
     * Serializes the specified component to a String
     * 
     * @param component - The IComponent to serialize.
     */
	public String serialize(MetricData component) throws ConversionException
	{
		long start = System.currentTimeMillis();
		if (component != null)
		{
			return xStream.toXML(component);
		}
		storeTime = System.currentTimeMillis() - start;
		return null;
	}

	/**
     * Deserializes the specified data using a previously specified converter,
     * returning the constructed <code>IComponent</code>.
     */
	public MetricData deSerialize(String data) throws ConversionException
	{
		long start = System.currentTimeMillis();
		MetricData md = (MetricData) xStream.fromXML(data);
		loadTime = System.currentTimeMillis() - start;
		return md;
	}

	public MetricData deSerialize(Reader data) throws ConversionException
	{
		long start = System.currentTimeMillis();
		MetricData md = (MetricData) xStream.fromXML(data);
		loadTime = System.currentTimeMillis() - start;
		return md;
	}

	public void close() throws IOException
	{
		if (out != null)
		{
			out.close();
		}
	}

	public String getFileExtension()
	{
		return FILE_EXTENSION;
	}

	/**
     * @return the storeTime
     */
	public final long getStoreTime()
	{
		return storeTime;
	}

	/**
     * @return the loadTime
     */
	public final long getLoadTime()
	{
		return loadTime;
	}

	public int getCompletion()
	{
		return completion;
	}

	public void update(Observable observable, Object o)
	{
		MetricDataToXML md = (MetricDataToXML) observable;
		completion = md.getCompletion();
		setChanged();
		notifyObservers();
	}
}
