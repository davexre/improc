package a.myDelaunay;

import java.awt.geom.Point2D;

public class Circle {
	public Point2D.Double center;
	public double r;
	
	public Circle() {
		center = new Point2D.Double();
	}

	public Circle(Point2D.Double center, double r) {
		this.center = center;
		this.r = r;
	}
	
	public boolean contains(Point2D p) {
		double x = p.getX() - center.getX();
		double y = p.getY() - center.getY();
		return x*x + y*y <= r*r;
	}
}
