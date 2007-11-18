package metric.gui.swt.explorer;

import java.io.IOException;
import java.util.HashMap;

import metric.core.exception.ReportException;
import metric.core.model.HistoryMetricData;
import metric.core.report.Report;
import metric.core.report.decorator.ReportDecorator;
import metric.core.report.decorator.TextDecorator;
import metric.gui.swt.core.JSeatGUI;
import metric.gui.swt.core.vocabulary.GUI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class JSeatExplorer extends JSeatGUI implements SelectionListener
{
	private static final String TITLE = "JSeat Explorer";
	private static final String CONFIG = "jseat.conf";

	private CenterComposite cc;
	private LeftComposite lc;

	public JSeatExplorer(Shell shell, String title, String configFile) throws IOException
	{
		super(shell, title, configFile);

		// The left composite holds version and reporting components.
		lc = new LeftComposite(navComposite, SWT.BORDER, properties.get(GUI.DEFAULT_REPORTSET));

		cc = new CenterComposite(mainComposite, SWT.NONE);
		// We shall listen for clear and execute events.
		cc.addExecuteListener(this);
		cc.addClearListener(this);

		// We use more horizontal space than vertical, suited to widescreen
		// Not any particular format though.
		shell.setSize(1024, 600);

		// This stuff is required to be setup by JSeatGUI
		navComposite.setBackgroundMode(SWT.NONE);
		mainComposite.setBackgroundMode(SWT.NONE);
		
		// Override weighting of sash panels.
		sashForm.setWeights(new int[] { 33, 67 });
	}

	public static void main(String[] args) throws IOException
	{
		Display display = new Display();
		Shell shell = new Shell(display);
		final JSeatExplorer jseat = new JSeatExplorer(shell, TITLE, CONFIG);

		shell.addListener(SWT.Close, new Listener()
		{
			public void handleEvent(Event event)
			{
				jseat.dispose();
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

	public void widgetDefaultSelected(SelectionEvent arg0)
	{
	} // Not interested in this.

	public void widgetSelected(SelectionEvent event)
	{
		if (event.widget.toString().indexOf("Clear") != -1)
		{
		} else if (event.widget.toString().indexOf("Execute") != -1)
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

				try
				{
					execute(rd, hmd, null);
				} catch (ReportException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public List getVersionList()
	{
		return lc.getVersionList();
	}

}
