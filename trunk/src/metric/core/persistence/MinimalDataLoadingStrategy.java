package metric.core.persistence;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import metric.core.exception.ConversionException;
import metric.core.model.VersionMetricData;
import metric.core.vocabulary.SerializeType;

public class MinimalDataLoadingStrategy implements DataLoadingStrategy
{
	public Map<Integer, String[]> versions;

	public MinimalDataLoadingStrategy(Map<Integer, String[]> versions)
	{
		this.versions = versions;
	}

	public VersionMetricData getVersion(int rsn)
	{
		// Position 1 stores the filename.
		String filename = versions.get(rsn)[1] + SerializeType.CLASSES.getExt();
		FileReader reader;
		VersionMetricData vmd = null;
		try
		{
			reader = new FileReader(filename);
			MetricDataConverter converter = new CSVConverter(SerializeType.CLASSES);
			vmd = converter.deSerialize(reader);
			converter.close();
			reader.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (ConversionException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return vmd;
	}
}
