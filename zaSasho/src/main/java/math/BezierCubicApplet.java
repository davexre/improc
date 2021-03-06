package math;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import a.myDelaunay.BasePointsListApplet;

import com.slavi.math.BezierCubicCurve;

public class BezierCubicApplet extends BasePointsListApplet {

	BezierCubicCurve spline = new BezierCubicCurve(
		new Point2D.Double(75, 75),
		new Point2D.Double(125, 125),
		new Point2D.Double(225, 125),
		new Point2D.Double(325, 75));

	public BezierCubicApplet() {
		super();
		fixedNumberOfPoints = true;
		points.add(spline.p0);
		points.add(spline.p1);
		points.add(spline.p2);
		points.add(spline.p3);
	}

	static Path2D.Double toPath(List<? extends Point2D> points) {
		Path2D.Double path = new Path2D.Double();
		if (points == null || points.size() == 0)
			return path;
		int i = 0;
		Point2D p = points.get(i++);
		path.moveTo(p.getX(), p.getY());
		for (; i < points.size(); i++) {
			p = points.get(i);
			path.lineTo(p.getX(), p.getY());
		}
		return path;
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		g2.setColor(Color.white);
		g2.fillRect(0, 0, getWidth(), getWidth());
		g2.setColor(Color.lightGray);
		g2.drawLine((int)spline.p0.x, (int)spline.p0.y, (int)spline.p1.x, (int)spline.p1.y);
		g2.drawLine((int)spline.p1.x, (int)spline.p1.y, (int)spline.p2.x, (int)spline.p2.y);
		g2.drawLine((int)spline.p2.x, (int)spline.p2.y, (int)spline.p3.x, (int)spline.p3.y);
		g2.setColor(Color.red);

		Path2D.Double path = new Path2D.Double();
		spline.appendToPath(path);
		g2.draw(path);

		double minLineLength = 100;
		double maxBreadth = 0.1;

		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		spline.appendToPointsList(points, maxBreadth, minLineLength);
		path = toPath(points);
		g2.setColor(Color.green);
		g2.draw(path);

//		g2.setColor(Color.cyan);
//		g2.drawLine(0, 10, (int) minLineLength, 10);

		g2.setColor(Color.black);
		System.out.println();
		System.out.println(spline.p0);

		for (int i = 1; i < 10; i++) {
			Point2D.Double p = spline.getPointAt(i / 10.0);
			System.out.println(p);
			g2.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);
		}
		System.out.println(spline.p3);
		super.paint(g);
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.add(new BezierCubicApplet());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(400, 400);
		f.setVisible(true);
		System.out.println("Done.");
	}
}
