package intentions;

import metric.core.MetricEngine;
import metric.core.model.HistoryMetricData;
import metric.core.report.ReportFactory;
import metric.core.report.decorator.CompositeReportDecorator;
import metric.core.report.decorator.ReportDecorator;
import metric.core.report.decorator.TextDecorator;
import metric.core.report.visitor.ReportVisitor;
import metric.core.util.logging.ConsoleHandler;
import metric.core.util.logging.LogOrganiser;
import metric.gui.swt.core.decorator.BarChartDecorator;
import metric.gui.swt.core.decorator.LineChartDecorator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * The ReportDecoratorDemo shows how ReportDecorators are used to "decorate"
 * ReportVisitors. This allows a Reports representation to change independent of
 * the report itself.
 * 
 * The basic setup is:
 * 
 * Create ReportDefinition. Instantiate ReportVisitor with respective
 * ReportDefinition. Instantiate appropriate ReportDecorator. Have the intended
 * MetricData accept the ReportDecorator.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 * 
 */
public class ReportDecoratorDemo
{
	// Path to the version file we want to load.
	private static final String PATH_TO_VERSIONS_FILE = "B:/workspace/builds/groovy/groovy.ver";
	private static final String TITLE = "JFreeChart running with SWT - Demonstrating Report Decorators";

	public static void main(String[] args)
	{
		// Setup display and shells.
		Display display = new Display();
		Shell shell = getNewShell(display, TITLE);
		Shell shell2 = getNewShell(display, TITLE);

		// Add a console handler so we can catch some of the output.
		LogOrganiser.addHandler(new ConsoleHandler());

		try
		{
			// We are going to create two reports, having the ReportFactory
			// return the appropriate report for each.
			String report = "62,EvolutionReportVisitor,Evolution Analysis (Modified),"
					+ "modified,[name,branch_count,layer,fan_in_count,fan_out_count]";
			String pReport = "51,PredictionReportVisitor,Prediction Analysis"
					+ " (General),[method_count,field_count,interface_count]";

			// The ReportFactory returns the Corect ReportVisitor for the
			// specified definition.
			ReportVisitor modifiedClassesReport = (ReportVisitor) ReportFactory
					.getReport(report);
			ReportVisitor predictionReport = (ReportVisitor) ReportFactory
					.getReport(pReport);

			// Create a basic text decorator, bar chart decorator and line
			// decorator.
			ReportDecorator textDecorator = new TextDecorator(predictionReport);

			ReportDecorator barDecorator = new BarChartDecorator(
					predictionReport, shell);

			ReportDecorator lineDecorator = new LineChartDecorator(
					predictionReport, shell);
			// Put them in a composite decorator.
			CompositeReportDecorator crd1 = new CompositeReportDecorator(
					predictionReport);
			crd1.add(textDecorator);
			crd1.add(barDecorator);
			crd1.add(lineDecorator);

			// Create a basic text decorator, bar chart decorator and line
			// decorator.
			ReportDecorator barDecorator2 = new BarChartDecorator(
					modifiedClassesReport, shell2);

			ReportDecorator lineDecorator2 = new LineChartDecorator(
					modifiedClassesReport, shell2);
			// Put them in a composite decorator.
			CompositeReportDecorator crd2 = new CompositeReportDecorator(
					modifiedClassesReport);
			crd2.add(textDecorator);
			crd2.add(barDecorator2);
			crd2.add(lineDecorator2);

			// Setup a new MetricEngine to process our versions file.
			MetricEngine me = new MetricEngine(true);
			// Create metric model.
			HistoryMetricData hmd = me.process(PATH_TO_VERSIONS_FILE);

			// Finally, have the model accept the composite decorator (which is
			// actually three decorators; the TextDecorator,
			// BarChartDecorator and LineChartDecorator).
			hmd.accept(crd1);
			// This time, with a different report.
			hmd.accept(crd2);

		} catch (Exception e)
		{
			e.printStackTrace();
		}

		// Open shells.
		shell.open();
		shell2.open();

		// Standard event loop.
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	private static Shell getNewShell(Display display, String title)
	{
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		shell.setSize(1024, 768);
		shell.setText(title);
		return shell;
	}
}
