package intentions;

import metric.core.MetricEngine;
import metric.core.model.HistoryMetricData;
import metric.core.report.ReportFactory;
import metric.core.report.decorator.CompositeReportDecorator;
import metric.core.report.decorator.ReportDecorator;
import metric.core.report.decorator.TextDecorator;
import metric.core.report.visitor.ModificationIntensityReportVisitor;
import metric.core.util.logging.ConsoleHandler;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.SupportedFileType;
import metric.gui.swt.core.decorator.ColourMapDecorator;
import metric.gui.swt.core.decorator.ColourMapDecorator.IntensityStyle;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates using the <code>ModificationIntensityReportVisitor</code>
 * with the <code>ColourMapDecorator</code> to provide a hot/cold
 * visualization of change and its respective intensity.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class IntensityVisualizer
{
	private static final String PROJECT_NAME = "groovy";

	// Path to output metric model data to.
	private static final String OUTPUT_LOCATION = "B:/workspace/builds/"
			+ PROJECT_NAME + "/" + PROJECT_NAME;

	public static void main(String[] args)
	{
		Display display = new Display();
		Shell shell = getNewShell(display);
		Shell shell2 = getNewShell(display);
		Shell shell3 = getNewShell(display);

		// Catch logging output.
		LogOrganiser.addHandler(new ConsoleHandler());
		try
		{
			// Create a new MetricEngine.
			MetricEngine me = new MetricEngine(true, 3);
			// Process model data.
			HistoryMetricData hmd = me.process(OUTPUT_LOCATION + SupportedFileType.VERSION.toString());

			// Get a ModificationIntensityReport
			String report = "1,ModificationIntensityReportVisitor,Modification Intensity Report";
			ModificationIntensityReportVisitor rv = (ModificationIntensityReportVisitor) ReportFactory
					.getReport(report);

			// Create a text decorator, heatmap decorator, coolmap decorator and hybridmap decorator
			ReportDecorator textDecorator = new TextDecorator(rv);
			ReportDecorator coolDecorator = new ColourMapDecorator(rv, shell, IntensityStyle.CoolMap);
			ReportDecorator heatDecorator = new ColourMapDecorator(rv, shell2, IntensityStyle.HeatMap);
			ReportDecorator hybridDecorator = new ColourMapDecorator(rv, shell3, IntensityStyle.HybridMap);

			// Add them all to a composite report decorator.
			CompositeReportDecorator compositeDecorator = new CompositeReportDecorator(rv);
			compositeDecorator.add(textDecorator);
			compositeDecorator.add(coolDecorator);
			compositeDecorator.add(heatDecorator);
			compositeDecorator.add(hybridDecorator);

			// Have model accept report.
			hmd.accept(compositeDecorator);
			
			
			// Standard event loop for our 3 shells.
			shell.open();
			shell2.open();
			shell3.open();
			while (!shell.isDisposed())
			{
				while (!display.readAndDispatch())
					display.sleep();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static Shell getNewShell(Display display)
	{
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setSize(800, 600);
		return shell;
	}
}
