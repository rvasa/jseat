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
	public static LookupPaintScale generatePaintScale(IntensityStyle style, int max)
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
			System.out.println("colourIndex: " + colorIndex);
			if (style == IntensityStyle.HeatMap)
			{
				ps.add(StatUtils.toFixedDecPlaces(v, 3), new Color(255, colorIndex, colorIndex));
			} else if (style == IntensityStyle.CoolMap)
			{
				int finalCoolValue = 255 - colorIndex;
				ps.add(StatUtils.toFixedDecPlaces(v, 3), new Color(finalCoolValue, finalCoolValue, 255));
			}
		}
		return ps;
	}
	
	public static LookupPaintScale generateEarthquakePaintScale(IntensityStyle style, int max)
	{
		LookupPaintScale ps = new LookupPaintScale(0.000, 100, new Color(255,225,225));
		ps.add(0.0, Color.WHITE);

		int MAX_COLOUR_VALUE = 255;
		int colorIndex = -1;

		for (int i = 1; i < 100; i+=5) // Add mappings in 5% values
		{
			colorIndex = (MAX_COLOUR_VALUE - 25) - (int)(MAX_COLOUR_VALUE*(double)i / 100);

			if (colorIndex > 0)
			{
				ps.add((double)i, new Color(MAX_COLOUR_VALUE, colorIndex, colorIndex));
			}
			else // wrap around
			{
				colorIndex *= -1;
				ps.add((double)i, new Color(colorIndex, 0, 0));
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
	private static double getNewColourValue(double upperLimit, double value, double max)
	{
		return upperLimit * (value / max);
	}
}
