package metric.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This is a pretty ugly implementation of a table. This should really be
 * re-written to allow columns to be added instead of rows, allowing each column
 * to specify the data type for the column. This would allow a table to have
 * String columns, double coulmns. etc.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 * 
 * @param <H> The heading datatype.
 * @param <R> The row datatype.
 */
public class MetricTable<H, R> implements Iterable<ArrayList<R>>
{
	private ArrayList<ArrayList<R>> rows;
	private ArrayList<H> heading;

	private int columnPadding;

	private boolean upperCaseHeadings;
	private boolean displayTitle;

	private String title;

	// private Alignment alignment;
	private AlignmentStrategy alignmentStrategy;

	public enum Alignment {
		LEFT, CENTER, RIGHT;
	}

	private MetricTable()
	{
		rows = new ArrayList<ArrayList<R>>();
		heading = new ArrayList<H>();
		columnPadding = 1;
		title = ""; // blank unless specified.

		// Use default alignment.
		alignmentStrategy = new LeftAlignStrategy();
	}

	/**
     * Creates a new MetricTable with the specified headings.
     * 
     * @param headings The headings.
     */
	public MetricTable(H[] headings)
	{
		this();
		setHeadings(headings);
	}

	/**
     * Creates a new MetricTable with the specified headings and title.
     * 
     * @param headings The headings.
     * @param title The title.
     */
	public MetricTable(H[] headings, String title)
	{
		this(headings);
		this.title = title;
	}

	/**
     * Sets the amount of space to pad between columns.
     * 
     * @param padding the amount of padding positions.
     */
	public void setColumnPadding(int padding)
	{
		this.columnPadding = padding;
	}

	/**
     * Sets the heading for each column of the table, specified by the inputted
     * headings.
     * 
     * @param headings the array of H to use as headings.
     */
	public void setHeadings(H[] headings)
	{
		for (H object : headings)
		{
			heading.add(object);
		}
	}

	/**
     * Sets whether or not headings should be capitalised.
     * 
     * @param boldHeadings true or false.
     */
	public void setUpperCaseHeadings(boolean value)
	{
		this.upperCaseHeadings = value;
	}

	/**
     * Sets the alignment mode used to lay out the table used in its String
     * representation.
     * 
     * @param alignment Alignment.LEFT, Alignment.CENTER or Alignment.RIGHT.
     */
	public void setAlignment(Alignment alignment)
	{
		if (alignment == Alignment.CENTER)
		{
			alignmentStrategy = new CenterAlignStrategy();
		} else if (alignment == Alignment.RIGHT)
		{
			alignmentStrategy = new RightAlignStrategy();
		} else
		{
			alignmentStrategy = new LeftAlignStrategy();
		}

	}

	/**
     * Add's the collection of rows to the table.
     * 
     * @param rows to add.
     */
	public void addRows(Collection<R[]> rows)
	{
		for (R[] row : rows)
		{
			addRow(row);
		}
	}

	/**
     * Add's a row to the table.
     * 
     * @param row the row of data to add.
     */
	public void addRow(R[] row)
	{
		ArrayList<R> aRow = new ArrayList<R>();
		for (R r : row)
		{
			aRow.add(r);
		}
		rows.add(aRow);
	}

	/**
     * @return The element at the specified row and specified column.
     */
	public R get(int row, int column)
	{
		return rows.get(row).get(column);
	}

	/**
     * @return The element at the specified column in the heading.
     */
	public H get(int index)
	{
		return heading.get(index);
	}

	@Override
	/**
     * @return String representation of this MetricTable.
     */
	public String toString()
	{
		StringBuffer strbuffer = new StringBuffer();
		try
		{
			if (displayTitle)
			{
				strbuffer.append(printTitle());
				strbuffer.append("\n");
			}

			strbuffer.append(alignmentStrategy.align());
		} catch (IndexOutOfBoundsException e)
		{
			// TODO add proper logger.
			System.out.println("Warning: " + e.toString());
			e.printStackTrace();
		}
		return strbuffer.toString();
	}

	/**
     * 
     * @return
     */
	private String printTitle()
	{
		StringBuffer titleBuffer = new StringBuffer();
		int[] colWidths = getAmountToPad();
		int sumWidth = 0;

		for (int i : colWidths)
		{
			sumWidth += i;
		}

		int pad = sumWidth - title.length();

		for (int i = 0; i < pad; i++)
		{
			if (i < pad / 2)
				titleBuffer.append(" ");
			else if (i == pad / 2)
				titleBuffer.append(title);
			else
				titleBuffer.append(" ");
		}
		titleBuffer.append("\n");
		for (int i = 0; i < sumWidth; i++)
			titleBuffer.append("-");

		return titleBuffer.toString();
	}

	/**
     * Calculates the width of each column according to the largest value.
     * 
     * @return an array of values corresponding to the maximum width for each
     *         column.
     */
	private int[] getAmountToPad()
	{
		// width to pad each column.
		int[] maxWidth = new int[heading.size()];

		// set all columns to zero padding.
		for (int i = 0; i < maxWidth.length; i++)
		{
			maxWidth[i] = heading.get(i).toString().length() + columnPadding;
		}

		if (rows.size() > 0)
		{
			for (int c = 0; c < rows.get(0).size(); c++)
			{
				for (int r = 0; r < rows.size(); r++)
				{
					try
					{
						// int currentWidth = table[r][c].length();
						int currentWidth = rows.get(r).get(c).toString()
								.length();
						try
						{
							if (currentWidth > maxWidth[c])
								maxWidth[c] = currentWidth + columnPadding;
						} catch (ArrayIndexOutOfBoundsException e)
						{
							System.err
									.println("Table formatted incorrectly. Rows have more columns than headings were specified.");
						}
					} catch (NullPointerException e)
					{
					} // handle
				}
			}
		}
		return maxWidth;
	}

	interface AlignmentStrategy
	{
		public String align();

		public String pad(String str, int pad);
	}

	/**
     * Provides the basic algorithm for aligning text. The part that differs,
     * the actual alignment, is handled by a subclassing strategy. In this case,
     * LeftAlignmentStrategy, RightAlignmentStrategy or CenterAlignmentStrategy.
     */
	public abstract class BasicAlignment implements AlignmentStrategy
	{
		public String align()
		{
			StringBuffer buffer = new StringBuffer();
			int[] maxElementWidth = getAmountToPad();
			int rowsToAlign = rows.size();

			// The heading row.
			int colIndex = 0;
			for (H h : heading)
			{
				String element = h.toString();
				if (element.length() < maxElementWidth[colIndex])
					element = pad(element, maxElementWidth[colIndex]
							- element.length());

				if (upperCaseHeadings)
					buffer.append(element.toUpperCase());
				else
					buffer.append(element);
				colIndex++;
			}
			buffer.append("\n");

			// For each row.
			for (int r = 0; r < rowsToAlign; r++)
			{
				// print each column.
				for (int c = 0; c < rows.get(0).size(); c++)
				{
					String element = "";
					try
					{
						element = rows.get(r).get(c).toString();
					} catch (NullPointerException e)
					{
					} // handle

					if (element.length() < maxElementWidth[c])
						buffer.append(pad(element, maxElementWidth[c]
								- element.length()));
					else
						buffer.append(element);
				}
				buffer.append("\n"); // end of row.
			}
			return buffer.toString();
		}

		public abstract String pad(String str, int pad);
	}

	/**
     * Strategy used to right align text.
     * 
     * @author Joshua Hayes,Swinburne University (ICT),2007
     */
	public class RightAlignStrategy extends BasicAlignment
	{
		public String pad(String str, int pad)
		{
			StringBuffer buff = new StringBuffer(str);
			for (int i = 0; i < pad; i++)
			{
				buff.insert(0, " ");
			}
			return buff.toString();
		}
	}
	/**
     * Strategy used to left align text.
     * 
     * @author Joshua Hayes,Swinburne University (ICT),2007
     */
	public class LeftAlignStrategy extends BasicAlignment
	{
		public String pad(String str, int pad)
		{
			StringBuffer buff = new StringBuffer(str);
			for (int i = 0; i < pad; i++)
			{
				buff.insert(buff.length(), " ");
			}
			return buff.toString();
		}
	}
	/**
     * Strategy used to center align text.
     * 
     * @author Joshua Hayes,Swinburne University (ICT),2007
     */
	public class CenterAlignStrategy extends BasicAlignment
	{
		public String pad(String str, int pad)
		{
			StringBuffer buff = new StringBuffer();
			for (int i = 0; i < pad; i++)
			{
				if (i < pad / 2)
					buff.append(" ");
				else if (i == pad / 2)
					buff.append(str);
				else
					buff.append(" ");
			}
			for (int i = 0; i < columnPadding; i++)
			{
				buff.append(" ");
			}
			return buff.toString();
		}
	}

	/**
     * @return The number rows.
     */
	public final int getRows()
	{
		return rows.size();
	}

	/**
     * @return The number of columns. As a MetricTable should have the same
     *         nubmer of columns in the heading as its subsequenty rows, this
     *         column count is based off the heading.
     */
	public final int getCols()
	{
		return rows.get(0).size();
	}

	/**
     * @return the displayTitle
     */
	public final boolean displayTitle()
	{
		return displayTitle;
	}

	/**
     * @param displayTitle the displayTitle to set
     */
	public final void setDisplayTitle(boolean displayTitle)
	{
		this.displayTitle = displayTitle;
	}

	/**
     * @return An Iterator to iterate over the rows in this table.
     */
	public Iterator<ArrayList<R>> iterator()
	{
		return rows.iterator();
	}

	/**
     * @return An Iterator to iterate over the columns in this table.
     */
	public Iterator<ArrayList<Object>> columnIterator()
	{
		return new ColumnIterator<Object>();
	}

	/**
     * An implementation of a column iterator. Returns an iterator for iterating
     * over the columns in this table. This is a pretty ugly implementation as
     * we have to check against the specified row type to allow mixed types in a
     * row. e.g. string, doubles, ints. A better approach would be to add data
     * in columns instead of rows, allowing for column type checking to stay
     * consistent but row data to vary from column to column.
     */
	@SuppressWarnings("hiding")
	class ColumnIterator<Object> implements Iterator<ArrayList<Object>>
	{
		private int current;

		public ColumnIterator()
		{
			current = 0;
		}

		public boolean hasNext()
		{
			// Have to include +1 to include last result.
			return current != rows.size(); // at last row.
		}

		@SuppressWarnings("unchecked")
		public ArrayList<Object> next()
		{
			ArrayList<Object> column = new ArrayList<Object>();
			for (ArrayList<R> r : rows)
			{
				Object element = (Object) r.get(current);
				column.add(element);
			}
			current++;
			return column;
		}

		public void remove()
		{
		} // not implemented.

	}

	public String getTitle()
	{
		return title;
	}
}
