package metric.core.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import metric.core.exception.ConversionException;
import metric.core.model.VersionMetricData;
import metric.core.util.SimpleWorkTimer;

public interface MetricDataStrategy
{
	public void to(VersionMetricData md, BufferedWriter bw, SimpleWorkTimer workTimer) throws ConversionException;

	public VersionMetricData from(BufferedReader br, SimpleWorkTimer workTimer) throws ConversionException;
}
