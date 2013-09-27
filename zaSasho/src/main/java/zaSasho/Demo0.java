package zaSasho;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;


public class Demo0 {

	public static final int colors[] = {
//		0x00ff0000,
		0x0000ff00,
		0x000000ff,
		0x0000ffff,
		0x00ff00ff,
		0x00ffff00,
		0x00ff9600
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
		circle(100,  50, 100),
		circle(200, 100, 300),
	};
	
	void drawIt() throws Exception {
		Rectangle2D bounds = new Rectangle2D.Double();
		for (Ellipse2D e : el) {
			Rectangle2D ext = e.getBounds2D();
			Rectangle2D.union(ext, bounds, bounds);
		}
		Area area = new Area(bounds);
		Stroke stroke = new BasicStroke((float) 20, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
		Stroke s1 = new BasicStroke((float) 2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
		Stroke s2 = new BasicStroke((float) 5, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
		bounds = stroke.createStrokedShape(area).getBounds2D();
		
		BufferedImage bi = new BufferedImage(
				(int) bounds.getWidth() + 1, 
				(int) bounds.getHeight() + 1,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();

		g.setColor(Color.black);
		g.fillRect(0, 0, bi.getWidth() - 1, bi.getHeight() - 1);
		g.setColor(Color.green);
		g.setStroke(s1);
		g.drawRect(0, 0, bi.getWidth() - 1, bi.getHeight() - 1);

		Arrays.sort(el, new Comparator<Ellipse2D>() {
			public int compare(Ellipse2D o1, Ellipse2D o2) {
				return new Double(o2.getWidth()).compareTo(o1.getWidth());
			}
		});

		Area all = new Area();
		for (int i = 0; i < el.length; i++) {
			Ellipse2D e = el[i];
			
			Area ea = new Area(e);
			ea.subtract(all);
			all.add(new Area(e));

			g.setColor(Color.red);
			g.setStroke(s1);
			g.draw(ea);
			
			ea.subtract(new Area(stroke.createStrokedShape(ea)));
			g.setColor(new Color(getNextColor()));
			g.setStroke(s2);
			g.fill(ea);
		}
		
		File fou = new File("output.png");
		System.out.println("Output file is: " + fou.getAbsolutePath());
		ImageIO.write(bi, "png", fou);
	}
	
	
	public static void main(String[] args) throws Exception {
		new Demo0().drawIt();
	}
}
