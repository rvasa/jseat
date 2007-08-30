package metric.core.util.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ConsoleHandler extends Handler
{

	private StringBuffer sb;

	public ConsoleHandler()
	{
		sb = new StringBuffer();
	}

	@Override
	public void close() throws SecurityException
	{
		sb = null; // Just free the stringbuffer.
	}

	@Override
	public void flush()
	{
		System.out.print(sb.toString());
		sb.delete(0, sb.length());
	}

	@Override
	public void publish(LogRecord log)
	{
		synchronizedPublish(log);
	}

	// This is to synchronize multiple threads logging messages.
	private synchronized void synchronizedPublish(LogRecord log)
	{
		sb.append(log.getMessage() + "\n");
		flush(); // autoflush for now.
	}

}
