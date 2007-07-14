package metric.core;

/**
 * A <code>ReportDefinition</code> is used to define or configure a concrete
 * <code>ReportVisitor</code>. It specifies the classname of the concrete
 * <code>ReportVisitor</code>, its arguments, paramater types, and respective
 * description or name.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class ReportDefinition implements Comparable<ReportDefinition>
{
	public Object[] params;
	public Object[] args;
	public String className;
	public String description;

	/**
     * Creates a <code>ReportDefinition</code> that is used to define or
     * configure a concrete <code>ReportVisitor</code>.
     * 
     * @param className The concrente classname of the
     *            <code>ReportVisitor</code>.
     * @param description A description or name for the report.
     * @param params An array of types matching args
     * @param args An array of arguments
     */
	public ReportDefinition(String className, String description,
			Object[] params, Object[] args)
	{
		this.className = className;
		this.description = description;
		this.params = params;
		this.args = args;
	}

	public String toString()
	{
		return description;
	}

	/**
     * Compares this report to another by its description.
     */
	public int compareTo(ReportDefinition other)
	{
		return this.description.compareTo(other.description);
	}
}
