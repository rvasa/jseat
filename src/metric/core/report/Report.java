package metric.core.report;

import java.util.HashMap;
import java.util.Observable;

import metric.core.ReportDefinition;
import metric.core.exception.ReportException;
import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.MethodMetricMap;
import metric.core.model.VersionMetricData;
import metric.core.report.visitor.ReportVisitor;
import metric.core.util.MetricTable;

/**
 * Provides a stub for metric reports. Any data the subclass report supports, it
 * should override the corresponding visit method.
 * 
 * A report is initialized with a <code>ReportDefinition</code>, which is
 * used to construct and initialise a report. Additionally, it can be used to
 * pass arguments to the underlying report visitor.
 */
public abstract class Report extends Observable implements ReportVisitor
{
	protected static final String INVALID = "This report and provided report" +
			" definition does not support repoty generation for ";

	protected ReportDefinition rd;
	private MetricTable table;

	protected int completion;

	public Report(ReportDefinition rd) throws ReportException
	{
		this.rd = rd;
		processArgs();
	}

	public void visit(HistoryMetricData hmd) throws ReportException
	{
		throw new ReportException(INVALID + hmd.getClass().getSimpleName());
	};

	public void visit(VersionMetricData vmd) throws ReportException
	{
		throw new ReportException(INVALID + vmd.getClass().getSimpleName());
	};

	public void visit(ClassMetricData cmd) throws ReportException
	{
		throw new ReportException(INVALID + cmd.getClass().getSimpleName());
	};

	public void visit(MethodMetricMap mmd) throws ReportException
	{
		throw new ReportException(INVALID + mmd.getClass().getSimpleName());
	};

	/**
     * @return The underlying table used to collate this reports information.
     */
	public final MetricTable getTable()
	{
		return table;
	}

	/**
     * Logs a progress update. Can be used to notify handlers of the current
     * status of report generation.
     * 
     * @param at How much work has been done processing this report.
     * @param total How much work in total there is.
     */
	protected void updateProgress(int at, int total)
	{
		completion = (int) (((double) at / (double) total) * 100);
		setChanged();
		notifyObservers();
	}

	/**
     * Sets the table used for this report. Once a table has been set, it is
     * added to the log buffer and logged.
     * 
     * @param table The table that should be logged.
     */
	protected void setTable(MetricTable table)
	{
		this.table = table;
		table.setUpperCaseHeadings(true);
		table.setAlignment(MetricTable.Alignment.CENTER);
	}

	/**
     * @return the completion
     */
	public final int getCompletion()
	{
		return completion;
	}

	/**
     * Sets the arguments used for this report. This should update the
     * <code>ReportDefinition</code> with the values specified in this
     * HashMap.
     * 
     * @param args The arguments the ReportDefinition should be updated with.
     */
	public abstract void setArguments(HashMap<String, Object> args);

	/**
     * Should return a HashMap of arguments used to configure this report. The
     * key should be the name of the field being configured and the value should
     * be representive of the types of values the key can take on.
     * 
     * e.g If the value is a string use a String[] to confine values. The first
     * value in the provided String[] is selected by default. If this field can
     * be specified multiple times, use a Collection<String[]> as the value.
     * 
     * If the value can only be a boolean, the value should be a boolean. What
     * ever value is specified is the default.
     * 
     * If the value can only be an int, the value should be an int. The value
     * specified is the default.
     * 
     * 
     * @return The HashMap of valid arguments for this report.
     */
	public abstract HashMap<String, Object> getArguments();

	/**
     * Should extract any arguments from the provided
     * <code>ReportDefinition</code>
     */
	protected abstract void processArgs() throws ReportException;

	/**
     * Re-processes the arguments from the provided
     * <code>ReportDefinition</code>, effectively resetting any argument
     * changes made to this report.
     */
	public void reset()
	{
		try
		{
			processArgs();
		} catch (ReportException e)
		{
		} // Suppress this one.
	}
}
