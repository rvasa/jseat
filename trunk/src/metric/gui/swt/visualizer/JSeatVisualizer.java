package metric.gui.swt.visualizer;

import java.io.IOException;

import metric.core.exception.MalformedReportDefinition;
import metric.core.exception.ReportException;
import metric.core.model.HistoryMetricData;
import metric.core.report.ReportFactory;
import metric.core.report.decorator.ReportDecorator;
import metric.core.report.visitor.ModificationIntensityReportVisitor;
import metric.core.report.visitor.ReportVisitor;
import metric.gui.swt.core.JSeatGUI;
import metric.gui.swt.core.decorator.BarChartDecorator;
import metric.gui.swt.core.decorator.ColourMapDecorator;
import metric.gui.swt.core.decorator.LineChartDecorator;
import metric.gui.swt.core.decorator.ColourMapDecorator.IntensityStyle;
import metric.gui.swt.core.util.SWTFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class JSeatVisualizer extends JSeatGUI
{
	private static final String TITLE = "JSeat Visualizer";
	private static final String CONFIG = "jseat.conf";
	private List versionList;
	private ReportDecorator currentDecoration;

	public JSeatVisualizer(Shell shell, String title, String configFile)
			throws IOException
	{
		super(shell, title, configFile);

		// Setup visualizations.
		initVisualizations();

		// Store versions in this.
		versionList = SWTFactory.createList(navComposite, SWT.MULTI
				| SWT.V_SCROLL, null);

		// Override the navigation group heading.
		navGroup.setText("Versions");
		navComposite.setBackgroundMode(SWT.NONE);
	}

	public static void main(String[] args)
	{
		try
		{
			Display display = new Display();
			Shell shell = new Shell(display);

			final JSeatVisualizer tv = new JSeatVisualizer(shell,
					TITLE, CONFIG);

			shell.addListener(SWT.Close, new Listener()
			{
				public void handleEvent(Event event)
				{
					tv.dispose();
				}
			});
			shell.pack();
			shell.setSize(800, 600);
			shell.open();

			while (!shell.isDisposed())
			{
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
     * Initialises visualizations and sets up appropriate Menu/MenuItems for
     * them.
     */
	private void initVisualizations()
	{
		Menu menuBar = menu.getMenuBar();
		final MenuItem visualizationMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		visualizationMenuHeader.setText("Visualization");
		final Menu visualizationMenu = new Menu(shell, SWT.DROP_DOWN);
		visualizationMenuHeader.setMenu(visualizationMenu);

		try
		{
			// Colour Map Visualization
			String report = "1,ModificationIntensityReportVisitor,Modification Intensity Report";
			ModificationIntensityReportVisitor rv;

			rv = (ModificationIntensityReportVisitor) ReportFactory
					.getReport(report);
			ReportDecorator heatMapDecorator = new ColourMapDecorator(rv,
					mainComposite, IntensityStyle.HeatMap);
			ReportDecorator coolMapDecorator = new ColourMapDecorator(rv,
					mainComposite, IntensityStyle.CoolMap);
			ReportDecorator hybridMapDecorator = new ColourMapDecorator(rv,
					mainComposite, IntensityStyle.HybridMap);

			Visualization v = new Visualization(visualizationMenu,
					"Colour Map", SWT.DROP_DOWN);

			MenuItemListener mil = new MenuItemListener();
			v.addSubVisualization("Heat Map", rv, heatMapDecorator, mil);
			v.addSubVisualization("Cool Map", rv, coolMapDecorator, mil);
			v.addSubVisualization("Hybrid Map", rv, hybridMapDecorator, mil);

			// Evolutionary Bar Chart
			String evoReport = "62,EvolutionReportVisitor,Evolution Analysis (Modified),"
					+ "modified,[name,fan_in_count,fan_out_count]";

			ReportVisitor modifiedClassesReport = (ReportVisitor) ReportFactory
					.getReport(evoReport);

			ReportDecorator barDecorator = new BarChartDecorator(
					modifiedClassesReport, mainComposite);
			Visualization v2 = new Visualization(visualizationMenu,
					"Maodified Evolutionary BarChart", SWT.NONE);
			v2.setReport(modifiedClassesReport);
			v2.setDecorator(barDecorator);
			v2.addSelectionListener(mil);

			// Evolutionary Line Chart
			ReportDecorator lineDecorator = new LineChartDecorator(
					modifiedClassesReport, mainComposite);
			Visualization v3 = new Visualization(visualizationMenu,
					"aModified Evolutionary LineChart", SWT.NONE);
			v3.setReport(modifiedClassesReport);
			v3.setDecorator(lineDecorator);
			v3.addSelectionListener(mil);

		} catch (ReportException e)
		{
			e.printStackTrace();
		} catch (MalformedReportDefinition e)
		{
			e.printStackTrace();
		}
	}

	/**
     * Handles selection events.
     * 
     * @author Joshua Hayes,Swinburne University (ICT),2007
     */
	class MenuItemListener implements SelectionListener
	{
		public void widgetDefaultSelected(SelectionEvent arg0)
		{
		} // Not interested in this.

		public void widgetSelected(SelectionEvent event)
		{
			MenuItem item = (MenuItem) event.widget;
			ReportDecorator rd = (ReportDecorator) event.widget
					.getData("decorator");
			
			if (versionList != null)
			{
				HistoryMetricData hmd = (HistoryMetricData) versionList
						.getData();

				if (rd != currentDecoration && item.getSelection())
				{
					try
					{
						execute(rd, hmd, mainComposite);
					} catch (ReportException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public List getVersionList()
	{
		return versionList;
	}
}
