package metric.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import metric.core.exception.ConversionException;
import metric.core.extraction.MetricEngine;
import metric.core.model.HistoryMetricData;
import metric.core.persistence.CSVConverter;
import metric.core.persistence.MetricDataConverter;
import metric.core.util.logging.ConsoleHandler;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.History;
import metric.core.vocabulary.JSeatFileType;
import metric.core.vocabulary.SerializeType;

public class Project
{
	private Logger logger = Logger.getLogger(getClass().getSimpleName());
	private String actualHistoryName, inputFileName, outputFileName;
	private String projectName;
	private HistoryMetricData hmd;
	private MetricEngine me;

	public Project(String inputFileName)
	{
		this.inputFileName = inputFileName;

		// Setup logging
		LogOrganiser.addLogger(logger);
		logger.setLevel(Level.ALL);

		// Setup project name from output filename.
		projectName = inputFileName.substring(
				inputFileName.lastIndexOf(File.separator) + 1,
				(inputFileName.length() - JSeatFileType.PROJECT.toString().length()));
	}

	/**
     * Creates a new Project for the specifed version file.
     * 
     * @param inputFileName The path to the version file.
     * @param outputPath The path to write the project to.
     * @param numThreads The number of threads to scale version processing
     *            across.
     * @param observer An observer that is notified of processing progress.
     */
	public Project(String inputFileName, String outputFileName, int numThreads)
	{
		this(inputFileName);
		this.outputFileName = outputFileName;

		me = new MetricEngine(inputFileName, outputFileName, numThreads, true);

		// Setup project name from output filename.
		projectName = outputFileName.substring(
				outputFileName.lastIndexOf(File.separator) + 1,
				(outputFileName.length() - JSeatFileType.PROJECT.toString().length()));
	}

	public void addObserver(Observer observer)
	{
		if (observer != null)
			me.addObserver(observer);
	}

	public HistoryMetricData build()
	{
		// Building project from versions file.
		if (me != null)
		{
			try
			{
				// Process and set hmd.
				this.hmd = me.process();

				// Write a new project file for processed data.
				writeNewProjectFile();
			} catch (Exception e)
			{
				if (!me.interrupted)
					logger.log(Level.SEVERE, e.toString());
			}
		} else
		{
			// Building project from project file.
			if (inputFileName != null)
			{
				try
				{
					loadProjectFromFile();
				} catch (IOException e)
				{
					logger.log(Level.SEVERE, toString());
				}
			}
		}

		return hmd;
	}

	private void loadProjectFromFile() throws IOException
	{
		Map<Integer, String[]> history = new HashMap<Integer, String[]>();

		BufferedReader br = new BufferedReader(new FileReader(inputFileName));
		while (br.ready())
		{
			String line = br.readLine();

			// Skip comments, blank lines and lines that start with a spaces.
			if (line.startsWith("#") || line.length() == 0 || line.matches("^_+"))
				continue;

			// Project data.
			else if (line.startsWith("$") && (line.indexOf(",") != -1))
			{
				String[] toks = line.split(",");
				// +1 starts substring just after $.
				this.projectName = toks[0].substring(line.indexOf("$") + 1, toks[0].length());
				this.actualHistoryName = toks[1].trim();

			} // Version data.
			else if (line.startsWith("$"))
			{
				line = line.trim(); // just in case there is a trailing space.
				// versions = Integer.parseInt(line.substring(1,
                // line.length()));
			} // Version
			else
			// if (line.endsWith(SupportedFileType.CSV.toString()))
			{
				String[] toks = line.split(",");
				int rsn = Integer.parseInt(toks[0].trim());
				String versionName = toks[1].trim();
				String versionPath = toks[2].trim();
				history.put(rsn, new String[] { versionName, versionPath });
			}

			// Finally, create history.
			this.hmd = new HistoryMetricData(actualHistoryName, history);
		}
		// Clean up.
		br.close();
	}

	private void writeNewProjectFile()
	{
		String filename = outputFileName;
		if (!filename.endsWith(JSeatFileType.PROJECT.toString()))
			filename += JSeatFileType.PROJECT.toString();

		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			bw.write("# Java Software Evolution Analysis Tool (JSeat) Project File #");
			bw.newLine();
			bw.write("# Created: ");
			bw.write(Calendar.getInstance().getTime().toString());
			bw.newLine();
			bw.write("$");
			bw.write(projectName);
			bw.write(",");
			bw.write(hmd.get(History.NAME));
			bw.newLine();
			bw.write("# The set of versions that exist in this project.");
			bw.newLine();

			// Write version information.
			writeVersionFiles(bw, SerializeType.CLASSES);
			bw.newLine();

			// Write Visualizer code.
			bw.newLine();
			bw.write("# JSeatVisualizer settings.");
			bw.newLine();

			// Close stream.
			bw.close();
			logger.log(Level.ALL, "Finished writing project file to: " + filename);

		} catch (IOException e)
		{
			e.printStackTrace();
			logger.log(Level.SEVERE, e.toString());
		}
	}

	private void writeVersionFiles(BufferedWriter bw, SerializeType type) throws IOException
	{
		bw.write("$");
		bw.write(hmd.get(History.VERSIONS));
		bw.write(",");
		bw.write(type.toString());
		bw.newLine();

		for (int i = 1; i <= hmd.size(); i++)
		{
			String file = String.valueOf(i) + ", " + hmd.getNameOf(i) + ", " + hmd.getPathOf(i);
			bw.write(file);
			bw.newLine();
		}
	}

	/**
     * Basic command line invocation for creating a new project.
     * 
     * @param args
     */
	public static void main(String[] args)
	{
		String versionPath = null;
		String projectPath = null;
		int numThreads = 1;

		if (args.length != 6)
		{
			System.out.println("Invalid arguments.");
			System.exit(1);
		}

		// TODO Rewrite this.
		for (int i = 0; i < args.length; i++)
		{
			// Project name
			if (args[i].equals("-i"))
			{
				if (args.length >= i + 2)
				{
					versionPath = args[++i];
				} else
					System.err.println("You must specify a filename after the -i command.");
			} else if (args[i].equals("-o"))
			{
				if (args.length >= i + 2)
				{
					projectPath = args[++i];
				} else
					System.err.println("You must specify a path after the -o command.");
			} else if (args[i].equals("-t"))
			{
				if (args.length >= i + 2)
				{
					numThreads = Integer.parseInt(args[++i]);
				} else
					System.err.println("You must specify a number after the -t command.");
			}
		}

		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		LogOrganiser.addHandler(consoleHandler);

		Project project = new Project(versionPath, projectPath, numThreads);
		// project.addObserver(project);
		project.build();

		System.out.println("testing...");
		try
		{
			MetricDataConverter fromConverter = new CSVConverter(SerializeType.CLASSES);
			fromConverter.deSerialize(new FileReader("D:\\MyASMProject\\data\\1.cme"));
			MetricDataConverter fromConverter2 = new CSVConverter(SerializeType.DEPENDENCIES);
			fromConverter2.deSerialize(new FileReader("D:\\MyASMProject\\data\\1.dep"));

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (ConversionException e)
		{
			e.printStackTrace();
		}
	}

	public void interrupt()
	{
		if (me != null)
		{
			me.interrupt();
		}
	}
}
