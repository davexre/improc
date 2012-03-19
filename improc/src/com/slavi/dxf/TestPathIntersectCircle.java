package com.slavi.dxf;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import com.slavi.math.GeometryUtil;

/*
t=[0..1]

B(n,m) = mth coefficient of nth degree Bernstein polynomial
B(n,m) = C(n,m) * t^(m) * (1 - t)^(n-m)

C(n,m) = Combinations of n things, taken m at a time
C(n,m) = n! / (m! * (n-m)!)

C(2,0) = 2! / (0! * 2!) = 1
C(2,1) = 2! / (1! * 1!) = 1
C(2,2) = 2! / (2! * 0!) = 1

C(3,0) = 3! / (0! * 3!) = 1
C(3,1) = 3! / (1! * 2!) = 3
C(3,2) = 3! / (2! * 1!) = 3
C(3,3) = 3! / (3! * 0!) = 1

B(2,0) = (1-t)^2
B(2,1) = t * (1-t)
B(2,2) = t^2

B(3,0) = (1-t)^3
B(3,1) = 3 * t * (1-t)^2
B(3,2) = 3 * t^2 * (1-t)
B(3,3) = t^3

SEG_QUADTO
P(t) = B(2,0)*CP + B(2,1)*P1 + B(2,2)*P2
P(t) = CP*(1-t)^2 + P1*t*(1-t) + P2*t^2

SEG_CUBICTO
P(t) = B(3,0)*CP + B(3,1)*P1 + B(3,2)*P2 + B(3,3)*P3
P(t) = CP*(1-t)^3 + P1*3*t*(1-t)^2 + P2*3*t^2*(1-t) + P3*t^3

 */

public class TestPathIntersectCircle {
	public static void main(String[] args) {
		Path2D path = new Path2D.Double();
		path.moveTo(0, 0);
		path.lineTo(10, 10);
	
		Ellipse2D circ = new Ellipse2D.Double(0, 0, 5, 5);
		System.out.println(GeometryUtil.pathIteratorToString(circ.getPathIterator(null)));
		
	}
}
