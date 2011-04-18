package com.slavi.math;

import org.junit.Test;

import com.slavi.TestUtils;
import com.slavi.math.matrix.Matrix;

public class Rotation3DTest {

	Rotation3D rots[] = {
			new RotationXYZ(),
			new RotationZXZ(),
			new RotationZYX(),
			new RotationZYZ(),
			new RotationZYZObjects()
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
				TestUtils.assertEqual("", point, tmp2);
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
				TestUtils.assertEqual("", angles, tmp1);
			}
		});
	}
	
	@Test
	public void testGetRotationAnglesBackword() throws Exception {
		forEachRotation3D("GetRotationAnglesBackword", new TestRotation() {
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
				TestUtils.assertEqual("", point, tmp3);
				
				rot.getRotationAnglesBackword(angles[0], angles[1], angles[2], tmp3);
				TestUtils.assertEqual("", tmp2, tmp3);
				
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
				double angles[] = { 10 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 30 * MathUtil.deg2rad };
				double point[] = { 5, 15, 25 };
				double tmp1[] = new double[3];
				double tmp2[] = new double[3];
				double tmp3[] = new double[3];
				Matrix r = rot.makeAngles(angles);
				rot.transformForward(r, point, tmp1);
				double delta = 0.000001 * MathUtil.deg2rad;

				Matrix dF[] = {
						rot.make_dF_dR1(angles[0], angles[1], angles[2]), 
						rot.make_dF_dR2(angles[0], angles[1], angles[2]), 
						rot.make_dF_dR3(angles[0], angles[1], angles[2])
				};
				
				for (int dindex = 0; dindex < 3; dindex++) {
					double d[] = { 0, 0, 0 };
					d[dindex] = delta;
					rot.transformForward(dF[dindex], point, tmp2);
					for (int i = 0; i < 3; i++) {
						tmp2[i] = tmp1[i] + tmp2[i] * delta;
					}
					double d2[] = point.clone();
					d2[dindex] += delta;
					rot.transformForward(r, d2, tmp3);
					TestUtils.assertEqual("", tmp3, tmp2);
				}
			}
		});
	}
}
