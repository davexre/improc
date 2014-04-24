package a.myDelaunay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.jdom.Element;
import org.jdom.JDOMException;

import a.ajDelaunay;
import a.ajPoint;
import a.ajTriangle;

import com.slavi.util.Const;
import com.slavi.util.xml.XMLHelper;

public class TestOriginalDelaunay {

	static class TestData {
		public String name;
		public ArrayList<ajPoint> points = new ArrayList<ajPoint>();
		public Rectangle2D.Double extent = new Rectangle2D.Double();
	}
	
	private static ArrayList<TestData> readTests() throws JDOMException, IOException {
		InputStream fin = TestOriginalDelaunay.class.getResourceAsStream("testData.xml");
		ArrayList<TestData> r = new ArrayList<TestData>();
		Element tests = XMLHelper.readXML(fin);
		for (Object otest : tests.getChildren()) {
			Element eltest = (Element) otest;
			TestData testData = new TestData();
			testData.name = eltest.getChildText("name");
			for (Object opoint : eltest.getChild("points").getChildren()) {
				Element elpoint = (Element) opoint;
				double x = Double.parseDouble(elpoint.getAttributeValue("x"));
				double y = Double.parseDouble(elpoint.getAttributeValue("y"));
				ajPoint point = new ajPoint(x, y);
				testData.points.add(point);
				testData.extent.add(x, y);
			}
			r.add(testData);
		}
		return r;
	}
	
	static void drawTriangle(Graphics g, ajTriangle t) {
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

	static double distance(ajPoint a, ajPoint b) {
		return Math.hypot(a.getX() - b.getX(), a.getY() - b.getY());
	}

	static double inscribedCircle(ajPoint a, ajPoint b, ajPoint c, ajPoint center) {
		double ab = distance(a, b);
		double bc = distance(b, c);
		double ca = distance(c, a);
		double p = ab + bc + ca;
		if (p == 0) {
			center.setX(a.getX());
			center.setY(a.getY());
			return 0;
		}
		center.setX((a.getX() * bc + b.getX() * ca + c.getX() * ab) / p); 
		center.setY((a.getY() * bc + b.getY() * ca + c.getY() * ab) / p);
		p *= 0.5;
		return Math.sqrt((p - ab) * (p - bc) * (p - ca) / p);
	}

	static void drawTriangleCenter(Graphics g, ajTriangle t, String label) {
		if (label == null && "".equals(label))
			return;
		ajPoint p;
		if (t.c != null) {
			p = new ajPoint();
			inscribedCircle(t.a, t.b, t.c, p);
			g.setColor(Color.blue);
		} else {
			p = new ajPoint(
					(2 * t.a.getX() + t.b.getX()) / 3,
					(2 * t.a.getY() + t.b.getY()) / 3);
			g.setColor(Color.red);
		}
		g.drawString(label, (int) p.getX(), (int) p.getY());
	}

	public static ArrayList<ajTriangle> getTriangles(ajTriangle root) {
		ArrayList<ajTriangle> r = new ArrayList<ajTriangle>();
		recursiveAddTriangle(root, r);
		return r;
	}

	static void recursiveAddTriangle(ajTriangle t, ArrayList<ajTriangle> r) {
		if (t == null)
			return;
		if (r.contains(t))
			return;
		r.add(t);
		recursiveAddTriangle(t.getAb(), r);
		recursiveAddTriangle(t.getBc(), r);
		recursiveAddTriangle(t.getCa(), r);
	}
	
	public static String triangle2String(ajTriangle t, ArrayList<ajTriangle> triangles, ArrayList<ajPoint> points) {
		String isOk = ""; //MyDelaunay.isTriangleOk(t) ? "  " : "* ";
		String id = Integer.toString(triangles.indexOf(t));
		String a = Integer.toString(points.indexOf(t.a));
		String b = Integer.toString(points.indexOf(t.b));
		String c = t.c == null ? "null" : Integer.toString(points.indexOf(t.c));
		String ab = Integer.toString(triangles.indexOf(t.getAb()));
		String bc = c == null ? " null" : Integer.toString(triangles.indexOf(t.getBc()));
		String ca = c == null ? " null" : Integer.toString(triangles.indexOf(t.getCa()));
		return isOk +
				"id=" + id + 
				"\ta=" + a + 
				"\tb=" + b + 
				"\tc=" + c +
				"\tab=" + ab +
				"\tbc=" + bc +
				"\tca=" + ca;
	}

	public static File makeTestImage(TestData test, ajDelaunay d, String fouPart) throws Exception {
		// Calc image extent
		int border = 20;
		BufferedImage bo = new BufferedImage(
				(int) test.extent.width + border + border, 
				(int) test.extent.height + border + border, 
				BufferedImage.TYPE_INT_RGB);
		Graphics g = bo.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, bo.getWidth(), bo.getHeight());
		g.translate(
				(int) (border - test.extent.x), 
				(int) (border - test.extent.y));

		// draw
		ArrayList<ajTriangle> triangles = getTriangles(d.root);
		for (ajTriangle t : triangles) {
			drawTriangle(g, t);
		}
		for (int i = 0; i < triangles.size(); i++) {
			ajTriangle t = triangles.get(i);
			drawTriangleCenter(g, t, Integer.toString(i));
		}
		for (int i = 0; i < test.points.size(); i++) {
			ajPoint p = test.points.get(i);
			Utils.drawPoint(g, (int) p.x, (int) p.y, Color.black, Integer.toString(i));
		}
		
		File fou = new File(Const.workDir, "test " + test.name + " " + fouPart + ".png");
		ImageIO.write(bo, "png", fou);
		return fou;
	}

	static void testAndMakeImageOnError(TestData test, boolean makeImageForEachAddedPoint) throws Exception {
		ajDelaunay d = new ajDelaunay(null);
		
		for (int i = 0; i < test.points.size(); i++) {
			ajPoint p = test.points.get(i);
			d.insertPoint(p);
			if (makeImageForEachAddedPoint)
				makeTestImage(test, d, Integer.toString(i));
		}
	}
	
	public static void main(String[] args) throws Exception {
		ArrayList<TestData> tests = readTests();
		for (TestData test : tests) {
			System.out.println("Using data for test \"" + test.name + "\"");
			testAndMakeImageOnError(test, true);
		}
		System.out.println("Done.");
	}
}
