package com.slavi.util;

import org.junit.Test;

import com.slavi.TestUtils;
import com.slavi.math.MathUtil;

public class ColorConversionTest {
	public static class Data {
		public double R, G, B, H, H2, C, C2, V, L, I, Y601, SHSV, SHSL, SHSI;

		public Data(double R, double G, double B, double H, double H2, double C, double C2, double V, double L, double I, double Y601, double SHSV, double SHSL, double SHSI) {
			this.R = R; 
			this.G = G; 
			this.B = B; 
			this.H = H; 
			this.H2 = H2; 
			this.C = C; 
			this.C2 = C2; 
			this.V = V; 
			this.L = L; 
			this.I = I; 
			this.Y601 = Y601;
			this.SHSV = SHSV;
			this.SHSL = SHSL;
			this.SHSI = SHSI;
		}
	}
		
	Data testData[] = {
			new Data(1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0),
			new Data(0.5, 0.5, 0.5, 0, 0, 0, 0, 0.5, 0.5, 0.5, 0.5, 0, 0, 0),
			new Data(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
			new Data(1, 0, 0, 0, 0, 1, 1, 1, 0.5, 0.333, 0.299, 1, 1, 1),
			new Data(0.75, 0.75, 0, 60, 60, 0.75, 0.75, 0.75, 0.375, 0.5, 0.664, 1, 1, 1),
			new Data(0, 0.5, 0, 120, 120, 0.5, 0.5, 0.5, 0.25, 0.167, 0.293, 1, 1, 1),
			new Data(0.5, 1, 1, 180, 180, 0.5, 0.5, 1, 0.75, 0.833, 0.85, 0.5, 1, 0.4),
			new Data(0.5, 0.5, 1, 240, 240, 0.5, 0.5, 1, 0.75, 0.667, 0.557, 0.5, 1, 0.25),
			new Data(0.75, 0.25, 0.75, 300, 300, 0.5, 0.5, 0.75, 0.5, 0.583, 0.457, 0.667, 0.5, 0.571),
			new Data(0.628, 0.643, 0.142, 61.8, 61.5, 0.501, 0.494, 0.643, 0.393, 0.471, 0.581, 0.779, 0.638, 0.699),
			new Data(0.255, 0.104, 0.918, 251.1, 250, 0.814, 0.75, 0.918, 0.511, 0.426, 0.242, 0.887, 0.832, 0.756),
			new Data(0.116, 0.675, 0.255, 134.9, 133.8, 0.559, 0.504, 0.675, 0.396, 0.349, 0.46, 0.828, 0.707, 0.667),
			new Data(0.941, 0.785, 0.053, 49.5, 50.5, 0.888, 0.821, 0.941, 0.497, 0.593, 0.748, 0.944, 0.893, 0.911),
			new Data(0.704, 0.187, 0.897, 283.7, 284.8, 0.71, 0.636, 0.897, 0.542, 0.596, 0.423, 0.792, 0.775, 0.686),
			new Data(0.931, 0.463, 0.316, 14.3, 13.2, 0.615, 0.556, 0.931, 0.624, 0.57, 0.586, 0.661, 0.817, 0.446),
			new Data(0.998, 0.974, 0.532, 56.9, 57.4, 0.466, 0.454, 0.998, 0.765, 0.835, 0.931, 0.467, 0.991, 0.363),
			new Data(0.099, 0.795, 0.591, 162.4, 163.4, 0.696, 0.62, 0.795, 0.447, 0.495, 0.564, 0.875, 0.779, 0.8),
			new Data(0.211, 0.149, 0.597, 248.3, 247.3, 0.448, 0.42, 0.597, 0.373, 0.319, 0.219, 0.75, 0.601, 0.533),
			new Data(0.495, 0.493, 0.721, 240.5, 240.4, 0.228, 0.227, 0.721, 0.607, 0.57, 0.52, 0.316, 0.29, 0.135),
	};

	@Test
	public void testHSV2() {
		double dest0[] = new double[3];
		double dest1[] = new double[3];
		double dest2[] = new double[3];
		double dest3[] = new double[3];

		TestUtils.precision = 1.0 / 1000.0;
		for (int index = 0; index < testData.length; index++) {
			Data i = testData[index];
			dest0[0] = i.R;
			dest0[1] = i.G;
			dest0[2] = i.B;
			
			dest3[0] = i.H * MathUtil.deg2rad;
			dest3[1] = i.SHSV;
			dest3[2] = i.V;
			
			ColorConversion.HSV.fromDRGB(dest0, dest1);
			ColorConversion.HSV.toDRGB(dest1, dest2);
			try {
				TestUtils.assertEqual("Color not matched HSV", dest1, dest3);
				TestUtils.assertEqual("Color not matched RGB", dest0, dest2);
			} catch (RuntimeException e) {
				System.out.println("Error at index " + index);
				TestUtils.dumpArray("", dest0);
				TestUtils.dumpArray("", dest1);
				TestUtils.dumpArray("", dest2);
				throw e;
			}
		}
	}
	
	@Test
	public void testHSV() {
		double dest0[] = new double[3];
		double dest1[] = new double[3];
		double dest2[] = new double[3];
		
		for (int r = 0; r <= 255; r++) {
			for (int g = 0; g <= 255; g++) {
				for (int b = 0; b <= 255; b++) {
					dest0[0] = r;
					dest0[1] = g;
					dest0[2] = b;
					ColorConversion.RGB.toDRGB(dest0, dest1);
					ColorConversion.HSV.fromDRGB(dest1, dest1);
					ColorConversion.HSV.toDRGB(dest1, dest2);
					ColorConversion.RGB.fromDRGB(dest2, dest2);
					try {
						TestUtils.assertEqual("Color not matched", dest0, dest2);
					} catch (RuntimeException e) {
						TestUtils.dumpArray("", dest0);
						TestUtils.dumpArray("", dest1);
						TestUtils.dumpArray("", dest2);
						System.out.println("----");
						throw e;
					}
				}
			}			
		}
	}
	
	@Test
	public void testHSL2() {
		double dest0[] = new double[3];
		double dest1[] = new double[3];
		double dest2[] = new double[3];
		double dest3[] = new double[3];

		TestUtils.precision = 1.0 / 1000.0;
		for (int index = 0; index < testData.length; index++) {
			Data i = testData[index];
			dest0[0] = i.R;
			dest0[1] = i.G;
			dest0[2] = i.B;
			
			dest3[0] = i.H * MathUtil.deg2rad;
			dest3[1] = i.SHSL;
			dest3[2] = i.L;
			
			ColorConversion.HSL.fromDRGB(dest0, dest1);
			ColorConversion.HSL.toDRGB(dest1, dest2);
			try {
				TestUtils.assertEqual("Color not matched HSL", dest1, dest3);
				TestUtils.assertEqual("Color not matched RGB", dest0, dest2);
			} catch (RuntimeException e) {
				System.out.println("Error at index " + index);
				TestUtils.dumpArray("", dest0);
				TestUtils.dumpArray("", dest1);
				TestUtils.dumpArray("", dest2);
				TestUtils.dumpArray("", dest3);
				throw e;
			}
		}
	}
	
	@Test
	public void testHSL() {
		double dest0[] = new double[3];
		double dest1[] = new double[3];
		double dest2[] = new double[3];
		
		for (int r = 0; r <= 255; r++) {
			for (int g = 0; g <= 255; g++) {
				for (int b = 0; b <= 255; b++) {
					try {
						int color = r << 16 | g << 8 | b;
						dest0[0] = r;
						dest0[1] = g;
						dest0[2] = b;

						ColorConversion.RGB.toDRGB(dest0, dest1);
						ColorConversion.RGB.fromRGB(color, dest2);
						TestUtils.assertEqual("Color not matched 1", dest1, dest2);

						ColorConversion.HSL.fromDRGB(dest1, dest1);
						ColorConversion.HSL.toDRGB(dest1, dest1);
						ColorConversion.RGB.fromDRGB(dest1, dest2);
						TestUtils.assertEqual("Color not matched 2", dest0, dest2);

						int color2 = ColorConversion.RGB.toRGB(dest1);
						TestUtils.assertEqual("Color not matched 3", color, color2);
					} catch (RuntimeException e) {
						System.out.println("R=" + r);
						System.out.println("G=" + g);
						System.out.println("B=" + b);
						TestUtils.dumpArray("", dest0);
						TestUtils.dumpArray("", dest1);
						TestUtils.dumpArray("", dest2);
						System.out.println("----");
						throw e;
					}
				}
			}			
		}
	}
	
	@Test
	public void testCMYK() {
		double rgb[] = new double[3];
		double drgb[] = new double[3];
		double cmyk[] = new double[4];
		double drgb2[] = new double[3];
		double rgb2[] = new double[3];
		
		for (int r = 0; r <= 255; r++) {
			for (int g = 0; g <= 255; g++) {
				for (int b = 0; b <= 255; b++) {
					try {
						int color = r << 16 | g << 8 | b;
						rgb[0] = r;
						rgb[1] = g;
						rgb[2] = b;

						ColorConversion.RGB.toDRGB(rgb, drgb);
						ColorConversion.CMYK.fromDRGB(drgb, cmyk);
						ColorConversion.CMYK.toDRGB(cmyk, drgb2);
						ColorConversion.RGB.fromDRGB(drgb2, rgb2);

						int color2 = ColorConversion.RGB.toRGB(drgb2);
						TestUtils.assertEqual("Color not matched", color, color2);
					} catch (RuntimeException e) {
						System.out.println("R=" + r);
						System.out.println("G=" + g);
						System.out.println("B=" + b);
						TestUtils.dumpArray("rgb", rgb);
						TestUtils.dumpArray("drgb", drgb);
						TestUtils.dumpArray("cmyk", cmyk);
						TestUtils.dumpArray("drgb2", drgb2);
						TestUtils.dumpArray("rgb2", rgb2);
						System.out.println("----");
						throw e;
					}
				}
			}			
		}
	}
}
