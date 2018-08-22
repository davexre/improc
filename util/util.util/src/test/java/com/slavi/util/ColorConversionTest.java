package com.slavi.util;

import org.junit.Assert;
import org.junit.Test;

public class ColorConversionTest {
	@FunctionalInterface
	private interface IToDRGB {
		void toDRGB(double value[], double DRGB[]);
	}

	@FunctionalInterface
	private interface IFromDRGB {
		void fromDRGB(double DRGB[], double value[]);
	}

	private void runTest(IFromDRGB fromDRGB, IToDRGB toDRGB) {
		double drgb[] = new double[3];
		double value[] = new double[4]; // Because of CMYK
		double drgb2[] = new double[3];

		for (int r = 0; r <= 255; r++) {
			for (int g = 0; g <= 255; g++) {
				for (int b = 0; b <= 255; b++) {
					int color = r << 16 | g << 8 | b;
					ColorConversion.RGB.fromRGB(color, drgb);
					fromDRGB.fromDRGB(drgb, value);
					toDRGB.toDRGB(value, drgb2);
					int color2 = ColorConversion.RGB.toRGB(drgb2);
					Assert.assertEquals("Color not matched", color, color2);
				}
			}
		}
	}

	@Test
	public void testHSV() {
		runTest(
			(drgb, value) -> ColorConversion.HSV.fromDRGB(drgb, value),
			(value, drgb) -> ColorConversion.HSV.toDRGB(value, drgb)
		);
	}

	@Test
	public void testHSL() {
		runTest(
			(drgb, value) -> ColorConversion.HSL.fromDRGB(drgb, value),
			(value, drgb) -> ColorConversion.HSL.toDRGB(value, drgb)
		);
	}

	@Test
	public void testCMYK() {
		runTest(
			(drgb, value) -> ColorConversion.CMYK.fromDRGB(drgb, value),
			(value, drgb) -> ColorConversion.CMYK.toDRGB(value, drgb)
		);
	}

	@Test
	public void testRBW() {
		runTest(
			(drgb, value) -> ColorConversion.RBW.fromDRGB(drgb, value),
			(value, drgb) -> ColorConversion.RBW.toDRGB(value, drgb)
		);
	}

	@Test
	public void testXYZ() {
		runTest(
			(drgb, value) -> ColorConversion.XYZ.fromDRGB(drgb, value),
			(value, drgb) -> ColorConversion.XYZ.toDRGB(value, drgb)
		);
	}

	@Test
	public void testLAB() {
		runTest(
			(drgb, value) -> ColorConversion.LAB.fromDRGB(drgb, value),
			(value, drgb) -> ColorConversion.LAB.toDRGB(value, drgb)
		);
	}
}
