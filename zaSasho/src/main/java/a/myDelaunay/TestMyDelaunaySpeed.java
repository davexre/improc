package a.myDelaunay;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

import com.slavi.util.Marker;

public class TestMyDelaunaySpeed {

	void doIt() throws Exception {
		ArrayList<Point2D.Double> generatedPoints = new ArrayList<Point2D.Double>();
		int maxPoints = 100000;
		
		int sizeX = Math.max(1, (int) Math.sqrt(maxPoints));
		int sizeY = (int) Math.ceil((double) maxPoints / sizeX);
		int curX = 0;
		int curY = 0;
		int step = 10;
		
		Random rnd = new Random();
		while (generatedPoints.size() < maxPoints) {
			if (curX >= sizeX) {
				curX = 0;
				curY++;
			}
			
/*			Point2D.Double p = new Point2D.Double(
				curX * step + rnd.nextDouble(),
				curY * step + rnd.nextDouble());
*/
			Point2D.Double p = new Point2D.Double(curX * step, curY * step);
//			Point2D.Double p = new Point2D.Double(rnd.nextDouble() * sizeX, rnd.nextDouble() * sizeY);
/*			for (Point2D.Double i : generatedPoints) {
				if (i.distance(p) < 0.00001) {
					p = null;
					break;
				}
			}*/
			if (p != null) {
				generatedPoints.add(p);
				curX++;
			}
		}

		MyDelaunay d = new MyDelaunay() {
			public int getPointId(Point2D p) {
				return points.indexOf(p);
			}
		};
		Marker.mark();
		if (false) {
			for (Point2D.Double p : generatedPoints) {
				d.insertPoint(p);
			}
		} else {
			while (!generatedPoints.isEmpty()) {
				int n = generatedPoints.size() - 1;
				if (n > 0)
					n = rnd.nextInt(n);
				Point2D.Double p = generatedPoints.remove(n);
				d.insertPoint(p);
			}
		}
//		d.checkAndFlip();
		Marker.release();
		d.dumpStatistics();
	}

	public static void main(String[] args) throws Exception {
		new TestMyDelaunaySpeed().doIt();
		System.out.println("Done.");
	}
}
