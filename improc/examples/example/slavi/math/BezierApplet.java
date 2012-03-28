package example.slavi.math;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.slavi.math.BezierCurve;

public class BezierApplet extends Applet {

	BezierCurve spline = new BezierCurve(
		new Point2D.Double(25, 25),
		new Point2D.Double(125, 125),
		new Point2D.Double(225, 125),
		new Point2D.Double(325, 25));

	static int controlNodeWidth = 3;
	
	static int controlNodeHeight = 3;

	Point2D.Double selected = null;

	void checkControlNode(Point2D.Double p, int i, int j) {
		if ((p.x - controlNodeWidth <= i) && 
			(i <= p.x + controlNodeWidth) && 
			(p.y - controlNodeHeight <= j) && 
			(j <= p.y + controlNodeHeight))
			selected = p;
	}

	public void init() {
		MouseAdapter listener = new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				if (selected != null) {
					selected.x = x;
					selected.y = y;
					repaint();
				}
			}

			public void mouseReleased(MouseEvent e) {
				selected = null;
				repaint();
			}
			
			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				selected = null;
				checkControlNode(spline.p0, x, y);
				checkControlNode(spline.p1, x, y);
				checkControlNode(spline.p2, x, y);
				checkControlNode(spline.p3, x, y);

				if (selected != null) {
					selected.x = x;
					selected.y = y;
					repaint();
				}
			}
		};

		addMouseListener(listener);
		addMouseMotionListener(listener);
	}
	
	void drawControlNode(Point2D.Double p, Graphics2D g) {
		g.setColor(Color.lightGray);
		g.fillRect((int) (p.x - controlNodeWidth), (int) (p.y - controlNodeHeight), 
				2 * controlNodeWidth, 2 * controlNodeHeight);
		g.setColor(Color.black);
		g.drawRect((int) (p.x - controlNodeWidth), (int) (p.y - controlNodeHeight), 
				2 * controlNodeWidth, 2 * controlNodeHeight);
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
		
		g2.setColor(Color.cyan);
		g2.drawLine(0, 10, (int) minLineLength, 10); 
		
		g2.setColor(Color.black);
		System.out.println();
		System.out.println(spline.p0);
		
		for (int i = 1; i < 10; i++) {
			Point2D.Double p = spline.getPointAt(i / 10.0);
			System.out.println(p);
			g2.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);
		}
		System.out.println(spline.p3);
//		for (Point2D.Double point : points)
//			g2.fillRect((int) point.x - 1, (int) point.y - 1, 2, 2);
		drawControlNode(spline.p0, g2);
		drawControlNode(spline.p1, g2);
		drawControlNode(spline.p2, g2);
		drawControlNode(spline.p3, g2);
	}
}
