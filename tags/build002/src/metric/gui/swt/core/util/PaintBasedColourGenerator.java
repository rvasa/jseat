package metric.gui.swt.core.util;

import java.awt.Color;

import metric.core.util.StatUtils;
import metric.gui.swt.core.decorator.ColourMapDecorator.IntensityStyle;

import org.jfree.chart.renderer.LookupPaintScale;

public class PaintBasedColourGenerator
{
	/**
     * Generates a <code>LookupPaintScale</code> for the specified
     * IntensityStyle with the specified number of shades
     * 
     * @param style The IntensityStyle
     * @param max The number of shades
     * @return The LookupPaintScale
     */
	public static LookupPaintScale generatePaintScale(IntensityStyle style,
			int max)
	{
		LookupPaintScale ps = new LookupPaintScale(0.000, 1, Color.WHITE);
		ps.add(0.0, Color.LIGHT_GRAY);
		int colorIndex = -1;
		if (style == IntensityStyle.HeatMap)
			colorIndex = 255;
		else if (style == IntensityStyle.CoolMap)
			colorIndex = 255;

		for (int i = 1; i <= max; i++)
		{
			double v = getNewColourValue(0.10, i, max);
			colorIndex -= (colorIndex / max);
			if (style == IntensityStyle.HeatMap)
			{
				ps.add(StatUtils.toFixedDecPlaces(v, 3), new Color(255,
						colorIndex, colorIndex));
			} else if (style == IntensityStyle.CoolMap)
			{
				int finalCoolValue = 255 - colorIndex;
				ps.add(StatUtils.toFixedDecPlaces(v, 3), new Color(
						finalCoolValue, finalCoolValue, 255));
			}
		}
		return ps;
	}

	/**
     * Returns a new value to map a Color to.
     * 
     * @param upperLimit The upper limit for mapping a color.
     * @param value The current value
     * @param max The number of values that fall within the upper limit.
     * @return The value to map a color to.
     */
	private static double getNewColourValue(double upperLimit, double value,
			double max)
	{
		return upperLimit * (value / max);
	}
}
