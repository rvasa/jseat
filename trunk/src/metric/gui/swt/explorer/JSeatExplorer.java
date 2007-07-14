package metric.gui.swt.explorer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.logging.Logger;

import metric.core.model.HistoryMetricData;
import metric.core.model.MetricData;
import metric.core.persistence.CSVConverter;
import metric.core.persistence.MetricDataConverter;
import metric.core.persistence.XMLConverter;
import metric.core.report.Report;
import metric.core.report.decorator.ReportDecorator;
import metric.core.report.decorator.TextDecorator;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.SupportedFileType;
import metric.gui.swt.core.dialog.ProgressDialog;
import metric.gui.swt.core.dialog.SaveDialog;
import metric.gui.swt.core.threading.ThreadedMetricDataConverter;
import metric.gui.swt.core.threading.ThreadedReporter;
import metric.gui.swt.core.util.SWTUtils;
import metric.gui.swt.core.vocabulary.GUI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * A tool for exploring software metrics through the use of
 * customizable/configurable reports and charts.
 * 
 * The interactive reporting allows reports and charts to be updated and
 * displayed on the fly without having to re-process data. Good for exploring
 * many different types of metrics on a new piece of software when you don't
 * necessarily know what you are looking for.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 * 
 */
// TODO This code requires a re-write and is pretty old.
public class JSeatExplorer implements Observer
{
	private static Logger logger = Logger.getLogger(JSeatExplorer.class
			.getSimpleName());

	private Shell shell;
	private LeftComposite lc;
	private CenterComposite cc;
	private static final String TITLE = "Java Software Evolution Analysis Tool (jSEAT)";
	private static final String CONFIG_FILE = "jseat.conf";
	private static final String[] SAVE_EXTS = {
			SupportedFileType.CSV.getExtension(),
			SupportedFileType.XML.getExtension() };
	private String[] SAVE_FILTER_NAMES = {
			SupportedFileType.CSV.getExtensionName(),
			SupportedFileType.XML.getExtensionName() };

	private ProgressDialog pDialog;
	private HashMap<GUI, String> properties;
	private MenuItem fileExitItem, fileOpenItem, modelSaveItem;

	public static void main(String[] args)
	{
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText(TITLE);
		final JSeatExplorer jeat = new JSeatExplorer(shell);

		shell.addListener(SWT.Close, new Listener()
		{
			public void handleEvent(Event event)
			{
				jeat.dispose();
			}
		});

		// Display window.
		shell.open();

		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	public JSeatExplorer(Shell shell)
	{
		this.shell = shell;
		shell.setSize(800, 600);
		LogOrganiser.addLogger(logger);
		// Prepare properties.
		properties = new HashMap<GUI, String>();
		try
		{
			loadConfig();
		} // Attempt to load the default configuration file.
		catch (IOException e)
		{
			// TODO Update with a proper GUI logger.
			e.printStackTrace();
		}

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		shell.setLayout(gridLayout);

		// The top composite holds directory/model input components.
		TopComposite tc = new TopComposite(this, shell, SWT.BORDER);

		// The left composite holds version and reporting components.
		lc = new LeftComposite(this, shell, SWT.BORDER, logger);
		tc.setVersions(lc.getVersions());
		// The center composite holds the main output area and related
		// components.

		cc = new CenterComposite(this, shell, SWT.NONE);

		// Initialise the menu.
		initMenu();

		shell.pack();
		// suited to widescreen
		// not any particular format though.
		shell.setSize(1024, 600);
	}

	/**
     * Loads the deafult configuration file (jeat.config)
     * 
     * @throws IOException
     */
	private void loadConfig() throws IOException
	{
		Properties prop = new Properties();

		FileInputStream fis = new FileInputStream(CONFIG_FILE);
		prop.load(fis);

		setProperty(GUI.DEFAULT_REPORT_DIR, (String) prop
				.get(GUI.DEFAULT_REPORT_DIR.toString()));
		setProperty(GUI.DEFAULT_VERSION_DIR, (String) prop
				.get(GUI.DEFAULT_VERSION_DIR.toString()));
		setProperty(GUI.DEFAULT_REPORTSET, (String) prop
				.get(GUI.DEFAULT_REPORTSET.toString()));
		setProperty(GUI.AUTO_PROCESS, (String) prop.get(GUI.AUTO_PROCESS
				.toString()));
	}

	/**
     * Initialises the main MenuBar
     */
	private void initMenu()
	{
		Menu menuBar, fileMenu, modelMenu;
		MenuItem fileMenuHeader, modelMenuHeader;

		MenuItem helpMenuHeader, aboutMenuItem;
		Menu aboutMenu;

		menuBar = new Menu((Decorations) shell, SWT.BAR);

		// Setup file menu.
		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&File");

		fileMenu = new Menu((Decorations) shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);

		fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenItem.setText("&Open");
		fileOpenItem.addSelectionListener(new MenuItemListener());

		fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText("E&xit");

		// Setup model menu.
		modelMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		modelMenuHeader.setText("Model Data");
		modelMenu = new Menu(shell, SWT.DROP_DOWN);
		modelMenuHeader.setMenu(modelMenu);

		modelSaveItem = new MenuItem(modelMenu, SWT.PUSH);
		modelSaveItem.setText("Save");

		helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText("Help");
		aboutMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuHeader.setMenu(aboutMenu);

		aboutMenuItem = new MenuItem(aboutMenu, SWT.PUSH);
		aboutMenuItem.setText("About");

		((Decorations) shell).setMenuBar(menuBar);

		// Register selection events.
		fileExitItem.addSelectionListener(new MenuItemListener());
		modelSaveItem.addSelectionListener(new MenuItemListener());
	}

	class MenuItemListener implements SelectionListener
	{
		public void widgetDefaultSelected(SelectionEvent arg0)
		{
		} // Not interested in this.

		public void widgetSelected(SelectionEvent event)
		{
			if (event.getSource() == fileExitItem)
			{
				// FIXME May need extra cleanup.
				shell.dispose();
			} else if (event.getSource() == modelSaveItem)
			{
				// Get current history metric data.
				try
				{
					MetricData md = (MetricData) lc.getVersions().getData();

					SaveDialog sd = new SaveDialog(shell, SAVE_EXTS,
							SAVE_FILTER_NAMES);
					String toSave = sd.open();
					MetricDataConverter mc = null;
					if (toSave.indexOf(".mmd") != -1)
					{
						mc = new CSVConverter();
						addObserver((CSVConverter) mc);
					} else if (toSave.indexOf(".xmd") != -1)
					{
						mc = new XMLConverter();
						addObserver((XMLConverter) mc);
					}

					ThreadedMetricDataConverter tc = new ThreadedMetricDataConverter(
							mc, md, toSave);
					openNewProgressDialog("Saving model data...");
					tc.start();

				} catch (NullPointerException e)
				{
				} // No history to save.
			}
			else if (event.getSource() == fileOpenItem)
			{
				 
			}
		}
	}

	private void addObserver(Observable o)
	{
		o.addObserver(this);
	}

	public void setProperty(GUI prop, String value)
	{
		properties.put(prop, value);
	}

	public String getProperty(GUI prop)
	{
		return properties.get(prop);
	}

	public void update(Observable observable, Object o)
	{
		// Updates ProgressDialog with the completion information
		// from a MetricConverter.
		if (observable instanceof MetricDataConverter)
		{
			MetricDataConverter mc = (MetricDataConverter) observable;
			pDialog.updateForMe(mc.getCompletion());
		}
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
     * Executes the currently selected report on the currently seleted
     * HistoryMetricData.
     */
	public void executeReport()
	{
		HistoryMetricData hmd = lc.getHistoryMetricData();
		Report rv = lc.getSelectedReport();
		HashMap<String, Object> customArgs = lc.getReportConfig();

		// TODO: Use TextDecorator by default for now.
		ReportDecorator rd = new TextDecorator(rv);

		if (hmd != null && rv != null)
		{
			if (customArgs != null)
				rv.setArguments(customArgs);

			ThreadedReporter tr = new ThreadedReporter(hmd, rd);
			tr.start();
		}
	}

	public void dispose()
	{
		shell.dispose();
	}
}
