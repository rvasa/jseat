package test;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
import metric.core.util.MetricTable;
import metric.core.util.MetricTable.Alignment;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Version;

/**
 * This is really just for visual sanity check testing of the MetricTable class.
 * Does it print correctly, do the alignment strategies look correct etc.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 * 
 */
public class MetricTableTest extends TestCase
{
	private MetricTable<Enum, Object> et;

	public void setUp()
	{
		Enum[] headings = { Version.RSN, Version.ID, ClassMetric.FAN_OUT_COUNT,
				ClassMetric.AGE };

		// Create new variable enum table.
		et = new MetricTable<Enum, Object>(headings, "LAYER FREQUENCY DISPLAY");

		Object[] row1 = { "1.0r1", "a really long row", 4, 5};
		Object[] row2 = { "1.0r2", 2, 8 + "", 10 + "f" };
		Object[] row3 = { "1.0r3", 3, "a really long row", 15 };

		// Add rows to variable enum table.
		et.addRow(row1);
		et.addRow(row2);
		et.addRow(row3);
		et.setColumnPadding(1);
	}

	public void testUpperCaseHeadings()
	{
		System.out.println("Upper case headings: ");
		et.setUpperCaseHeadings(true);
		System.out.println(et);

		System.out.println("Lower case headings: ");
		et.setUpperCaseHeadings(false);
		System.out.println(et);
	}

	public void testLeftAlignment()
	{
		System.out.println("Left alignment: ");
		et.setAlignment(Alignment.LEFT);
		System.out.println(et);
	}

	public void testCenterAlignment()
	{
		System.out.println("Center alignment: ");
		et.setAlignment(Alignment.CENTER);
		System.out.println(et);
	}

	public void testRightAlignment()
	{
		System.out.println("Right alignment: ");
		et.setAlignment(Alignment.RIGHT);
		System.out.println(et);
	}

	public void testPrintTitle()
	{
		System.out.println("With title: ");
		et.setDisplayTitle(true);
		System.out.println(et);

		System.out.println("Without title: ");
		et.setDisplayTitle(false);
		System.out.println(et);
	}
	
	public void testIterableRows()
	{
		System.out.println("\nTesting iterable over rows.");
		for (ArrayList<Object> row : et)
		{
			for (Object s : row)
			{
				System.out.print(s);
			}
			System.out.println();
		}
	}
	
	public void testColumnIterator()
	{
		System.out.println("\nTesting column iterator.");
		Iterator<ArrayList<Object>> it = et.columnIterator();
		while (it.hasNext())
		{
			System.out.println(it.next());
		}
	}
}
