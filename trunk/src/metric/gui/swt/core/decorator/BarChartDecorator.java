package metric.gui.swt.core.decorator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import metric.core.report.decorator.ReportDecorator;
import metric.core.report.visitor.ReportVisitor;
import metric.core.util.MetricTable;
import metric.core.vocabulary.Version;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.experimental.chart.swt.ChartComposite;

/**
 * Displays a Bar Chart for the specified report.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class BarChartDecorator extends GraphicalDecorator
{
	public BarChartDecorator(ReportVisitor report, Composite composite)
	{
		super(report, composite);
	}
	
	public BarChartDecorator(ReportVisitor report)
	{
		super(report);
	}
	
	@Override
	protected JFreeChart createChart()
	{
		CategoryDataset dataset = createDataset();
		return createChart(
				decoratedReport.getTable().getTitle(),
				Version.RSN.toString(),
				"Value",
				dataset);
	}

	/**
     * Returns a dataset that can be used with a barchart, linechart. The
     * dataset is created from the MetricTable found in the underlying report.
     * 
     * @return The dataset.
     */
	@SuppressWarnings("unchecked")
	private CategoryDataset createDataset()
	{
		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		MetricTable table = decoratedReport.getTable();
		// Geta column iterator from the metric table.
		Iterator<ArrayList<Object>> colIterator = table.columnIterator();
		try
		{
			int colIndex = 0;
			while (colIterator.hasNext())
			{
				ArrayList column = colIterator.next();

				// Skip Name, RSN and ID columns.
				// Only interested in metric columns.
				while (colIndex < 2)
				{
					column = colIterator.next();
					colIndex++;
				}
				iterateOverColumn(dataset, table, column, colIndex);

				// Starting from the start of the next row.
				colIndex++;
			}
		} catch (IndexOutOfBoundsException e)
		{
			System.err.println("SOMETHING BROKE!!! - THIS SHOULD NOT HAPPEN\n" + e.getMessage());
		} // handle. SHOULD NOT HAPPEN.
		return dataset;
	}

	private void iterateOverColumn(DefaultCategoryDataset dataset, MetricTable table, ArrayList column, int colIndex)
	{
		String element = (String) column.get(0);
		String stringVer = null;
		int rowIndex = 0;
		try
		{
			// TODO: This is a hack to test the type of the column
			// because we currently store as strings.
			double value = Double.parseDouble(element);

			for (Object colValue : column)
			{
				value = Double.parseDouble(String.valueOf(colValue));
				
				// Find out what version this for.
				stringVer = (String) table.get(rowIndex, 1);
				if (stringVer.indexOf("-") != -1)
					stringVer = stringVer.substring(0, stringVer.indexOf("-"));
				
				int version = Integer.valueOf(stringVer);
				// Find out what column we are actually traversing.
				String colName = (String) table.get(colIndex);

				try
				{
					dataset.incrementValue(value, colName, String.valueOf(version));
				} catch (UnknownKeyException e)
				{
					dataset.addValue(value, colName, String.valueOf(version));
				}
				rowIndex++;
			}
		} catch (NumberFormatException e)
		{
			System.out.println("Cant parse: " + (String) table.get(rowIndex, 1));
		}
	}

	/**
     * Creates a bar chart.
     * 
     * @param dataset the dataset.
     * 
     * @return The chart.
     */
	protected JFreeChart createChart(String title, String domainLabel, String rangeLabel, CategoryDataset dataset)
	{

		// create the chart...
		JFreeChart chart = ChartFactory.createBarChart(title, // chart
				// title
				domainLabel, // domain axis label
				rangeLabel, // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// Disable bar outlines...
		// BarRenderer renderer = (BarRenderer) plot.getRenderer();
		// renderer.setDrawBarOutline(false);

		// Slant x-axis.
		// CategoryAxis domainAxis = plot.getDomainAxis();
		// domainAxis.setCategoryLabelPositions(CategoryLabelPositions
		// .createUpRotationLabelPositions(Math.PI / 6.0));

		return chart;

	}
}
