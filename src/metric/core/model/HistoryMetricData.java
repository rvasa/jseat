package metric.core.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import metric.core.exception.ConversionException;
import metric.core.exception.ReportException;
import metric.core.persistence.CSVConverter;
import metric.core.persistence.DataLoaderFactory;
import metric.core.persistence.DataLoadingStrategy;
import metric.core.persistence.MetricDataConverter;
import metric.core.report.visitor.ReportVisitor;
import metric.core.vocabulary.History;
import metric.core.vocabulary.LoadType;
import metric.core.vocabulary.SerializeType;
import metric.core.vocabulary.Version;

/**
 * History - The set of versions that make up a systems evolution history.<b/>
 * By default, when a version is loaded (or requested if it has not already been
 * loaded), it is loaded minimally. That is, only with ClassMetric metrics as
 * this is where most processing occurs. So class dependencies and method
 * related data are not loaded and hence not available.<br/> This behavour can
 * be changed by specifying the loading type in the appropriate constructor.
 * 
 * Currently, the two supported types are:<br />
 * LoadType.MINIMAL - Same as default behaviour.<br />
 * LoadType.MAXIMAL - Same as MINIMAL with the addition of class dependences and
 * class method data.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class HistoryMetricData extends MetricData<History>
{
	// Position 0 = VersionName, position 1 = FileLocaiton.
	public Map<Integer, String[]> versions; // = new HashMap<Integer,
	// String[]>();
	private DataLoadingStrategy dataLoader;

	/**
     * Creates a new HistoryMetricData for a set of version mappings. Uses the
     * default <code>LoadType.MINIMAL</code> data loading strategy.
     * 
     * @param productName The name of the product.
     * @param versions A mapping of RSN's to their respective data files.
     */
	public HistoryMetricData(String productName, Map<Integer, String[]> versions)
	{
		properties.put(History.NAME, productName);
		properties.put(History.SHORTNAME, productName);
		setSimpleMetric(History.VERSIONS, versions.size());
		this.versions = versions;

		// Minimal by default
		DataLoaderFactory factory = DataLoaderFactory.getInstance();
		dataLoader = factory.getDataLoader(versions, LoadType.MINIMAL);
	}

	/**
     * Creates a new HistoryMetricData for a set of version mappings with the
     * specified data loading stategy.
     * 
     * @param productName The name of the product.
     * @param versions A mapping of RSN's to their respective data files.
     * @param loadType The data loading strategy this class should use when
     *            loading VersionMetricData.
     */
	public HistoryMetricData(String productName,
			Map<Integer, String[]> versions, LoadType loadType)
	{
		this(productName, versions);

		DataLoaderFactory factory = DataLoaderFactory.getInstance();
		dataLoader = factory.getDataLoader(versions, loadType);
	}

	/**
     * Configures how this class loads VersionMetricData.
     * 
     * @param type Sets the data loading strategy to the type specified.
     */
	public void setLoadType(LoadType type)
	{
		DataLoaderFactory factory = DataLoaderFactory.getInstance();
		dataLoader = factory.getDataLoader(versions, type);
	}

	public int size()
	{
		return versions.size();
	}

	public HistoryMetricData(String productName)
	{
		properties.put(History.NAME, productName);
	}

	public String getPathOf(int rsn)
	{
		// Position 1 is the path to file.
		return versions.get(rsn)[1];
	}

	public String getNameOf(int rsn)
	{
		// Position 0 is the path to file.
		return versions.get(rsn)[0];
	}

	public Set<Map.Entry<Integer, String>> getVersionList()
	{
		Map<Integer, String> ret = new HashMap<Integer, String>();
		Iterator<Entry<Integer, String[]>> it = versions.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<Integer, String[]> entry = it.next();
			// Position 0 stores the name.
			ret.put(entry.getKey(), entry.getValue()[0]);
		}
		return ret.entrySet();
	}

	// TODO Provide some form of caching strategy on the loader side.
	public VersionMetricData getVersion(int rsn)
	{
		return dataLoader.getVersion(rsn);
	}

	public void accept(ReportVisitor visitor) throws ReportException
	{
		visitor.visit(this);
	}
}
