package com.test.improc;

import com.slavi.improc.myadjust.SpherePanoTransformer2;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationZYZ;
import com.slavi.math.matrix.Matrix;

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

		SpherePanoTransformer2.rotateForeward(dest1[0], dest1[1], 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, dest2);
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

	public static void main(String[] args) {
		new Dummy().qwe();
	}
}
