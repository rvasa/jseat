package metric;

import metric.core.MetricEngine;
import metric.core.ReportDefinitionRepository;
import metric.core.model.HistoryMetricData;
import metric.core.report.ReportFactory;
import metric.core.report.decorator.TextDecorator;
import metric.core.report.visitor.ReportVisitor;
import metric.core.util.logging.ConsoleHandler;
import metric.core.util.logging.LogOrganiser;

/**
 * Basic Console program for running, extracting and printing metrics.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class JSeatExtractor
{
	/**
     * -f: Filename -v: Verbose output -p: Property file.
     * Example: JSeatExtractor -f b:/workspace/builds/groovy/groovy.ver -r default.rep -p 5
     */
	public static void main(String[] args)
	{
		String filename = "";
		String report = "";
		int reportNumber = 1;
		
		//TODO Rewrite this.
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-f"))
			{
				if (args.length >= i + 2)
				{
					filename = args[++i];
				} else
					System.err
							.println("You must specify a filename after the -f command.");
			}
			else if (args[i].equals("-r"))
			{
				if (args.length >= i + 2)
				{
					report = args[++i];
				} else
					System.err
							.println("You must specify a filename after the -r command.");
			}
			else if (args[i].equals("-p"))
			{
				if (args.length >= i + 2)
				{
					reportNumber = Integer.parseInt(args[++i]);
				} else
					System.err
							.println("You must specify a number after the -p command.");
			}
		}

		try
		{
			// Add a console handler so we can listen to output
			LogOrganiser.addHandler(new ConsoleHandler());

			// Create a new metric engine.
			MetricEngine me = new MetricEngine(true, 3);
			// Process a versions file.
			HistoryMetricData hmd = me.process(filename);

			// Setup the ReportDefinitionRepository
			ReportDefinitionRepository mdr = new ReportDefinitionRepository(
					report);

			// Create report from number 16.
			// TODO Should be user specified def.
			ReportVisitor rv = ReportFactory.getReport(mdr.getDefinition(reportNumber));
			// Decorate report with a basic TextDecorator.
			TextDecorator td = new TextDecorator(rv);
			hmd.accept(td);

		} catch (Exception e)
		{
			//TODO Handle exceptions, print appropriate messages etc.
			e.printStackTrace();
		}
	}
}
