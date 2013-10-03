package com.slavi.math;

import org.junit.Test;

import com.slavi.math.matrix.Matrix;
import com.slavi.util.testUtil.TestUtil;

public class Rotation3DTest {

	Rotation3D rots[] = {
			new RotationXYZ(),
			new RotationZYX(),
			new RotationZYZObjects(),
			new RotationZYZ(),	// ??? need a check of the derivatives
			new RotationZXZ(),	// ??? need a check of the derivatives
	};
	
	private interface TestRotation {
		public void doIt(Rotation3D rot);
	}
	
	private void forEachRotation3D(String testStr, TestRotation testTask) {
		for (Rotation3D rot : rots) {
			System.out.println("Testing " + testStr + " for " + rot.getClass().getName());
			testTask.doIt(rot);
		}
	}
	
	@Test
	public void testTransform() throws Exception {
		forEachRotation3D("Transform", new TestRotation() {
			public void doIt(Rotation3D rot) {
				double angles[] = { 10 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 30 * MathUtil.deg2rad };
				double point[] = { 5, 15, 25 };
				double tmp1[] = new double[3];
				double tmp2[] = new double[3];
				Matrix r = rot.makeAngles(angles);

				rot.transformForward(r, point[0], point[1], point[2], tmp1);
				rot.transformBackward(r, tmp1[0], tmp1[1], tmp1[2], tmp2);
				TestUtil.assertEqual("", point, tmp2);
			}
		});
	}
	
	@Test
	public void testGetRotationAngles() throws Exception {
		forEachRotation3D("GetRotationAngles", new TestRotation() {
			public void doIt(Rotation3D rot) {
				double angles[] = { 10 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 30 * MathUtil.deg2rad };
				double tmp1[] = new double[3];
				Matrix r = rot.makeAngles(angles);

				rot.getRotationAngles(r, tmp1);
				TestUtil.assertEqual("", angles, tmp1);
			}
		});
	}
	
	@Test
	public void testGetRotationAnglesBackword() throws Exception {
		forEachRotation3D("GetRotationAnglesBackward", new TestRotation() {
			public void doIt(Rotation3D rot) {
				double angles[] = { 10 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 30 * MathUtil.deg2rad };
				double point[] = { 5, 15, 25 };
				double tmp1[] = new double[3];
				double tmp2[] = new double[3];
				double tmp3[] = new double[3];
				Matrix r = rot.makeAngles(angles);
				
				rot.transformForward(r, point[0], point[1], point[2], tmp1);
				rot.getRotationAnglesBackword(r, tmp2);
				Matrix back = rot.makeAngles(tmp2);
				rot.transformForward(back, tmp1[0], tmp1[1], tmp1[2], tmp3);
				TestUtil.assertEqual("", point, tmp3);
				
				rot.getRotationAnglesBackword(angles[0], angles[1], angles[2], tmp3);
				TestUtil.assertEqual("", tmp2, tmp3);
				
				// check identity
				Matrix tmpM = new Matrix();
				r.mMul(back, tmpM);
				if (!tmpM.isE(1E-15))
					throw new RuntimeException("Failed");
			}
		});
	}
	
	@Test
	public void test_dF() throws Exception {
		forEachRotation3D("dF", new TestRotation() {
			public void doIt(Rotation3D rot) {
				double point[] = { 5, 15, 25 };

				double angles[] = { 10 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 30 * MathUtil.deg2rad };
				double dest1[] = new double[3];
				Matrix r = rot.makeAngles(angles);
				rot.transformForward(r, point, dest1);

				double delta = 0.01 * MathUtil.deg2rad;
				double angles0[] = { 
						angles[0] + delta,
						angles[1] + delta,
						angles[2] + delta};
				double dest0[] = new double[3];
				Matrix r0 = rot.makeAngles(angles0);
				rot.transformForward(r0, point, dest0);
				
				Matrix dF[] = {
						rot.make_dF_dR1(angles0[0], angles0[1], angles0[2]), 
						rot.make_dF_dR2(angles0[0], angles0[1], angles0[2]), 
						rot.make_dF_dR3(angles0[0], angles0[1], angles0[2])
				};
				
				double dest2[] = dest0.clone();
				
				for (int dindex = 0; dindex < 3; dindex++) {
					double tmp[] = new double[3];
					rot.transformForward(dF[dindex], point, tmp);
					for (int i = 0; i < 3; i++) {
						dest2[i] -= tmp[i] * delta;
					}
				}
				TestUtil.dumpArray("dest0", dest0);
				TestUtil.dumpArray("dest1", dest1);
				TestUtil.dumpArray("dest2", dest2);
				
				TestUtil.assertEqual("", dest1, dest2);
			}
		});
	}
	
	public static void main(String[] args) throws Exception {
		new Rotation3DTest().test_dF();
	}
}
