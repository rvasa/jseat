package metric.core.util;

public class SimpleWorkTimer
{
	private int at, totalWorkUnits, resets;

	private long startTime, finishTime;
	private boolean running;

	public SimpleWorkTimer()
	{
	}

	public SimpleWorkTimer(int totalWorkUnits)
	{
		this.totalWorkUnits = totalWorkUnits;
	}

	/**
     * Starts the timer running.
     */
	public void start()
	{
		startTime = System.currentTimeMillis();
		running = true;
	}

	/**
     * Stops the timer.
     */
	public void stop()
	{
		finishTime = System.currentTimeMillis();
		running = false;
	}

	/**
     * Resets the timer and starts it coounting again.
     */
	public void reset()
	{
		startTime = finishTime = 0;
		resets++;
		// Start timer again.
		start();
	}

	/**
     * @return The number of times this timer has been reset since it was first
     *         started.
     */
	public final int getResets()
	{
		return resets;
	}

	/**
     * @return The time elapsed before the timer was stopped or the total time
     *         elapsed since it was started if it is still running.
     */
	public long getTimeElapsed()
	{
		if (!running)
			return finishTime - startTime;
		else
			return System.currentTimeMillis() - startTime;
	}

	public void addCompletedWork(int completedWorkUnits)
	{
		at += completedWorkUnits;
	}

	/**
     * Sets the total number of units of work that will be completed.
     */
	public void setWorkUnits(int totalWorkUnits)
	{
		this.totalWorkUnits = totalWorkUnits;
	}

	/**
     * @return The percentage of work done.
     */
	public int getWorkStatus()
	{
		return (int) (((double) at / (double) totalWorkUnits) * 100);
	}
}
