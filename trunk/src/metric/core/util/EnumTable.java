package metric.core.util;

import java.text.NumberFormat;
import java.util.Collection;

public class EnumTable
{
	private int cols, rows;
	private String[][] table;
	private int rowIndex; // The row we add to.
	private int columnPadding;

	private boolean upperCaseHeadings;
	private boolean compactTable;
	private boolean displayTitle;
	private boolean enableSmartCast;

	private String title;

	// private Alignment alignment;
	private AlignmentStrategy alignmentStrategy;

	public enum Alignment {
		LEFT(0), CENTER(1), RIGHT(2);

		private final int value;

		private Alignment(int value)
		{
			this.value = value;
		}

		public final int getValue()
		{
			return value;
		}
	}

	public EnumTable(int rows, int cols)
	{
		this.rows = rows;
		this.cols = cols;
		table = new String[rows][cols];
		rowIndex = 0;
		columnPadding = 1;

		// Use default alignment.
		alignmentStrategy = new LeftAlignStrategy();
	}

	public EnumTable(Object[] headings, int rows)
	{
		// Include extra row for headings.
		this(rows + 1, headings.length);
		populateRow(headings);
	}

	public EnumTable(Object[] headings, String title, int rows)
	{
		// Include extra row for headings.
		this(rows + 1, headings.length);
		populateRow(headings);
		this.title = title;
	}

	/**
     * Sets the amount of space to pad between columns.
     * 
     * @param padding the amount of pads.
     */
	public void setColumnPadding(int padding)
	{
		this.columnPadding = padding;
	}

	/**
     * Sets the heading for each column of the table, specified by the inputted
     * headings.
     * 
     * @param headings the array of Enum's to use as headings.
     */
	public void setHeadings(Enum[] headings)
	{
		updateHeadings(false);
		if (rowIndex == 0)
			rowIndex++;
	}

	/**
     * Sets whether or not headings should be bolded (capitalised)
     * 
     * @param boldHeadings true or false.
     */
	public void setUpperCaseHeadings(boolean value)
	{
		this.upperCaseHeadings = value;
		updateHeadings(value);
	}

	/**
     * Sets the alignment mode used to lay out the table.
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
	public void addRows(Collection<String[]> rows)
	{
		for (Object[] row : rows)
		{
			populateRow(row);
		}
	}

	/**
     * Add's a row to the table.
     * 
     * @param row the row of data to add.
     */
	public void addRow(Object[] row)
	{
		populateRow(row);
	}

	/**
     * Add's the specified array of data (typically a row) to the table.
     * 
     * @param toPop the array of objects to add.
     */
	private void populateRow(Object[] toPop)
	{
		for (int c = 0; c < cols; c++)
		{
			table[rowIndex][c] = toPop[c].toString();
		}
		rowIndex++;
	}

	/**
     * Converts the headings in the table (always the first row) to upperCase if
     * upperCaseHeadings is true. Otherwise, just sets the headings.
     * 
     * @param upperCaseHeadings whether or not to capitalize headings.
     */
	private void updateHeadings(boolean upperCaseHeadings)
	{
		for (int c = 0; c < cols; c++)
		{
			if (upperCaseHeadings)
			{
				table[0][c] = table[0][c].toUpperCase();
			} else
				table[0][c] = table[0][c].toLowerCase();
		}
	}

	@Override
	public String toString()
	{
		StringBuffer strbuffer = new StringBuffer();
		if (displayTitle)
		{
			strbuffer.append(printTitle());
			strbuffer.append("\n");
		}
		strbuffer.append(alignmentStrategy.align());
		return strbuffer.toString();
	}

	public String printTitle()
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
		int[] maxWidth = new int[cols];

		// set all columns to zero padding.
		for (int i = 0; i < maxWidth.length; i++)
		{
			maxWidth[i] = 0;
		}

		for (int c = 0; c < cols; c++)
		{
			for (int r = 0; r < rows; r++)
			{
				try
				{
					int currentWidth = table[r][c].length();
					if (currentWidth > maxWidth[c])
						maxWidth[c] = currentWidth + columnPadding;
				} catch (NullPointerException e)
				{
				} // handle
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

			int rowsToAlign = rows;
			if (compactTable)
				rowsToAlign = rowIndex;
			// For each row.
			for (int r = 0; r < rowsToAlign; r++)
			{
				// print each column.
				for (int c = 0; c < cols; c++)
				{
					String element = "";
					try
					{
						element = table[r][c].toString();

						// Bold headings?
						if (r == 0 && upperCaseHeadings)
							element = table[r][c].toUpperCase();
					} catch (NullPointerException e)
					{
					} // handle

					try
					{
						if (enableSmartCast && r > 1 && c > 1)
							NumberFormat.getIntegerInstance().format(element);
					} catch (IllegalArgumentException e)
					{
					}
					// doSmartCast(element);

					if (element.length() < maxElementWidth[c])
						buffer.append(pad(element, maxElementWidth[c] - element.length()));
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
     * Strategy used to right justify text in a table cell.
     * 
     * @author Joshua Hayes
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
     * Strategy used to align left justify text in a table cell.
     * 
     * @author Joshua Hayes
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
     * Strategy used to align center text in table cells.
     * 
     * @author Joshua Hayes
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
     * @return the rows
     */
	public final int getRows()
	{
		return rows;
	}

	/**
     * Whether or not empty rows will be compacted when toString() is invoked.
     * E.g. A table which was constructed with 10 rows but only had 8 rows added
     * to it, will remove the last two rows instead of printing empty rows is
     * table compacting is enabled.
     * 
     * @return the compactTable
     */
	public final boolean isCompactTable()
	{
		return compactTable;
	}

	/**
     * Sets whether or not empty rows will be compacted when toString() is
     * invoked. E.g. A table which was constructed with 10 rows but only had 8
     * rows added to it, will remove the last two rows instead of printing empty
     * rows is table compacting is enabled.
     * 
     * @param compactTable whether or not to compact the table
     */
	public final void setCompactTable(boolean compactTable)
	{
		this.compactTable = compactTable;
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
     * @return the enableSmartCast
     */
	public final boolean isEnableSmartCast()
	{
		return enableSmartCast;
	}

	/**
     * @param enableSmartCast the enableSmartCast to set
     */
	public final void setEnableSmartCast(boolean enableSmartCast)
	{
		this.enableSmartCast = enableSmartCast;
	}
}
