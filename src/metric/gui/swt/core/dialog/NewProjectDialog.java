package metric.gui.swt.core.dialog;

import java.io.File;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import metric.core.extraction.MetricEngine;
import metric.core.vocabulary.JSeatFileType;
import metric.gui.swt.core.threading.ThreadedProjectBuilder;
import metric.gui.swt.core.util.JSeatFactory;
import metric.gui.swt.core.util.SWTFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewProjectDialog implements SelectionListener, Observer
{
	private Shell shell;
	private Display display;
	private static final String TITLE = "Create a new JSeat Project";
	private static final String PADDING = "padding to preserve width of cell before packing.......";

	private Button okButton, cancelButton, inputBrowse, outputBrowse;
	private Text projectNameText, projectInputText, projectOutputText;
	private List versions;
	private LinkedList<Observer> observers = new LinkedList<Observer>();
	private ProgressDialog progressDialog;
	private int concurrentVerThreads;
	private ThreadedProjectBuilder tpb;

	/**
     * Displays a dialog for inputting the required information to create a
     * project.
     * 
     * @param concurrentVerThreads The number of threads version processing
     *            should be scaled across.
     */
	public NewProjectDialog(int concurrentVerThreads)
	{
		this.concurrentVerThreads = concurrentVerThreads;
		display = Display.getCurrent();
		shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.TITLE);
		shell.setText(TITLE);

		shell.setLayout(new GridLayout());

		Composite nameComposite = SWTFactory.centerComposite(shell, SWT.NONE);
		SWTFactory.createLabel(nameComposite, SWT.NONE, "Project Name: ");
		projectNameText = SWTFactory.createText(
				nameComposite,
				SWT.NONE,
				PADDING,
				null);

		// Project input
		Composite inputComposite = SWTFactory.centerComposite(shell, SWT.NONE);
		SWTFactory.createLabel(inputComposite, SWT.NONE, "Project Input: ");
		projectInputText = SWTFactory.createText(
				inputComposite,
				SWT.NONE,
				PADDING,
				null);
		inputBrowse = SWTFactory.createButton(
				inputComposite,
				SWT.NONE,
				"Browse",
				this);

		// Project output
		Composite outputComposite = SWTFactory.centerComposite(shell, SWT.NONE);
		SWTFactory.createLabel(outputComposite, SWT.NONE, "Project File: ");
		projectOutputText = SWTFactory.createText(
				outputComposite,
				SWT.NONE,
				PADDING,
				null);
		outputBrowse = SWTFactory.createButton(
				outputComposite,
				SWT.NONE,
				"Browse",
				this);

		// Decision
		Composite actionComposite = SWTFactory.centerComposite(shell, SWT.NONE);
		okButton = SWTFactory.createButton(
				actionComposite,
				SWT.NONE,
				"Ok",
				this);
		cancelButton = SWTFactory.createButton(
				actionComposite,
				SWT.NONE,
				"Cancel",
				this);

		shell.pack();

		// Clear padding information.
		projectInputText.setText("");
		projectOutputText.setText("");
		projectNameText.setText("");
//		 projectInputText.setText("B:\\workspace\\builds\\groovy\\groovy.ver");
//		 projectOutputText.setText("D:\\MyGroovyProject\\TestGroovy.jpf");
//		 projectNameText.setText("TestGroovy");

		// Setup verification and modification rules for the project name and
        // project output. This keeps them synchronized.
		final ModifyListener projectNameModifyListener = setupProjectNameModifyListener();
		projectNameText.addModifyListener(projectNameModifyListener);

		setupProjectOutputVerifyListener();
		setupProjectOutputModifyListener(projectNameModifyListener);
	}

	private void setupProjectOutputModifyListener(
			final ModifyListener projectNameModifyListener)
	{
		projectOutputText.addModifyListener(new ModifyListener()
		{

			public void modifyText(ModifyEvent event)
			{
				// Offset the file separate index by 1 to skip past the \.
				int startIndex = projectOutputText.getText().lastIndexOf(
						File.separator) + 1;
				int endIndex = projectOutputText.getText().indexOf(
						JSeatFileType.PROJECT.toString());
				if (endIndex > 0)
				{
					String filename = projectOutputText.getText().substring(
							startIndex,
							endIndex);

					// Remove update listener whilst we change project name to
					// stop a nasty cyclic stack overflow :)
					projectNameText
							.removeModifyListener(projectNameModifyListener);
					projectNameText.setText(filename);
					projectNameText
							.addModifyListener(projectNameModifyListener);
				}
			}
		});
	}

	private void setupProjectOutputVerifyListener()
	{
		projectOutputText.addVerifyListener(new VerifyListener()
		{

			public void verifyText(VerifyEvent event)
			{
				String filename = projectOutputText.getText();

				if (event.text == "")
				{
					String removed = filename.substring(event.start, event.end);
					filename = filename.replace(removed, "");
					if (filename.indexOf(JSeatFileType.PROJECT.toString()) == -1)
						event.doit = false;
				} else
				{
					String start = filename.subSequence(0, event.start)
							.toString();
					String end = filename.subSequence(
							event.end,
							filename.length()).toString();
					filename = start + event.text + end;
					if (filename.indexOf(JSeatFileType.PROJECT.toString()) == -1)
						event.doit = false;
				}
			}

		});
	}

	private ModifyListener setupProjectNameModifyListener()
	{
		// We declare it here with a reference and not as an anonymous inner
		// method so we have a reference to it. This allows us to remove
		// the listener when we do an update and re-add it again.
		final ModifyListener projectNameModifyListener = new ModifyListener()
		{
			public void modifyText(ModifyEvent event)
			{
				int caretPos = projectNameText.getCaretPosition();
				// Is empty, so set to project name for now.
				if (projectOutputText.getText().indexOf(File.separator) == -1)
				{
					projectOutputText.setText(projectNameText.getText()
							+ JSeatFileType.PROJECT.toString());
				} else
				// User has already selected a output file and is changing
				// project name.
				{
					File tmp = new File(projectOutputText.getText());
					String newFileName = tmp.getParent() + File.separator
							+ projectNameText.getText()
							+ JSeatFileType.PROJECT.toString();
					projectOutputText.setText(newFileName);
				}
				projectNameText.setSelection(caretPos);
			}
		};
		return projectNameModifyListener;
	}

	/**
     * Displays a dialog for inputting the required information to create a
     * project.
     * 
     * @param versions The list the processed versions will be put on.
     * @param concurrentVerThreads The number of threads version processing
     *            should be scaled across.
     */
	public NewProjectDialog(List versions, int concurrentVerThreads)
	{
		this(concurrentVerThreads);
		this.versions = versions;
	}

	public void addObserver(Observer observer)
	{
		observers.add(observer);
	}

	public void open()
	{
		shell.open();
	}

	public Shell getShell()
	{
		return shell;
	}

	public void widgetDefaultSelected(SelectionEvent event)
	{
	} // Not interested in this

	public void widgetSelected(SelectionEvent event)
	{
		if (event.getSource() == inputBrowse)
		{
			openOpenDialog();
		} else if (event.getSource() == outputBrowse)
		{
			openSaveDialog();
		} else if (event.getSource() == cancelButton)
		{
			shell.dispose();
		} else if (event.getSource() == okButton)
		{
			// Begin creating new project.
			tpb = new ThreadedProjectBuilder(versions,
					projectInputText.getText(), projectOutputText.getText(),
					concurrentVerThreads);
			tpb.addObserver(this);

			// Open a progress dialog.
			progressDialog = new ProgressDialog("Creating new project",
					"Please Wait...", 100);
			SWTFactory.centerDialog(shell, progressDialog.getShell());
			progressDialog.getCancelButton().addSelectionListener(this);
			progressDialog.open();
			// Start processing to create project.
			tpb.start();
			shell.dispose();
		} else if (event.getSource() == progressDialog.getCancelButton())
		{
			tpb.interrupt();
			progressDialog.dispose();
			tpb = null;
		}
	}

	private void openSaveDialog()
	{
		SaveDialog sd = JSeatFactory.getInstance().getJSeatSaveDialog(
				shell,
				JSeatFileType.PROJECT,
				"Save Project",
				null,
				projectNameText.getText());
		String selected = sd.open();
		if (selected != null)
			projectOutputText.setText(selected);
	}

	private void openOpenDialog()
	{
		OpenDialog od = JSeatFactory.getInstance().getJSeatOpenDialog(
				shell,
				JSeatFileType.VERSION,
				"Open Version File",
				null);
		String selected = od.open();
		if (selected != null)
			projectInputText.setText(selected);
	}

	public void update(Observable ob, Object o)
	{
		if (ob instanceof MetricEngine)
		{
			MetricEngine me = (MetricEngine) ob;
			progressDialog.updateForMe(me.getCompletion(), o.toString());
		}
	}

	class SynchronizeProjectInput implements SelectionListener
	{

		public void widgetDefaultSelected(SelectionEvent arg0)
		{
		} // Not

		public void widgetSelected(SelectionEvent arg0)
		{
			// TODO Auto-generated method stub

		}

	}
}
