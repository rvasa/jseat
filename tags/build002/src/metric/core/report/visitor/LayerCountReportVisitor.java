package metric.core.report.visitor;

import java.util.ArrayList;

import metric.core.ReportDefinition;
import metric.core.exception.ReportException;
import metric.core.model.VersionMetricData;
import metric.core.util.StatUtils;
import metric.core.util.StringUtils;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Version;

public class LayerCountReportVisitor extends CountReportVisitor
{
	public LayerCountReportVisitor(ReportDefinition m) throws ReportException
	{
		super(m);
	}

	@Override
	public String[] getRowCounts(VersionMetricData vmd, boolean isum,
			boolean beta, String[] fields)
	{
		ArrayList<String> row = new ArrayList<String>();
		row.add(vmd.get(Version.NAME));
		row.add(vmd.get(Version.RSN));
		row.add(vmd.get(Version.ID));
		row.add(vmd.get(Version.CLASS_COUNT));
		// Limited to 4 layers for now.
		row.add(StringUtils.toCSVString(StatUtils.createRelFreqTable(vmd
				.getMetricRange(ClassMetric.LAYER), 4)));

		for (String field : fields)
		{
			if (isum)
				row.add(vmd.get(Version.ISUM, ClassMetric.parse(field)));
			if (beta)
				row.add(vmd.get(Version.BETA, ClassMetric.parse(field)));
		}
		return StringUtils.asStrings(row);

	}

	@Override
	protected String[] getHeading(String[] fields)
	{
		// Have to construct heading dynamically
		ArrayList<String> heading = new ArrayList<String>();
		heading.add("name");
		heading.add(Version.RSN.toString());
		heading.add(Version.ID.toString());
		heading.add(Version.CLASS_COUNT.toString());
		heading.add("layer");

		for (String str : fields)
		{
			if (doIsum)
				heading.add("isum_" + ClassMetric.parse(str));
			if (doBeta)
				heading.add("beta_" + ClassMetric.parse(str));
		}

		// Setup final heading array to add to table.
		Object[] headings = new Object[heading.size()];
		heading.toArray(headings);
		return StringUtils.asStrings(heading);
	}

}
