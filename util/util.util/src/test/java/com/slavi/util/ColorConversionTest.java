package com.slavi.util;

import org.junit.Assert;
import org.junit.Test;

public class ColorConversionTest {
	private void runTest(ColorConversion cc) {
		double drgb[] = new double[3];
		double value[] = new double[4]; // Because of CMYK
		double drgb2[] = new double[3];

		for (int r = 0; r <= 255; r++) {
			for (int g = 0; g <= 255; g++) {
				for (int b = 0; b <= 255; b++) {
					int color = r << 16 | g << 8 | b;
					ColorConversion.RGB.fromRGB(color, drgb);
					cc.fromDRGB(drgb, value);
					cc.toDRGB(value, drgb2);
					int color2 = ColorConversion.RGB.toRGB(drgb2);
					Assert.assertEquals("Color not matched", color, color2);
				}
			}
		}
	}

	@Test
	public void testHSV() {
		runTest(ColorConversion.HSV.instance);
	}

	@Test
	public void testHSL() {
		runTest(ColorConversion.HSL.instance);
	}

	@Test
	public void testCMYK() {
		runTest(ColorConversion.CMYK.instance);
	}

	@Test
	public void testRBW() {
		runTest(ColorConversion.RBW.instance);
	}

	@Test
	public void testXYZ() {
		runTest(ColorConversion.XYZ.instance);
	}

	@Test
	public void testLAB() {
		runTest(ColorConversion.LAB.instance);
	}
}
