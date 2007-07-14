package metric.core.exception;

/**
 * Thrown by a MetricConverter if any problem from which it cannot recover is
 * encountered.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 * 
 */
public class ConversionException extends Exception
{
	private static final long serialVersionUID = 1L;

	public ConversionException()
	{
	}

	public ConversionException(String msg)
	{
		super(msg);
	}
}
