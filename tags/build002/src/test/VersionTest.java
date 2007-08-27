package test;

import junit.framework.TestCase;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Version;

public class VersionTest extends TestCase
{

	public void testParseVersion()
	{
		assertEquals(Version.GUI_CLASS_COUNT, Version.parse("gui_class_count"));
		
		assertEquals(Version.BETA, Version.parse("beta"));
		
		assertEquals(Version.ALPHA, Version.parse("alpha"));
		
		assertEquals(ClassMetric.FAN_IN_COUNT, ClassMetric.parse("fan_in_count"));
		assertEquals(ClassMetric.FAN_OUT_COUNT, ClassMetric.parse("fan_out_count"));
	}

}
