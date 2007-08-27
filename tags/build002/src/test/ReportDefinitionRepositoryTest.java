package test;

import junit.framework.TestCase;
import metric.core.ReportDefinition;
import metric.core.ReportDefinitionRepository;
import metric.core.exception.MalformedReportDefinition;

public class ReportDefinitionRepositoryTest extends TestCase
{

	public void testParseDefinition()
	{
		String line = "1,CloneReportVisitor,some description,fan_out_count,2,true,"
				+ "instability";

		ReportDefinition md = null;
		try
		{
			md = ReportDefinitionRepository.parseDefinition(line);
		} catch (MalformedReportDefinition e)
		{
			fail(e.toString());
		}

		assertNotNull(md);
	}

	public void testParseDefinitionWithArrayArgs()
	{
		String line = "1,CloneReportVisitor,some description,fan_out_count,2,true,"
				+ "[1,instability,something_else,true],some_other_arg,"
				+ "other_arg,[modified,instability]";
		ReportDefinition md = null;
		try
		{
			md = ReportDefinitionRepository.parseDefinition(line);
		} catch (MalformedReportDefinition e)
		{
			fail(e.toString());
		}

		assertNotNull(md);

		for (int i = 0; i < md.args.length; i++)
		{
			if (md.args[i] instanceof Object[])
			{
				printObjectArray((Object[]) md.args[i], false);
			} else
				System.out.print(md.args[i] + " ");
		}
		System.out.println();
		for (int i = 0; i < md.params.length; i++)
		{
			if (md.params[i] instanceof Object[])
			{
				System.out.println("\nprinting params");
				printObjectArray((Object[]) md.params[i], true);
			} else
				System.out.print(((Class) md.params[i]).getSimpleName() + " ");
		}
	}

	private void printObjectArray(Object[] toPrint, boolean isClass)
	{
		System.out.print("[");
		for (int i = 0; i < toPrint.length; i++)
		{
			if (isClass)
				System.out.print(toPrint[i].getClass() + " ");
			else
				System.out.print(toPrint[i] + " ");
		}
		System.out.print("]");
		// System.out.println();
	}
}
