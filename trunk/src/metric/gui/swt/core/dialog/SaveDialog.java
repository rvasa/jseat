package metric.gui.swt.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Provides a basic Save style dialog for saving files
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class SaveDialog
{
	private String pathToSave;
	private FileDialog fd;
	private static final String TITLE = "Save";

	public SaveDialog(Shell shell)
	{
		fd = new FileDialog(shell, SWT.SAVE);
		fd.setText(TITLE);
	}

	public SaveDialog(Shell shell, String[] filterExtensions, String[] filterNames, String filterPath)
	{
		this(shell);
		if (filterExtensions != null)
			fd.setFilterExtensions(filterExtensions);
		if (filterNames != null)
			fd.setFilterNames(filterNames);
		if (filterPath != null)
			fd.setFilterPath(filterPath);
	}

	public String open()
	{
		pathToSave = fd.open();
		return pathToSave;
	}

	public Shell getParent()
	{
		return fd.getParent();
	}

	public String getPath()
	{
		return pathToSave;
	}

	public void setTitle(String title)
	{
		fd.setText(title);
	}

	public void setFileName(String filename)
	{
		fd.setFileName(filename);
	}
}
