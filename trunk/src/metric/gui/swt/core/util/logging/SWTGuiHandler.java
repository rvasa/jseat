package metric.gui.swt.core.util.logging;

import java.util.LinkedList;
import java.util.Observer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import metric.gui.swt.core.vocabulary.GUI;

import org.eclipse.swt.widgets.Display;

public class SWTGuiHandler extends Handler
{

	LinkedList<LogEvent> events;
	LinkedList<Observer> observers;

	public SWTGuiHandler()
	{
		events = new LinkedList<LogEvent>();
		observers = new LinkedList<Observer>();
	}

	@Override
	public void close() throws SecurityException
	{
	}

	@Override
	public void flush()
	{
		// For each listener we have listening.
		for (Observer observer : observers)
		{
			// Send all currently queued events.
			for (LogEvent event : events)
			{
				GuiSafeEventPoster(observer, event);
			}
		}
		events.clear(); // events have been sent now, so can safely clear.
	}

	@Override
	public void publish(LogRecord log)
	{
		synchronizedPublish(log);
	}

	// This is to synchronize multiple threads logging messages.
	private synchronized void synchronizedPublish(LogRecord log)
	{
		String s = log.getMessage() + "\n";
		// Default status code for now.
		int statusCode = GUI.REQ_OAREA_UPDATE.getValue(); // default

		LogEvent le = new LogEvent(log.getLoggerName(), s, log.getParameters(), statusCode);
		events.add(le);
		flush(); // default autoflush for now.
	}

	/**
     * This is a GUI SAFE method for notifing the specified listener with the
     * specified event.
     * 
     * @param listener The listener to be notified
     * @param event The event to be sent
     */
	private void GuiSafeEventPoster(Observer observer, LogEvent event)
	{
		final Observer o = observer;
		final LogEvent e = event;
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				o.update(e, e);
			}
		};
		Display.getDefault().asyncExec(runnable);
	}

	public void addObserver(Observer observer)
	{
		observers.add(observer);
	}

}
