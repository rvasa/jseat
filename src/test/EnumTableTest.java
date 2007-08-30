package test;

import junit.framework.TestCase;
import metric.core.util.EnumTable;
import metric.core.util.MetricTable;
import metric.core.util.EnumTable.Alignment;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Version;

/**
 * This is really just for visual sanity check testing of the EnumTable class.
 * Does it print correctly, do the alignment strategies look correct etc.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 * 
 */
public class EnumTableTest extends TestCase
{
	private EnumTable et;
	private MetricTable varET;

	public void setUp()
	{
		Enum[] headings = { Version.RSN, Version.ID, ClassMetric.FAN_OUT_COUNT, ClassMetric.AGE };

		// Create new enum table.
		et = new EnumTable(headings, "LAYER FREQUENCY DISPLAY", 3);

		String[] row1 = { "1.0r1", "afasdfsd", 4 + "", 5 + "" };
		String[] row2 = { "1.0r2", "2", 8 + "", 10 + "f" };
		String[] row3 = { "1.0r3", "3", "fadsfasdf", 15 + "" };

		// Add rows to enum table.
		et.addRow(row1);
		et.addRow(row2);
		et.addRow(row3);
		et.setColumnPadding(1);
	}

	public void testUpperCaseHeadings()
	{
		System.out.println("Upper case headings: ");
		et.setUpperCaseHeadings(true);
		varET.setUpperCaseHeadings(true);
		System.out.println(et);

		System.out.println("Lower case headings: ");
		et.setUpperCaseHeadings(false);
		varET.setUpperCaseHeadings(false);
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
}
