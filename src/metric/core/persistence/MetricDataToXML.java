package metric.core.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.MetricData;
import metric.core.model.VersionMetricData;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.History;
import metric.core.vocabulary.Version;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Provides an <code>XStream Converter</code> for converting MetricData.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 * 
 */
public class MetricDataToXML extends Observable implements Converter
{
	private static final Logger logger = Logger.getLogger(MetricDataToXML.class
			.toString());

	private String shortName;
	private HashMap<Integer, VersionMetricData> versions;

	private int completion;

	public MetricDataToXML()
	{
		LogOrganiser.addLogger(logger);
		versions = new HashMap<Integer, VersionMetricData>();
	}

	/**
     * Marshall's the specified value, using the specified
     * <code>HierarchicalStreamWriter</code> and
     * <code>MarshallingContext</code>.
     */
	public void marshal(Object value, HierarchicalStreamWriter writer,
			MarshallingContext context)
	{
		MetricData md = (MetricData) value;
		writeMetricData(writer, md);
	}

	/**
     * Writes the specified MerticData to xml, using the specified writer.
     * 
     * @param <T>
     * @param writer - The stream writer to use.
     * @param composite - The component to write.
     */
	private <T> void writeMetricData(HierarchicalStreamWriter writer,
			MetricData md)
	{
		// Type
		writer.startNode(md.getClass().getSimpleName());

		if (md instanceof HistoryMetricData)
		{
			HistoryMetricData hmd = (HistoryMetricData) md;
			// Print all my metricInfo
			prettyPrintMetricData(writer, md);

			if (hmd.versions != null)
			{
				Collection<VersionMetricData> vmds = hmd.versions.values();
				int work = vmds.size();
				int i = 1;
				for (VersionMetricData vmd : vmds)
				{
					writeMetricData(writer, vmd);
					completion = (int) (((double) i / (double) work) * 100);
					setChanged();
					notifyObservers();
					i++;
				}
			}
		} else if (md instanceof VersionMetricData)
		{
			VersionMetricData vmd = (VersionMetricData) md;
			// Print all my metricInfo
			// prettyPrintEntrySet(writer, md.metricIterator());
			prettyPrintMetricData(writer, md);

			if (vmd.metricData != null)
			{
				Collection<ClassMetricData> cmds = vmd.metricData.values();
				for (ClassMetricData cmd : cmds)
				{
					writeMetricData(writer, cmd);
				}
			}
		} else if (md instanceof ClassMetricData)
		{
			ClassMetricData cmd = (ClassMetricData) md;
			// Pretty print metric information
			prettyPrintMetricData(writer, md);

			prettyPrintSet(writer, cmd.dependencies, "dependencies");
			prettyPrintSet(writer, cmd.internalDeps, "internalDeps");
			prettyPrintSet(writer, cmd.users, "users");
//			Iterator mmdIt = cmd.methods.iterator();
//			while (mmdIt.hasNext())
//			{
//				MethodMetricData mmd = (MethodMetricData) mmdIt.next();
//				writeMetricData(writer, mmd);
//			}
		}
//		} else if (md instanceof MethodMetricData)
//		{
//			prettyPrintMetricData(writer, md);
//		}
		writer.endNode();
	}

	private void prettyPrintMetricData(HierarchicalStreamWriter writer,
			MetricData md)
	{
		// Print metric properties.
		prettyPrintEntrySet(writer, md.propertySet(), "Properties");

		// Print simple metrics.
		prettyPrintEntrySet(writer, md.simpleMetricSet(), "SimpleMetrics");

		// Print complex metrics.
		// prettyPrintEntrySet(writer, md.complexMetricSet(), "ComplexMetrics");
	}

	@SuppressWarnings("unchecked")
	private <T, K> void prettyPrintEntrySet(HierarchicalStreamWriter writer,
			Set entrySet, String nodeName)
	{
		writer.startNode(nodeName);
		Iterator it = entrySet.iterator();
		while (it.hasNext())
		{
			Entry<T, K> entry = (Entry<T, K>) it.next();
			T key = entry.getKey();
			K value = entry.getValue();

			// Writer start of node.
			writer.startNode(key.toString());
			writer.setValue(value.toString());
			writer.endNode();
		}
		writer.endNode();
	}

	private void prettyPrintSet(HierarchicalStreamWriter writer, Set set,
			String s)
	{
		writer.startNode(s);
		writer.setValue(set.toString());
		writer.endNode();
	}

	/**
     * Unmarshalls the data from the specified
     * <code>HierarchicalStreamReader</code>.
     * 
     * @param reader The <code>HierarchicalStreamReader</code> to use when
     *            unmarshalling.
     * @param context The <code>UnmarshallingContext</code> used when
     *            unmarshalling.
     */
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context)
	{
		HistoryMetricData hmd = null;
		while (reader.hasMoreChildren())
		{
			reader.moveDown();
			if (reader.getNodeName().equals("HistoryMetricData"))
			{
				hmd = createHistory(reader);
			}
		}
		return hmd;
	}

	private HistoryMetricData createHistory(HierarchicalStreamReader reader)
	{
		HistoryMetricData hmd = null;
		String name = "";
		int work = 0;
		while (reader.hasMoreChildren())
		{
			reader.moveDown();
			if (reader.hasMoreChildren())
			{
				if (reader.getNodeName().equals("Properties"))
				{
					while (reader.hasMoreChildren())
					{
						reader.moveDown();
						if (reader.getNodeName()
								.equals(History.NAME.toString()))
						{
							name = reader.getValue();
							hmd = new HistoryMetricData(name);
							this.shortName = name;
						}
						reader.moveUp();
					}

				} else if (reader.getNodeName().equals("SimpleMetrics"))
				{
					while (reader.hasMoreChildren())
					{
						reader.moveDown();
						hmd.setSimpleMetric(History.parse(reader.getNodeName()),
								Integer.parseInt(reader.getValue()));
						reader.moveUp();
					}
					work = hmd.getSimpleMetric(History.VERSIONS);
				} else if (reader.getNodeName().equals("VersionMetricData"))
				{
					VersionMetricData vmd = createVersion(reader);
					vmd.setProperty(Version.NAME, shortName);
					versions.put(vmd.getSimpleMetric(Version.RSN), vmd);
					completion = (int) (((double) versions.size() / (double) work) * 100);
					setChanged();
					notifyObservers();
				}
			} else
			{
				// String currentNode = reader.getNodeName();
			}
			reader.moveUp();

		}
		hmd.versions = versions;
		return hmd;
	}

	private VersionMetricData createVersion(HierarchicalStreamReader reader)
	{
		VersionMetricData vmd = new VersionMetricData();
		String id = "";

		while (reader.hasMoreChildren())
		{
			reader.moveDown();
			if (reader.hasMoreChildren())
			{
				if (reader.getNodeName().equals("Properties"))
				{
					while (reader.hasMoreChildren())
					{
						reader.moveDown();
						if (reader.getNodeName().equals(Version.ID.toString()))
							id = reader.getValue();
						vmd.setProperty(Version.ID, id);
						reader.moveUp();
					}

				} else if (reader.getNodeName().equals("SimpleMetrics"))
				{
					while (reader.hasMoreChildren())
					{
						reader.moveDown();
						vmd.setSimpleMetric(Version.parse(reader.getNodeName()),
								Integer.parseInt(reader.getValue()));
						reader.moveUp();
					}
				} else if (reader.getNodeName().equals("ClassMetricData"))
					vmd.addClass(createClassMetric(reader));
			} else
			{
			}
			reader.moveUp();
		}
		return vmd;
	}

	private ClassMetricData createClassMetric(HierarchicalStreamReader reader)
	{
		ClassMetricData cmd = new ClassMetricData(this.shortName);
		while (reader.hasMoreChildren())
		{
			reader.moveDown();
			if (reader.hasMoreChildren())
			{
				if (reader.getNodeName().equals("Properties"))
				{
					while (reader.hasMoreChildren())
					{
						reader.moveDown();
						cmd.setProperty(
								ClassMetric.parse(reader.getNodeName()), reader
										.getValue());
						reader.moveUp();
					}

				} else if (reader.getNodeName().equals("SimpleMetrics"))
				{
					while (reader.hasMoreChildren())
					{
						reader.moveDown();
						cmd.setSimpleMetric(ClassMetric.parse(reader.getNodeName()),
								Integer.parseInt(reader.getValue()));
						reader.moveUp();
					}
				}
			} else
			{
				String node = reader.getNodeName();
				if (node.equals("dependencies"))
					cmd.dependencies = getHashSetFromString(reader.getValue());
				else if (node.equals("internalDeps"))
					cmd.internalDeps = getHashSetFromString(reader.getValue());
				else if (node.equals("users"))
					cmd.users = getHashSetFromString(reader.getValue());
			}
			reader.moveUp();
		}
		return cmd;
	}

	private HashSet<String> getHashSetFromString(String str)
	{
		HashSet<String> set = new HashSet<String>();
		String[] toks = str.substring(1, str.length() - 1).split(",");
		for (String dep : toks)
		{
			set.add(dep);
		}
		return set;
	}

	/**
     * @return Whether or not the specified <code>Class</code> can be
     *         converted by this <code>Converter</code>
     */
	public boolean canConvert(Class aClass)
	{
		if (aClass.getSuperclass() == MetricData.class)
			return true;
		return false;
	}

	/**
     * @return the completion
     */
	public final int getCompletion()
	{
		return completion;
	}

}
