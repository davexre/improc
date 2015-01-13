package a.myDelaunay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import com.slavi.math.ColorSetPick;
import com.slavi.util.Const;
import com.slavi.util.xml.XMLHelper;

public class TestMyDelaunay {

	public static class TestData {
		public String name;
		public ArrayList<Point2D> points = new ArrayList<Point2D>();
		public Rectangle2D extent = new Rectangle2D.Double();
	}
	
	public static ArrayList<TestData> readTests() throws JDOMException, IOException {
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
				Point2D point = new Point2D.Double(
						Double.parseDouble(elpoint.getAttributeValue("x")),
						Double.parseDouble(elpoint.getAttributeValue("y")));
				testData.points.add(point);
				if (isFirst) {
					testData.extent.setRect(point.getX(), point.getY(), 0, 0);
					isFirst = false;
				} else {
					testData.extent.add(point);
				}
			}
			r.add(testData);
		}
		return r;
	}

	public static File makeTestImage(TestData test, MyDelaunay d, String fouPart) throws Exception {
		// Calc image extent
		int border = 120;
		ColorSetPick colorPick = new ColorSetPick();
		Rectangle2D extent = new Rectangle2D.Double(
				test.extent.getX() - border, 
				test.extent.getY() - border, 
				test.extent.getWidth() + border + border, 
				test.extent.getHeight() + border + border);
		BufferedImage bo = new BufferedImage(
				(int) extent.getWidth(), 
				(int) extent.getHeight(), 
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) bo.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, bo.getWidth(), bo.getHeight());
		g.translate(
				(int) (border - test.extent.getX()), 
				(int) (border - test.extent.getY()));

		// draw
		ArrayList<Triangle> triangles = new ArrayList<Triangle>(d.getTriangles());
		for (Triangle t : triangles) {
			Utils.drawTriangle(g, t);
		}
		for (int i = 0; i < triangles.size(); i++) {
			Triangle t = triangles.get(i);
			Utils.drawTriangleCenter(g, t, Integer.toString(i));
		}
		for (int i = 0; i < test.points.size(); i++) {
			Point2D p = test.points.get(i);
			Utils.drawPoint(g, (int) p.getX(), (int) p.getY(), Color.black, Integer.toString(i));
		}
		
		ArrayList<Path2D> voronoi = MyVoronoi.computeVoroni(d, extent);
		for (Path2D path : voronoi) {
			g.setColor(colorPick.getNextColor(80));
			g.fill(path);
			g.setColor(Color.blue);
			g.draw(path);
		}
		
		File fou = new File(Const.workDir, "test " + test.name + " " + fouPart + ".png");
		ImageIO.write(bo, "png", fou);
		return fou;
	}
	
	public static void dumpPoints(TestData test) {
		for (int i = 0; i < test.points.size(); i++) {
			Point2D p = test.points.get(i);
			System.out.println("points.add(new Point2D.Double(" + p.getX() + ", " + p.getY() + "));");
		}
	}
	
	static void testAndMakeImageOnError(TestData test, boolean makeImageForEachAddedPoint) throws Exception {
		MyDelaunay d = new MyDelaunay() {
			public int getPointId(Point2D p) {
				return points.indexOf(p);
			}
		};

		for (int i = 0; i < test.points.size(); i++) {
			Point2D p = test.points.get(i);
			try {
				d.insertPoint(p);
			} catch (Throwable t) {
				dumpPoints(test);
				throw t;
			}
			if (!d.isTopologyOk()) {
				System.out.println("Error in test " + test.name + " at point " + i);
				System.out.println("Created image " + makeTestImage(test, d, Integer.toString(i)).getAbsolutePath());
				d.dumpTriangles("-----");
				throw new Error("Error in test " + test.name + " at point " + i);
			}
//			if (makeImageForEachAddedPoint)
//				makeTestImage(test, d, Integer.toString(i));
		}
		d.dumpStatistics();
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
