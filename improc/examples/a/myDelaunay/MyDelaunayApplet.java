package a.myDelaunay;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.ArrayList;

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
	
	void dumpTriangle(MyDelaunay d, Triangle t, ArrayList<Triangle> triangles) {
		String id = Integer.toString(triangles.indexOf(t));
		String a = Integer.toString(points.indexOf(t.a));
		String b = Integer.toString(points.indexOf(t.b));
		String c = t.c == null ? "null" : Integer.toString(points.indexOf(t.c));
		String ab = Integer.toString(d.triangles.indexOf(t.getAb()));
		String bc = c == null ? " null" : Integer.toString(triangles.indexOf(t.getBc()));
		String ca = c == null ? " null" : Integer.toString(triangles.indexOf(t.getCa()));
		System.out.println(
				"id=" + id + 
				"\ta=" + a + 
				"\tb=" + b + 
				"\tc=" + c +
				"\tab=" + ab +
				"\tbc=" + bc +
				"\tca=" + ca);
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

	void drawTriangleCenter(Graphics g, Triangle t, ArrayList<Triangle> triangles) {
		Point2D.Double p;
		if (t.c != null) {
			p = t.getInscribedCircle().center;
			g.setColor(Color.blue);
		} else {
			p = new Point2D.Double(
					(t.a.getX() + t.b.getX()) * 0.5,
					(t.a.getY() + t.b.getY()) * 0.5);
			g.setColor(Color.red);
		}
		String text = Integer.toString(triangles.indexOf(t));
		g.drawChars(text.toCharArray(), 0, text.length(),
			(int) p.getX(),
			(int) p.getY());
	}
	
	public void paint(Graphics g) {
		MyDelaunay d = new MyDelaunay() {
			public int getPointId(Point2D p) {
				return points.indexOf(p);
			}
		};
		for (int i = 0; i < points.size(); i++) {
			Point2D.Double p = points.get(i);
			d.insertPoint(p);
		}
		ArrayList<Triangle> triangles = new ArrayList<Triangle>(d.getTriangles());
/*
		for (int i = 0; i < points.size(); i++) {
			Point2D.Double p = points.get(i);
			System.out.println(p);
		}
		System.out.println("Points:    " + points.size());
		System.out.println("Triangles: " + d.triangles.size());

		for (Triangle t : triangles) {
			dumpTriangle(d, t, triangles);
		}
*/
		for (Triangle t : triangles) {
			drawTriangle(g, t);
		}
		
		g.setColor(Color.blue);
		for (Triangle t : triangles) {
			drawTriangleCenter(g, t, triangles);
		}
		paintPoints(g);
	}
}
