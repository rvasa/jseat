package metric.gui.swt.core.threading;

import java.util.logging.Level;
import java.util.logging.Logger;

import metric.core.ReportDefinitionRepository;
import metric.core.util.logging.LogOrganiser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * Creates a <code>ReportDefinitionRepository</code> on its own Thread and
 * updates the specified report configuration Combo box with the repository once
 * it has been created.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class ThreadedDefinitionProcessor extends Thread
{
	private final String filename;
	private Combo reportConfigs;

	private Logger logger = Logger.getLogger("DefProcessor");

	public ThreadedDefinitionProcessor(Combo reportConfigs, String filename)
	{
		this.filename = filename;
		this.reportConfigs = reportConfigs;

		LogOrganiser.addLogger(logger);
		logger.setLevel(Level.ALL);
	}

	@Override
	public void run()
	{
		// Create repository
		final ReportDefinitionRepository mdr = new ReportDefinitionRepository(filename);

		// Send information safely to the gui and have it update itself.
		Runnable r = new Runnable()
		{
			public void run()
			{
				reportConfigs.add(mdr.toString());
				reportConfigs.setData(mdr.toString(), mdr);

				// Set report config just added to the currently selected one.
				reportConfigs.select(reportConfigs.indexOf(mdr.toString()));
				// Fake selection event to force report selection combo to
				// update.
				reportConfigs.notifyListeners(SWT.Selection, new Event());
			}
		};
		Display.getDefault().asyncExec(r);
	}
}