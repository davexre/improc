package com.slavi.util.tree;

import com.slavi.math.matrix.Matrix;
import com.slavi.util.ColorConversion;

public class TestXYZ {

	public static void main(String[] args) {
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
		
		m1.printM("M1");

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

		System.out.println(color);
		System.out.println(color2);
	}
}
