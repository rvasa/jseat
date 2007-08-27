package metric.core.report.decorator;

import java.util.Collection;
import java.util.LinkedList;

import metric.core.report.visitor.ReportVisitor;

/**
 * Provides a composite implementation for <code>CompositeDecorators</code>,
 * allowing each added CompositeDecorator a chance to decorate the underlying
 * </code>ReportVisitor</code>.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class CompositeReportDecorator extends ReportDecorator
{
	private Collection<ReportDecorator> decorators;

	public CompositeReportDecorator(ReportVisitor decoratedReport)
	{
		super(decoratedReport);
		decorators = new LinkedList<ReportDecorator>();
	}

	public void add(ReportDecorator decorator)
	{
		decorators.add(decorator);
	}

	public boolean remove(ReportDecorator decorator)
	{
		return decorators.remove(decorator);
	}

	@Override
	public void display()
	{
		for (ReportDecorator decorator : decorators)
		{
			decorator.display();
		}
	}

}
