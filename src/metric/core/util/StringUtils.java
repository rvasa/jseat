package metric.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;

public class StringUtils
{
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

	@SuppressWarnings("unchecked")
	public static <T, K> String toCSVString(Set entrySet, char delim,
			HashMap<T, String> exclusionList)
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
					&& ((!exclusionList.get(key).equals(value) || (exclusionList
							.get(key).equals("")))))
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
	public static <T> String toCSVString(Set entrySet, char delim,
			boolean includeZeroSets)
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
	public static <T> String toCSVString(Set entrySet, char delim,
			boolean includeZeroSets, HashMap<T, Integer> exclusionList)
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
				if (!exclusionList.containsKey(key))
				{
					buffer.append(key);
					buffer.append("=");
					buffer.append(value);
					buffer.append(delim);
				} else if (exclusionList.containsKey(key)
						&& ((exclusionList.get(key) == value) || (exclusionList
								.get(key) != -1)))
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
		buffer.append("}");
		return buffer.toString();
	}

	public static String toCSV(Set set)
	{
		return set.toString();
	}

	/**
     * @return Whether or not the specified String represents a boolean value.
     */
	public static boolean isBoolean(String str)
	{
		if (str.equals("true") || str.equals("false"))
			return true;
		return false;
	}

	/**
     * If the specified value exists in the specified array, the index of its
     * position in the array is returned. If it doesn't exist, -1 is returned.
     * 
     * @param values The array of values
     * @param value The value to look for in values
     * @return The index of value within values or -1
     */
	public static int indexOf(String[] values, String value)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(value))
				return i;
		}
		return -1; // Was not found.
	}

	/**
     * Sorts the array of values.
     * 
     * @param values The array of values to sort.
     * @return The sorted array.
     */
	public static String[] sort(String[] values)
	{
		LinkedList<String> ll = new LinkedList<String>();
		for (String val : values)
			ll.add(val);

		Collections.sort(ll);
		String[] ret = new String[ll.size()];
		ll.toArray(ret);
		return ret;
	}

	/**
     * Sorts the array of values.
     * 
     * @param values The array of values to sort.
     * @parm first The value that should be placed in first position after
     *       sorting
     * @return The sorted array.
     */
	public static String[] sort(String[] values, String first)
	{
		LinkedList<String> ll = new LinkedList<String>();
		for (String val : values)
			ll.add(val);

		Collections.sort(ll);
		// Remove first from the list and re-add in first position.
		ll.remove(first);
		ll.addFirst(first);

		String[] ret = new String[ll.size()];
		ll.toArray(ret);
		return ret;
	}

	/**
     * @return Returns a toString() array representation of the object array
     *         specified.
     */
	public static String[] asStrings(Object[] objects)
	{
		String[] strings = new String[objects.length];
		for (int i = 0; i < objects.length; i++)
			strings[i] = objects[i].toString();
		return strings;
	}
	
	public static String[] asStrings(double[] doubles)
	{
		String[] strings = new String[doubles.length];
		for (int i = 0; i < doubles.length; i++)
			strings[i] = String.valueOf(doubles[i]);
		return strings;
	}
	
	/**
	 * @return A string[] for the collection of strings.
	 */
	public static String[] asStrings(Collection<String> list)
	{
		String[] strings = new String[list.size()];
		list.toArray(strings);
		return strings;
	}

	/**
     * For each field in fields, sorts the values in constrainedValues and
     * places the value in fields at teh front of the sorted list. This is done
     * for each field in the fields array and the result of which is returned as
     * an ArrayList<String[]>.
     * 
     * @param fields The fields that the constrainedValues should be sorted on.
     * @param constrainedValues The subset of constrained values (should include
     *            any values that are in the fields array)
     * @return An ArrayList of sorted constrainedValues for each field.
     */
	public static ArrayList<String[]> sortAndExpand(String[] fields,
			String[] constrainedValues)
	{
		ArrayList<String[]> ret = new ArrayList<String[]>();
		for (String metric : fields)
		{
			constrainedValues = StringUtils.sort(constrainedValues, metric);
			ret.add(constrainedValues);
		}
		return ret;
	}
}
