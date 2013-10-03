package com.test.math;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.testUtil.TestUtil;

public class ShearXYZTest {
	public static void main(String[] args) {
		ShearXYZ shear = new ShearXYZ();
		
		double dR = MathUtil.deg2rad * 0.1;
		double angle[] = { MathUtil.deg2rad * 10, MathUtil.deg2rad * 20, MathUtil.deg2rad * 30, MathUtil.deg2rad * 40 };
		double angle0[] = new double[angle.length];
		for (int i = 0; i < angle.length; i++)
			angle0[i] = angle[i] + dR;
		
		double point[] = { 123, 234, 345 };


		Matrix m = shear.makeAngles(angle[0], angle[1], angle[2], angle[3]);
		Matrix dm = shear.makeAngles(angle0[0], angle0[1], angle0[2], angle0[3]);
		Matrix dF_dr1  = shear.make_dF_dR1 (angle0[0], angle0[1], angle0[2], angle0[3]);
		Matrix dF_dr2  = shear.make_dF_dR2 (angle0[0], angle0[1], angle0[2], angle0[3]);
		Matrix dF_dr3  = shear.make_dF_dR3 (angle0[0], angle0[1], angle0[2], angle0[3]);
		Matrix dF_drot = shear.make_dF_dROT(angle0[0], angle0[1], angle0[2], angle0[3]);

		double dest0[] = new double[3];
		double dest1[] = new double[3];
		double dest2[] = new double[3];
		double dr1[] = new double[3];
		double dr2[] = new double[3];
		double dr3[] = new double[3];
		double drot[] = new double[3];
		
		shear.transformForward(dm, point, dest0);
		shear.transformForward(dF_dr1, point, dr1);
		shear.transformForward(dF_dr2, point, dr2);
		shear.transformForward(dF_dr3, point, dr3);
		shear.transformForward(dF_drot, point, drot);

		shear.transformForward(m, point, dest1);
		
		TestUtil.dumpArray("dest0", dest0);
		TestUtil.dumpArray("dest1", dest1);
		for (int i = 0; i < dest2.length; i++) {
			dest2[i] = dest0[i] - dr1[i]*dR - dr2[i]*dR - dr3[i]*dR - drot[i]*dR;
		}
		
		TestUtil.dumpArray("dest2", dest2);
//		Assert.assertArrayEquals(dest1, dest2, 0.001);
	}
}
