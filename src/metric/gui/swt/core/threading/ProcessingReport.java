package metric.gui.swt.core.threading;

/**
 * Useful for threads that should be able to report on how much processing they
 * have done.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 * 
 */
public interface ProcessingReport
{
	public int getTotalProcessingDone();

	public int getProcessingDone();
}
