package metric.gui.swt.visualizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class VisMenu
{
	private Menu menuBar, fileMenu, aboutMenu,  visualizationMenu;
	
	private MenuItem fileMenuHeader, fileNewItem, fileOpenItem, fileExitItem;
	private MenuItem visualizationMenuHeader;
	private MenuItem helpMenuHeader, aboutMenuItem;
	
	public VisMenu(Shell shell, int type)
	{

		// Setup file menu.
		menuBar = new Menu(shell, SWT.BAR);
		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&File");
		fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);


		fileNewItem = new MenuItem(fileMenu, SWT.PUSH);
		fileNewItem.setText("New");

		fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenItem.setText("&Open File...");

		fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText("E&xit");

		helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText("Help");
		aboutMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuHeader.setMenu(aboutMenu);

		aboutMenuItem = new MenuItem(aboutMenu, SWT.PUSH);
		aboutMenuItem.setText("About");

		visualizationMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		visualizationMenuHeader.setText("Visualization");
		visualizationMenu = new Menu(shell, SWT.DROP_DOWN);
		visualizationMenuHeader.setMenu(visualizationMenu);
	}
	
	public Menu getVisualizationMenu()
	{
		return visualizationMenu;
	}
	
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
