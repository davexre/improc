package a.myDelaunay;

import java.awt.geom.Point2D;

import com.slavi.math.GeometryUtil;

public class Triangle {
	public Point2D.Double a;
	public Point2D.Double b;
	public Point2D.Double c;	// c == null -> Half plane
	
	private Triangle ab;
	private Triangle bc;		// bc == null -> Half plane
	private Triangle ca;		// ca == null -> Half plane
	
	public Triangle getAb() {
		return ab;
	}

	public void setAb(Triangle t) {
		if (t == this)
			throw new Error();
		this.ab = t;
	}

	public Triangle getBc() {
		return bc;
	}

	public void setBc(Triangle t) {
		if (t == this)
			throw new Error();
		this.bc = t;
	}

	public Triangle getCa() {
		return ca;
	}

	public void setCa(Triangle t) {
		if (t == this)
			throw new Error();
		this.ca = t;
	}

	public Triangle() {}

	public Triangle(Point2D.Double a, Point2D.Double b) {
		this.a = a;
		this.b = b;
	}
	
	public Triangle(Point2D.Double a, Point2D.Double b, Point2D.Double c) {
		this.a = a;
		switch (GeometryUtil.pointToLine(a, b, c)) {
		case GeometryUtil.PointToLinePosition.Inside:
		case GeometryUtil.PointToLinePosition.NegativePlane:
		case GeometryUtil.PointToLinePosition.BeforeTheStartPoint:
		case GeometryUtil.PointToLinePosition.AfterTheEndPoint:
			this.b = b;
			this.c = c;
			break;
		default:
			this.b = c;
			this.c = b;
			break;
		}
	}

	public void switchneighbors(Triangle oldTr, Triangle newTr) {
		if (getAb() == oldTr) {
			setAb(newTr);
			return;
		}
		if (getBc() == oldTr) {
			setBc(newTr);
			return;
		}
		if (getCa() == oldTr) {
			setCa(newTr);
			return;
		}
	}
	
	private Circle circumCircle;
	
	public Circle getCircumCircle() {
		circumCircle = new Circle();
		circumCircle.r = GeometryUtil.circleTreePoints(a, b, c, circumCircle.center);
		return circumCircle;
	}
}
