package metric.gui.swt.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Provides a basic Open style dialog for opening files
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class OpenDialog
{
	private String fileToOpen;
	private FileDialog fd;
	private static final String TITLE = "Open";

	public OpenDialog(Shell shell)
	{
		fd = new FileDialog(shell, SWT.OPEN);
		fd.setText(TITLE);
	}

	public OpenDialog(Shell shell, String[] filterExtensions, String[] filterNames, String filterPath)
	{
		this(shell);
		if (filterExtensions != null)
			fd.setFilterExtensions(filterExtensions);
		if (filterNames != null)
			fd.setFilterNames(filterNames);
		if (filterPath != null)
			fd.setFilterPath(filterPath);
	}

	public void setTitle(String title)
	{
		fd.setText(title);
	}

	public void setFilerExtensions(String[] filters)
	{
		fd.setFilterExtensions(filters);
	}

	public void setFilterPath(String filterPath)
	{
		fd.setFilterPath(filterPath);
	}

	public Shell getParent()
	{
		return fd.getParent();
	}

	public String open()
	{
		fileToOpen = fd.open();
		return fileToOpen;
	}

	public String getPath()
	{
		return fileToOpen;
	}

	public void setFileName(String filename)
	{
		fd.setFileName(filename);
	}
}
