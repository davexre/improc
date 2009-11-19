package com.unitTest;

import com.slavi.math.MathUtil;

public class TestUtils {

	static double precision = 1000;
	
	public static boolean equal(double a, double b) {
		return ((int)(Math.abs(a - b) * precision) == 0);
	}
	
	public static void assertEqualAngle(String str, double a, double b) {
		a = MathUtil.fixAngle2PI(a);
		b = MathUtil.fixAngle2PI(b);
		if (equal(a, b)) 
			return;
		System.out.println("Angles not equal: " + str);
		System.out.println(MathUtil.rad2degStr(a));
		System.out.println(MathUtil.rad2degStr(b));
		throw new RuntimeException("Failed");
	}
	
	public static void assertEqual(String str, double a, double b) {
		if (equal(a, b)) 
			return;
		System.out.println("Values not equal: " + str);
		System.out.println(MathUtil.d20(a));
		System.out.println(MathUtil.d20(b));
		throw new RuntimeException("Failed");
	}

	public static void assertEqual(String str, double a[], double b[]) {
		if (a.length != b.length) {
			System.out.println("Arrays have different sizes: " + str);
		}
		for (int i = 0; i < a.length; i++) {
			if (!equal(a[i], b[i])) {
				System.out.println("Arrays differ: " + str);
				System.out.println("at index " + i);
				System.out.println(MathUtil.d20(a[i]));
				System.out.println(MathUtil.d20(b[i]));
				throw new RuntimeException("Failed");
			}
		}
	}
	
	public static void dumpArray(String str, double p[]) {
		System.out.print(str);
		for (int i = 0; i < p.length; i++) 
			System.out.print("\t" + MathUtil.d4(p[0]));
		System.out.println();
	}
	
	public static void dumpAngles(String str, double p[]) {
		System.out.print(str);
		for (int i = 0; i < p.length; i++) 
			System.out.print("\t" + MathUtil.rad2degStr(p[0]));
		System.out.println();
	}
}
