package metric.gui.swt.core.decorator;

import java.awt.Color;

import metric.core.report.decorator.ReportDecorator;
import metric.core.report.visitor.EarthquakeReportVisitor;
import metric.core.report.visitor.ModificationIntensityReportVisitor;
import metric.core.util.MetricTable;
import metric.gui.swt.core.util.PaintBasedColourGenerator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.experimental.chart.swt.ChartComposite;

/**
 * Displays a colour map for the underlying <code>ReportVisitor</code>.
 * HeatMap: Displays warm areas with an emphasis on hot spots.<br />
 * CoolMap: Displays cool areas with an emphasis on cold spot.<br />
 * HybridMap: Displays warm and cool areas together.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class ColourMapDecorator extends ReportDecorator
{
	private ChartComposite cc;
	private Composite composite;
	private IntensityStyle style;
	private int noColourShades = 10;

	public ColourMapDecorator(EarthquakeReportVisitor report, Composite composite, IntensityStyle style)
	{
		super(report);
		this.composite = composite;
		this.style = style;
	}

	@Override
	public void display()
	{
		// Create new chart if this is the first display or was
		// previously disposed.
		if (cc == null || cc.isDisposed())
		{
			// Setup dataset.
			MetricTable mt = decoratedReport.getTable();
			XYDataset dataset = createXYZDataset(mt);
			String title = mt.getTitle() + " (" + style.toString() + ")";
			JFreeChart chart = createChart(dataset, mt.getCols(), mt.getRows(), style, title);
			chart.setTextAntiAlias(true);

			cc = new ChartComposite(composite, SWT.NONE, chart);
			cc.setLayout(new FillLayout());
		} else
		{
		} // Haven't been disposed so don't need to do anything.
	}

	/**
     * Create Dataset from MetricTable.
     * 
     * @param mt The MetricTable from which to create a Dataset
     * @return The Dataset.
     */
	private XYDataset createXYZDataset(MetricTable mt)
	{
		DefaultXYZDataset dataset = new DefaultXYZDataset();

		int cols = mt.getCols();
		int rows = mt.getRows();

		for (int r = 0; r < rows; r++)
		{
			double[][] data = new double[3][cols];
			for (int c = 0; c < cols; c++)
			{
				data[0][c] = c + 1; // row
				data[1][c] = r + 1; // column
				// z value is used to store value.
				data[2][c] = Double.parseDouble((String) mt.get(r, c));
			}
			dataset.addSeries("Series " + r, data);
		}
		return dataset;
	}

	private JFreeChart createChart(XYDataset dataset, int xSize, int ySize, IntensityStyle style, String title)
	{
		NumberAxis xAxis = new NumberAxis("Magnitude");
		NumberAxis yAxis = new NumberAxis("RSN");
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);

		XYBlockRenderer r = new XYBlockRenderer();

		LookupPaintScale ps = null;

		switch (style)
		{
			case HeatMap:
				ps = getDefaultHeatScale();
				break;
			case CoolMap:
				ps = getDefaultCoolScale();
				break;
			case HybridMap:
				ps = getDefaultHybridScale();
				break;
			default:
				ps = getDefaultHeatScale();
		}

		r.setPaintScale(ps);
		r.setBlockHeight(0.90f);
		r.setBlockWidth(0.90f);
		plot.setRenderer(r);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);

		xAxis.setRangeWithMargins(1, xSize);
		yAxis.setRangeWithMargins(1, ySize);
		yAxis.setInverted(true);
		xAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());

		JFreeChart chart = new JFreeChart(title, plot);
		chart.removeLegend();
		chart.setBackgroundPaint(Color.WHITE);

		SymbolAxis scaleAxis = new SymbolAxis(null, new String[] { "", "OK", "Uncertain", "Bad" });
		scaleAxis.setRange(0.0, 1.0);
		scaleAxis.setPlot(new PiePlot());

		return chart;
	}

	/**
     * The paint scale used by the chart to highlight values. The HybridScale is
     * set for both warm and cool highlighting.
     * 
     * @return The hybrid paint scale.
     */
	private LookupPaintScale getDefaultHybridScale()
	{
		LookupPaintScale ps = new LookupPaintScale(0.0, 1.0, new Color(79, 129, 189));
		ps.add(0.0, new Color(140, 173, 212)); // modified but not enough to
		// trigger metrics from
		// ReportVisitor.
		ps.add(0.001, new Color(255, 225, 225)); // Very minor modification.
		ps.add(0.01, new Color(255, 190, 190));
		ps.add(0.02, new Color(255, 179, 179));
		ps.add(0.03, new Color(255, 155, 155)); // Moderate modification.
		ps.add(0.04, new Color(255, 117, 117));
		ps.add(0.5, new Color(255, 90, 90)); // Large modification.
		ps.add(0.1, new Color(255, 71, 71));
		ps.add(0.15, new Color(255, 40, 40));
		ps.add(0.25, new Color(255, 0, 0));
		return ps;
	}

	/**
     * The paint scale used by the chart to highlight values. The HeatScale is
     * set for warm highlighting.
     * 
     * @return The hot paint scale.
     */
	private LookupPaintScale getDefaultHeatScale()
	{
//		LookupPaintScale ps = PaintBasedColourGenerator.generatePaintScale(IntensityStyle.HeatMap, noColourShades);
		LookupPaintScale ps = PaintBasedColourGenerator.generateEarthquakePaintScale(IntensityStyle.HeatMap, 10);
		return ps;
	}

	/**
     * The paint scale used by the chart to highlight values. The CoolScale is
     * set for cool highlighting.
     * 
     * @return The cold paint scale.
     */
	private LookupPaintScale getDefaultCoolScale()
	{
		LookupPaintScale ps = PaintBasedColourGenerator.generatePaintScale(IntensityStyle.CoolMap, noColourShades);

		return ps;
	}

	public enum IntensityStyle {
		HeatMap, CoolMap, HybridMap;
	}
}
