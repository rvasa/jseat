package metric.gui.swt.core.decorator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import metric.core.report.Report;
import metric.core.report.decorator.ReportDecorator;
import metric.core.util.MetricTable;
import metric.core.vocabulary.Version;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.experimental.chart.swt.ChartComposite;

public class HistogramDecorator extends ReportDecorator
{
	private Composite composite;
	private ChartComposite cc;

	public HistogramDecorator(Report report, Composite composite)
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
			IntervalXYDataset dataset = createDataset();
			JFreeChart chart = createChart(
					decoratedReport.getTable().getTitle(),
					Version.RSN.toString(),
					"Value",
					dataset,
					PlotOrientation.VERTICAL);
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
     * Returns a sample dataset.
     * 
     * @param <R>
     * 
     * @return The dataset.
     */
	@SuppressWarnings("unchecked")
	private IntervalXYDataset createDataset()
	{
		// create the dataset...
		HistogramDataset dataset = new HistogramDataset();

		MetricTable table = decoratedReport.getTable();
		// Geta column iterator from the metric table.
		Iterator<ArrayList<Object>> colIterator = table.columnIterator();
		try
		{
			int rowIndex = 0, colIndex = 0;
			while (colIterator.hasNext())
			{
				ArrayList column = colIterator.next();
				String element = (String) column.get(0);
				// Skip Name, RSN and ID columns.
				// Only interested in metric columns.
				while (colIndex < 2)
				{
					column = colIterator.next();
					colIndex++;
				}

				try
				{
					// TODO: This is a hack to test the type of the column
					// because we currently store as strings.
					double value = Double.parseDouble(element);
					for (int i = 0; i < column.size(); i++)
					{
						int startVersion = Integer.valueOf((String) table.get(rowIndex, 1));
						System.out.println("start version " + startVersion);
						int currentVersion = startVersion;
						ArrayList<Double> series = new ArrayList<Double>();
						while (currentVersion == startVersion && rowIndex != table.getRows())
						{

							value = Double.parseDouble((String) column.get(i++));
							System.out.println("version: " + currentVersion + " value: " + value);
							// Find out what version this for.
							currentVersion = Integer.valueOf((String) table.get(rowIndex, 1));

							// Add colValue to our dataset and update our
							// rowIndex.
							series.add(Double.valueOf(value));
							rowIndex++;
						}
						// Find out what column we are actually traversing.
//						String colName = (String) table.get(colIndex);
						double[] dSeries = new double[series.size()];
						for (int j = 0; j < series.size(); j++)
							dSeries[j] = series.get(j);
						dataset.addSeries("RSN" + currentVersion, dSeries, 4, 1, 4);
					}
				} catch (NumberFormatException e)
				{
				}

				// Starting from the start of the next row.
				colIndex++;
				rowIndex = 0;
			}
		} catch (IndexOutOfBoundsException e)
		{
		} // handle. SHOULD NOT HAPPEN.
		return dataset;
	}

	/**
     * Creates a sample chart.
     * 
     * @param dataset the dataset.
     * 
     * @return The chart.
     */
	private JFreeChart createChart(String title, String domainLabel, String rangeLabel, IntervalXYDataset intervalSet,
			PlotOrientation orientation)
	{

		// create the chart...
		JFreeChart chart = ChartFactory.createHistogram(title, domainLabel, // domain
				// axis
				// label
				rangeLabel, // range axis label
				intervalSet, // data
				orientation, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);

		// JFreeChart chart = ChartFactory.createBarChart(title, // chart
		// // title
		// domainLabel, // domain axis label
		// rangeLabel, // range axis label
		// dataset, // data
		// PlotOrientation.VERTICAL, // orientation
		// true, // include legend
		// true, // tooltips?
		// false // URLs?
		// );

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		// CategoryPlot plot = (CategoryPlot) chart.getPlot();
		// plot.setBackgroundPaint(Color.lightGray);
		// plot.setDomainGridlinePaint(Color.white);
		// plot.setDomainGridlinesVisible(true);
		// plot.setRangeGridlinePaint(Color.white);

		// set the range axis to display integers only...
		// final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

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
