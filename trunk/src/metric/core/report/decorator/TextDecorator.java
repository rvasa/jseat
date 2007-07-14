package metric.core.report.decorator;

import java.util.logging.Level;
import java.util.logging.Logger;

import metric.core.report.visitor.ReportVisitor;
import metric.core.util.MetricTable;
import metric.core.util.logging.LogOrganiser;

/**
 * Provides a textual representation of the underlying ReportVisitor.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 * 
 */
public class TextDecorator extends ReportDecorator
{
	private Logger logger;
	
	public TextDecorator(ReportVisitor decoratedReport)
	{
		super(decoratedReport);
		
		logger = Logger.getLogger(getClass().getSimpleName());
		LogOrganiser.addLogger(logger);
		logger.setLevel(Level.ALL);
	}

	public void display()
	{
		MetricTable t = decoratedReport.getTable();
		logger.log(Level.ALL, t.toString());
	}
}
