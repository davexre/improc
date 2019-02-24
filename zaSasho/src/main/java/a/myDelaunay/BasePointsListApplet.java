package a.myDelaunay;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JFrame;

public class BasePointsListApplet extends Canvas {

	protected boolean fixedNumberOfPoints = false;

	protected boolean drawPointIndex = false;

	protected ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();

	protected int selectedIndex = -1;

	void fixPoint(Point2D.Double p) {
		if (p.x < 0)
			p.x = 0;
		if (p.y < 0)
			p.y = 0;
		if (p.x >= getWidth())
			p.x = getWidth();
		if (p.y >= getHeight())
			p.y = getHeight();
	}

	public BasePointsListApplet() {
		setName(getClass().getName());
		MouseAdapter listener = new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				if (selectedIndex >= 0) {
					Point2D.Double p = points.get(selectedIndex);
					p.x = x;
					p.y = y;
					fixPoint(p);
					repaint();
				}
			}

			public void mouseReleased(MouseEvent e) {
				selectedIndex = -1;
				repaint();
			}

			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();

				int oldSelectedIndex = selectedIndex;
				selectedIndex = -1;
				for (int i = 0; i < points.size(); i++) {
					Point2D.Double p = points.get(i);
					if ((p.x - Utils.controlNodeWidth <= x) &&
						(x <= p.x + Utils.controlNodeWidth) &&
						(p.y - Utils.controlNodeHeight <= y) &&
						(y <= p.y + Utils.controlNodeHeight)) {
						selectedIndex = i;
						break;
					}
				}

				if (selectedIndex >= 0) {
					if ((!fixedNumberOfPoints) &&
						((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)) {
						// delete point
						points.remove(selectedIndex);
						selectedIndex = -1;
					} else {
						Point2D.Double p = points.get(selectedIndex);
						p.x = x;
						p.y = y;
						fixPoint(p);
					}
					repaint();
				} else if (!fixedNumberOfPoints) {
					Point2D.Double p = new Point2D.Double(x, y);
					points.add(p);
					selectedIndex = points.size() - 1;
					repaint();
				} else if (selectedIndex != oldSelectedIndex) {
					repaint();
				}
			}
		};

		addMouseListener(listener);
		addMouseMotionListener(listener);
	}

	public void paint(Graphics g) {
		for (int i = 0; i < points.size(); i++) {
			Point2D.Double p = points.get(i);
			Utils.drawPoint(g, (int) p.x, (int) p.y,
				i == selectedIndex ? Color.yellow : Color.black,
				drawPointIndex ? Integer.toString(i) : null);
		}
	}
}
