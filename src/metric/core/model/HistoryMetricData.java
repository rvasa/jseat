package metric.core.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import metric.core.exception.ReportException;
import metric.core.persistence.DataLoaderFactory;
import metric.core.persistence.DataLoadingStrategy;
import metric.core.report.visitor.ReportVisitor;
import metric.core.vocabulary.History;
import metric.core.vocabulary.LoadType;

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
	public HistoryMetricData(String productName, Map<Integer, String[]> versions, LoadType loadType)
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

	/**
     * The path of the version matching the specified version. It first assumes
     * an absolute version number, if this is not found it will try to retrieve
     * the relative position of the requested version in the history data set.
     * 
     * @param version The version to retrieve (absolute or relative)
     * @return The VersionMetricData
     */
	public String getPathOf(int version)
	{
		// Position 1 is the path to file.
		if (versions.containsKey(version))
			return versions.get(version)[1];
		return versions.get(findActualRSN(version))[1];
	}

	/**
     * The name of the version matching the specified version. It first assumes
     * an absolute version number, if this is not found it will try to retrieve
     * the relative position of the requested version in the history data set.
     * 
     * @param version The version to retrieve (absolute or relative)
     * @return The VersionMetricData
     */
	public String getNameOf(int version)
	{
		// Position 0 is the name of file.
		if (versions.containsKey(version))
			return versions.get(version)[0];
		return versions.get(findActualRSN(version))[0];
	}

	/**
     * A map of all the version release numbers and respective version names in
     * this histories data set.
     * 
     * @return The Map<Integer, String>
     */
	public Set<Map.Entry<Integer, String>> getVersions()
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

	/**
     * Returns the version matching the specified version. It first assumes an
     * absolute version number, if this is not found it will try to retrieve the
     * relative position of the requested version in the history data set.
     * 
     * @param version The version to retrieve (absolute or relative)
     * @return The VersionMetricData
     */
	public VersionMetricData getVersion(int version)
	{
		if (versions.containsKey(version))
			return dataLoader.getVersion(version);
		return dataLoader.getVersion(findActualRSN(version));
	}

	/**
     * Finds the actual RSN of the VersionMetricData
     * 
     * @param version Which version to retrieve.
     * @return The RSN of the version.
     */
	public int findActualRSN(int version)
	{
		Iterator<Entry<Integer, String[]>> it = versions.entrySet().iterator();
		int index = 1;
		while (it.hasNext())
		{
			Entry<Integer, String[]> entry = it.next();
			if (index == version)
				return entry.getKey();
			index++;
		}
		return -1; // Not found
	}

	public void accept(ReportVisitor visitor) throws ReportException
	{
		visitor.visit(this);
	}
}
