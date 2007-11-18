package metric;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;

import metric.core.Project;
import metric.core.ReportDefinition;
import metric.core.ReportDefinitionRepository;
import metric.core.exception.ReportException;
import metric.core.model.HistoryMetricData;
import metric.core.report.ReportFactory;
import metric.core.report.decorator.ReportDecorator;
import metric.core.report.decorator.TextDecorator;
import metric.core.report.visitor.ReportVisitor;
import metric.core.util.logging.ConsoleHandler;
import metric.core.util.logging.LogOrganiser;
import metric.gui.swt.core.decorator.BarChartDecorator;
import metric.gui.swt.core.decorator.GraphicalDecorator;

/**
 * Basic Console program for producing JSeat charts and graphs to file.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class JSeatCharter
{
	/**
     * Example usage: -i input.jpf -r default.rep -s picture_size -n filename reportNum decoratorNum
     * JSeatReport -i groovy.jpf -r default.rep -s 800x600 -n test.png 200 1
     */
	private static int width = 400;
	private static int height = 400;
	
	public static void main(String[] args)
	{
		String input = null, report = null, size = null, filename = null;
		int reportNumber = -1, decoratorNumber = -1;
		// Add a console handler so we can listen to output.
		LogOrganiser.addHandler(new ConsoleHandler());

		if (args.length != 10)
			System.err.println("Invalid arguments provided.");

		try
		{
			if (args[0].equals("-i"))
				input = args[1];
			else
			{
				System.err.println("Invalid argument specified. Expected [-i]");
				System.exit(1);
			}
			if (args[2].equals("-r"))
				report = args[3];
			else
			{
				System.err.println("Invalid argument specified. Expected [-r]");
				System.exit(1);
			}
			if (args[4].equals("-s"))
			{
				size = args[5];
				int index = size.indexOf("x");
				try
				{
					JSeatCharter.width = Integer.parseInt(size.substring(0, index));
					JSeatCharter.height = Integer.parseInt(size.substring(index+1, size.length()));
				}
				catch (NumberFormatException e)
				{
					System.err.println("Invalid size specified. Expected [400x400]");
					System.exit(1);
				}
			}
			else
			{
				System.err.println("Invalid argument specified. Expected [-s]");
				System.exit(1);
			}
			if (args[6].equals("-n"))
				filename = args[7];
			else
			{
				System.err.println("Invalid argument specified. Expected [-n]");
				System.exit(1);
			}

			try
			{
				reportNumber = Integer.parseInt(args[8]);
			}catch (Exception e)
			{
				System.err.println("Report number either not specified or is invalid");
				System.exit(1);
			}
			try
			{
				decoratorNumber = Integer.parseInt(args[9]);
			}catch (Exception e)
			{
				System.err.println("Chart number either not specified or is invalid");
				System.exit(1);
			}
		} catch (Exception e)
		{
			System.err.println("One or more invalid arguments specified.");
			System.exit(1);
		}

		// If all arguments have been provided, run the report.
		if (input != null && report != null && filename != null && reportNumber != -1 && decoratorNumber != -1)
		{
			
			runReport(input, report, filename, reportNumber, decoratorNumber);
		}
	}
	
	/**
     * Runs a report on a project.
     * 
     * @param input The JSeat project.
     * @param report The report file.
     * @param reportNumber The report number from the report file.
     */
	private static void runReport(String input, String report, String filename, int reportNumber, int decoratorNumber)
	{
		Project p = new Project(input);
		HistoryMetricData hmd = p.build();
		if (hmd != null)
		{
			ReportDefinitionRepository rdr = new ReportDefinitionRepository(report);
			ReportDefinition definition = rdr.getDefinition(reportNumber);
			
			try
			{
				ReportVisitor rv = ReportFactory.getReport(definition);
				GraphicalDecorator rd = getDecorator(rv, decoratorNumber);
				TextDecorator td = new TextDecorator(rd);
				hmd.accept(td);
				
				File f = new File(filename);
		         try {
					ChartUtilities.saveChartAsJPEG(f, rd.getChart(), width,height);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (ReportException e)
			{
				System.err.println(e.getMessage());
			}
		} else
		{
			System.err.println("Unable to load project. Bad project file or project data?");
		}
	}

	private static GraphicalDecorator getDecorator(ReportVisitor rv, int decoratorNumber)
	{
		GraphicalDecorator rd = null;
		switch(decoratorNumber)
		{
			case 1:
			{
				rd = new BarChartDecorator(rv);
			}
		}
		return rd;
	}
}
