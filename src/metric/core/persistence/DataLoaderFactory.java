package metric.core.persistence;

import java.util.Map;

import metric.core.vocabulary.LoadType;

public class DataLoaderFactory
{
	private static DataLoaderFactory loader;

	public static DataLoaderFactory getInstance()
	{
		if (loader == null)
			loader = new DataLoaderFactory();
		return loader;
	}

	public DataLoadingStrategy getDataLoader(Map<Integer, String[]> versions, LoadType type)
	{
		if (type == LoadType.MINIMAL)
		{
			return new MinimalDataLoadingStrategy(versions);
		} else if (type == LoadType.MAXIMAL)
		{
			return new MaximalDataLoadingStrategy(versions);
		}
		return null;
	}

}
