package metric.gui.swt.visualizer;

import metric.core.report.decorator.ReportDecorator;
import metric.core.report.visitor.ReportVisitor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Binds one or more visualizations to a Menu so that it may be added to, and
 * selected from other Menu's.
 * 
 * A <code>Visualization</code> uses a <code>ReportVisitor</code> to
 * generate the underlying metric table and a <code>ReportDecorator</code> to
 * provide a visual representation.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class Visualization
{
	private Menu visMenu;
	private MenuItem visMenuHeader;

	/**
     * Creates a new visualization with the specified name, binding it to the
     * specified parent.
     * 
     * @param parent The parent of this visualization.
     * @param name The name.
     * @param type The type of menu.<br />
     *            Use SWT.DROP_DOWN if using a drop down menu.<br />
     *            For a leaf item use SWT.NONE
     */
	public Visualization(Menu parent, String name, int type)
	{
		if (type == SWT.DROP_DOWN)
		{
			visMenuHeader = new MenuItem(parent, SWT.CASCADE);
			visMenuHeader.setText(name);
			visMenu = new Menu(parent);
			visMenuHeader.setMenu(visMenu);
		} else
			visMenuHeader = addMenuItem(parent, name);
	}

	public void addSubVisualization(String name, ReportVisitor rv, ReportDecorator rd, SelectionListener listener)
	{
		MenuItem menuItem = addMenuItem(visMenu, name);
		menuItem.setData("report", rv);
		menuItem.setData("decorator", rd);
		menuItem.addSelectionListener(listener);
	}

	private MenuItem addMenuItem(Menu parent, String name)
	{
		MenuItem visMenuItem = new MenuItem(parent, SWT.RADIO);
		visMenuItem.setText(name);
		return visMenuItem;
	}

	public void setReport(ReportVisitor report)
	{
		visMenuHeader.setData("report", report);
	}

	public void setDecorator(ReportDecorator decorator)
	{
		visMenuHeader.setData("decorator", decorator);
	}

	public void addSelectionListener(SelectionListener listener)
	{
		visMenuHeader.addSelectionListener(listener);
	}
}
