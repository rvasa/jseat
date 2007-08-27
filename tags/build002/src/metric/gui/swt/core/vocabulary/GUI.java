package metric.gui.swt.core.vocabulary;

public enum GUI
{
	REQ_OAREA_UPDATE(1), // Request output area be updated.
	REQ_VLIST_UPDATE(2), // Request version list be updated.
	REQ_SBAR_UPDATE(3),  // Request status bar be updated.
	HISTORY_PROGRESS(4), // Indicates a progress update from history processing.
	REPORT_PROGRESS(5),  // Indicates a progress update from report processing.
	DEFAULT_VERSION_DIR, // Default directory to open when browsing for versions.
	DEFAULT_REPORT_DIR,  // Default directory to open when browsing for reports.
	DEFAULT_REPORTSET,   // Default report file to load.
	AUTO_PROCESS;        // Whether or not to auto-process after browsing for a
						 // versions file.
	
	private int value;
	
	private GUI(int value) { this.value = value; }
	
	private GUI() { this(0); } // Default value
	
	public final int getValue() { return value; }
	
	public String toString()
	{
		return name().toLowerCase();
	}
	
	public static GUI parse(String toParse)
	{
		for (GUI g : values())
		{
			if (toParse.equals(g.toString()))
				return g;
		}
		return null;
	}
}
