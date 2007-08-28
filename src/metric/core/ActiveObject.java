package metric.core;

import java.util.Observable;

public abstract class ActiveObject<T> extends Observable implements Runnable
{
	Thread thread;
	private boolean stopRequested;
	private Object lock;
	
	public ActiveObject()
	{
		lock = new Object();
		stopRequested = false;
	}
	
	public void start()
	{
		synchronized (lock)
		{
			if (thread == null)
			{
				thread = new Thread(this);
				thread.setDaemon(true);
				thread.start();
			}	
//			else // Do nothing
//				throw new Exception("Thread already started...");
		}
	}
	
	public void stop() throws InterruptedException
	{
		Thread t;
		synchronized (lock)
		{
			t = thread;
			stopRequested = true;
			thread.interrupt();
		}
		
		if (Thread.currentThread() != t)
			t.join();
	}
	
	public void run()
	{
		synchronized (lock)
		{
			stopRequested = false;
		}
		
		try
		{
			boolean running;
			
			synchronized (lock)
			{
				running = !stopRequested;
			}
			
			while (running)
			{
				doWork(getWork());
				//Check if we have been requested to stop
				synchronized (lock)
				{
					running = !stopRequested;
				}
			}
		}
		finally
		{
			synchronized (lock)
			{
				thread = null;
//				notifyAll();
			}
		}
	}
	
	public abstract void doWork(T toDo);
	public abstract T getWork();
}
