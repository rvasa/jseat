package extractor;
import java.util.Collection;

/**
 * Data class that stores fanout information for a particular class
 * @author rvasa
 */
public class FanoutData
{
	private String className;
	private Collection<String> fanOutDeps;

	public FanoutData(String cn, Collection<String> deps)
	{
		className = cn;
		fanOutDeps = deps;
	}
	
	/** Returns the set of types that this class depends on */
	public Collection<String> getDependencies()
	{
		return fanOutDeps;
	}
	
	/**
	 * Returns the number of classes that this class depends on
	 */
	public int getFanOutCount()
	{
		return fanOutDeps.size();
	}
	
	/** Returns the basic dependency metrics as a string */
	public String toString()
	{
		String str = "Class Name: "+className+"\n";
		str += "Fanout dependencies: "+fanOutDeps.size()+"\n";
		for (String d : fanOutDeps) str +=d+"\n";  			
		return str;
	}
}
