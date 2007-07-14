package test;

import junit.framework.TestCase;
import metric.core.model.MetricData;
import metric.core.vocabulary.MethodMetric;

public class MetricDataTest extends TestCase
{

	private MetricData<MethodMetric> md;

	public void setUp()
	{
		md.setSimpleMetric(MethodMetric.BRANCH_COUNT, 0);
	}

	public void testGet()
	{
		// test getting a metric back as a string
		md.setSimpleMetric(MethodMetric.BRANCH_COUNT, 2);
		assertEquals("2", md.get(MethodMetric.BRANCH_COUNT));

		// test getting a complexMetric back as string
		md.setSimpleMetric(MethodMetric.BRANCH_COUNT, 2);
		assertEquals("2", md.get(MethodMetric.BRANCH_COUNT));
	}

	public void testGetMetric()
	{
		assertEquals(0, md.getSimpleMetric(MethodMetric.BRANCH_COUNT));
	}

	public void testSetMetric()
	{
		md.setSimpleMetric(MethodMetric.BRANCH_COUNT, 2);
		assertEquals(2, md.getSimpleMetric(MethodMetric.BRANCH_COUNT));
		md.setSimpleMetric(MethodMetric.BRANCH_COUNT, 99);
		assertEquals(99, md.getSimpleMetric(MethodMetric.BRANCH_COUNT));
	}

	public void testIncrementMetric()
	{
		testSetMetric();
		md.incrementMetric(MethodMetric.BRANCH_COUNT);
		assertEquals(100, md.getSimpleMetric(MethodMetric.BRANCH_COUNT));
		md.incrementMetric(MethodMetric.BRANCH_COUNT);
		assertEquals(101, md.getSimpleMetric(MethodMetric.BRANCH_COUNT));
	}

	public void testDecrementMetric()
	{
		md.setSimpleMetric(MethodMetric.TRY_CATCH_BLOCK_COUNT, 2);
		md.decrementMetric(MethodMetric.TRY_CATCH_BLOCK_COUNT);

		assertEquals(1, md.getSimpleMetric(MethodMetric.TRY_CATCH_BLOCK_COUNT));
		md.decrementMetric(MethodMetric.TRY_CATCH_BLOCK_COUNT);
		assertEquals(0, md.getSimpleMetric(MethodMetric.TRY_CATCH_BLOCK_COUNT));
		md.decrementMetric(MethodMetric.TRY_CATCH_BLOCK_COUNT);
		assertEquals(-1, md.getSimpleMetric(MethodMetric.TRY_CATCH_BLOCK_COUNT));
	}

	public void testAddMetric()
	{
		md.setSimpleMetric(MethodMetric.TRY_CATCH_BLOCK_COUNT, 2);
		md.incrementMetric(MethodMetric.TRY_CATCH_BLOCK_COUNT, 8);

		assertEquals(10, md.getSimpleMetric(MethodMetric.TRY_CATCH_BLOCK_COUNT));
		md.incrementMetric(MethodMetric.TRY_CATCH_BLOCK_COUNT, -10);
		assertEquals(0, md.getSimpleMetric(MethodMetric.TRY_CATCH_BLOCK_COUNT));
	}
}
