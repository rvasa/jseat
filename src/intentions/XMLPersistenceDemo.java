package intentions;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import metric.core.MetricEngine;
import metric.core.model.HistoryMetricData;
import metric.core.model.MetricData;
import metric.core.persistence.MetricDataConverter;
import metric.core.persistence.XMLConverter;
import metric.core.report.ReportFactory;
import metric.core.report.visitor.ReportVisitor;
import metric.core.util.logging.ConsoleHandler;
import metric.core.util.logging.LogOrganiser;

/**
 * The XMLPersistenceDemo shows how xml persistence is used to store metric model
 * data. By doing so, you can load this metric model data back into the system
 * and generate reports off it, without having to re-load and process the original
 * bytecode.
 * 
 * This is useful for two reasons; You don't have to keep every version of
 * bytecode available after you have processed it and two, the file size of
 * the resultant xml metric model is significantly smaller than that of the
 * bytecode (in some cases).
 * 
 * Varies depending on the size of the bytecode in each version
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 *
 */
public class XMLPersistenceDemo
{
	// Your base directory where your builds are located.
	private static final String BASE_DIR = "B:/workspace/builds/";

	// Path from baseDir to locate versions file.
	private static final String PATH_TO_VERSIONS_FILE = "hibernate/hibernate.ver";

	// Path to output metric model data to.
	private static final String OUTPUT_LOCATION = "B:/workspace/builds/hibernate/hibernate";

	public static void main(String[] args)
	{
		// Add a console handler so we can catch some of the output.
		LogOrganiser.addHandler(new ConsoleHandler());

		try
		{
			// Create a new XML MetricConverter.
			MetricDataConverter mc = new XMLConverter();

			// Do processing and serialization.
			doExampleSerialization(mc);

			// Derserialize xml back into model data.
			HistoryMetricData hmd = doDeSerialization(mc);

			// We are going to create an a report that lists modified classes
			// for each version from a String. The ReportFactory will return us
			// the correct report.
			String report = "5,ClassListReportVisitor,Modified Classes,modified";

			// The ReportFactory returns the Corect ReportVisitor for the
			// specified definition.
			ReportVisitor modifiedClassesReport = ReportFactory.getReport(report);

			// Finally, have the model accept the report.
			hmd.accept(modifiedClassesReport);

			System.out.println("Time spent storing to file: "
					+ (float)mc.getStoreTime() / 1000 + "s");
			System.out.println("Time spent loading from file: "
					+ (float)mc.getLoadTime() / 1000 + "s");

		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void doExampleSerialization(MetricDataConverter mc)
			throws Exception
	{
		// Create a new metric engine.
		MetricEngine me = new MetricEngine(true);
		// Process a versions file to create model.
		MetricData md = me.process(BASE_DIR + PATH_TO_VERSIONS_FILE);

		// Serialize metric model data to file.
		mc.serialize(md, OUTPUT_LOCATION);
	}

	private static HistoryMetricData doDeSerialization(MetricDataConverter mc)
			throws FileNotFoundException, Exception
	{
		// Deserialize the *.mmb file at the outputLocation to a
		// HistoryMetricData object and return it.
		return (HistoryMetricData) mc.deSerialize(new FileReader(
				OUTPUT_LOCATION + ".mmd"));
	}
	
}
