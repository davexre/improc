package a.myDelaunay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.math.GeometryUtil;
import com.slavi.util.Const;
import com.slavi.util.xml.XMLHelper;

public class TestMyDelaunay {

	static class TestData {
		public String name;
		public ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		public Rectangle2D.Double extent = new Rectangle2D.Double();
	}
	
	private static ArrayList<TestData> readTests() throws JDOMException, IOException {
		InputStream fin = TestMyDelaunay.class.getResourceAsStream("testData.xml");
		ArrayList<TestData> r = new ArrayList<TestData>();
		Element tests = XMLHelper.readXML(fin);
		for (Object otest : tests.getChildren()) {
			Element eltest = (Element) otest;
			TestData testData = new TestData();
			testData.name = eltest.getChildText("name");
			boolean isFirst = true;
			for (Object opoint : eltest.getChild("points").getChildren()) {
				Element elpoint = (Element) opoint;
				Point2D.Double point = new Point2D.Double(
						Double.parseDouble(elpoint.getAttributeValue("x")),
						Double.parseDouble(elpoint.getAttributeValue("y")));
				testData.points.add(point);
				if (isFirst) {
					testData.extent.setRect(point.x, point.y, 0, 0);
					isFirst = false;
				} else {
					testData.extent.add(point);
				}
			}
			r.add(testData);
		}
		return r;
	}

	static final Point2D.Double p1 = new Point2D.Double();
	static final Point2D.Double p2 = new Point2D.Double();
	
	static void drawTriangle(AffineTransform tr, Graphics g, Triangle t) {
		g.setColor(Color.black);
		tr.transform(t.a, p1);
		tr.transform(t.b, p2);
		g.drawLine(
			(int) p1.getX(),
			(int) p1.getY(),
			(int) p2.getX(),
			(int) p2.getY());
		if (t.c != null) {
			tr.transform(t.a, p1);
			tr.transform(t.c, p2);
			g.drawLine(
				(int) p1.getX(),
				(int) p1.getY(),
				(int) p2.getX(),
				(int) p2.getY());
			tr.transform(t.b, p1);
			tr.transform(t.c, p2);
			g.drawLine(
				(int) p1.getX(),
				(int) p1.getY(),
				(int) p2.getX(),
				(int) p2.getY());
		}
	}

	static void drawTriangleCenter(AffineTransform tr, Graphics g, Triangle t, ArrayList<Triangle> triangles) {
		Point2D.Double p;
		if (t.c != null) {
			p = t.getInscribedCircle().center;
			g.setColor(Color.blue);
		} else {
			p =	GeometryUtil.midPoint(t.a, t.b);
			g.setColor(Color.red);
		}
		String text = Integer.toString(triangles.indexOf(t));
		tr.transform(p, p1);
		g.drawChars(text.toCharArray(), 0, text.length(),
			(int) p1.getX(),
			(int) p1.getY());
	}

	protected static int controlNodeWidth = 3;
	
	protected static int controlNodeHeight = 3;

	static void drawPoint(AffineTransform tr, Graphics g, Point2D.Double p, String label) {
		g.setColor(Color.lightGray);
		tr.transform(p, p1);
		g.fillRect((int) (p1.x - controlNodeWidth), (int) (p1.y - controlNodeHeight), 
				2 * controlNodeWidth, 2 * controlNodeHeight);
		g.setColor(Color.black);
		g.drawRect((int) (p1.x - controlNodeWidth), (int) (p1.y - controlNodeHeight), 
				2 * controlNodeWidth, 2 * controlNodeHeight);
		
		g.drawString(label,
			(int) (p1.getX() + controlNodeWidth + controlNodeWidth),
			(int) (p1.getY() - controlNodeHeight));
	}
	
	
	static void makeTestImage(TestData test, MyDelaunay d, String fouPart) throws Exception {
		double w = test.extent.width * 0.1;
		double h = test.extent.height * 0.1;
		Rectangle2D.Double ext = new Rectangle2D.Double(
				test.extent.getX() - w,
				test.extent.getY() - h,
				test.extent.getWidth() + w + w,
				test.extent.getHeight() + h + h);
		AffineTransform tr = new AffineTransform();
		tr.scale(1, -1);
		tr.translate(-ext.getX(), -ext.getY()-ext.getHeight());
		
		BufferedImage bo = new BufferedImage((int) ext.getWidth(), (int) ext.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) bo.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, bo.getWidth(), bo.getHeight());
//		g.translate((int) -ext.getX(), (int) -ext.getY());
		
		ArrayList<Triangle> triangles = d.getTriangles();
		for (Triangle t : triangles) {
			drawTriangle(tr, g, t);
		}
		
		g.setColor(Color.blue);
		for (Triangle t : triangles) {
			drawTriangleCenter(tr, g, t, triangles);
		}
		for (int i = 0; i < test.points.size(); i++) {
			Point2D.Double p = test.points.get(i);
			drawPoint(tr, g, p, Integer.toString(i));
		}
		
		ImageIO.write(bo, "png", new File(Const.workDir, "test " + test.name + " " + fouPart + ".png"));
	}
	
	static void test1(TestData test) throws Exception {
		MyDelaunay d = new MyDelaunay() {
			public int getPointId(Point2D p) {
				return points.indexOf(p);
			}
		};

		for (int i = 0; i < test.points.size(); i++) {
			Point2D.Double p = test.points.get(i);
			System.out.println(">>> Point " + i);
			d.insertPoint(p);
			System.out.println("Inserted point " + i);
			d.dumpIfBadTrianglesExist();
			makeTestImage(test, d, Integer.toString(i));
		}

		makeTestImage(test, d, "final");
	}
	
	public static void main(String[] args) throws Exception {
		ArrayList<TestData> tests = readTests();
		for (TestData test : tests) {
			System.out.println("Using data for test \"" + test.name + "\"");
			test1(test);
		}
		System.out.println("Done.");
	}
}
