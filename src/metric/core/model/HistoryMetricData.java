package metric.core.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import metric.core.exception.ReportException;
import metric.core.report.visitor.ReportVisitor;
import metric.core.vocabulary.History;
import metric.core.vocabulary.Version;

/**
 * History - The set of versions that make up a systems evolution history
 * @author rvasa
 */
public class HistoryMetricData extends MetricData<History>
{
    public Map<Integer, VersionMetricData> versions = new HashMap<Integer, VersionMetricData>();

    public HistoryMetricData(String productName, Map<Integer, VersionMetricData> versions)
    {
    	properties.put(History.NAME, productName);
    	metrics.put(History.VERSIONS, versions.size());
        this.versions = versions;
    }
    
    public HistoryMetricData (String productName)
    {
    	properties.put(History.NAME, productName);
    }
      
    public Collection<VersionMetricData> getVersionList()
    {
        return versions.values();
    }
        
    public void addVersion(VersionMetricData v)
    {
        versions.put(v.getSimpleMetric(Version.RSN), v); // new version
    }
    
    public VersionMetricData getVersion(int rsn)
    {
        return versions.get(rsn);
    }

	public void accept(ReportVisitor visitor) throws ReportException
	{
		visitor.visit(this);
	}
}
