package metric.core.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
import java.util.Map.Entry;

import metric.core.exception.ConversionException;
import metric.core.io.TextFile;
import metric.core.model.ClassMetricData;
import metric.core.model.VersionMetricData;
import metric.core.util.CSVUtil;
import metric.core.util.SimpleWorkTimer;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.SerializeType;
import metric.core.vocabulary.Version;

public class CSVConverter extends Observable implements MetricDataConverter
{
	private BufferedWriter bw;
	private BufferedReader br;

	private long storeTime, loadTime;
	protected int completion;
	private char delim = ',';
	private SimpleWorkTimer workTimer;
	private SerializeType type;
	private MetricDataStrategy conversionStrategy;

	public CSVConverter(SerializeType type)
	{
		storeTime = loadTime = 0;
		completion = 0;
		this.type = type;

		if (type == SerializeType.CLASSES)
			conversionStrategy = new ClassMetricCSVConverter();
		else if (type == SerializeType.METHODS)
			conversionStrategy = new MethodCSVConverter();
		else if (type == SerializeType.DEPENDENCIES)
			conversionStrategy = new DepCSVConverter();
	}

	public void close() throws IOException
	{
		if (bw != null)
			bw.close();
		if (br != null)
			br.close();
	}

	public VersionMetricData deSerialize(String data)
			throws ConversionException
	{
		throw new ConversionException("Not yet implemented...");
	}

	public VersionMetricData deSerialize(Reader data)
			throws ConversionException
	{
		workTimer = new SimpleWorkTimer();
		workTimer.start();

		br = new BufferedReader(data);
		VersionMetricData vmd = conversionStrategy.from(br, workTimer);
		workTimer.stop();
		loadTime = workTimer.getTimeElapsed();
		return vmd;
	}

	public void serialize(VersionMetricData md, String path)
			throws ConversionException
	{
		workTimer = new SimpleWorkTimer();
		workTimer.start();
		try
		{
			bw = new BufferedWriter(new FileWriter(path + type.getExt()));
			conversionStrategy.to(md, bw, workTimer);
		} catch (IOException e)
		{
			throw new ConversionException();
		}
		workTimer.stop();
		storeTime = workTimer.getTimeElapsed();
	}

	public String serialize(VersionMetricData md) throws ConversionException
	{
		workTimer = new SimpleWorkTimer();
		workTimer.start();
		StringWriter sw = new StringWriter();
		bw = new BufferedWriter(sw);
		conversionStrategy.to(md, bw, workTimer);
		workTimer.stop();
		storeTime = workTimer.getTimeElapsed();
		return sw.toString();
	}

	public final String getFileExtension()
	{
		return type.getExt();
	}

	public long getLoadTime()
	{
		return loadTime;
	}

	public long getStoreTime()
	{
		return storeTime;
	}

	public int getCompletion()
	{
		return completion;
	}

	private abstract class BasicCSVConverter implements MetricDataStrategy
	{
		protected String type;
		protected StringBuffer buffer;

		public BasicCSVConverter(String type)
		{
			this.type = type;
		}

		public void to(VersionMetricData md, BufferedWriter bw,
				SimpleWorkTimer workTimer) throws ConversionException
		{
			if (md.getClass() != VersionMetricData.class)
				throw new ConversionException();
			VersionMetricData vmd = (VersionMetricData) md;
			workTimer.setWorkUnits(vmd.metricData.size());

			buffer = new StringBuffer();
			buffer.append(CSVUtil.getCSVHeader(vmd, type));
			buffer.append("\n");

			Collection<ClassMetricData> classes = vmd.metricData.values();
			Iterator<ClassMetricData> it = classes.iterator();

			while (it.hasNext())
			{
				ClassMetricData cmd = it.next();
				classDataToWrite(cmd);
			}

			try
			{
				bw.write(buffer.toString());
			} catch (IOException e)
			{
				throw new ConversionException();
			}
		}

		public VersionMetricData from(BufferedReader br,
				SimpleWorkTimer workTimer) throws ConversionException
		{
			TextFile file = new TextFile(br);
			Iterator<String> it = file.iterator();

			VersionMetricData vmd = null;
			while (it.hasNext())
			{
				String line = null;
				try
				{
					line = it.next();
					// Skip comments, blank lines.
					if (line.startsWith("#") || line.length() == 0)
						continue;
					else if (line.startsWith("$"))
					{
						vmd = createVersionFromDescriptor(line);
					} else
					// Process class
					{
						classDataToRead(line, vmd);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("bad line: " + line);
					System.exit(1);
				}
			}
			
			return vmd;
		}

		/**
         * Creates an empty VersionMetricData object from the the line of text
         * specified.
         */
		private VersionMetricData createVersionFromDescriptor(String line)
		{
			VersionMetricData vmd = null;
			String[] toks = line.split("$|,");
			String name = toks[0].substring(1); // skip the $

			int rsn = Integer.parseInt(toks[1]);
			String id = toks[2];
			int numberOfClasses = Integer.parseInt(toks[3]);

			vmd = new VersionMetricData(rsn, id, name);
			vmd.setSimpleMetric(Version.CLASS_COUNT, numberOfClasses);
			return vmd;
		}

		protected abstract void classDataToWrite(ClassMetricData cmd);

		protected abstract void classDataToRead(String line,
				VersionMetricData vmd);
	}

	// -----------------------------------------------------------------------
	// CLASS SERIALIZATION STRATEGY FOR VERSION METRIC DATA
	// -----------------------------------------------------------------------
	public class ClassMetricCSVConverter extends BasicCSVConverter
	{
		public ClassMetricCSVConverter()
		{
			super("class metric");
		}

		@Override
		protected void classDataToWrite(ClassMetricData cmd)
		{
			buffer.append(cmd.get(ClassMetric.NAME));
			buffer.append(",");
			buffer.append(cmd.get(ClassMetric.SUPER_CLASS_NAME));
			buffer.append(",");
			buffer.append(CSVUtil.toCSVString(cmd.getMetrics(), false));
			buffer.append("\n");
			workTimer.addCompletedWork(1);
			completion = workTimer.getWorkStatus();
			setChanged();
			notifyObservers();
		}

		@Override
		protected void classDataToRead(String line, VersionMetricData vmd)
		{			
			String[] toks = line.split(",");
			String classname = toks[0];
			String superClassname = toks[1];
			int[] metrics = new int[ClassMetric.getNumberOfMetrics()];
			for (int i = 2; i < toks.length; i++)
			{
				metrics[i - 2] = Integer.parseInt(toks[i]);
			}
			ClassMetricData cmd = new ClassMetricData(vmd.get(Version.NAME),
					metrics);
			cmd.setProperty(ClassMetric.NAME, classname);
			cmd.setProperty(ClassMetric.SUPER_CLASS_NAME, superClassname);
			vmd.addClass(cmd);
		}
	}

	// -----------------------------------------------------------------------
	// METHOD SERIALIZATION STRATEGY FOR VERSION METRIC DATA
	// -----------------------------------------------------------------------
	public class MethodCSVConverter extends BasicCSVConverter
	{
		public MethodCSVConverter()
		{
			super("method metric");
		}

		@Override
		protected void classDataToRead(String line, VersionMetricData vmd)
		{
			// TODO Not yet implemented.
		}

		@Override
		protected void classDataToWrite(ClassMetricData cmd)
		{
			buffer.append(cmd.get(ClassMetric.NAME));
			buffer.append(",");

			if (cmd.methods != null)
			{
				HashMap<String, int[]> methods = cmd.methods.methods();
				Set<Entry<String, int[]>> entrySet = methods.entrySet();
				Iterator<Entry<String, int[]>> methodIterator = entrySet
						.iterator();
				while (methodIterator.hasNext())
				{
					Entry<String, int[]> entry = methodIterator.next();
					buffer.append("[");
					buffer.append(entry.getKey().toString());
					buffer.append(",");
					buffer.append(CSVUtil.toCSVString(entry.getValue(), false));
					buffer.append("]");
				}
				buffer.append("\n");
			}
			completion = workTimer.getWorkStatus();
			setChanged();
			notifyObservers();
		}
	}

	// -----------------------------------------------------------------------
	// DEPENDENCY SERIALIZATION STRATEGY FOR VERSION METRIC DATA
	// -----------------------------------------------------------------------
	public class DepCSVConverter extends BasicCSVConverter
	{
		public DepCSVConverter()
		{
			super("class dependency");
		}

		@Override
		protected void classDataToRead(String line, VersionMetricData vmd)
		{
			String[] toks = line.split(",");
			String classname = toks[0];
			Set<String> deps = new HashSet<String>();

			// First position (0) is classname, so we skip it.
			for (int i = 1; i < toks.length; i++)
			{
				deps.add(toks[i]);
			}

			ClassMetricData cmd = new ClassMetricData();
			cmd.setProperty(ClassMetric.PRODUCT_NAME, vmd.get(Version.NAME));
			cmd.dependencies = deps;
			cmd.setProperty(ClassMetric.NAME, classname);
			vmd.addClass(cmd);
		}

		@Override
		protected void classDataToWrite(ClassMetricData cmd)
		{
			buffer.append(cmd.get(ClassMetric.NAME));
			buffer.append(",");

			buffer.append(CSVUtil.toCSV(cmd.dependencies, true, true));
			buffer.append("\n");
			completion = workTimer.getWorkStatus();
			setChanged();
			notifyObservers();
		}
	}
}
