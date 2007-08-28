package metric.gui.swt.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class JSeatMenu
{
	private final Menu menuBar, fileMenu, aboutMenu; //,  visualizationMenu;
	
	private final MenuItem fileMenuHeader, fileNewItem, fileOpenItem, fileExitItem;
//	private final MenuItem visualizationMenuHeader;
	private final MenuItem helpMenuHeader, aboutMenuItem;
	
	public JSeatMenu(Shell shell, int type)
	{

		// Setup file menu.
		menuBar = new Menu(shell, SWT.BAR);
		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&File");
		fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);


		fileNewItem = new MenuItem(fileMenu, SWT.PUSH);
		fileNewItem.setText("&New Project\tCtrl+N");
		fileNewItem.setAccelerator(SWT.CTRL + 'N');

		fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenItem.setText("&Open Project...\tCtrl+O");
		fileOpenItem.setAccelerator(SWT.CTRL + 'O');

		final MenuItem separator = new MenuItem(fileMenu, SWT.SEPARATOR);
		
		fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText("E&xit");

		helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText("&Help");
		
		aboutMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuHeader.setMenu(aboutMenu);

		aboutMenuItem = new MenuItem(aboutMenu, SWT.PUSH);
		aboutMenuItem.setText("About");

//		visualizationMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
//		visualizationMenuHeader.setText("Visualization");
//		visualizationMenu = new Menu(shell, SWT.DROP_DOWN);
//		visualizationMenuHeader.setMenu(visualizationMenu);
	}
	
//	public Menu getVisualizationMenu()
//	{
//		return visualizationMenu;
//	}
	
	/**
	 * @return the fileExitItem
	 */
	public final Menu getMenuBar()
	{
		return menuBar;
	}

	/**
	 * @return the fileExitItem
	 */
	public final MenuItem getFileExitItem()
	{
		return fileExitItem;
	}

	/**
	 * @return the fileNewItem
	 */
	public final MenuItem getFileNewItem()
	{
		return fileNewItem;
	}

	/**
	 * @return the fileOpenItem
	 */
	public final MenuItem getFileOpenItem()
	{
		return fileOpenItem;
	}
}
