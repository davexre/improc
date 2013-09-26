package com.slavi.util.testUtil;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;

public class TestUtil {

	public static double precision = 1.0 / 10000.0;
	
	public static boolean equal(double a, double b) {
		return Math.abs(a - b) <= precision;
	}
	
	public static void assertTrue(String msg, boolean b) {
		if (b)
			return;
		System.out.println("Expected true, but was false: " + msg);
		throw new RuntimeException("Failed");
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

	public static void assertEqual(String msg, String strA, String strB) {
		if ((strA == null) && (strB == null))
			return;
		if ((strA != null) && strA.equals(strB))
			return;
		System.out.println("Strings not equal: " + msg);
		System.out.println(strA);
		System.out.println(strB);
		throw new RuntimeException("Failed");
	}
	
	public static void assertEqual(String msg, int a, int b) {
		if (a == b)
			return;
		System.out.println("Values not equal: " + msg);
		System.out.println(a);
		System.out.println(b);
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

	public static void assertEqual(String msg, byte a[], byte b[]) {
		if (a.length != b.length) {
			System.out.println("Arrays have different sizes: " + msg);
		}
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) {
				System.out.println("Arrays differ: " + msg);
				System.out.println("at index " + i);
				System.out.println(MathUtil.d20(a[i]));
				System.out.println(MathUtil.d20(b[i]));
				throw new RuntimeException("Failed");
			}
		}
	}

	public static void assertMatrix0(String msg, Matrix m) {
		if (m.is0(precision))
			return;
		System.out.println("Matrix elements not equal to 0: " + msg);
		System.out.println(m.toString());
		throw new RuntimeException("Failed");
	}
	
	public static void assertMatrixE(String msg, Matrix m) {
		if (m.isE(precision))
			return;
		System.out.println("Matrix not equal to E: " + msg);
		System.out.println(m.toString());
		throw new RuntimeException("Failed");
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
