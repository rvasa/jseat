package test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for test");
		//$JUnit-BEGIN$
		suite.addTestSuite(MetricDataTest.class);
		suite.addTestSuite(VersionMetricDataTest.class);
		suite.addTestSuite(EnumTableTest.class);
//		suite.addTestSuite(FileUtilTest.class);
		suite.addTestSuite(ReportDefinitionRepositoryTest.class);
		suite.addTestSuite(MetricTableTest.class);
		suite.addTestSuite(VersionTest.class);
		//$JUnit-END$
		return suite;
	}

}
