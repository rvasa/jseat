package metric.core.util.logging;

import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogOrganiser
{
	private static LinkedList<Logger> loggers;
	private static LinkedList<Handler> handlers;

	static
	{
		loggers = new LinkedList<Logger>();
		handlers = new LinkedList<Handler>();
	}

	public static void addHandler(Handler handler)
	{
		// This handler has requested to be notifed
		// of logs generated from the loggers registered
		// with this log organiser.
		handlers.add(handler);
		for (Logger logger : loggers)
		{
			logger.addHandler(handler);
			logger.setLevel(Level.ALL);
		}
	}

	public static synchronized void addLogger(Logger logger)
	{
		if (!loggerAlreadyRegistered(logger))
		{
			loggers.add(logger);

			// Note: Even though this is likely to be a new logger
			// Should probably check it is not already registered here
			// or does not already have handlers we are about to attach.

			for (Handler handler : handlers)
			{
				logger.addHandler(handler);
			}
		}
	}

	/**
     * Whether or not the specified logger has already registered with the
     * LogOrganiser.
     */
	private static boolean loggerAlreadyRegistered(Logger l)
	{
		for (Logger logger : loggers)
		{
			if (logger.getName().equals(l.getName()))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean removeLogger(Logger logger)
	{
		return loggers.remove(logger);
	}
}
