package a.myDelaunay;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import example.slavi.BasePointsListApplet;

public class MyDelaunayApplet extends BasePointsListApplet {

	public void init() {
		drawPointIndex = true;
		super.init();
/*		points.add(new Point2D.Double(38.0, 79.0));
		points.add(new Point2D.Double(84.0, 138.0));
		points.add(new Point2D.Double(92.0, 79.0));
		points.add(new Point2D.Double(135.0, 79.0));*/
		
		// Error in flip
		points.add(new Point2D.Double(38.0, 79.0));
		points.add(new Point2D.Double(84.0, 138.0));
		points.add(new Point2D.Double(90.0, 21.0));
		points.add(new Point2D.Double(135.0, 79.0));
		points.add(new Point2D.Double(115.0, 108.0));
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
	
	String[] triangleToIds(ArrayList<Triangle> triangles) {
		int pointIds[] = new int[3];
		Set<String> tr = new HashSet<String>();
		for (Triangle t : triangles) {
			pointIds[0] = points.indexOf(t.a);
			pointIds[1] = points.indexOf(t.b);
			pointIds[2] = points.indexOf(t.c);
			Arrays.sort(pointIds);
			tr.add(
				pointIds[0] + ":" + 
				pointIds[1] + ":" + 
				pointIds[2]);
		}
		String r[] = tr.toArray(new String[tr.size()]);
		Arrays.sort(r);
		return r;
	}

	public static void main(String[] args) throws Exception {
		MyDelaunayApplet applet = new MyDelaunayApplet();
		applet.init();
		applet.dummy();
	}
	
	void dummy() throws IOException {
		MyDelaunay d = new MyDelaunay() {
			public int getPointId(Point2D p) {
				return points.indexOf(p);
			}
		};

		for (int i = 0; i < points.size(); i++) {
			Point2D.Double p = points.get(i);
			d.insertPoint(p);

			BufferedImage bo = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
			Graphics g = bo.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, bo.getWidth(), bo.getHeight());
			
			ArrayList<Triangle> triangles = new ArrayList<Triangle>(d.getTriangles());
			for (Triangle t : triangles) {
				drawTriangle(g, t);
			}
			
			g.setColor(Color.blue);
			for (Triangle t : triangles) {
				drawTriangleCenter(g, t, triangles);
			}
			paintPoints(g);
			
			ImageIO.write(bo, "png", new File("out" + i + ".png"));
		}
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
			System.out.println("------------");
			System.out.println(d);
		}
		ArrayList<Triangle> triangles = new ArrayList<Triangle>(d.getTriangles());
/*		for (Triangle t : triangles) {
			if (!d.triangles.contains(t))
				System.out.println("NOT FOUND 1 " + d.triangle2String(t));
		}
		for (Triangle t : d.triangles) {
			if (!triangles.contains(t))
				System.out.println("NOT FOUND 2 " + d.triangle2String(t));
		}*/
		
/*		String[] a = triangleToIds(triangles);
		String[] b = triangleToIds(d.triangles);
		System.out.println(a.length);
		System.out.println(b.length);
		System.out.println(Arrays.toString(a));
		System.out.println(Arrays.toString(b));
		System.out.println("----------------");*/

		System.out.println(d);
/*		for (int i = 0; i < points.size(); i++) {
			Point2D.Double p = points.get(i);
			System.out.println(p);
		}
		System.out.println("Points:    " + points.size());
		System.out.println("Triangles: " + d.triangles.size());
/*
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
