package metric.gui.swt.visualizer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import metric.core.MetricEngine;
import metric.core.exception.MalformedReportDefinition;
import metric.core.exception.ReportException;
import metric.core.model.HistoryMetricData;
import metric.core.persistence.CSVConverter;
import metric.core.persistence.MetricDataConverter;
import metric.core.persistence.XMLConverter;
import metric.core.report.ReportFactory;
import metric.core.report.decorator.ReportDecorator;
import metric.core.report.visitor.ModificationIntensityReportVisitor;
import metric.core.report.visitor.ReportVisitor;
import metric.core.vocabulary.SupportedFileType;
import metric.gui.swt.core.decorator.BarChartDecorator;
import metric.gui.swt.core.decorator.ColourMapDecorator;
import metric.gui.swt.core.decorator.LineChartDecorator;
import metric.gui.swt.core.decorator.ColourMapDecorator.IntensityStyle;
import metric.gui.swt.core.dialog.OpenDialog;
import metric.gui.swt.core.dialog.ProgressDialog;
import metric.gui.swt.core.threading.ThreadedMetricDataConverter;
import metric.gui.swt.core.threading.ThreadedMetricEngine;
import metric.gui.swt.core.util.SWTUtils;
import metric.gui.swt.core.vocabulary.GUI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * A visualization tool that displays <code>Visualization</code>'s of
 * software metrics.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class JSeatVisualizer implements Observer, SelectionListener
{
	private Shell shell;
	private ProgressDialog pDialog;
	private VisMenu menu;

	private HashMap<GUI, String> properties;

	private List lVersions;
	private Composite chartComposite;
	private Group chartGroup;
	private ReportDecorator currentDecoration;

	private ThreadedMetricDataConverter tc;

	public static void main(String[] args)
	{
		try
		{
			Display display = new Display();
			Shell shell = new Shell(display);

			final JSeatVisualizer mv = new JSeatVisualizer(shell,
					"jSeat Visualizer", "jseat.conf");

			shell.addListener(SWT.Close, new Listener()
			{
				public void handleEvent(Event event)
				{
					mv.dispose();
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

	public JSeatVisualizer(Shell shell, String title, String configFile)
			throws IOException
	{
		this.shell = shell;

		shell.setText(title);
		shell.setLayout(new FillLayout());

		SashForm sashForm = new SashForm(shell, SWT.HORIZONTAL);
		sashForm.setLayout(new FillLayout());

		Group versionGroup = SWTUtils.createGroup(
				sashForm,
				SWT.NONE,
				"Versions",
				new FillLayout(SWT.VERTICAL),
				null);

		chartGroup = SWTUtils.createGroup(
				sashForm,
				SWT.NONE,
				"",
				new FillLayout(SWT.VERTICAL),
				null);

		lVersions = SWTUtils.createList(
				versionGroup,
				SWT.MULTI | SWT.V_SCROLL,
				null);
		lVersions.addSelectionListener(this);

		chartComposite = new Composite(chartGroup, SWT.NONE);
		chartComposite.setLayout(new FillLayout(SWT.VERTICAL));

		sashForm.setWeights(new int[] { 20, 80 });

		// Load the configuration file.
		loadConfig(configFile);

		// Setup menu.
		initMenu();

		// Setup visualizations.
		initVisualizations();
	}

	protected void dispose()
	{
		shell.dispose();
	}

	/**
     * Initialises the menu.
     * 
     */
	private void initMenu()
	{
		menu = new VisMenu(shell, SWT.BAR);
		shell.setMenuBar(menu.getMenuBar());

		MenuItemListener ml = new MenuItemListener();
		menu.getFileNewItem().addSelectionListener(ml);
		menu.getFileOpenItem().addSelectionListener(ml);
		menu.getFileExitItem().addSelectionListener(ml);
	}

	/**
     * Initialises visualizations and sets up appropriate Menu/MenuItems for
     * them.
     */
	private void initVisualizations()
	{
		Menu visualizationMenu = menu.getVisualizationMenu();

		try
		{
			// Colour Map Visualization
			String report = "1,ModificationIntensityReportVisitor,Modification Intensity Report";
			ModificationIntensityReportVisitor rv;

			rv = (ModificationIntensityReportVisitor) ReportFactory
					.getReport(report);
			ReportDecorator heatMapDecorator = new ColourMapDecorator(rv,
					chartComposite, IntensityStyle.HeatMap);
			ReportDecorator coolMapDecorator = new ColourMapDecorator(rv,
					chartComposite, IntensityStyle.CoolMap);
			ReportDecorator hybridMapDecorator = new ColourMapDecorator(rv,
					chartComposite, IntensityStyle.HybridMap);

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
					modifiedClassesReport, chartComposite);
			Visualization v2 = new Visualization(visualizationMenu,
					"Modified Evolutionary BarChart", SWT.NONE);
			v2.setReport(modifiedClassesReport);
			v2.setDecorator(barDecorator);
			v2.addSelectionListener(mil);

			// Evolutionary Line Chart
			ReportDecorator lineDecorator = new LineChartDecorator(
					modifiedClassesReport, chartComposite);
			Visualization v3 = new Visualization(visualizationMenu,
					"Modified Evolutionary LineChart", SWT.NONE);
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
     * Loads the deafult configuration file (jeat.config)
     * 
     * @throws IOException
     */
	private void loadConfig(String configFile) throws IOException
	{
		Properties prop = new Properties();

		FileInputStream fis = new FileInputStream(configFile);
		prop.load(fis);

		properties = new HashMap<GUI, String>();

		properties.put(GUI.DEFAULT_REPORT_DIR, (String) prop
				.get(GUI.DEFAULT_REPORT_DIR.toString()));
		properties.put(GUI.DEFAULT_VERSION_DIR, (String) prop
				.get(GUI.DEFAULT_VERSION_DIR.toString()));
		properties.put(GUI.DEFAULT_REPORTSET, (String) prop
				.get(GUI.DEFAULT_REPORTSET.toString()));
		properties.put(GUI.AUTO_PROCESS, (String) prop.get(GUI.AUTO_PROCESS
				.toString()));
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
			if (event.getSource() == menu.getFileExitItem())
				dispose(); // cleanup
			else if (event.getSource() == menu.getFileOpenItem())
			{
				String filterPath = properties.get(GUI.DEFAULT_VERSION_DIR);
				OpenDialog od = new OpenDialog(shell, SupportedFileType
						.getExtensions(),
						SupportedFileType.getExtensionNames(), filterPath);
				String selected = od.open();

				if (selected != null)
				{
					processHistoryFromFile(selected);
				}
			} else
			{
				MenuItem item = (MenuItem) event.widget;
				ReportVisitor rv = (ReportVisitor) event.widget
						.getData("report");
				ReportDecorator rd = (ReportDecorator) event.widget
						.getData("decorator");
				HistoryMetricData hmd = (HistoryMetricData) lVersions.getData();

				if (rd != currentDecoration && item.getSelection())
				{
					updateVisualization(rv, rd, hmd);
				}
			}
		}
	}

	/**
     * Starts the <code>ThreadedConverter</code> to process the specified file
     * if it is not a general versions file. Opens a <code>ProgressDialog</code>
     * to display update information from the <code>MetricEngine</code>.
     * 
     * @param filename The file to process.
     */
	private void processHistoryFromFile(String filename)
	{
		MetricDataConverter mc = null;
		if (filename.indexOf(SupportedFileType.VERSION.toString()) != -1)
		{
			processHistoryFromVersionsFile(filename);
			return;
		} else if (filename.indexOf(SupportedFileType.CSV.toString()) != -1)
		{
			mc = new CSVConverter();
			mc.addObserver(this);
		} else if (filename.indexOf(SupportedFileType.XML.toString()) != -1)
		{
			mc = new XMLConverter();
			mc.addObserver(this);
		}

		try
		{
			tc = new ThreadedMetricDataConverter(mc, filename, lVersions, this);
			openNewProgressDialog("Restoring model data...");
			tc.start();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
     * Starts the <code>ThreadedMetricEngine</code> to process the specified
     * filename. Opens a <code>ProgressDialog</code> to display update
     * information from the <code>MetricEngine</code>.
     * 
     * @param filename The file the ThreadedMetricEngine should process.
     */
	private ThreadedMetricEngine processHistoryFromVersionsFile(String filename)
	{
		ThreadedMetricEngine tme = new ThreadedMetricEngine(this, lVersions,
				filename);
		tme.start();

		// Show new progress dialog.
		openNewProgressDialog("Processing version(s)");
		return tme;
	}

	/**
     * Opens a new <code>ProgressDialog</code>
     * 
     * @param title The title of the ProgressDialog.
     */
	private void openNewProgressDialog(String title)
	{
		pDialog = new ProgressDialog(title, "Please wait...", 100);
		SWTUtils.centerDialog(shell, pDialog.getShell());
		pDialog.open();
	}

	/**
     * Receives updates from <code>Observables</code> we are currently
     * observing.
     */
	public void update(Observable observable, final Object o)
	{
		if (observable instanceof MetricEngine)
		{
			MetricEngine me = (MetricEngine) observable;
			int value = me.getCompletion();
			pDialog.updateForMe(value);
		} else if (observable instanceof MetricDataConverter)
		{
			MetricDataConverter mc = (MetricDataConverter) observable;
			int value = mc.getCompletion();
			pDialog.updateForMe(value);
		} else if (o instanceof HistoryMetricData)
		{
			// TODO auto load a visualization here.
			// HistoryMetricData hmd = (HistoryMetricData) o;
		}
	}

	/**
     * Updates the currently displayed visualization using the specified
     * ReportVisitor, ReportDecorator and HistoryMetricData.
     * 
     * @param rv The ReportVisitor to be accept by the specified
     *            HistoryMetricData.
     * @param rd The ReportDecorator to decorate the specified ReportVisitor.
     * @param hmd The HistoryMetricData from which a report should be generated,
     *            and visualization displayed.
     */
	private void updateVisualization(ReportVisitor rv,
			final ReportDecorator rd, HistoryMetricData hmd)
	{
		try
		{
			if (hmd != null && rv != null)
			{
				// Have model accept report.
				hmd.accept(rv);

				Runnable displayChart = new Runnable()
				{
					public void run()
					{
						if (chartComposite.getChildren().length > 0)
							chartComposite.getChildren()[0].dispose();

						rd.display();
						// TODO This is a hack to force a redraw that doesn't
						// otherwise occur.
						shell.setSize(
								shell.getSize().x - 1,
								shell.getSize().y - 1);
						currentDecoration = rd;
					}
				};
				Display.getDefault().asyncExec(displayChart);
			}
		} catch (ReportException e)
		{
			e.printStackTrace();
		}
	}

	public void widgetDefaultSelected(SelectionEvent arg0)
	{
	} // Not interested in this.

	public void widgetSelected(SelectionEvent event)
	{
		// if (event.getSource() == bReChart
		// && (lVersions.getSelectionCount() == 0 || lVersions
		// .getSelectionCount() >= 2))
		// {
		// HistoryMetricData hmd = new HistoryMetricData("test");
		//
		// int[] selectedVersions = lVersions.getSelectionIndices();
		// HashMap<Integer, VersionMetricData> vMap = new HashMap<Integer,
		// VersionMetricData>();
		// for (int i = 0; i < selectedVersions.length; i++)
		// {
		// String selectedName = lVersions.getItem(selectedVersions[i]);
		// System.out.println(selectedName);
		// VersionMetricData vmd = (VersionMetricData) lVersions
		// .getData(selectedName);
		// vMap.put(vmd.getSimpleMetric(Version.RSN), vmd);
		// }
		// hmd.versions = vMap;
		// displayChart(hmd);
		// }
	}
}
