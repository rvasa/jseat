package metric.gui.swt.core.util;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * A collection of handy utils when working in SWT.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class SWTFactory
{
	/**
     * Centers the child shell in the parent shell.
     * 
     * @param parent the parent shell.
     * @param child the child shell.
     */
	public static void centerDialog(Shell parent, Shell child)
	{
		Rectangle parentSize = parent.getBounds();
		Rectangle childSize = child.getBounds();

		int x, y;
		x = (parentSize.width - childSize.width) / 2 + parentSize.x;
		y = (parentSize.height - childSize.height) / 2 + parentSize.y;

		child.setLocation(x, y);
	}

	public static void hookDisposeLisener(Widget parent, final Widget widget)
	{
		parent.addDisposeListener(new DisposeListener()
		{

			public void widgetDisposed(DisposeEvent arg0)
			{
				widget.dispose();
			}

		});
	}

	public static Button createButton(Composite parent, int type, String text, SelectionListener listener)
	{
		Button b = new Button(parent, type);
		b.setText(text);
		if (listener != null)
			b.addSelectionListener(listener);
		return b;
	}

	public static Label createLabel(Composite parent, int type, String text)
	{
		Label l = new Label(parent, type);
		l.setText(text);
		return l;
	}

	public static Group createGroup(Composite parent, int type, String text, Layout layout, Object layoutData)
	{
		Group g = new Group(parent, type);
		if (layout != null)
			g.setLayout(layout);
		if (layoutData != null)
			g.setLayoutData(layoutData);
		g.setText(text);
		return g;
	}

	public static Text createText(Composite parent, int type, String text, Object layoutData)
	{
		Text t = new Text(parent, type);
		if (layoutData != null)
			t.setLayoutData(layoutData);
		t.setText(text);
		return t;
	}

	public static List createList(Composite parent, int type, Object layoutData)
	{
		List l = new List(parent, type);
		if (layoutData != null)
			l.setLayoutData(layoutData);
		return l;
	}

	public static Combo createCombo(Composite parent, int type, int visibleItems, Layout layout, Object layoutData)
	{
		Combo c = new Combo(parent, type);
		c.setVisibleItemCount(visibleItems);
		if (layout != null)
			c.setLayout(layout);
		if (layoutData != null)
			c.setLayoutData(layoutData);
		return c;
	}

	public static GridData createGridData(int type, int heightHint, int widthHint, int verticalAlign)
	{
		GridData gd = new GridData(type);
		gd.heightHint = heightHint;
		gd.widthHint = widthHint;
		gd.verticalAlignment = verticalAlign;
		return gd;
	}

	public static Composite centerComposite(Composite parent, int type)
	{
		Composite cComp = new Composite(parent, type);
		cComp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		RowLayout rowLayout = new RowLayout();
		rowLayout.justify = true;
		// rowLayout.pack = true;
		// rowLayout.fill = true;
		cComp.setLayout(rowLayout);
		return cComp;
	}
}
