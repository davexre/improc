package a.myDelaunay;
import java.awt.Graphics;
import java.awt.geom.Point2D;

import example.slavi.BasePointsListApplet;

public class MyDelaunayApplet extends BasePointsListApplet {

	public void init() {
		drawPointIndex = true;
		super.init();
//		points.add(new Point2D.Double(38.0, 79.0));
//		points.add(new Point2D.Double(84.0, 138.0));
//		points.add(new Point2D.Double(92.0, 79.0));
//		points.add(new Point2D.Double(135.0, 79.0));
	}
	
	void dumpTriangle(MyDelaunay d, Triangle t) {
		String id = Integer.toString(d.triangles.indexOf(t));
		String a = Integer.toString(points.indexOf(t.a));
		String b = Integer.toString(points.indexOf(t.b));
		String c = t.c == null ? "null" : Integer.toString(points.indexOf(t.c));
		System.out.println("id=" + id + ":" + a + ":" + b + ":" + c);
	}
	
	void drawTriangle(Graphics g, Triangle t) {
		g.drawLine(
			(int) t.a.getX(),
			(int) t.a.getY(),
			(int) t.b.getX(),
			(int) t.b.getY());
		if (t.c != null) {
			g.drawLine(
				(int) t.a.getX(),
				(int) t.a.getY(),
				(int) t.c.getX(),
				(int) t.c.getY());
			g.drawLine(
				(int) t.b.getX(),
				(int) t.b.getY(),
				(int) t.c.getX(),
				(int) t.c.getY());
		}
	}
	
	public void paint(Graphics g) {
		MyDelaunay d = new MyDelaunay();
		for (Point2D.Double p : points) {
			System.out.println(p);
			d.insertPoint(p);
		}
		System.out.println("Points:    " + points.size());
		System.out.println("Triangles: " + d.triangles.size());
		
//		for (Triangle t : d.triangles) {
		for (Triangle t : d.getTriangles()) {
			dumpTriangle(d, t);
			drawTriangle(g, t);
		}
		System.out.println();
		
		paintPoints(g);
	}
}
