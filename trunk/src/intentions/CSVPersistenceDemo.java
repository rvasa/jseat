package intentions;

import metric.core.report.ReportFactory;
import metric.core.report.visitor.ReportVisitor;
import metric.core.util.logging.ConsoleHandler;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.JSeatFileType;

/**
 * The CSVPersistenceDemo shows how csv persistence is used to store metric model
 * data. By doing so, you can load this metric model data back into the system
 * and generate reports off it, without having to re-load and process the original
 * bytecode.
 * 
 * This is useful for two reasons; You don't have to keep every version of
 * bytecode available after you have processed it and two, the file size of
 * the resultant csv metric model is usually smaller than that of the
 * bytecode (in some cases).
 * 
 * Varies depending on the size of the bytecode in each version
 * 
 * Regardless of size, CSV metric model data is faster to process than
 * re-extracting the bytecode.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 *
 */
public class CSVPersistenceDemo
{
	private static final String PROJECT_NAME = "hibernate";
	// Your base directory where your builds are located.
	private static final String BASE_DIR = "B:/workspace/builds/";

	// Path from baseDir to locate versions file.
	private static final String PATH_TO_VERSIONS_FILE = PROJECT_NAME + "/"
			+ PROJECT_NAME + JSeatFileType.VERSION.toString();

	// Path to output metric model data to.
	private static final String OUTPUT_LOCATION = "B:/workspace/builds/"
			+ PROJECT_NAME + "/" + PROJECT_NAME;

	public static void main(String[] args) throws Exception
	{
		// Add a console handler so we can catch some of the output.
		LogOrganiser.addHandler(new ConsoleHandler());

		// Create a new CSV MetricConverter.
//		MetricDataConverter mc = new CSVConverter();

		// Do processing and serialization.
//		doExampleSerialization(mc);

		// Do extraction/de-serialization.
//		HistoryMetricData hmd = doDeSerialization(mc);

		// We are going to create an a report that lists modified classes
		// for each version from a String. The ReportFactory will return us
		// the correct report.
		String report = "5,ClassListReportVisitor,Modified Classes,modified";

		// The ReportFactory returns the Corect ReportVisitor for the
		// specified definition.
		ReportVisitor modifiedClassesReport = ReportFactory.getReport(report);

		// Finally, have the model accept the report.
//		hmd.accept(modifiedClassesReport);
//
//		System.out.println("Time spent storing to file: "
//				+ (float) mc.getStoreTime() / 1000 + "s");
//		System.out.println("Time spent loading from file: "
//				+ (float) mc.getLoadTime() / 1000 + "s");
	}

//	private static void doExampleSerialization(MetricDataConverter mc)
//			throws Exception
//	{
//		// Create a new metric engine.
//		MetricEngine me = new MetricEngine(BASE_DIR + PATH_TO_VERSIONS_FILE, "testProject", 3, true);
//		// Process a versions file to create model.
//		MetricData md = me.process();
//
//		// Serialize metric model data to file.
////		mc.serialize(md, OUTPUT_LOCATION);
////		mc.close();
//		// System.out.println(mc.serialize(md));
//		// mc.close();
//	}

//	private static HistoryMetricData doDeSerialization(MetricDataConverter mc)
//			throws FileNotFoundException, Exception
//	{
		// Deserialize the *.mmd file at the outputLocation to a
		// HistoryMetricData object and return it.
//		return (HistoryMetricData) mc.deSerialize(new FileReader(
//				OUTPUT_LOCATION + mc.getFileExtension()));
//	}
}