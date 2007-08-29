package metric.core.persistence;

import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import metric.core.model.ClassMetricData;
import metric.core.model.VersionMetricData;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.SerializeType;

public class MaximalDataLoadingStrategy implements DataLoadingStrategy
{
	private Logger logger = Logger.getLogger(getClass().getSimpleName());
	private Map<Integer, String[]> versions;

	public MaximalDataLoadingStrategy(Map<Integer, String[]> versions)
	{
		this.versions = versions;
		LogOrganiser.addLogger(logger);
		logger.setLevel(Level.ALL);
	}

	public VersionMetricData getVersion(int rsn)
	{
		// Position 1 stores the filename.
		VersionMetricData vmd = null, vmdDependencies = null;
		try
		{
			String file = versions.get(rsn)[1];
			// Load up classes.
			FileReader reader = new FileReader(file + SerializeType.CLASSES.getExt());
			MetricDataConverter classConverter = new CSVConverter(
					SerializeType.CLASSES);
			vmd = classConverter.deSerialize(reader);
			reader.close();
			classConverter.close();

			// Load up dependencies.
			reader = new FileReader(file + SerializeType.DEPENDENCIES.getExt());
			MetricDataConverter dependencyConverter = new CSVConverter(
					SerializeType.DEPENDENCIES);
			vmdDependencies = dependencyConverter.deSerialize(reader);
			dependencyConverter.close();

			// Merge dependencies with classes.
			for (ClassMetricData clazz : vmd.metricData.values())
			{
				try
				{
				clazz.dependencies = vmdDependencies.metricData.get(clazz
						.get(ClassMetric.NAME)).dependencies;
				} catch (NullPointerException e)
				{
					logger.log(Level.SEVERE, "A series error has occured when trying to de-serialize dependency data");
					System.exit(1);
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage());
		}
		return vmd;
	}

	public boolean hasNext()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public VersionMetricData next()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void remove()
	{
		// TODO Auto-generated method stub
		
	}
}
