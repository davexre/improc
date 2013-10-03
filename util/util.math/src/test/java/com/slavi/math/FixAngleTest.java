package com.slavi.math;

import org.junit.Test;

public class FixAngleTest {

	public static double precision = 1.0 / 10000.0;
	
	static boolean equal(double a, double b) {
		return Math.abs(a - b) <= precision;
	}
	static void assertEqualAngle(String msg, double a, double b) {
		if (equal(a, b))
			return;
		System.out.println("Angles not equal: " + msg);
		System.out.println(MathUtil.rad2degStr(a));
		System.out.println(MathUtil.rad2degStr(b));
		throw new RuntimeException("Failed");
	}

	private void doTestFixAngle2PI(double angle, double expected) {
		double a, e;
		a = angle * MathUtil.deg2rad;
		e = MathUtil.fixAngle2PI(a);
		assertEqualAngle("", e, expected * MathUtil.deg2rad);
	}
	
	@Test
	public void testFixAngle2PI() {
		doTestFixAngle2PI(0, 0);
		doTestFixAngle2PI(10, 10);
		doTestFixAngle2PI(-10, 350);
		doTestFixAngle2PI(-170, 190);
		
		doTestFixAngle2PI(720, 0);
		doTestFixAngle2PI(-720, 0);
		doTestFixAngle2PI(180, 180);
		doTestFixAngle2PI(-180, 180);
		doTestFixAngle2PI(360, 0);
		doTestFixAngle2PI(-360, 0);
	}
	
	private void doTestFixAnglePI(double angle, double expected) {
		double a, e;
		a = angle * MathUtil.deg2rad;
		e = MathUtil.fixAnglePI(a);
		assertEqualAngle("", e, expected * MathUtil.deg2rad);
	}
	
	@Test
	public void testFixAnglePI() {
		doTestFixAnglePI(0, 0);
		doTestFixAnglePI(10, 10);
		doTestFixAnglePI(-10, 170);
		doTestFixAnglePI(-170, 10);
		doTestFixAnglePI(180, 0);
	}
	
	public static void main(String[] args) {
		FixAngleTest test = new FixAngleTest();
		test.testFixAnglePI();
		test.testFixAngle2PI();
		System.out.println("Done.");
	}
}
