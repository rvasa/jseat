package metric.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import metric.core.exception.MalformedReportDefinition;
import metric.core.io.TextFile;
import metric.core.util.StringUtils;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Evolution;
import metric.core.vocabulary.History;
import metric.core.vocabulary.MethodMetric;
import metric.core.vocabulary.TypeModifier;
import metric.core.vocabulary.Version;

/**
 * Uses a <code>HashMap</code> to store <code>ReportDefinition</code>'s for
 * all reports defined in the provided report configuration file.
 * 
 * Alternatively, a static method is provided for parsing definitions from a
 * string. Used by other Facade and Factory classes (such as the ReportFactory).
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class ReportDefinitionRepository
{
	private HashMap<Integer, ReportDefinition> table;

	private static int lastTableIndex;

	private static Logger logger = Logger.getLogger(ReportDefinitionRepository.class.getSimpleName());

	private String shortname;

	static
	{
		LogOrganiser.addLogger(logger);
		logger.setLevel(Level.ALL);
	}

	private ReportDefinitionRepository()
	{
		table = new HashMap<Integer, ReportDefinition>();
	}

	/**
     * Creates a repository for <code>MetricDefinitions</code> found whilst
     * proessing the specified configuration file.
     * 
     * @param filename The file from which to create a repository.
     * @throws MalformedReportDefinition
     */
	public ReportDefinitionRepository(String filename)
	{
		this();
		process(filename);
	}

	private void process(String filename)
	{
		// Open an iterate to iterate over config file.
		File f = new File(filename);
		// So we can ask this repository what file it loaded.
		shortname = f.getName();

		TextFile tf = new TextFile(f);
		Iterator it = tf.iterator();

		while (it.hasNext())
		{
			String line = (String) it.next();
			// Skip commented/blank lines
			while ((line.trim().startsWith("#") || line.trim().equals("")))
			{
				line = (String) it.next();

				if (!it.hasNext())
					return;
			}
			ReportDefinition md;
			try
			{
				md = parseDefinition(line);
				table.put(lastTableIndex, md); // store for later
			} catch (MalformedReportDefinition e)
			{
				logger.log(Level.WARNING, "Skipping " + e.toString());
			}
		}
		tf.close(); // Close file.
		logger.fine("Repository loaded " + table.size() + " definitions.");
	}

	/**
     * @return a collection of ReportDefintions in this repository.
     */
	public Collection<ReportDefinition> getDefinitions()
	{
		return table.values();
	}

	/**
     * Parses and extracts a <code>ReportDefintion</code> from the specified
     * line of text.
     * 
     * @param line The line form which to extract.
     * @return The ReportDefinition.
     * @throws MalformedReportDefinition
     */
	public static ReportDefinition parseDefinition(String line) throws MalformedReportDefinition
	{
		// TODO replace this with a proper regular expression.
		// Will remove a lot of the extra processing on arguments
		// below.
		String[] tokens = line.split(",");

		lastTableIndex = Integer.parseInt(tokens[0]);

		String visitorName = tokens[1]; // , metricMethod = tokens[2];
		String description = tokens[2];

		int argStartIndex = 3; // Where arguments start from.

		ArrayList<Object> args = new ArrayList<Object>();
		ArrayList<Object> params = new ArrayList<Object>();
		ArrayList<Object> bunchedArgs;
		ArrayList<Class> bunchedParams;

		Object[] tmp;
		if (tokens != null && tokens.length > argStartIndex)
		{

			for (int i = argStartIndex; i < tokens.length; i++)
			{
				// normal argument
				if (tokens[i].indexOf("[") == -1)
				{
					tmp = getArgAndParam(tokens[i]);
					params.add(tmp[0]);
					args.add(tmp[1]);
				} else
				// argument is actually a collection of args. e.g. [t1,t2]
				{
					bunchedArgs = new ArrayList<Object>();
					bunchedParams = new ArrayList<Class>();

					// Strip '[' on first array arg.
					tmp = getArgAndParam(tokens[i].substring(1, tokens[i].length()));
					// Add to bunch/group collections.
					bunchedParams.add((Class) tmp[0]);
					bunchedArgs.add(tmp[1]);

					i++; // move to next arg and continue processing bunched
					// args
					while (tokens[i].indexOf("]") == -1)
					{
						tmp = getArgAndParam(tokens[i++]);
						bunchedParams.add((Class) tmp[0]);
						bunchedArgs.add(tmp[1]);
					}
					// Strip ']' on last array arg and add to bunch/group
					// collections.
					tmp = getArgAndParam(tokens[i].substring(0, tokens[i].length() - 1));
					bunchedParams.add((Class) tmp[0]);
					bunchedArgs.add(tmp[1]);
					i++;
					// Add our array of args & params we just processed to
					// arg/param collections.
					params.add(bunchedParams.toArray());
					args.add(bunchedArgs.toArray());
				} // End of array
			}
		} else
		{
			// classParams = new Class[] {};
		}

		checkArgValidity(args);
		ReportDefinition md = new ReportDefinition(visitorName, description, params.toArray(), args.toArray());
		return md;
	}

	private static void checkArgValidity(ArrayList<Object> args) throws MalformedReportDefinition
	{
		for (Object arg : args)
		{
			if (arg instanceof Object[])
			{
				String[] s = StringUtils.asStrings((Object[]) arg);
				for (String string : s)
				{
					// TODO: Move error message.
					if (!ReportDefinitionRepository.isValidVocabulary(string))
						throw new MalformedReportDefinition("The specified string: " + string
								+ ", does not appear to be valid.");
				}
			} else if (arg instanceof String)
			{
				if (!ReportDefinitionRepository.isValidVocabulary(arg.toString()))
					throw new MalformedReportDefinition("The specified string: " + arg
							+ ", does not appear to be valid.");
			}
		}
	}

	/**
     * Checks all known vocabulary for an existence of the specified vocab
     * string. Returns whether or not it exists.
     * 
     * @param vocab
     * @return
     */
	private static boolean isValidVocabulary(String vocab)
	{
		if (ClassMetric.parse(vocab) != null)
			return true;
		else if (MethodMetric.parse(vocab) != null)
			return true;
		else if (Version.parse(vocab) != null)
			return true;
		else if (History.parse(vocab) != null)
			return true;
		else if (TypeModifier.parse(vocab) != null)
			return true;
		else if (Evolution.parse(vocab) != null)
			return true;
		return false;
	}

	// Used for testing.
	@SuppressWarnings("unused")
	private void printObjectArray(Collection toPrint)
	{
		Iterator it = toPrint.iterator();
		while (it.hasNext())
		{
			System.out.print(it.next() + " ");
		}
		System.out.println();
	}

	private static Object[] getArgAndParam(String str)
	{
		Class param = null;
		Object arg = null;
		// String is a boolean.
		if (str.equals("true") || str.equals("false"))
		{
			Boolean b = Boolean.valueOf((String) str);
			param = Boolean.TYPE;
			arg = b;
		} else
		{
			try
			{ // String is a number.
				int theValue = Integer.valueOf((String) str);
				param = Integer.TYPE;
				arg = theValue;
			} catch (NumberFormatException e)
			{
				// Must just be a string.
				param = str.getClass();
				arg = str;
			}
		}
		// System.out.println(param + " " + arg);
		return new Object[] { param, arg };
	}

	/**
     * Retrieves the definition for the metric from the repository.
     * 
     * @param index The index in the repository of the
     *            <code>ReportDefinition</code>
     * @return the <code>ReportDefinition</code>
     */
	public ReportDefinition getDefinition(int index)
	{
		return table.get(index);
	}

	public int getSize()
	{
		return table.size();
	}

	public String toString()
	{
		return shortname;
	}

	public String getName()
	{
		return shortname;
	}
}
