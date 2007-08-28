package metric.core.persistence;

import java.util.Iterator;

import metric.core.model.VersionMetricData;

public interface DataLoadingStrategy
{
	public VersionMetricData getVersion(int rsn);
}
