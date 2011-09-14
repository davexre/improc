package com.slavi.math;

public class GeometryUtil {
	/**
	 * Returns the distance^2 from point to a ray.
	 * 
	 * Code borrowed from: http://www.codeguru.com/forum/printthread.php?t=194400
	 * 
	 * Suppose you have points A(xa, ya), B(xb, yb) and C(xc,yc). The distance 
	 * between point C and line segment AB equals the area of parallelgram ABCC' 
	 * divided by the length of AB.
	 * 
	 * distance = |AB X AC| / sqrt(AB * AB)
	 * Here X mean cross product of vectors, and * mean dot product 
	 * of vectors. This applied in both 2 dimentional and three dimentioanl 
	 * space.
	 * 
	 * In 2-D it becomes:
	 * sqrt(((yb-ya)*(xc-xa)+(xb-xa)*(yc-ya))^2/((xb-xa)^2 + (yb-ya)^2))
	 */
	public static double distanceFromPointToRaySquared(
			double lineX1, double lineY1, 
			double lineX2, double lineY2, 
			double pointX, double pointY) {
		double vx = lineX1 - pointX; 
		double vy = lineY1 - pointY;
		double ux = lineX2 - lineX1;
		double uy = lineY2 - lineY1;
		double length = ux * ux + uy * uy;

		double det = ux * vy - uy * vx;
		return (det * det) / length;
	}

	public static double distanceFromPointToLineSquared(
			double lineX1, double lineY1, 
			double lineX2, double lineY2, 
			double pointX, double pointY) {
		double vx = lineX1 - pointX; 
		double vy = lineY1 - pointY;
		double ux = lineX2 - lineX1;
		double uy = lineY2 - lineY1;
		double length = ux * ux + uy * uy;

		double det = (-vx * ux) + (-vy * uy); //if this is < 0 or > length then its outside the line segment
		if(det < 0 || det > length) {
			ux = lineX2 - pointX;
			uy = lineY2 - pointY;
			return Math.min(vx * vx + vy * vy, ux * ux + uy * uy);
		}
		det = ux * vy - uy * vx;
		return (det * det) / length;
    }
}
