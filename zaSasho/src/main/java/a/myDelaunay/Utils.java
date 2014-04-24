package a.myDelaunay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

public class Utils {
	public static int controlNodeWidth = 3;
	
	public static int controlNodeHeight = 3;

	public static void drawPoint(Graphics g, int x, int y, Color c, String label) {
		g.setColor(Color.lightGray);
		g.fillRect(
				x - controlNodeWidth, 
				y - controlNodeHeight, 
				2 * controlNodeWidth, 
				2 * controlNodeHeight);
		g.setColor(c);
		g.drawRect(
				x - controlNodeWidth, 
				y - controlNodeHeight, 
				2 * controlNodeWidth, 
				2 * controlNodeHeight);
		
		if (label != null && !"".equals(label)) {
			g.drawString(label,
				x + controlNodeWidth + controlNodeWidth,
				y - controlNodeHeight);
		}
		
	}
	
	public static void drawTriangle(Graphics g, Triangle t) {
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

	public static void drawTriangleCenter(Graphics g, Triangle t, String label) {
		if (label == null && "".equals(label))
			return;
		Point2D.Double p;
		if (t.c != null) {
			p = t.getInscribedCircle().center;
			g.setColor(Color.blue);
		} else {
			p = new Point2D.Double(
					(2 * t.a.getX() + t.b.getX()) / 3,
					(2 * t.a.getY() + t.b.getY()) / 3);
			g.setColor(Color.red);
		}
		g.drawString(label, (int) p.getX(), (int) p.getY());
	}
}
