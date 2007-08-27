package metric.gui.swt.core.decorator;

import java.awt.Color;

import metric.core.report.visitor.ReportVisitor;

import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

/**
 * Displays a Line Chart for the specified ReportVisitor.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007s
 */
public class LineChartDecorator extends BarChartDecorator
{
	public LineChartDecorator(ReportVisitor visitor, Composite composite)
	{
		super(visitor, composite);
	}

	/**
     * Creates a sample chart.
     * 
     * @param dataset the dataset.
     * 
     * @return The chart.
     */
	protected JFreeChart createChart(String title, String domainLabel,
			String rangeLabel, CategoryDataset dataset)
	{

		// create the chart...
		JFreeChart chart = ChartFactory.createLineChart(title, // chart
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
