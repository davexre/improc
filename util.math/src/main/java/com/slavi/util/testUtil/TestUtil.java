package com.slavi.util.testUtil;

import com.slavi.math.MathUtil;

public class TestUtil {

	public static double precision = 1.0 / 10000.0;
	
	public static boolean equal(double a, double b) {
		return Math.abs(a - b) <= precision;
	}
	
	public static void assertEqualAngle(String msg, double a, double b) {
		a = MathUtil.fixAngle2PI(a);
		b = MathUtil.fixAngle2PI(b);
		if (equal(a, b)) 
			return;
		System.out.println("Angles not equal: " + msg);
		System.out.println(MathUtil.rad2degStr(a));
		System.out.println(MathUtil.rad2degStr(b));
		throw new RuntimeException("Failed");
	}
	
	public static void assertEqualIgnoreCase(String msg, String strA, String strB) {
		if ((strA == null) && (strB == null))
			return;
		if ((strA != null) && strA.equalsIgnoreCase(strB))
			return;
		System.out.println("Strings not equal: " + msg);
		System.out.println(strA);
		System.out.println(strB);
		throw new RuntimeException("Failed");
	}

	public static void assertEqual(String msg, double a, double b) {
		if (equal(a, b)) 
			return;
		System.out.println("Values not equal: " + msg);
		System.out.println(MathUtil.d20(a));
		System.out.println(MathUtil.d20(b));
		throw new RuntimeException("Failed");
	}

	public static void assertEqual(String msg, double a[], double b[]) {
		if (a.length != b.length) {
			System.out.println("Arrays have different sizes: " + msg);
		}
		for (int i = 0; i < a.length; i++) {
			if (!equal(a[i], b[i])) {
				System.out.println("Arrays differ: " + msg);
				System.out.println("at index " + i);
				System.out.println(MathUtil.d20(a[i]));
				System.out.println(MathUtil.d20(b[i]));
				throw new RuntimeException("Failed");
			}
		}
	}

	public static void dumpArray(String msg, double p[]) {
		System.out.print(msg);
		for (int i = 0; i < p.length; i++) 
			System.out.print("\t" + MathUtil.d4(p[i]));
		System.out.println();
	}
	
	public static void dumpAngles(String msg, double p[]) {
		System.out.print(msg);
		for (int i = 0; i < p.length; i++) 
			System.out.print("\t" + MathUtil.rad2degStr(p[i]));
		System.out.println();
	}
}
