package metric.core.exception;

/**
 * This should be thrown by a concrete <code>ReportVisitor</code> if it is
 * unable to make sense of the provided ReportDefinition or otherwise encounters
 * a problem.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class ReportException extends Exception
{
	private static final long serialVersionUID = 1L;

	public ReportException(String msg)
	{
		super(msg);
	}

	public ReportException()
	{
	};
}
