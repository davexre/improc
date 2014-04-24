package a.myDelaunay;
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
		// draw
		ArrayList<Triangle> triangles = new ArrayList<Triangle>(d.getTriangles());
		for (Triangle t : triangles) {
			Utils.drawTriangle(g, t);
		}
		for (int i = 0; i < triangles.size(); i++) {
			Triangle t = triangles.get(i);
			Utils.drawTriangleCenter(g, t, Integer.toString(i));
		}
		super.paint(g);
	}
}
