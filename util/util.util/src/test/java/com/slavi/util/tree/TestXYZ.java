package com.slavi.util.tree;

import org.junit.Assert;

import com.slavi.math.matrix.Matrix;
import com.slavi.util.ColorConversion;

public class TestXYZ {

	public static void main(String[] args) {
		double drgb[] = new double[3];
		double value[] = new double[4]; // Because of CMYK
		double drgb2[] = new double[3];

		int color = 7;
		ColorConversion.RGB.fromRGB(color, drgb);
		ColorConversion.LAB.fromDRGB(drgb, value);
		ColorConversion.LAB.toDRGB(value, drgb2);
		int color2 = ColorConversion.RGB.toRGB(drgb2);
		Assert.assertEquals("Color not matched", color, color2);
	}

	public static void main1(String[] args) {
		double rgb[] = new double[3];
		double drgb[] = new double[3];
		double tmp[] = new double[3];
		double drgb2[] = new double[3];
		double rgb2[] = new double[3];

		double minTMP[] = new double[]{ Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
		double maxTMP[] = new double[]{ Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE};

		for (int r = 0; r <= 255; r++) {
			for (int g = 0; g <= 255; g++) {
				for (int b = 0; b <= 255; b++) {
					int color = r << 16 | g << 8 | b;
					rgb[0] = r;
					rgb[1] = g;
					rgb[2] = b;

					ColorConversion.RGB.toDRGB(rgb, drgb);
					ColorConversion.LAB.fromDRGB(drgb, tmp);
					ColorConversion.LAB.toDRGB(tmp, drgb2);
					ColorConversion.RGB.fromDRGB(drgb2, rgb2);

					if (maxTMP[0] < tmp[0]) maxTMP[0] = tmp[0];
					if (maxTMP[1] < tmp[1]) maxTMP[1] = tmp[1];
					if (maxTMP[2] < tmp[2]) maxTMP[2] = tmp[2];

					if (minTMP[0] > tmp[0]) minTMP[0] = tmp[0];
					if (minTMP[1] > tmp[1]) minTMP[1] = tmp[1];
					if (minTMP[2] > tmp[2]) minTMP[2] = tmp[2];

					int color2 = ColorConversion.RGB.toRGB(drgb2);
					//Assert.assertEquals("Color not matched", color, color2);
				}
			}
		}
		System.out.println("MIN");
		System.out.println(minTMP[0]);
		System.out.println(minTMP[1]);
		System.out.println(minTMP[2]);
		System.out.println("MAX");
		System.out.println(maxTMP[0]);
		System.out.println(maxTMP[1]);
		System.out.println(maxTMP[2]);
	}

	public static void main2(String[] args) {
		double d1[] = {
		0.412453, 0.357580, 0.180423,
		0.212671, 0.715160, 0.072169,
		0.019334, 0.119193, 0.950227 };

		double d2[] = {
		3.240479 , - 1.537150, - 0.498535,
		-0.969256,   1.875992, + 0.041556,
		0.055648 , - 0.204043, + 1.057311};

		Matrix m1 = new Matrix(3,3);
		m1.loadFromVector(d1);

		System.out.println(m1.toMatlabString("M1"));

		Matrix m2 = new Matrix(3,3);
		m2.loadFromVector(d2);

		m2.printM("M2");

		Matrix m1i = m1.makeCopy();
		System.out.println(m1i.inverse());

		m1i.printM("M1'");

		Matrix tmp = new Matrix();
		m1.mMul(m1i, tmp);

		tmp.printM("M1 * M1'");

		m1i.mSub(m2, tmp);

		tmp.printM("M1' - M2");


		double drgb[] = new double[3];
		double xyz[] = new double[3];
		double drgb2[] = new double[3];
		double rgb2[] = new double[3];

		int color = 28671;
		ColorConversion.RGB.fromRGB(color, drgb);
		ColorConversion.XYZ.fromDRGB(drgb, xyz);
		ColorConversion.XYZ.toDRGB(xyz, drgb2);
		ColorConversion.RGB.fromDRGB(drgb2, rgb2);
		int color2 = ColorConversion.RGB.toRGB(drgb2);

		System.out.println(ColorConversion.XYZ.toString(xyz));
		System.out.println(ColorConversion.XYZ.toString(drgb2));

		System.out.println(color);
		System.out.println(color2);
	}
}
