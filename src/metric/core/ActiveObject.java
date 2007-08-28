package metric.core;

import java.util.Observable;

public abstract class ActiveObject<T> extends Observable implements Runnable
{
	Thread thread;
	private boolean stopRequested;
	private Object lock;
	private String name;

	public ActiveObject()
	{
		lock = new Object();
		stopRequested = false;
	}

	public ActiveObject(String name)
	{
		lock = new Object();
		stopRequested = false;
		this.name = name;
	}

	public void start()
	{
		synchronized (lock)
		{
			if (thread == null)
			{
				thread = new Thread(this);
				if (name != null)
					thread.setName(name);
				thread.setDaemon(true);
				thread.start();
			}
			// else // Do nothing
			// throw new Exception("Thread already started...");
		}
	}

	public void stop() throws InterruptedException
	{
		Thread t;
		synchronized (lock)
		{
			t = thread;
			stopRequested = true;
			Interrupt(); // Interrupt the current thread.
		}

		// This should be here to join the main thread who owns this object.
		// if (Thread.currentThread() != t)
		// t.join();
	}

	// this allows the interrupt to be overriden.
	protected void Interrupt()
	{
		thread.interrupt();
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
			initialise(); // Initialize before thread starts.
			synchronized (lock)
			{
				running = !stopRequested;
			}

			while (running)
			{
				doWork(getWork());
				// Check if we have been requested to stop
				synchronized (lock)
				{
					running = !stopRequested;
				}
			}
		} finally
		{
			synchronized (lock)
			{
				cleanup(); // Cleanup before thread dies.
				thread = null;
			}
		}
	}

	public String getName()
	{
		return thread.getName();
	}

	public abstract void doWork(T toDo);

	public abstract T getWork();

	// Can override if subclass wants to.
	protected void initialise()
	{
	};

	protected void cleanup()
	{
	};
}
