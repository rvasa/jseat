package metric.core.persistence;

import metric.core.model.VersionMetricData;

public interface DataLoadingStrategy
{
	public VersionMetricData getVersion(int rsn);
}
