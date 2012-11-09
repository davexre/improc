package a.myDelaunay;

import java.awt.Color;
import java.awt.Graphics;
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

import com.slavi.io.xml.XMLHelper;
import com.slavi.util.Const;

import example.slavi.BasePointsListApplet;

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
			for (Object opoint : eltest.getChild("points").getChildren()) {
				Element elpoint = (Element) opoint;
				Point2D.Double point = new Point2D.Double(
						Double.parseDouble(elpoint.getAttributeValue("x")),
						Double.parseDouble(elpoint.getAttributeValue("y")));
				testData.points.add(point);
				testData.extent.add(point);
			}
			r.add(testData);
		}
		return r;
	}

	static void drawTriangle(Graphics g, Triangle t) {
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

	static void drawTriangleCenter(Graphics g, Triangle t, ArrayList<Triangle> triangles) {
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

	protected static int controlNodeWidth = 3;
	
	protected static int controlNodeHeight = 3;

	static void drawPoint(Graphics g, Point2D.Double p, String label) {
		g.setColor(Color.lightGray);
		g.fillRect((int) (p.x - controlNodeWidth), (int) (p.y - controlNodeHeight), 
				2 * controlNodeWidth, 2 * controlNodeHeight);
		g.setColor(Color.black);
		g.drawRect((int) (p.x - controlNodeWidth), (int) (p.y - controlNodeHeight), 
				2 * controlNodeWidth, 2 * controlNodeHeight);
		
		g.drawString(label,
			(int) (p.getX() + controlNodeWidth + controlNodeWidth),
			(int) (p.getY() - controlNodeHeight));
	}
	
	static void test1(TestData test) throws IOException {
		MyDelaunay d = new MyDelaunay() {
			public int getPointId(Point2D p) {
				return points.indexOf(p);
			}
		};

		for (int i = 0; i < test.points.size(); i++) {
			Point2D.Double p = test.points.get(i);
			d.insertPoint(p);
		}

		double w = test.extent.width * 0.1;
		double h = test.extent.height * 0.1;
		Rectangle2D.Double ext = new Rectangle2D.Double(
				test.extent.getX() - w,
				test.extent.getY() - h,
				test.extent.getWidth() + w + w,
				test.extent.getHeight() + h + h);
		
		BufferedImage bo = new BufferedImage((int) ext.getWidth(), (int) ext.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = bo.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, bo.getWidth(), bo.getHeight());
		g.translate((int) -ext.getX(), (int) -ext.getY()); 
		
		ArrayList<Triangle> triangles = d.getTriangles();
		for (Triangle t : triangles) {
			System.out.println(d.triangle2String(t));
			drawTriangle(g, t);
		}
		
		g.setColor(Color.blue);
		for (Triangle t : triangles) {
			drawTriangleCenter(g, t, triangles);
		}
		for (int i = 0; i < test.points.size(); i++) {
			Point2D.Double p = test.points.get(i);
			drawPoint(g, p, Integer.toString(i));
		}
		
		ImageIO.write(bo, "png", new File(Const.workDir, "test " + test.name + ".png"));
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
