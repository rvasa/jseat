package metric.core.exception;

/**
 * This should be thrown if a <code>ReportDefinition</code> is not well
 * formed.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class MalformedReportDefinition extends Exception
{
	private static final long serialVersionUID = 1L;

	public MalformedReportDefinition()
	{
	}

	public MalformedReportDefinition(String msg)
	{
		super(msg);
	}
}
