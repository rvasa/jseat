package metric.core.util.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ConsoleHandler extends Handler
{

	private StringBuffer sb;
	
	public ConsoleHandler()
	{
		sb = new StringBuffer();
	}
	
	@Override
	public void close() throws SecurityException {
		sb = null; // Just free the stringbuffer.
	}

	@Override
	public void flush() {
		System.out.println(sb.toString());
		sb.delete(0, sb.length());
	}

	@Override
	public void publish(LogRecord log) {
		if (log.getLevel() == Level.FINER)
		{
//			if (log.getMessage().indexOf(GUI.HISTORY_PROGRESS.toString()) != -1) // history progress.
//				sb.append("."); // standard console progress dot.
//			if (log.getMessage().indexOf(Version.REPORT_PROGRESS.toString()) != -1) // report progress.
//				sb.append("."); // standard console progress dot.
			return;
		}
		else
			sb.append(log.getMessage()+"\n");
		flush(); // autoflush for now.
	}

}
