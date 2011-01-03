package com.slavi.math;

import java.util.Locale;

public class MathUtil {
	public static final double eps = calcEps();
	
	public static final double epsAngle = calcAngleEps(0);
	
	public static final double C2PI = Math.PI * 2.0;
	
	public static final double PIover2 = Math.PI / 2.0;

	public static final double deg2rad = Math.PI / 180;
	
	public static final double rad2deg = 180 / Math.PI;
	
	public static final double rad2grad = 200 / Math.PI;

	public static final double grad2rad = Math.PI / 200;

	public static String d20(double d) {
		return String.format(Locale.US, "%1$1.20f", d);
	}
	
	public static String l10(long d) {
		return String.format(Locale.US, "%1$11d", d);
	}
	
	public static String d4(double d) {
		return String.format(Locale.US, "%1$11.4f", d);
	}
	
	public static String d2(double d) {
		return String.format(Locale.US, "%1$1.2f", d);
	}
	
	public static String rad2radStr(double angle) {
		return String.format(Locale.US, "%+9.7f rad", angle);
	}
	
	public static String rad2gradStr(double angle) {
		return String.format(Locale.US, "%+1.5f", angle * rad2grad);
	}
	
	public static String rad2degStr(double angle) {
		angle = angle * rad2deg;
		double gr = (int) angle;
		if ((gr == 0) & (angle < 0))
			gr = -0.0000000000001;
		angle = Math.abs(angle - gr) * 60;
		int min = (int) Math.floor(angle);
		double sec = (angle - min) * 60;
		return String.format(Locale.US, "% 4.0f°%2d'%4.1f\"", gr, min, sec);
	}
	
	/**
	 * Returns a number from one range to another. Out-of-range values are re-mapped accordingly.
	 * The "lower bounds" of either range may be larger or smaller than the "upper bounds". 
	 */
	public static double mapValue(double vale, double fromLow, double fromHigh, double toLow, double toHigh) {
	  return (vale - fromLow) * (toHigh - toLow) / (fromHigh - fromLow) + toLow;
	}
	
	/**
	 * Returns the value if min <= value <= max else returns min or max respecively.
	 */
	public static double clipValue(double value, double min, double max) {
		return Math.min(max, Math.max(min, value));
	}
	
	/**
	 * Returns the value if min <= value <= max else returns min or max respecively.
	 */
	public static int clipValue(int value, int min, int max) {
		return Math.min(max, Math.max(min, value));
	}
	
	/**
	 * Returns the specified angle in the range [0..2*pi)
	 */
	public static double fixAngle2PI(double angle) {
		angle %= MathUtil.C2PI;
		return (angle < 0) ? MathUtil.C2PI + angle : angle;
//		return Math.abs(angle - Math.floor(angle / C2PI) * C2PI);
	}

	/**
	 * Returns the specified angle in the range (-pi..pi] 
	 */
	public static double fixAngleMPI_PI(double angle) {
		angle += Math.PI;
		angle %= MathUtil.C2PI;
		return ((angle < 0) ? MathUtil.C2PI + angle : angle) - Math.PI;
//		angle += Math.PI;
//		return Math.abs(angle - Math.floor(angle / C2PI) * C2PI) - Math.PI;
	}
	
	/**
	 * Returns the specified angle in the range [0..pi)
	 */
	public static double fixAnglePI(double angle) {
		angle %= Math.PI;
		return (angle < 0) ? Math.PI + angle : angle;
//		return Math.abs(angle - Math.floor(angle / Math.PI) * Math.PI);
	}

	/**
	 * Transfer the sign of B on to A. 
	 */
	public static final double SIGN(double a, double b) {
		return (b >= 0.0 ? Math.abs(a) : -Math.abs(a));
	}
	
	/** 
	 * sqrt(a^2 + b^2) without under/overflow. 
	 */
	public static double hypot(double a, double b) {
		double r;
		double absA = Math.abs(a);
		double absB = Math.abs(b);
		if (absA > absB) {
			r = b / a;
			r = absA * Math.sqrt(1 + r * r);
		} else if (b != 0) {
			r = a / b;
			r = absB * Math.sqrt(1 + r * r);
		} else {
			r = 0.0;
		}
		return r;
	}
	
	/**
	 * Calculates the machine precision for trigonometric functions.
	 * @returns The maximum angle (epsAngle) in radians such that Math.cos(angle) == Math.cos(angle+epsAngle)  
	 */
	private static double calcAngleEps(double angle) {
		double eps = 1.0; // 1 radian
		double c1 = Math.cos(angle);
		while(true) {
			double c2 = Math.cos(angle + eps);
			if (c1 == c2) {
				return eps;
			}
			eps /= 10.0;
		}
	}
	
	/**
	 * Calculates the machine precision.
	 * @returns The maximum eps such that 1.0 == 1.0 + eps  
	 */
	private static double calcEps() {
		double one = 1.0;
		double eps = 1.0;
		while(true) {
			double onePlusEps = one + eps;
			if (one == onePlusEps) {
				return eps;
			}
			eps /= 10.0;
		}
	}
}
