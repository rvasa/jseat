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
public class BarChartDecorator extends ReportDecorator
{
	private Composite composite;
	private ChartComposite cc;

	public BarChartDecorator(ReportVisitor report, Composite composite)
	{
		super(report);
		this.composite = composite;
	}

	@Override
	public void display()
	{
		// Create new chart if this is the first display or was
		// previously disposed.
		if (cc == null || cc.isDisposed())
		{
			CategoryDataset dataset = createDataset();
			JFreeChart chart = createChart(decoratedReport.getTable()
					.getTitle(), Version.RSN.toString(), "Value", dataset);
			cc = new ChartComposite(composite, SWT.NONE, chart);
		} else
		{
		} // Haven't been disposed so don't need to do anything.
	}

	public void dispose()
	{
		cc.dispose();
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
			System.err.println("SOMETHING BROKE!!! - THIS SHOULD NOT HAPPEN\n"
					+ e.getMessage());
		} // handle. SHOULD NOT HAPPEN.
		return dataset;
	}

	private void iterateOverColumn(DefaultCategoryDataset dataset,
			MetricTable table, ArrayList column, int colIndex)
	{
		String element = (String) column.get(0);
		try
		{
			// TODO: This is a hack to test the type of the column
			// because we currently store as strings.
			double value = Double.parseDouble(element);
			int rowIndex = 0;
			for (Object colValue : column)
			{
				value = Double.parseDouble((String) colValue);
				// Find out what version this for.
				int version = Integer.valueOf((String) table.get(rowIndex, 1));
				// Find out what column we are actually traversing.
				String colName = (String) table.get(colIndex);

				try
				{
					dataset.incrementValue(value, colName, String
							.valueOf(version));
				} catch (UnknownKeyException e)
				{
					dataset.addValue(value, colName, String.valueOf(version));
				}
				rowIndex++;
			}
		} catch (NumberFormatException e)
		{
		}
	}

	/**
     * Creates a bar chart.
     * 
     * @param dataset the dataset.
     * 
     * @return The chart.
     */
	protected JFreeChart createChart(String title, String domainLabel,
			String rangeLabel, CategoryDataset dataset)
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
