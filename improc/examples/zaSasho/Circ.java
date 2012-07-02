package zaSasho;
import java.awt.geom.Point2D;

import com.slavi.math.GeometryUtil;


public class Circ {

	public static void main(String[] args) {
		Point2D a = new Point2D.Double(-5, 0);
		Point2D b = new Point2D.Double(5, 0);
		Point2D c = new Point2D.Double(0, 5);
		Point2D p = new Point2D.Double();
		double r = GeometryUtil.circleTreePoints(a, b, c, p);
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(p);
		System.out.println(r);
	}
	
	public static void main2(String[] args) {
		Point2D p1 = new Point2D.Double();
		Point2D p2 = new Point2D.Double();
		int result;
		result = GeometryUtil.intersectTwoCircles(
				0, 0, 10, 
				0, 10, 5, p1, p2);
		System.out.println(result);
		System.out.println(p1);
		System.out.println(p2);
	}
}
