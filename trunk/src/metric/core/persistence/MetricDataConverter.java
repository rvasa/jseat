package metric.core.persistence;

import java.io.IOException;
import java.io.Reader;
import java.util.Observer;

import metric.core.exception.ConversionException;
import metric.core.model.VersionMetricData;

/**
 * A contract for converting to and from <code>VersionMetricData</code>.
 * 
 * A concrete converter should provide an implementation for both serialization
 * and de-serialization. This is the main conversion interface intended to be
 * used by callers serializing and de-serializing VersionMetricData.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public interface MetricDataConverter
{
	public void serialize(VersionMetricData component, String path) throws ConversionException;

	public String serialize(VersionMetricData component) throws ConversionException;

	public VersionMetricData deSerialize(String data) throws ConversionException;

	public VersionMetricData deSerialize(Reader data) throws ConversionException;

	public String getFileExtension();

	/**
     * @return Time spent serializing to file.
     */
	public long getStoreTime();

	/**
     * @return Time spent de-serializing from file.
     */
	public long getLoadTime();

	/**
     * @return The current completion state of the converter.
     */
	public int getCompletion();

	/**
     * Closes any open streams.
     * 
     * @throws IOException
     */
	public void close() throws IOException;

	/**
     * Adds an observer to the MetricConverter. Can be used for example, to
     * notifiy interested observers of completion status changes during
     * serialization or de-serialization of a file.
     * 
     * @param o The Observer that should be notified.
     */
	public void addObserver(Observer o);
}
