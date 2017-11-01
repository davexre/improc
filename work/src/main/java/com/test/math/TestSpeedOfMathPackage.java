package com.test.math;

import com.slavi.util.Util;

public class TestSpeedOfMathPackage {

	static final double initialValue = 1.000999993456789;
	
	public static void testNumberOfMultiplies() {
		long astart = System.currentTimeMillis();
		double a = initialValue;
		double b;
		for (int i = 1; i <= 10_000_000; i++) 
			for (int j = 1; j <= 10; j++) {
				a = i + j;
				b = a * 2.0;
				b *= a;
				a *= a;
				b /= a;
				if (b < 1)
					System.out.println("Should not happen");
				a = initialValue;
			}
		long aend = System.currentTimeMillis();
		System.out.println("DUM Elapsed " + Util.getFormatedMilliseconds(aend - astart));
	}

	public static void testNumberOfSQRs() {
		long astart = System.currentTimeMillis();
		double a = initialValue;
		double b;
		for (int i = 1; i <= 10_000_000; i++) 
			for (int j = 1; j <= 10; j++) {
				a = i + j;
				b = a * a * a * a; // * a * a;
				//b = a * a;	b *= b;
				if (b < 1)
					System.out.println("Should not happen");
			}
		long aend = System.currentTimeMillis();
		System.out.println("MUL Elapsed " + Util.getFormatedMilliseconds(aend - astart));
	}

	public static void testNumberOfMathSQRs() {
		long astart = System.currentTimeMillis();
		double a = initialValue;
		double b;
		for (int i = 1; i <= 10_000_000; i++) 
			for (int j = 1; j <= 10; j++) {
				a = i + j;
				b = Math.sqrt(Math.sqrt(a));
				if (b < 1)
					System.out.println("Should not happen");
			}
		long aend = System.currentTimeMillis();
		System.out.println("SQR Elapsed " + Util.getFormatedMilliseconds(aend - astart));
	}

	public static void testNumberOfMathPOWs() {
		long astart = System.currentTimeMillis();
		double a = initialValue;
		double b;
		for (int i = 1; i <= 10_000_000; i++) 
			for (int j = 1; j <= 10; j++) {
				a = i + j;
				b = Math.pow(a, 4.0);
				if (b < 1)
					System.out.println("Should not happen");
			}
		long aend = System.currentTimeMillis();
		System.out.println("POW Elapsed " + Util.getFormatedMilliseconds(aend - astart));
	}
	
	
	public static void main(String[] args) {
		testNumberOfMultiplies();
		testNumberOfSQRs();
		testNumberOfMathSQRs();
		testNumberOfMathPOWs();
	}
}
