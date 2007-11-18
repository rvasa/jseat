package metric.gui.swt.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import metric.core.exception.ReportException;
import metric.core.model.HistoryMetricData;
import metric.core.report.Report;
import metric.core.report.decorator.ReportDecorator;
import metric.core.report.visitor.ReportVisitor;
import metric.core.vocabulary.JSeatFileType;
import metric.gui.swt.core.dialog.NewProjectDialog;
import metric.gui.swt.core.dialog.OpenDialog;
import metric.gui.swt.core.dialog.ProgressDialog;
import metric.gui.swt.core.threading.ThreadedProjectBuilder;
import metric.gui.swt.core.util.JSeatFactory;
import metric.gui.swt.core.util.SWTFactory;
import metric.gui.swt.core.vocabulary.GUI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

public abstract class JSeatGUI implements Observer
{
//	private List versionList;
	private ProgressDialog reportProgressDialog;
	protected Shell shell;
	protected HashMap<GUI, String> properties;
	protected JSeatMenu menu;
	protected Composite mainComposite, navComposite;
	protected Group mainGroup, navGroup;
	protected SashForm sashForm;

	public JSeatGUI(Shell shell, String title, String configFile) throws IOException
	{

		this.shell = shell;

		shell.setText(title);
		shell.setLayout(new FillLayout());

		sashForm = new SashForm(shell, SWT.HORIZONTAL);
		sashForm.setLayout(new FillLayout());

		navGroup = SWTFactory.createGroup(sashForm, SWT.NONE, "", new FillLayout(SWT.VERTICAL), null);

		mainGroup = SWTFactory.createGroup(sashForm, SWT.NONE, "", new FillLayout(SWT.VERTICAL), null);

		navComposite = new Composite(navGroup, SWT.NONE);
		navComposite.setLayout(new FillLayout(SWT.VERTICAL));

		mainComposite = new Composite(mainGroup, SWT.NONE);
		mainComposite.setLayout(new FillLayout(SWT.VERTICAL));

		sashForm.setWeights(new int[] { 20, 80 });

		// Load the configuration file.
		loadConfig(configFile);

		// Setup menu.
		initMenu();
	}

	/**
     * Initialises the menu.
     * 
     */
	private void initMenu()
	{
		menu = new JSeatMenu(shell, SWT.BAR);
		shell.setMenuBar(menu.getMenuBar());

		FileMenuItemListener ml = new FileMenuItemListener();
		menu.getFileNewItem().addSelectionListener(ml);
		menu.getFileOpenItem().addSelectionListener(ml);
		menu.getFileExitItem().addSelectionListener(ml);
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

		properties.put(GUI.DEFAULT_REPORT_DIR, (String) prop.get(GUI.DEFAULT_REPORT_DIR.toString()));
		properties.put(GUI.DEFAULT_VERSION_DIR, (String) prop.get(GUI.DEFAULT_VERSION_DIR.toString()));
		properties.put(GUI.DEFAULT_REPORTSET, (String) prop.get(GUI.DEFAULT_REPORTSET.toString()));
		properties.put(GUI.CONCURRENT_VER_THREADS, (String) prop.get(GUI.CONCURRENT_VER_THREADS.toString()));
	}

	/**
     * Handles selection events.
     * 
     * @author Joshua Hayes,Swinburne University (ICT),2007
     */
	class FileMenuItemListener implements SelectionListener
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
				OpenDialog od = JSeatFactory.getInstance().getJSeatOpenDialog(
						shell,
						JSeatFileType.PROJECT,
						"Open Project",
						filterPath);
				String selected = od.open();

				if (selected != null)
				{
					// Free data.
					// freeData();
					ThreadedProjectBuilder tpb = new ThreadedProjectBuilder(getVersionList(), selected);
					tpb.start();
				}
			} else if (event.getSource() == menu.getFileNewItem())
			{
				int numThreads = 1;
				try
				{
					numThreads = Integer.parseInt(properties.get(GUI.CONCURRENT_VER_THREADS));
				} catch (NumberFormatException e)
				{
					e.printStackTrace();
				} // Handle, just use 1 thread if bad input.

				NewProjectDialog npd = new NewProjectDialog(getVersionList(), numThreads);
				npd.open();
				SWTFactory.centerDialog(shell, npd.getShell());
			}
		}
	}

	protected void dispose()
	{
		shell.dispose();
	}

	/**
     * Executes the specified <code>ReportVisitor</code> on the specified
     * <code>HistoryMetricData<code>.
	 * @param rv The report to run.
	 * @param hmd The History to run the report on.
	 * @throws ReportException
     */
	void execute(ReportVisitor rv, HistoryMetricData hmd) throws ReportException
	{
		rv.addObserver(this);
		reportProgressDialog = new ProgressDialog("Analyzing Version(s)", "Please Wait...", 100);
		SWTFactory.centerDialog(shell, reportProgressDialog.getShell());
		reportProgressDialog.open();

		if (hmd != null && rv != null)
		{
			// Have model accept report.

			hmd.accept(rv);
		}
	}

	/**
     * Executes the specified <code>ReportDecorator</code> on the specified
     * <code>HistoryMetricData<code>. If the ReportDecorator has a graphical
	 *  display, it will be displayed on the specified <code>Composite</code>. 
	 * @param rv The report to run.
	 * @param hmd The History to run the report on.
	 * @throws ReportException
     */
	public void execute(final ReportDecorator rd, HistoryMetricData hmd, final Composite display)
			throws ReportException
	{
		if (rd != null && hmd != null)
		{
			// Execute the report first.
			execute(rd, hmd);

			Runnable displayChart = new Runnable()
			{
				public void run()
				{
					if (display != null && display.getChildren().length > 0)
						display.getChildren()[0].dispose();
					rd.display();
					// TODO This is a hack to force a redraw that doesn't
					// otherwise occur.
					shell.setSize(shell.getSize().x - 1, shell.getSize().y - 1);
				}
			};
			Display.getDefault().asyncExec(displayChart);
		}
	}

	/**
     * Concrete class should override this to return a list for storing
     * processed versions.
     * 
     * @return The list.
     */
	public abstract List getVersionList();

	/**
     * At present it listens for updates from an executing report and updates an
     * open progress dialog.
     */
	public void update(Observable o, Object event)
	{
		if (reportProgressDialog != null && event != null)
		{
			if (o instanceof Report)
			{
				Report report = (Report) o;
				int completion = report.getCompletion();
				reportProgressDialog.updateForMe(completion, event.toString());
			}
		}
	}
}
