package metric.core.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import metric.core.model.VersionMetricData;
import metric.core.vocabulary.Version;

public class CSVUtil
{
	public static String getCSVHeader(VersionMetricData vmd, String type)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("# JSeat ");
		buffer.append(type);
		buffer.append(" data");
		buffer.append("\n$");
		buffer.append(vmd.get(Version.NAME));
		buffer.append(",");
		buffer.append(vmd.get(Version.RSN));
		buffer.append(",");
		buffer.append(vmd.get(Version.ID));
		buffer.append(",");
		buffer.append(vmd.metricData.size());
		return buffer.toString();
	}

	public static String toCSV(Set set, boolean removeSpaces, boolean noBrackets)
	{
		String result = set.toString();
		if (removeSpaces)
			result = result.replaceAll(" ", "");
		if (noBrackets)
		{
			result = result.replaceAll("\\[|\\]", "");
			// result = result.replace("]", "");
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> String toCSVString(Set entrySet, char delim, boolean includeZeroSets,
			HashMap<T, Integer> exclusionList)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		Iterator it = entrySet.iterator();
		while (it.hasNext())
		{
			Entry entry = (Entry) it.next();
			T key = (T) entry.getKey();
			int value = (Integer) entry.getValue();
			if (((includeZeroSets == false) && value != 0) || includeZeroSets)
			{
				if (!exclusionList.containsKey(key))
				{
					buffer.append(key);
					buffer.append("=");
					buffer.append(value);
					buffer.append(delim);
				} else if (exclusionList.containsKey(key)
						&& ((exclusionList.get(key) == value) || (exclusionList.get(key) != -1)))
				{
					buffer.append(key);
					buffer.append("=");
					buffer.append(value);
					buffer.append(delim);
				}
			}
		}
		// Strip last , before closing
		buffer.delete(buffer.length() - 1, buffer.length());
		buffer.append("]");
		return buffer.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T> String toCSVString(Set entrySet, char delim, boolean includeZeroSets)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		Iterator it = entrySet.iterator();
		while (it.hasNext())
		{
			Entry entry = (Entry) it.next();
			T key = (T) entry.getKey();
			int value = (Integer) entry.getValue();
			if (((includeZeroSets == false) && value != 0) || includeZeroSets)
			{
				buffer.append(key);
				buffer.append("=");
				buffer.append(value);
				buffer.append(delim);
			}
		}
		// Strip last , before closing
		buffer.delete(buffer.length() - 1, buffer.length());
		buffer.append("}");
		return buffer.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T, K> String toCSVString(Set entrySet, char delim, HashMap<T, String> exclusionList)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		Iterator it = entrySet.iterator();
		while (it.hasNext())
		{
			Entry<T, K> entry = (Entry<T, K>) it.next();
			T key = entry.getKey();
			String value = (String) entry.getValue();
			if (!exclusionList.containsKey(key))
			{
				buffer.append(key);
				buffer.append("=");
				buffer.append(value);
				buffer.append(delim);
			} else if (exclusionList.containsKey(key)
					&& ((!exclusionList.get(key).equals(value) || (exclusionList.get(key).equals("")))))
			{
				buffer.append(key);
				buffer.append("=");
				buffer.append(value);
				buffer.append(delim);
			}
		}
		// Strip last , before closing
		buffer.delete(buffer.length() - 1, buffer.length());
		buffer.append("}");
		return buffer.toString();
	}

	/** Convert the array into a comma seperated value string */
	public static String toCSVString(double[] data)
	{
		String s = "";
		for (double f : data)
			s += String.format("%1.3f", f) + ", ";
		return s.trim().substring(0, s.lastIndexOf(','));
	}

	public static String toCSVString(int[] data, boolean spaceSeparated)
	{
		StringBuffer sb = new StringBuffer();
		for (int i : data)
		{
			sb.append(i);
			sb.append(",");
			if (spaceSeparated)
				sb.append(" ");
		}
		return sb.toString().trim().substring(0, sb.toString().lastIndexOf(','));
	}

	@SuppressWarnings("unchecked")
	public static <T, K> String toCSVString(Set entrySet, char delim)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		Iterator it = entrySet.iterator();
		while (it.hasNext())
		{
			Entry<T, K> entry = (Entry<T, K>) it.next();
			T key = entry.getKey();
			K value = entry.getValue();
			buffer.append(key);
			buffer.append("=");
			buffer.append(value);
			buffer.append(delim);
		}
		// Strip last , before closing
		buffer.delete(buffer.length() - 1, buffer.length());
		buffer.append("}");
		return buffer.toString();
	}
}
