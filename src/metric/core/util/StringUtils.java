package metric.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class StringUtils
{
	/**
     * @return Whether or not the specified String represents a boolean value.
     */
	public static boolean isBoolean(String str)
	{
		if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false"))
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
	public static ArrayList<String[]> sortAndExpand(String[] fields, String[] constrainedValues)
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
