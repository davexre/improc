package a.myDelaunay;

import java.awt.geom.Point2D;

public class Circle {
	public Point2D center;
	public double r;
	
	public Circle() {
		center = new Point2D.Double();
	}

	public Circle(Point2D center, double r) {
		this.center = center;
		this.r = r;
	}
	
	public static final double eps = 1E-8;
	
	public boolean isPointInsideEps(Point2D p) {
		double x = p.getX() - center.getX();
		double y = p.getY() - center.getY();
		return x*x + y*y + eps < r*r;
	}

	public boolean isPointInside(Point2D p) {
		double x = p.getX() - center.getX();
		double y = p.getY() - center.getY();
		return x*x + y*y < r*r;
	}
}
