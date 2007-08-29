package metric;

import metric.core.Project;
import metric.core.util.logging.ConsoleHandler;
import metric.core.util.logging.LogOrganiser;

/**
 * Basic Console program for extracting data and creating a JSeat Project.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class JSeatExtractor
{
	/**
     * -i Input file -o Output file -t number_of_concurrent_threads Example:
     * JSeatExtractor -i b:/workspace/builds/groovy/groovy.ver -o
     * b:/groovy/groovy.jpf -t 2
     */
	public static void main(String[] args)
	{
		String input = null, output = null;
		int threads = 1;
		// Add a console handler so we can listen to output.
		LogOrganiser.addHandler(new ConsoleHandler());

		if (args.length < 4 || args.length > 6)
			System.err.println("Invalid arguments provided.");

		try
		{
			if (args[0].equals("-i"))
				input = args[1];
			else
				System.err.println("Invalid argument specified. Expected [-i]");
			if (args[2].equals("-o"))
				output = args[3];
			else
				System.err.println("Invalid argument specified. Expected [-o]");

			if (args.length == 6 && args[4].equals("-t"))
				threads = Integer.parseInt(args[5]);
		} catch (Exception e)
		{
			System.err.println("One or more invalid arguments specified.");
		}

		if (input != null && output != null)
		{
			Project p = new Project(input, output, threads);
			p.build();
		}
	}
}
