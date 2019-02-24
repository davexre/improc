package a.myDelaunay;

import java.awt.geom.Point2D;

import com.slavi.math.MathUtil;

public class CalcEps {

	void calcCircleEps() throws Exception {
		Point2D.Double center = new Point2D.Double(0, 0);
		Circle c = new Circle(center, 1.0);
		Point2D.Double p = new Point2D.Double(1.0, 0);

		double eps = 1.0;
		double one = 1.0;
		while (true) {
//			p.x = one - eps;
			c.r = one + eps;
			if (!c.isPointInside(p)) {
				break;
			}
			eps /= 2.0;
		}
		eps *= 2.0;
		System.out.println(eps);
		System.out.println("Mathustil.eps " + MathUtil.eps);
		System.out.println("Mathustil.epsAngle " + MathUtil.epsAngle);
	}

	public static void main(String[] args) throws Exception {
		new CalcEps().calcCircleEps();
		System.out.println("Done.");
	}
}
