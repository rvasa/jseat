package metric.core;

public class WorkStatus<T>
{
	private String msg;
	private int value;
	private T originalSource;
	
	public WorkStatus(T originalSource, String msg, int value)
	{
		this.originalSource = originalSource;
		this.msg = msg;
		this.value = value;
	}

	/**
	 * @return the msg
	 */
	public final String getMsg()
	{
		return msg;
	}

	/**
	 * @return the value
	 */
	public final int getValue()
	{
		return value;
	}

	/**
	 * @return the originalSource
	 */
	public final T getOriginalSource()
	{
		return originalSource;
	}
}
