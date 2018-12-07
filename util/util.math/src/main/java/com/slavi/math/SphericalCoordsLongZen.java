package com.slavi.math;

public class SphericalCoordsLongZen {

	/**
	 * Find the angular (Great circle) distance between the two points on a sphere.
	 * http://en.wikipedia.org/wiki/Great-circle_distance
	 *
	 * rx - longitude
	 * ry - zenith angle (90 - latitude)
	 */
	public static double getSphericalDistance(double rx1, double ry1, double rx2, double ry2) {
		// sin(90-a) = cos(a)
		// cos(90-a) = sin(a)
		double cosY1 = Math.cos(ry1);
		double sinY1 = Math.sin(ry1);
		double cosY2 = Math.cos(ry2);
		double sinY2 = Math.sin(ry2);
		rx2 -= rx1;
		double cosDX = Math.cos(rx2);
		double sinDX = Math.sin(rx2);

		double tmp1 = sinY2 * sinDX;
		double tmp2 = sinY1 * cosY2 - cosY1 * sinY2 * cosDX;

		double dy = Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2);
		double dx = cosY1 * cosY2 + sinY1 * sinY2 * cosDX;
		return Math.atan2(dy, dx);
	}

	/**
	 * @param sx	longitude
	 * @param sy	zenith angle (90 - latitude)
	 * @param r		radius
	 * @param dest	[0] = x, [1] = y, [2] = z
	 */
	public static void polarToCartesian(double sx, double sy, double r, double dest[]) {
		double RsinSY = r * Math.sin(sy);
		dest[0] = Math.cos(sx) * RsinSY;
		dest[1] = Math.sin(sx) * RsinSY;
		dest[2] = r * Math.cos(sy);
	}

	/**
	 * @param src	[0] = sx - longitude,
	 * 				[1] = sy - zenith angle (90 - latitude),
	 * 				[2] = r - radius
	 * @param dest	[0] = x, [1] = y, [2] = z
	 */
	public static void polarToCartesian(double src[], double dest[]) {
		polarToCartesian(src[0], src[1], src[2], dest);
	}

	/**
	 * x=1,y=z=0 - long=0,zenith=pi/2,r=1
	 * y=1,x=z=0 - long=pi/2,zenith=pi/2,r=1
	 * z=1,x=y=0 - long=0,zenith=0,r=1
	 *
	 * @param dest	[0] = sx - longitude,
	 * 				[1] = sy - zenith angle (90 - latitude),
	 * 				[2] = r - radius
	 */
	public static void cartesianToPolar(double x, double y, double z, double dest[]) {
		dest[0] = ((x == 0.0) && (y == 0.0)) ? 0.0 : Math.atan2(y, x);
		dest[2] = Math.sqrt(x*x + y*y + z*z);
		dest[1] = dest[2] == 0.0 ? 0.0 : Math.acos(z / dest[2]);
	}

	/**
	 * @param src	[0] = x,
	 * 				[1] = y,
	 * 				[2] = z
	 * @param dest	[0] = sx - longitude,
	 * 				[1] = sy - zenith angle (90 - latitude)
	 * 				[2] = r - radius
	 */
	public static void cartesianToPolar(double src[], double dest[]) {
		cartesianToPolar(src[0], src[1], src[2], dest);
	}

	/**
	 * @param sx	longitude
	 * @param sy	zenith angle (90 - latitude)
	 */
	public static void rotateForeward(double sx, double sy, double IZ1, double IY, double IZ2, double dest[]) {
		sx -= IZ1;
		double sinDX = Math.sin(sx);
		double cosDX = Math.cos(sx);
		double sinIY = Math.sin(IY);
		double cosIY = Math.cos(IY);
		double sinSY = Math.sin(sy);
		double cosSY = Math.cos(sy);

		dest[0] = MathUtil.fixAngle2PI(Math.atan2(sinDX * sinSY, cosDX * cosIY * sinSY - sinIY * cosSY) - IZ2);
		double d = cosSY * cosIY + sinSY * sinIY * cosDX;
		if (d > 1.0)
			d = 1.0; // this might happen (ex. sx == IZ1 == 12 * MathUtil.deg2rad)
		dest[1] = Math.acos(d);
	}

	/**
	 * @param rx	longitude
	 * @param ry	zenith angle (90 - latitude)
	 */
	public static void rotateBackward(double rx, double ry, double IZ1, double IY, double IZ2, double dest[]) {
		rx += IZ2;
		double sinIY = Math.sin(IY);
		double cosIY = Math.cos(IY);
		double sinRY = Math.sin(ry);
		double cosRY = Math.cos(ry);
		double sinRX = Math.sin(rx);
		double cosRX = Math.cos(rx);

		dest[0] = MathUtil.fixAngle2PI(Math.atan2(sinRX * sinRY, sinIY * cosRY + cosRX * cosIY * sinRY) + IZ1);
		double d = cosRY * cosIY - sinRY * sinIY * cosRX;
		if (d > 1.0)
			d = 1.0; // this might happen (ex. sx == IZ1 == 12 * MathUtil.deg2rad)
		dest[1] = Math.acos(d);
	}
}
