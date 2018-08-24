package com.slavi.math.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.linear.MatrixUtils;

import com.slavi.math.MathUtil;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;

public class AffineTransformerTest2 {

	static ArrayList<TransformerDataTestImpl> generatePoints() {
		AffineTransform jTransform = new AffineTransform();
		jTransform.setToIdentity();
		jTransform.rotate(30 * MathUtil.deg2rad);
		jTransform.scale(2, 1);
//		jTransform.scale(123.456, 789.123);
		jTransform.shear(1.234, 2.345);
		jTransform.translate(100.567, 200.123);
//		jTransform.translate(10, 1);

		ArrayList<TransformerDataTestImpl> points = new ArrayList<TransformerDataTestImpl>();
		for (int xcounter = 1; xcounter < 4; xcounter++) {
			for (int ycounter = 0; ycounter < 4; ycounter++) {
				Point2D.Double sd = new Point2D.Double(xcounter, ycounter);
				TransformerDataTestImpl pair = new TransformerDataTestImpl(jTransform, sd);
				points.add(pair);
			}
		}
		jTransform.getMatrix(m);
		System.out.println(Arrays.toString(m));
		return points;
	}
	static double m[] = new double[6];

	/*

	x,y - input
	tx,ty - target output
	A(x,y) = c1*x + c3 * y + c5
	B(x,y) = c2*x + c4 * y + c6
	E(x,y,tx,ty,c1..c6) = 0.5 * ( (A(x,y) - tx)^2 + (B(x,y) - ty)^2 )
	dE/dA = (A(x,y) - tx)
	dE/dB = (B(x,y) - ty)
	dA/dc1 = x, dA/dc2 = 0, dA/dc3 = y, dA/dc4 = 0, dA/dc5 = 1, dA/dc6 = 0
	dB/dc1 = 0, dB/dc2 = x, dB/dc3 = 0, dB/dc4 = y, dB/dc5 = 0, dB/dc6 = 1
	dE/dc1 = ...

	 */

	public void doIt(String[] args) throws Exception {
		ArrayList<TransformerDataTestImpl> points = generatePoints();
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(6, 1);
		Matrix coefs = new Matrix(6, 1);
		AffineTransform jTransform = new AffineTransform();
		jTransform.setToIdentity();

		Point2D.Double dest = new Point2D.Double();
		lsa.clear();
		for (int i = 0; i < points.size(); i++) {
			TransformerDataTestImpl p  = points.get(i);
			coefs.make0();
			jTransform.transform(p.getKey(), dest);
			double dx = dest.getX() - p.getValue().getX();
			double dy = dest.getY() - p.getValue().getY();
/*			double L = (dx*dx + dy*dy);
			coefs.setItem(0, 0, dx * p.getKey().getX());
			coefs.setItem(1, 0, dy * p.getKey().getX());
			coefs.setItem(2, 0, dx * p.getKey().getY());
			coefs.setItem(3, 0, dy * p.getKey().getY());
			coefs.setItem(4, 0, dx);
			coefs.setItem(5, 0, dy);
			lsa.addMeasurement(coefs, 1, L, 0);*/

/*			coefs.make0();
			coefs.setItem(0, 0, dx * p.getKey().getX());
			coefs.setItem(2, 0, dx * p.getKey().getY());
			coefs.setItem(4, 0, dx);
			lsa.addMeasurement(coefs, 1, dx * 0.5, 0);

			coefs.make0();
			coefs.setItem(1, 0, dy * p.getKey().getX());
			coefs.setItem(3, 0, dy * p.getKey().getY());
			coefs.setItem(5, 0, dy);
			lsa.addMeasurement(coefs, 1, dy * 0.5, 0);*/

			coefs.make0();
			coefs.setItem(0, 0, dx * p.getKey().getX());
			coefs.setItem(2, 0, dx * p.getKey().getY());
			coefs.setItem(4, 0, dx);

			coefs.setItem(1, 0, dy * p.getKey().getX());
			coefs.setItem(3, 0, dy * p.getKey().getY());
			coefs.setItem(5, 0, dy);
			lsa.addMeasurement(coefs, 1, dx * dy, 0);

/*			double dx1 = dx;
			double dy1 = dy;
			coefs.make0();
			coefs.setItem(0, 0, dx1 * p.getKey().getX());
			coefs.setItem(2, 0, dx1 * p.getKey().getY());
			coefs.setItem(4, 0, dx1 * 1);
			lsa.addMeasurement(coefs, 1, dx1 * dx, 0);

			coefs.make0();
			coefs.setItem(1, 0, dy1 * p.getKey().getX());
			coefs.setItem(3, 0, dy1 * p.getKey().getY());
			coefs.setItem(5, 0, dy1 * 1);
			lsa.addMeasurement(coefs, 1, dy1 * dy, 0);*/

/*			coefs.make0();
			coefs.setItem(0, 0, p.getKey().getX());
			coefs.setItem(2, 0, p.getKey().getY());
			coefs.setItem(4, 0, 1);
			lsa.addMeasurement(coefs, 1, dx, 0);

			coefs.make0();
			coefs.setItem(1, 0, p.getKey().getX());
			coefs.setItem(3, 0, p.getKey().getY());
			coefs.setItem(5, 0, 1);
			lsa.addMeasurement(coefs, 1, dy, 0);*/
		}
		lsa.calculate();
		Matrix x = lsa.getUnknown();
		double xx[] = x.getVector();
		double jj[] = new double[6];
		jTransform.getMatrix(jj);
		System.out.println("Unknowns " + arrayToString(xx));
		System.out.println("Before   " + arrayToString(jj));
		for (int i = 0; i < xx.length; i++)
			jj[i] -= xx[i];
		System.out.println("After    " + arrayToString(jj));
		System.out.println("Original " + arrayToString(m));
		//jTransform = new AffineTransform(x.getVector());
	}

	public static String arrayToString(double d[]) {
		StringBuilder r = new StringBuilder();
		for (int i = 0; i < d.length; i++)
			r.append(MathUtil.d4(d[i])).append(' ');
		return r.toString();
	}

	public static void main(String[] args) throws Exception {
		new AffineTransformerTest2().doIt(args);
		System.out.println("Done.");
	}
}
