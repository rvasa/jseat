package metric.gui.swt.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * Provides a basic progres dialog for time consuming tasks. If the task this
 * progress dialog is being used to represent takes a lengthy period of time to
 * compute, a selection listener should be attached to the cancel button
 * allowing the task to be canceled.
 * 
 * UPDATING: Two update methods are provided, update() and updateForMe(). The
 * former is not thread safe and should only be called if the running thread is
 * the main GUI thread. The latter method; updateForMe(), can safely be called
 * from any thread as the dialog itself looks after ensuring it is updated on
 * the correct thread.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class ProgressDialog
{
	private Shell shell;
	private Display display;
	private ProgressBar pb;
	private Button cancelButton;

	private Label message, lineLabel, statusLabel, waitMsg;
	private int max;

	public ProgressDialog(String title, String waitMsg, int max)
	{
		this.max = max;
		display = Display.getDefault();
		shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.TITLE);
		shell.setText(title);

		GridLayout layout = new GridLayout();
		shell.setLayout(layout);

		message = new Label(shell, SWT.NONE);
		message.setText("Padding to keep some space before packing composite");
		message.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true,
				false));

		pb = new ProgressBar(shell, SWT.NONE);
		pb.setMaximum(max);
		pb.setMaximum(0);
		pb.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false,
				false));

		lineLabel = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
		lineLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				false, false));

		Composite bottomComposite = new Composite(shell, SWT.NONE);
		bottomComposite.setLayout(new GridLayout(4, false));
		bottomComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		statusLabel = new Label(bottomComposite, SWT.NONE);
		statusLabel.setText("0%  ");
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		statusLabel.setLayoutData(new GridData());
		
		this.waitMsg = new Label(bottomComposite, SWT.NONE);
		this.waitMsg.setText(waitMsg);
		GridData extraGridData = new GridData();
		extraGridData.horizontalSpan = 2;
		extraGridData.horizontalAlignment = GridData.BEGINNING;
		extraGridData.grabExcessHorizontalSpace = true;
		this.waitMsg.setLayoutData(extraGridData);

		cancelButton = new Button(bottomComposite, SWT.PUSH);
		cancelButton.setText("Cancel");
		GridData gd2 = new GridData();
		gd2.horizontalAlignment = GridData.END;
		cancelButton.setLayoutData(gd2);

		shell.setMinimumSize(250, SWT.DEFAULT);
		shell.pack();
		message.setText("");
	}

	public Shell getShell()
	{
		return shell;
	}

	public void open()
	{
		shell.open();
	}

	// public void centerDialog(Shell shell)
	// {
	// Rectangle parentSize = shell.getBounds();
	// Rectangle mySize = this.shell.getBounds();
	//
	// int x, y;
	// x = (parentSize.width - mySize.width) / 2 + parentSize.x;
	// y = (parentSize.height - mySize.height) / 2 + parentSize.y;
	//
	// this.shell.setLocation(x, y);
	// }

	/**
     * This should be run if the calling thread is not the event dispatcher (GUI
     * Thread). This allows another worker thread to ask the progress panel to
     * update itself safely. The dialog will automatically be closed when
     * progress has reached maximum (whatever that is).
     * 
     * @param value The value the progress bar should be set to.
     */
	public void updateForMe(final int value)
	{
		Runnable updateProgress = new Runnable()
		{
			public void run()
			{
				if (!shell.isDisposed())
				{
					pb.setSelection(value);
					statusLabel.setText(value + "%");

					// We are done. Dispose dialog.
					if (value == max)
						shell.dispose();
				}
			}
		};
		Display.getDefault().syncExec(updateProgress);
	}
	
	/**
     * This should be run if the calling thread is not the event dispatcher (GUI
     * Thread). This allows another worker thread to ask the progress panel to
     * update itself safely. The dialog will automatically be closed when
     * progress has reached maximum (whatever that is).
     * 
     * @param value The value the progress bar should be set to.
     * @param msg The msg that should be displayed.
     */
	public void updateForMe(final int value, final String msg)
	{
		Runnable updateProgress = new Runnable()
		{
			public void run()
			{
				if (!shell.isDisposed())
				{
					pb.setSelection(value);
					statusLabel.setText(value + "%");
					message.setText(msg);
					// We are done. Dispose dialog.
					if (value == max)
						shell.dispose();
				}
			}
		};
		Display.getDefault().syncExec(updateProgress);
	}

	/**
     * This is not thread safe. If calling this method, you muse ensure you are
     * only caling it from the main GUI thread. Otherwise, use 'updateForMe(int
     * value)' instead.
     * 
     * @param value The value to set.
     */
	public void update(int value)
	{
		pb.setSelection(value);
		statusLabel.setText(value + "%");
	}

	/**
     * This is useful so that a selection listener can be attached.
     * 
     * @return The Cancel Button used on this dialog.
     */
	public Button getCancelButton()
	{
		return cancelButton;
	}

	public void dispose()
	{
		shell.dispose();
	}
}
