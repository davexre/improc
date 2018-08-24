package com.slavi.math;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MathUtil {
	public static final double eps = calcDoubleEps();

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
		return String.format(Locale.US, "%1$10d", d);
	}

	public static String d2(double d) {
		return String.format(Locale.US, "%1$1.2f", d);
	}

	public static String d3(double d) {
		return String.format(Locale.US, "%1$5.3f", d);
	}

	public static String d4(double d) {
		return String.format(Locale.US, "%1$10.4f", d);
	}

	public static String d5(double d) {
		return String.format(Locale.US, "%1$10.5f", d);
	}

	public static String d6(double d) {
		return String.format(Locale.US, "%1$10.6f", d);
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

	public static boolean isInRange(double value, double minValue, double maxValue) {
		return minValue <= value && value <= maxValue;
	}

	public static boolean isInRange(int value, int minValue, int maxValue) {
		return minValue <= value && value <= maxValue;
	}

	public static boolean isInBetween(double value, double A, double B) {
		return A < B ?
				A <= value && value <= B :
				B <= value && value <= A;
	}

	public static boolean isInBetween(int value, int A, int B) {
		return A < B ?
				A <= value && value <= B :
				B <= value && value <= A;
	}

	/**
	 * Returns -1 - choose A, 1 - choose B, 0 - Value is in the middle between A and B or A == B
	 */
	public static int snap(double A, double value, double B) {
		return Double.compare(Math.abs(value - A), Math.abs(value - B));
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
	 * Returns the index "within" the 0..size-1 looping past the end if index is out of bounds.
	 */
	public static int fixIndexLooped(int index, int size) {
		if (size <= 0)
			return -1;
		index %= size;
		if (index < 0)
			index += size;
		return index;
	}

	public static int fixIndex(int index, int size) {
		if (index < 0)
			index = 0;
		if (index >= size)
			index = size - 1;
		if (size <= 0)
			return -1;
		return index;
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
		angle  = MathUtil.fixAngle2PI(angle);
		return (angle > Math.PI) ? MathUtil.C2PI - angle : angle;
/*		angle %= Math.PI;
		return (angle < 0) ? Math.PI + angle : angle;*/
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
	public static double calcDoubleEps() {
		double one = 1.0;
		double eps = 1.0;
		while(true) {
			double onePlusEps = one + eps;
			if (one == onePlusEps) {
				return eps;
			}
			eps /= 2.0;
		}
	}

	public static float calcFloatEps() {
		float one = 1.0f;
		float eps = 1.0f;
		while(true) {
			float onePlusEps = one + eps;
			if (one == onePlusEps) {
				return eps;
			}
			eps /= 2.0f;
		}
	}

	/**
	 * GCD - Greatest Common Divisor
	 *
	 * @see http://en.wikipedia.org/wiki/Greatest_common_divisor
	 * @see http://en.wikipedia.org/wiki/Euclidean_algorithm
	 *
	 */
	public static int gcd(int a, int b) {
		while (b != 0) {
			int t = b;
			b = a % b;
			a = t;
		}
		return a;
	}

	/**
	 * GCD - Greatest Common Divisor
	 *
	 * @see http://en.wikipedia.org/wiki/Greatest_common_divisor
	 * @see http://en.wikipedia.org/wiki/Euclidean_algorithm
	 *
	 */
	public static long gcd(long a, long b) {
		while (b != 0) {
			long t = b;
			b = a % b;
			a = t;
		}
		return a;
	}

	public static int gcd(Set<Integer> data) {
		Set<Integer> set = new HashSet<>(data);
		Set<Integer> newSet = new HashSet<>();
		while (data.size() > 1) {
			newSet.clear();
			Integer first = null;
			for (Integer i : data) {
				if (first == null) {
					first = i;
					continue;
				}
				newSet.add(MathUtil.gcd(first, i));
			}

			Set<Integer> dummy = data;
			data = set;
			set = dummy;
		}
		return data.size() > 0 ? data.iterator().next() : 0;
	}

}
