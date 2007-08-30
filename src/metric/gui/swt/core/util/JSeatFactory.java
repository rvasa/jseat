package metric.gui.swt.core.util;

import metric.core.vocabulary.JSeatFileType;
import metric.gui.swt.core.dialog.OpenDialog;
import metric.gui.swt.core.dialog.SaveDialog;

import org.eclipse.swt.widgets.Shell;

public class JSeatFactory
{
	private static JSeatFactory factory;

	public static JSeatFactory getInstance()
	{
		if (factory == null)
			factory = new JSeatFactory();
		return factory;
	}

	public OpenDialog getJSeatOpenDialog(Shell shell, JSeatFileType type, String title, String filterPath)
	{
		String[] exts = { type.getExtension() };
		String[] names = { type.getExtensionName() };
		OpenDialog od = new OpenDialog(shell, exts, names, filterPath);
		od.setTitle(title);
		return od;
	}

	public OpenDialog getJSeatOpenDialog(Shell shell, JSeatFileType type, String title, String filterPath,
			String filename)
	{
		String[] exts = { type.getExtension() };
		String[] names = { type.getExtensionName() };
		OpenDialog od = new OpenDialog(shell, exts, names, filterPath);
		od.setTitle(title);
		od.setFileName(filename);
		return od;
	}

	public SaveDialog getJSeatSaveDialog(Shell shell, JSeatFileType type, String title, String filterPath)
	{
		String[] exts = { type.getExtension() };
		String[] names = { type.getExtensionName() };
		SaveDialog sd = new SaveDialog(shell, exts, names, filterPath);
		sd.setTitle("Save Project");
		return sd;
	}

	public SaveDialog getJSeatSaveDialog(Shell shell, JSeatFileType type, String title, String filterPath,
			String filename)
	{
		String[] exts = { type.getExtension() };
		String[] names = { type.getExtensionName() };
		SaveDialog sd = new SaveDialog(shell, exts, names, filterPath);
		sd.setFileName(filename);
		sd.setTitle("Save Project");
		return sd;
	}
}
