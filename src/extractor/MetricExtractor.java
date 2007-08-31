package extractor;

import java.io.File;
import java.io.IOException;

/**
 * This class will extract some basic metrics from the byte-code MetricExtractor
 * 
 * @author rvasa
 */
public class MetricExtractor
{
	private static String BASE_DIR = "B:/workspace/builds/";
	private static boolean showProcessing = false;

	private static String[] om = {"rawSize", "bornRSN", "distanceMovedSinceBirth", "branchCount", 
		"fanInCount", "fanOutCount", "interfaceCount", "methodCount", 
			"loadCount", "storeCount", 
			"fieldCount", "methodCallCount", "internalFanOutCount"};
	
	public static void main(String[] args) throws Exception
	{
//		processVersionData("asm/asm.ver");

//		processVersionData("axis/axis.versions");
//		processVersionData("Azureus/azureus.versions");
//		processVersionData("castor/castor.versions");
//		processVersionData("findbugs/findbugs.versions");
//		processVersionData("jung/jung.versions");
//		processVersionData("spring/spring.versions");
//		processVersionData("struts/struts.versions");
//		processVersionData("webwork/webwork.versions");
//		processVersionData("wicket/wicket.versions");

		// These have machine generated parsers
//		processVersionData("checkstyle/checkstyle.versions");
//		processVersionData("hibernate/hibernate.versions");
		processVersionData("groovy/groovy.ver");
//		processVersionData("velocity/velocity.versions");
//		processVersionData("pmd/pmd.versions");
//		processVersionData("proguard/proguard.versions");
//		processVersionData("freemarker/freemarker.versions");

//		processVersionData("saxon/saxon.versions");		
//		processVersionData("lucene/lucene.versions");		
//		processVersionData("acegi/acegi.versions");
//		processVersionData("activeBPEL/activeBPEL.versions");
//		processVersionData("ant/ant.versions");
//		processVersionData("columba/columba.versions");
//		processVersionData("compass/compass.versions");
//		processVersionData("cocoon/cocoon.versions");
//		processVersionData("datavision/datavision.versions");
//		processVersionData("flow4j/flow4j.versions");
//		processVersionData("hsql-db/hsqldb.versions");
//		processVersionData("iText/iText.versions");
//		processVersionData("jabref/jabref.versions");		
//		processVersionData("jameleon/jameleon.versions"); // has not doubled in size yet 160 - 219
//		processVersionData("jasperreports/jasperreports.versions");
//		processVersionData("jena/jena.versions");
//		processVersionData("javolution/javolution.versions"); // nearly doubled 153 - 269
//		processVersionData("jetty/jetty.versions");
//		processVersionData("jgroups/jgroups.versions");
//		processVersionData("jmeter/jmeter.versions");
//		processVersionData("kolmafia/kolmafia.versions");
//		processVersionData("log4j/log4j.versions");
//		processVersionData("quartz/quartz.versions");  // has not doubled in size yet 115 - 223
//		processVersionData("rssowl/rssowl.versions"); // use swt inside their jar, ignore for now
//		processVersionData("tapestry/tapestry.versions");
//		processVersionData("xalan/xalan.versions");
//		processVersionData("xerces/xerces.versions");
//		processVersionData("xwork/xwork.versions");
		
//		processVersionData("activemq/activemq.versions");  // must remove libraries in core later
//		processVersionData("bfopdf/bfopdf.versions");  // commercial license
//		processVersionData("bforeport/bforeport.versions");  // commercial license
//		processVersionData("jchempaint/jchempaint.versions"); // goes from 3366 to 5533 classes
		
		//27-July-2007 46 versions data, 4 on the edge
		// TODO Next
		// jfreechart
	}

	private static void processVersionData(String fn) throws Exception
	{
		String fileName = BASE_DIR + fn;
		Version.showProcessing = showProcessing;
		if (showProcessing) System.out.println("Processing: " + fileName);
		long startTime = System.currentTimeMillis();
		History h = new History(new File(fileName));
		h.extractMetrics();
		long bytesProcessed = h.extractMetrics(); // extract for all versions
													 
		long mbProcessed = bytesProcessed >> 20;
		long endTime = System.currentTimeMillis();
		if (showProcessing)
		{
			System.out.printf("Processed %s (%d versions, %4dMb) in %4.1f seconds\n", h.name,
					h.getVersionList().size(), mbProcessed,
					(endTime - startTime) / 1000.0);
		}

//		h.printRawCounts(true);
//		h.printModifiedDistance();
		//h.printUnchangedClassList();
		//h.printModifiedClassList();
		//h.printLayerCounts(true);
//		h.printBeta(true);
		//h.printPredictions(true);
//		h.printAlpha(true);
//		h.printFreq(10, "typeConstructionCount", true);
//		h.printFreq(50, "fanInCount", false);
		//h.printFreq(11, "evolutionDistance", true);
//		h.printFreq(12, "localVarCount", true);
//		h.printFreq(20, "fanInCount", true);
		//h.printFreq(12, "storeFieldCount", true);
		//h.printFreq(5, "distanceMovedSinceBirth", false);
//		h.printFreq(11, "loadRatio", true);
//		h.printFreq(100, "loadCount", true);
//		h.printStats("fanInCount",true);
//		h.printFreq(25, "modifiedMetricCount", false);
//		h.printFreq(3, "modificationStatusSinceBirth", true);
//		h.printConstrainedFreq(10, "distanceMovedSinceBirth", true, "modificationStatusSinceBirth", ClassMetric.ECAT_NEVER_MODIFIED, ClassMetric.ECAT_MODIFIED_AFTER_BIRTH);
//		h.printConstrainedFreq(26, "modificationFrequency", true, "modificationStatusSinceBirth", ClassMetric.ECAT_MODIFIED_AFTER_BIRTH, ClassMetric.ECAT_MODIFIED_AFTER_BIRTH);
//		h.printConstrainedFreq(20, "distanceMovedSinceBirth", true, "modificationStatusSinceBirth", ClassMetric.ECAT_MODIFIED_AFTER_BIRTH, ClassMetric.ECAT_MODIFIED_AFTER_BIRTH);
//		h.printConstrainedFreq(10, "methodCount", true, "modificationStatusSinceBirth", ClassMetric.ECAT_NEVER_MODIFIED, ClassMetric.ECAT_NEVER_MODIFIED);
//		h.printConstrainedFreq(47, "modifiedMetricCountSinceBirth", true, "modificationStatusSinceBirth", ClassMetric.ECAT_MODIFIED_AFTER_BIRTH, ClassMetric.ECAT_MODIFIED_AFTER_BIRTH);
//		h.printConstrainedFreq(5, "bornRSN", true, "evolutionStatus", ClassMetric.ECAT_MODIFIED, ClassMetric.ECAT_MODIFIED);
//		h.printConstrainedFreq(10, "modifiedMetricCount", true, "evolutionStatus", ClassMetric.ECAT_MODIFIED, ClassMetric.ECAT_MODIFIED);
//		h.printConstrainedFreq(20, "modifiedMetricCountSinceBirth", true, "evolutionStatus", ClassMetric.ECAT_MODIFIED, ClassMetric.ECAT_MODIFIED);
//		h.printFreq(4, "evolutionStatus", true);
//		h.printConstrainedFreq(10, "normalizedBranchCount", true, "nextVersionStatus", ClassMetric.ECAT_MODIFIED, ClassMetric.ECAT_MODIFIED);
//		h.printConstrainedFreq(10, "normalizedBranchCount", true, "nextVersionStatus", ClassMetric.ECAT_UNCHANGED, ClassMetric.ECAT_UNCHANGED);		
//		h.printConstrainedFreq(6, "fanInCount", true, "evolutionStatus", ClassMetric.ECAT_NEW, ClassMetric.ECAT_NEW);
//		h.printCummulFreq(45, "age");
//		h.printMetricHistory("rawSize");
		//h.printMetricHistory("distanceMovedSinceBirth");
		//h.printHistory();
//		h.printGiniCoeff("fieldCount");
		h.printGiniCoeff(new String[] {"methodCount"});
//		h.printGiniCoeff("storeCount");
//		h.printGiniCoeff("loadCount");
//		h.printGiniCoeff("fanOutCount");
//		h.printGiniCoeff("branchCount");
//		h.printGiniCoeff("fanInCount");
//		h.printGiniCoeff("methodCount");
//		h.printGiniCoeff("methodCallCount");	
//		h.printGiniCoeff("publicMethodCount");
//		h.printGiniCoeff("fieldCount");

//		h.printMetricHistory("branchCount");

		String [] fieldNames = {"typeConstructionCount", "methodCallCount", "fieldCount", "publicMethodCount", "methodCount", "fanInCount", "fanOutCount", "branchCount", "loadCount", "storeCount"};
//		h.printGiniCoeff(fieldNames);
		

//		h.printLayerFreq(true);
//		h.printDistances("fanOutCount", 30, false);
//		h.printDistances("fanInCount", 100, false);
		//h.printDistances("branchCount", 30, false);
		//h.printDistances("methodCount", 30, false);
		
		//h.printClones();
//		h.printSurvivors();
		//h.printFreq(30, "evolutionDistance", true);
		//h.printFreq(20, "bornRSN",	 false);
		//h.printFreq(4, "nextVersionStatus", true);
		//h.printFreq(4, "evolutionStatus", true);
		//h.printMetricHistory("instability");
		//h.printFreq(5, "age", true);
//		h.printCorrelations("bornRSN", om);
//		h.printConstrainedCorrelations("distanceMovedSinceBirth", om, "evolutionStatus", ClassMetric.ECAT_MODIFIED, ClassMetric.ECAT_MODIFIED);
//		h.printConstrainedCorrelations("distanceMovedSinceBirth", om, "modificationStatusSinceBirth", ClassMetric.ECAT_MODIFIED_AFTER_BIRTH, ClassMetric.ECAT_MODIFIED_AFTER_BIRTH);
//		h.printConstrainedCorrelations("loadCount", om, "modificationStatusSinceBirth", ClassMetric.ECAT_MODIFIED_AFTER_BIRTH, ClassMetric.ECAT_MODIFIED_AFTER_BIRTH);
		//h.printMetrics(15);
		//System.gc();
	}
}
