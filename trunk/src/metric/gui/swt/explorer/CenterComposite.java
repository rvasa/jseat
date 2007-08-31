package metric.gui.swt.explorer;

import java.util.Observable;
import java.util.Observer;

import metric.core.util.EnumTable;
import metric.core.util.logging.LogOrganiser;
import metric.gui.swt.core.util.SWTFactory;
import metric.gui.swt.core.util.logging.LogEvent;
import metric.gui.swt.core.util.logging.SWTGuiHandler;
import metric.gui.swt.core.vocabulary.GUI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * Represents the center, or main composite in this GUI. This includes the main
 * output area and clear/execute buttons.
 * 
 * Acts as an <code>Observer</code> to the <code>GuiLogHandler</code>,
 * which is necessary to capture log events and safely display them in the GUI.
 * 
 * Is responsible for handling any listener events generated by components in
 * this class.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class CenterComposite extends Composite implements Observer, SelectionListener
{
	private Text tConsole;
	private Button bClear, bExecute;
	private Composite consoleComposite, displayComposite, executeComposite;
	private TabFolder tabFolder;

	public CenterComposite(Composite composite, int type)
	{
		super(composite, type);

		GridLayout gridLayout = new GridLayout();
		gridLayout.marginTop = 0;
		gridLayout.marginBottom = 0;
		setLayout(gridLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumWidth = composite.getSize().x / 2 + composite.getSize().x / 6;
		setLayoutData(gd);

		GridData outputGroupGrid = new GridData(GridData.FILL_BOTH);
		outputGroupGrid.widthHint = composite.getSize().x / 2 + composite.getSize().y / 4;

		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayoutData(outputGroupGrid);
		tabFolder.setLayout(new GridLayout());
		// tabFolder.addSelectionListener(this);

		TabItem consoleTabItem = new TabItem(tabFolder, SWT.NONE);
		consoleTabItem.setText("Console");
		TabItem displayTabItem = new TabItem(tabFolder, SWT.NONE);
		displayTabItem.setText("Display");

		consoleComposite = new Composite(tabFolder, SWT.NONE);
		consoleComposite.setLayout(new GridLayout());
		consoleComposite.setLayoutData(outputGroupGrid);

		tConsole = new Text(consoleComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		tConsole.setLayoutData(new RowData(composite.getSize().x / 2, 20));
		tConsole.setLayoutData(new GridData(GridData.FILL_BOTH));

		tConsole.setFont(new Font(composite.getDisplay(), "Courier New", 10, SWT.NORMAL));

		// executeComposite = SWTUtils.centerComposite(consoleComposite,
        // SWT.NONE);
		// bClear = SWTUtils.createButton(executeComposite, SWT.PUSH, "Clear",
        // this);
		// bExecute = SWTUtils.createButton(executeComposite, SWT.PUSH,
        // "Execute", this);
		// SWTUtils.hookDisposeLisener(executeComposite, bClear);
		// SWTUtils.hookDisposeLisener(executeComposite, bExecute);
		createExecuteComposite(consoleComposite);
		consoleTabItem.setControl(consoleComposite);

		displayComposite = new Composite(tabFolder, SWT.NONE);
		displayComposite.setLayout(new FillLayout());
		displayComposite.setLayoutData(outputGroupGrid);
		displayTabItem.setControl(displayComposite);

		pack();

		// Initialise gui handler.
		SWTGuiHandler handler = new SWTGuiHandler();
		handler.addObserver(this);
		LogOrganiser.addHandler(handler);
	}

	private void createExecuteComposite(Composite parent)
	{
		if (executeComposite != null)
			executeComposite.dispose();
		executeComposite = SWTFactory.centerComposite(parent, SWT.NONE);
		bClear = SWTFactory.createButton(executeComposite, SWT.PUSH, "Clear", this);
		bExecute = SWTFactory.createButton(executeComposite, SWT.PUSH, "Execute", null);
		SWTFactory.hookDisposeLisener(executeComposite, bClear);
		SWTFactory.hookDisposeLisener(executeComposite, bExecute);
		executeComposite.redraw();
	}

	public void addExecuteListener(SelectionListener listener)
	{
		bExecute.addSelectionListener(listener);
	}

	public void addClearListener(SelectionListener listener)
	{
		bClear.addSelectionListener(listener);
	}

	public void update(final Observable observable, Object arg1)
	{
		if (observable instanceof LogEvent)
		{
			Runnable toRun = new Runnable()
			{
				public void run()
				{
					LogEvent e = (LogEvent) observable;
					if (e.status == GUI.REQ_OAREA_UPDATE.getValue())
					{
						if (e.params != null)
						{
							if (e.params[0] instanceof EnumTable)
							{
								EnumTable et = (EnumTable) e.params[0];
								tConsole.append(et.toString());
							}
						} else
							tConsole.append(e.message);
					}
					return;
				}
			};
			Display.getDefault().asyncExec(toRun);
		}
	}

	/**
     * @return The composite used to display charts.
     */
	public Composite getDisplayArea()
	{
		return displayComposite;
	}

	public void widgetDefaultSelected(SelectionEvent arg0)
	{
	} // Not interested in this.

	public void widgetSelected(SelectionEvent event)
	{
		if (event.getSource() == bClear)
		{
			// clears the main text area.;
			tConsole.setText("");
		}
	}

}