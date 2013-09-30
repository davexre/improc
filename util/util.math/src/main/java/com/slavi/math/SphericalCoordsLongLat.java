package com.slavi.math;

public class SphericalCoordsLongLat {

	/**
	 * Find the angular (Great circle) distance between the two points on a sphere.
	 * http://en.wikipedia.org/wiki/Great-circle_distance
	 * 
	 * x -> geographical longitude
	 * y -> geographical latitude
	 */
	public static double getSphericalDistance(double rx1, double ry1, double rx2, double ry2) {
		double cosY1 = Math.cos(ry1);
		double sinY1 = Math.sin(ry1);
		double cosY2 = Math.cos(ry2);
		double sinY2 = Math.sin(ry2);
		rx2 -= rx1;
		double cosDX = Math.cos(rx2);
		double sinDX = Math.sin(rx2);
		
		double tmp1 = cosY2 * sinDX;
		double tmp2 = cosY1 * sinY2 - sinY1 * cosY2 * cosDX;
		
		double dy = Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2);
		double dx = sinY1 * sinY2 + cosY1 * cosY2 * cosDX;
		return Math.atan2(dy, dx);
	}
	
	/**
	 * sx -> longitude
	 * sy -> latitude
	 * r -> radius 
	 * dest[0] = x
	 * dest[1] = y
	 * dest[2] = z
	 */
	public static void polarToCartesian(double sx, double sy, double r, double dest[]) {
		double RcosSY = r * Math.cos(sy);
		dest[0] = Math.cos(sx) * RcosSY;
		dest[1] = Math.sin(sx) * RcosSY;
		dest[2] = r * Math.sin(sy);
	}
	
	/**
	 * src[0] = sx -> longitude
	 * src[1] = sy -> latitude
	 * src[2] = r -> radius 
	 * dest[0] = x
	 * dest[1] = y
	 * dest[2] = z
	 */
	public static void polarToCartesian(double src[], double dest[]) {
		polarToCartesian(src[0], src[1], src[2], dest);
	}
	
	/**
	 * x=1,y=z=0 -> long=0,lat=0,r=1
	 * y=1,x=z=0 -> long=pi/2,lat=0,r=1
	 * z=1,x=y=0 -> long=0,lat=pi/2,r=1
	 * 
	 * dest[0] = sx -> longitude
	 * dest[1] = sy -> latitude
	 * dest[2] = r -> radius 
	 */
	public static void cartesianToPolar(double x, double y, double z, double dest[]) {
		dest[0] = ((x == 0.0) && (y == 0.0)) ? 0.0 : Math.atan2(y, x);
		dest[2] = Math.sqrt(x*x + y*y + z*z);
		dest[1] = dest[2] == 0.0 ? 0.0 : Math.asin(z / dest[2]);
	}
	
	/**
	 * src[0] = x 
	 * src[1] = y 
	 * src[2] = z 
	 * dest[0] = sx -> longitude
	 * dest[1] = sy -> latitude
	 * dest[2] = r -> radius 
	 */
	public static void cartesianToPolar(double src[], double dest[]) {
		cartesianToPolar(src[0], src[1], src[2], dest);
	}
}
