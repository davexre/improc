package com.test;

import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.testUtil.TestUtil;

public class Dummy3 {
	public static void main2(String[] args) {
		double a = 3 * Math.PI / 5;
		double pi4 = Math.PI / 4;
		
		double b = a % pi4;
		double c = a / b;
		
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
	}
	
	public static void main3(String[] args) {
		double r[] = { -1 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 30 * MathUtil.deg2rad };
		Matrix m  = RotationXYZ.instance.makeAngles(r[0], r[1], r[2]);
		Matrix m0 = RotationXYZ.instance.makeAngles(r[0], 0, 0);
		Matrix m1 = RotationXYZ.instance.makeAngles(0, r[1], 0);
		Matrix m2 = RotationXYZ.instance.makeAngles(0, 0, r[2]);
		
		Matrix a = m.makeCopy();
		Matrix b = m.makeCopy();
		Matrix c = m.makeCopy();
		m0.mMul(m1, a);
		a.mMul(m2, b);
		b.mSub(m, c);
		System.out.println(c.is0(0.0000001));
		
		Matrix rot90 = RotationXYZ.instance.makeAngles(0, 70 * MathUtil.deg2rad, 0);
		rot90.mMul(m, a);
		double r2[] = r.clone();
		RotationXYZ.instance.getRotationAngles(a, r2);
		TestUtil.dumpAngles("R ", r);
		TestUtil.dumpAngles("R2", r2);
		
		double pi4 = 0.25 * Math.PI;
		double adjAngle1 = r2[1];
		
		double dived = adjAngle1 % pi4;
		System.out.println(pi4);
		System.out.println(dived);
		double index = (adjAngle1 - dived) / dived;
		System.out.println(index);
		System.out.println((int) (index));
	}
	
	public static void main(String[] args) {
		Matrix r1 = Matrix.fromOneLineString("1 0 0; 0 1 2; 0 0 1");
		Matrix r2 = Matrix.fromOneLineString("1 0 3; 0 1 0; 0 0 1");
		Matrix r3 = Matrix.fromOneLineString("1 4 0; 0 1 0; 0 0 1");
		
		Matrix a = new Matrix(3, 3);
		Matrix b = a.makeCopy();
		Matrix c = a.makeCopy();
		Matrix d = a.makeCopy();

		r1.printM("R1");
		r2.printM("R2");
		r3.printM("R3");
		r1.mMul(r2, a);
		a.mMul(r3, b);
		r2.mMul(r3, c);
		a.printM("R1 * R2");
		b.printM("R1 * R2 * R3");
		c.printM("R2 * R3");

		
	}
}
