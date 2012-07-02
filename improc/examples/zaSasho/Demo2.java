package zaSasho;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;

import com.slavi.math.GeometryUtil;


public class Demo2 {

	public static final int colors[] = {
//		0x00ff0000,
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
		return new Ellipse2D.Double(x, y, radius, radius);
	}
	
	Ellipse2D el[] = {
		circle(100, 100, 200),
		circle(100, 200, 150),
		circle(100,  50, 250),
		circle(200, 100, 300),
	};
	
	static double dist(double x1, double y1, double x2, double y2) {
		x1 -= x2;
		y1 -= y2;
		return Math.sqrt(x1*x1 + y1*y1);
	}
	
	void drawIt() throws Exception {
		Rectangle2D bounds = new Rectangle2D.Double();
		for (Ellipse2D e : el) {
			Rectangle2D ext = e.getBounds2D();
			Rectangle2D.union(ext, bounds, bounds);
		}
		Area area = new Area(bounds);
		Stroke stroke = new BasicStroke((float) 15, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
		bounds = stroke.createStrokedShape(area).getBounds2D();
		
		BufferedImage bi = new BufferedImage(
				(int) bounds.getWidth() + 1, 
				(int) bounds.getHeight() + 1,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();

		g.setColor(Color.black);
		g.fillRect(0, 0, bi.getWidth() - 1, bi.getHeight() - 1);
		g.setColor(Color.green);
		g.setStroke(stroke);
		g.drawRect(0, 0, bi.getWidth() - 1, bi.getHeight() - 1);

		ArrayList<Area> areas = new ArrayList<Area>();
		
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
				if (dist < 0.0001)
					dist = 0.0001;
				
				if (dist < sumR) {
					if (ri > dist) {
						if (rj < dist) {
							System.out.println("11 " + i + ":" + j);
							ri *= dist / sumR;
							// rj *= dist / sumR;
							Area aj = new Area(ej);
							aj.subtract(new Area(circle(ei.getCenterX(), ei.getCenterY(), ri)));
							ai.subtract(aj);
						} else {
							System.out.println("12 " + i + ":" + j);
//							ri *= dist / sumR;
							rj *= dist / sumR;

							double angle = Math.atan2(
									ei.getCenterY() - ej.getCenterY(),
									ei.getCenterX() - ej.getCenterX());
							AffineTransform tr = new AffineTransform();
							tr.rotate(angle);
							tr.translate(
									ej.getCenterX() + rj * Math.cos(angle),
									ej.getCenterY() + rj * Math.sin(angle));
							Area a = new Area(new Rectangle2D.Double(0, -sumR, sumR * 2, sumR * 2));
							a.transform(tr);
							ai.subtract(a);
						}
					} else {
						if (rj <= dist) {
							System.out.println("22 " + i + ":" + j);
							ri *= dist / sumR;
//							rj *= dist / sumR;

							double angle = Math.atan2(
									ej.getCenterY() - ei.getCenterY(),
									ej.getCenterX() - ei.getCenterX());
							AffineTransform tr = new AffineTransform();
							tr.rotate(angle);
							tr.translate(
									ei.getCenterX() + ri * Math.cos(angle),
									ei.getCenterY() + ri * Math.sin(angle));
							Area a = new Area(new Rectangle2D.Double(0, -sumR, sumR * 2, sumR * 2));
							a.transform(tr);
							ai.subtract(a);
						} else {
							System.out.println("21 " + i + ":" + j);
							ri *= dist / sumR;
							// rj *= dist / sumR;
							Area aj = new Area(ej);
							aj.subtract(new Area(circle(ei.getCenterX(), ei.getCenterY(), ri)));
							ai.subtract(aj);
						}
					}
				} else {
					System.out.println("00 " + i + ":" + j);
				}
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
		}
		
		g.setStroke(new BasicStroke((float) 1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
		g.setColor(Color.red);
		for (Area a : areas) {
			g.draw(a);
		}
		
		File fou = new File("output2.png");
		System.out.println("Output file is: " + fou.getAbsolutePath());
		ImageIO.write(bi, "png", fou);
	}
	
	
	public static void main(String[] args) throws Exception {
		new Demo2().drawIt();
	}
}
