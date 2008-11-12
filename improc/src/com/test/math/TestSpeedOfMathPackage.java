package com.test.math;

import com.slavi.util.Util;

public class TestSpeedOfMathPackage {

	public static void testNumberOfMultiplies() {
		long astart = System.currentTimeMillis();
		double a = 0.999993456789;
		double b;
		for (int i = 1; i <= 100000000; i++) 
			for (int j = 1; j <= 10; j++) {
			    b = i * a * 2.0;
				b /= a;
				if (b < 1)
					System.out.println("Should not happen");
			}
		long aend = System.currentTimeMillis();
		System.out.println("Elapsed " + Util.getFormatedMilliseconds(aend - astart));
	}

	public static void testNumberOfSQRs() {
		long astart = System.currentTimeMillis();
		double a = 1.000999993456789;
		for (int i = 1; i <= 10000000; i++) 
			for (int j = 1; j <= 10; j++) {
			    a = a * a * a; // * a * a;
				if (a < 1)
					System.out.println("Should not happen");
			}
		long aend = System.currentTimeMillis();
		System.out.println("Elapsed " + Util.getFormatedMilliseconds(aend - astart));
	}
	
	public static void testNumberOfMathSQRs() {
		long astart = System.currentTimeMillis();
		double a = 1.000999993456789;
		for (int i = 1; i <= 10000000; i++) 
			for (int j = 1; j <= 10; j++) {
			    a = Math.pow(a, 3.0);
				if (a < 1)
					System.out.println("Should not happen");
			}
		long aend = System.currentTimeMillis();
		System.out.println("Elapsed " + Util.getFormatedMilliseconds(aend - astart));
	}
	
	
	public static void main(String[] args) {
		//testNumberOfMultiplies();

		testNumberOfSQRs();
		testNumberOfMathSQRs();
	}

}
