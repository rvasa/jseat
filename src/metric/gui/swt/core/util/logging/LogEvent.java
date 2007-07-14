package metric.gui.swt.core.util.logging;

import java.util.Observable;

public class LogEvent extends Observable
{
	public String message;
	public Object[] params;
	public String source;
	public int status;
	
	public LogEvent(String source, String message, Object[] params, int status)
	{
		this.source = source;
		this.message = message;
		this.status = status;
		this.params = params;
	}
	
	
}
