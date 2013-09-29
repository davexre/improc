
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.slavi.math.GeometryUtil;


public class Demo {

	public static final int colors[] = {
		0x00ff9600,
		0x008f8f00,
		0x008f004f,
		0x000000ff,
		0x00005f00,
		0x00006f6f,
	};

	protected int nextColor = 0;

	public synchronized int getNextColor() {
		int result = colors[nextColor++];
		if (nextColor >= colors.length)
			nextColor = 0;
		return result;
	}

	public static Ellipse2D circle(double x, double y, double radius) {
		return new Ellipse2D.Double(x - radius, y - radius, 2*radius, 2*radius);
	}
	
	Ellipse2D el[] = {
//		circle(100,  80, 250),  // Проблемно ?!?
		circle(100, 100, 200),
		circle(100, 200, 150),
		circle(100, 100, 50),
		circle(200, 100, 300),
	};
	
	static double dist(double x1, double y1, double x2, double y2) {
		x1 -= x2;
		y1 -= y2;
		return Math.sqrt(x1*x1 + y1*y1);
	}
	
	static void drawCross(int x, int y, Graphics2D g) {
		int len = 10;
		g.drawLine(x - len, y, x + len, y);
		g.drawLine(x, y - len, x, y + len);
	}
	
	void drawIt() throws Exception {
		// Calc shape extent
		Rectangle2D bounds = new Rectangle2D.Double();
		for (Ellipse2D e : el) {
			Rectangle2D ext = e.getBounds2D();
			Rectangle2D.union(ext, bounds, bounds);
		}
		
		// Expand the extent 'a bit'
		Area extent = new Area(bounds);
		Stroke stroke = new BasicStroke((float) 15, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
		bounds = stroke.createStrokedShape(extent).getBounds2D();
		
		// Create the output image buffer
		BufferedImage bi = new BufferedImage(
				(int) bounds.getWidth() + 1, 
				(int) bounds.getHeight() + 1,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();

		// Draw a border in the output image
		g.setColor(Color.black);
		g.fillRect(0, 0, bi.getWidth() - 1, bi.getHeight() - 1);
		g.setColor(Color.green);
		g.setStroke(stroke);
		g.drawRect(0, 0, bi.getWidth() - 1, bi.getHeight() - 1);
		
		// Set the origin (translation) of output image
		g.translate(-bounds.getX(), -bounds.getY());

		ArrayList<Area> areas = new ArrayList<Area>();
		Point2D.Double center = new Point2D.Double();
		Point2D.Double p = new Point2D.Double();
		Point2D.Double p1 = new Point2D.Double();
		Point2D.Double p2 = new Point2D.Double();

		for (int i = 0; i < el.length; i++) {
			Ellipse2D ei = el[i];
			Area ai = new Area(ei);

			for (int j = 0; j < el.length; j++) {
				if (i == j)
					continue;
				Ellipse2D ej = el[j];
				
				double dist = dist(
						ei.getCenterX(), 
						ei.getCenterY(), 
						ej.getCenterX(), 
						ej.getCenterY());
				double ri = ei.getWidth() / 2.0;
				double rj = ej.getWidth() / 2.0;
				double sumR = ri + rj;
				if (dist >= sumR) {
					System.out.println("no intersect " + i + ":" + j);
					continue;
				}
				if (dist < 0.0001)
					dist = 0.0001;

				int intersect = GeometryUtil.intersectTwoCircles(
						ei.getCenterX(), ei.getCenterY(), ei.getWidth() / 2.0,
						ej.getCenterX(), ej.getCenterY(), ej.getWidth() / 2.0,
						p1, p2);
				if ((intersect == 0) || (intersect == 1)) {
					if (ri < rj) {
						System.out.println("overlap1 " + i + ":" + j);
						ai.intersect(new Area(circle(ei.getCenterX(), ei.getCenterY(), ri)));
					} else {
						System.out.println("overlap2 " + i + ":" + j);
						ai.subtract(new Area(circle(ej.getCenterX(), ej.getCenterY(), rj)));
					}
					continue;
				}
				
				double angle = Math.atan2(
						ej.getCenterY() - ei.getCenterY(),
						ej.getCenterX() - ei.getCenterX());
				p.x = ei.getCenterX() + ri * Math.cos(angle) * dist / sumR;
				p.y = ei.getCenterY() + ri * Math.sin(angle) * dist / sumR;

				if (intersect == 2) {
//					g.setColor(Color.green);
//					g.setStroke(new BasicStroke(15));
//					drawCross((int) p1.x, (int) p1.y, g);
//					drawCross((int) p2.x, (int) p2.y, g);
//					drawCross((int) p.x, (int) p.y, g);
					double r = GeometryUtil.circleTreePoints(p1, p2, p, center);
					if (r > 0) {
//						g.draw(circle(center.x, center.y, r));
						
						Area ar = new Area(circle(center.x, center.y, r));
						double di = dist(ei.getCenterX(), ei.getCenterY(), center.x, center.y);
						double dj = dist(ej.getCenterX(), ej.getCenterY(), center.x, center.y);
						
						if ((di < dj) || ((di == dj) && (ri < rj))) {
							System.out.println("intersect1 " + i + ":" + j);
							Area aj = new Area(ej);
							aj.subtract(ar);
							ai.subtract(aj);
						} else {
							System.out.println("intersect2 " + i + ":" + j);
							ai.subtract(ar);
						}
						continue;
					} else {
						System.out.println("r <= 0  " + i + ":" + j);
					}
				} else {
					System.out.println("rect " + i + ":" + j);
				}
				
				// intersect == 1 || r <= 0
				AffineTransform tr = new AffineTransform();
				tr.translate(p.x, p.y);
				tr.rotate(angle + Math.PI);
				Area rect = new Area(new Rectangle2D.Double(0, -sumR, sumR * 2, sumR * 2));
				rect.transform(tr);
				Area aj = new Area(ej);
				aj.subtract(rect);
				ai.subtract(aj);
			}
				
			areas.add(ai);
		}

		g.setStroke(new BasicStroke((float) 5, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
		for (Area a : areas) {
//			a = new Area(a);
			a.subtract(new Area(stroke.createStrokedShape(a)));
			g.setColor(new Color(getNextColor()));
//			g.fill(a);
			g.draw(a);
		}

		g.setStroke(new BasicStroke((float) 5, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
		g.setColor(Color.yellow);
		for (int i = 0; i < el.length; i++) {
			Ellipse2D ei = el[i];
			g.draw(ei);
			drawCross((int) ei.getCenterX(), (int) ei.getCenterY(), g);
		}
		
		g.setStroke(new BasicStroke((float) 1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
		g.setColor(Color.red);
		for (Area a : areas) {
			g.draw(a);
		}
		
		File fou = new File("output3.png");
		System.out.println("Output file is: " + fou.getAbsolutePath());
		ImageIO.write(bi, "png", fou);
	}

	public static void main(String[] args) throws Exception {
		new Demo().drawIt();
	}
}
