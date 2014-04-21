package a.myDelaunay;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JButton;

public class MyDelaunayApplet extends BasePointsListApplet {

	public void dumpPoints() {
		System.out.println("=== Dump points ===");
		for (int i = 0; i < points.size(); i++) {
			Point2D p = points.get(i);
			System.out.println("points.add(new Point2D.Double(" + p.getX() + ", " + p.getY() + "));");
		}
		System.out.println("======");
	}
	
	public void init() {
		JButton btn = new JButton("4code");
		this.add(btn);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dumpPoints();
			}
		});
		
		btn = new JButton("XML");
		this.add(btn);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("=== Dump points ===");
				for (int i = 0; i < points.size(); i++) {
					Point2D p = points.get(i);
					System.out.println("\t\t\t<point x=\"" + p.getX() + "\" y=\"" + p.getY() + "\"/>");
				}
				System.out.println("======");
			}
		});
		drawPointIndex = true;
		super.init();
		
		points.add(new Point2D.Double(38.0, 79.0));
		points.add(new Point2D.Double(84.0, 138.0));
		points.add(new Point2D.Double(90.0, 21.0));
		points.add(new Point2D.Double(135.0, 79.0));
		points.add(new Point2D.Double(84.0, 40.0));
	}
	
	void drawTriangle(Graphics g, Triangle t) {
		g.setColor(Color.black);
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
		try {
			for (int i = 0; i < points.size(); i++) {
				Point2D.Double p = points.get(i);
				d.insertPoint(p);
			}
		} catch (Throwable t) {
			dumpPoints();
			t.printStackTrace();
		}
		ArrayList<Triangle> triangles = new ArrayList<Triangle>(d.getTriangles());
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
