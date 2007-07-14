package test;

import junit.framework.TestCase;
import metric.core.io.FileUtil;

public class FileUtilTest extends TestCase
{
	public void testValidClassFile()
	{
		assertEquals(true, FileUtil.isClassFile("groovy/lang/GString.class",
				true));

		assertEquals(true, FileUtil.isClassFile("groovy/lang/GString.class",
				false));
	}

	public void testInvalidClassFile()
	{
		assertEquals(false, FileUtil.isClassFile("groovy/lang/GString", false));
	}

	public void testValidInnerClassFile()
	{
		assertEquals(
				true,
				FileUtil
						.isClassFile(
								"org/codehaus/groovy/tools/DocGenerator$_generate_closure6.class",
								false));

	}

	public void testInvalidInnerClassFile()
	{
		assertEquals(false, FileUtil.isClassFile(
				"org/codehaus/groovy/tools/DocGenerator$_generate_closure6",
				false));

		assertEquals(
				true,
				FileUtil
						.isClassFile(
								"org/codehaus/groovy/tools/DocGenerator$_generate_closure6.class",
								false));

		assertEquals(
				false,
				FileUtil
						.isClassFile(
								"org/codehaus/groovy/tools/DocGenerator$_generate_closure6.class",
								true));
	}

}
