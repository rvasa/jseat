package test;

import java.io.IOException;
import java.util.logging.ConsoleHandler;

import junit.framework.TestCase;
import metric.core.extraction.MetricEngine;
import metric.core.model.HistoryMetricData;
import metric.core.model.VersionMetricData;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Version;

public class VersionMetricDataTest extends TestCase
{
	VersionMetricData vmd;
	HistoryMetricData hmd;

	public void setUp()
	{
		LogOrganiser.addHandler(new ConsoleHandler());
		String baseDir = "b:/workspace/builds/";
		MetricEngine me = new MetricEngine(baseDir + "asm/asm.versions", "testProject", true);
		try
		{
			hmd = me.process();
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (InterruptedException e)
		{
		} // handle
	}

	public void testGetComplexMetric()
	{
		vmd = hmd.getVersion(1);
		// System.out.println(vmd.shortName);
		System.out.println(vmd.getComplexMetric(Version.ALPHA, ClassMetric.FAN_IN_COUNT));
		System.out.println(vmd.getComplexMetric(Version.BETA, ClassMetric.FIELD_COUNT));
		System.out.println(vmd.getComplexMetric(Version.RELATIVE_SIZE_CHANGE, hmd.getVersion(14)));
		// fail("Not yet implemented");
	}

	public void testGetRange()
	{
		vmd = hmd.getVersion(1);
		int[] t1 = vmd.getMetricRange(ClassMetric.FAN_IN_COUNT);
		for (int i : t1)
		{
			System.out.print(i + " ");
		}
	}

}
