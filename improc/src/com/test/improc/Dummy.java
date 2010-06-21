package com.test.improc;

import com.slavi.math.MathUtil;
import com.slavi.math.RotationZYZ;
import com.slavi.math.SphericalCoordsLongZen;
import com.slavi.math.matrix.Matrix;
import com.unitTest.TestUtils;

public class Dummy {

	void dump2(double dest[]) {
		double x = MathUtil.fixAngle2PI(dest[0]);
		double y = MathUtil.fixAngle2PI(dest[1]);
		System.out.println(MathUtil.rad2degStr(x) + "\t" + MathUtil.rad2degStr(y));
	}
	
	public static void dump3(double dest[]) {
		double x = MathUtil.fixAngle2PI(dest[0]);
		double y = MathUtil.fixAngle2PI(dest[1]);
		double z = MathUtil.fixAngle2PI(dest[2]);
		System.out.println(MathUtil.rad2degStr(x) + "\t" + MathUtil.rad2degStr(y) + "\t" + MathUtil.rad2degStr(z));
	}
	
	void asd() {
		double dest1[] = new double[2];
		double dest2[] = new double[2];

		dest1[0] = 190 * MathUtil.deg2rad;
		dest1[1] = -122 * MathUtil.deg2rad;

		SphericalCoordsLongZen.rotateForeward(dest1[0], dest1[1], 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, dest2);
		dump2(dest1);
		dump2(dest2);
	}
	
	void qwe() {
		RotationZYZ rot = RotationZYZ.instance;
		double angles1[] = new double[3];
		double angles2[] = new double[3];
		
		angles1[0] = 10 * MathUtil.deg2rad;
		angles1[1] = 179.999 * MathUtil.deg2rad;
		angles1[2] = 0 * MathUtil.deg2rad;
		
		Matrix m = rot.makeAngles(angles1);
		m.printM("ROT");
		System.out.println();
		rot.getRotationAngles(m, angles2);
		dump3(angles1);
		dump3(angles2);
	}

	void zxc() {
		RotationZYZ rot = RotationZYZ.instance;
		double p[] = new double[3];
		double p2[] = new double[3];
		double p3[] = new double[3];
		double angles1[] = new double[3];
		double angles2[] = new double[3];
		
		angles1[0] = 10 * MathUtil.deg2rad;
		angles1[1] = 20 * MathUtil.deg2rad;
		angles1[2] = 30 * MathUtil.deg2rad;
		
		p[0] = 1;
		p[1] = 0;
		p[2] = 0;
		
		Matrix m = rot.makeAngles(angles1);
		rot.transformForward(m, p, p2);
		
		angles2[0] = Math.atan2(p2[1], p2[0]);
		double r = Math.sqrt(p2[0] * p2[0] + p2[1] * p2[1]);
		angles2[1] = Math.atan2(p2[2], r);
		angles2[2] = -angles2[0];
		
		Matrix m2 = rot.makeAngles(angles2);
		dump3(angles2);
		rot.getRotationAngles(m2, angles2);
		dump3(angles2);
		
		
		rot.transformBackward(m2, p2, p3);
		System.out.println(p3[0]);
		System.out.println(p3[1]);
		System.out.println(p3[2]);
	}
	
	void aaa() {
		double x1 = 20 * MathUtil.deg2rad; 
		double y1 = 30 * MathUtil.deg2rad; 
		double x2 = 40 * MathUtil.deg2rad; 
		double y2 = 50 * MathUtil.deg2rad;
		double d1 = SphericalCoordsLongZen.getSphericalDistance(x1, y1, x2, y2);
		double d2 = SphericalCoordsLongZen.getSphericalDistance(x2, y2, x1, y1);
		System.out.println(d1 - d2);
	}
	
	void sss() {
		double angles[] = {
				10 * MathUtil.deg2rad,
				0 * MathUtil.deg2rad,
				30 * MathUtil.deg2rad
		};
		double tmp[] = new double[2];
		SphericalCoordsLongZen.rotateForeward(0, 0*MathUtil.deg2rad, angles[0], angles[1], angles[2], tmp);
		TestUtils.dumpAngles("", tmp);
		
		Matrix m = RotationZYZ.instance.makeAngles(angles);
		RotationZYZ.instance.getRotationAngles(m, angles);
		TestUtils.dumpAngles("", angles);
	}
	
	public static void main(String[] args) {
		new Dummy().sss();
	}
}
