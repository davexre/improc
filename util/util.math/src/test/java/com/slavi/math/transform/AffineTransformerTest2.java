package com.slavi.math.transform;

import java.util.ArrayList;

import org.apache.commons.math3.util.Pair;

import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;

public class AffineTransformerTest2 {

	RotationXYZ r = new RotationXYZ();
	Matrix initial;
	ArrayList<Pair<double[], double[]>> generatePoints() {
		initial = r.makeAngles(10 * MathUtil.deg2rad, 20 * MathUtil.deg2rad, 30 * MathUtil.deg2rad);
		ArrayList<Pair<double[], double[]>> points = new ArrayList<>();
		for (int ix = 1; ix < 4; ix++) {
			for (int iy = 0; iy < 4; iy++) {
				for (int iz = 0; iz < 4; iz++) {
					double src[] = new double[] { ix, iy, iz };
					double dest[] = new double[3];
					r.transformForward(initial, src, dest);
					points.add(new Pair<>(src, dest));
				}
			}
		}
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
		ArrayList<Pair<double[], double[]>> points = generatePoints();
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(9, 1);
		Matrix coefs = new Matrix(lsa.getNumPoints(), 1);
		Matrix m = r.makeAngles(0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad, 0 * MathUtil.deg2rad);

		double dest[] = new double[3];
		lsa.clear();
		for (int i = 0; i < points.size(); i++) {
			Pair<double[], double[]> p  = points.get(i);
			coefs.make0();
			r.transformForward(m, p.getKey(), dest);
			double dx = dest[0] - p.getValue()[0];
			double dy = dest[1] - p.getValue()[1];
			double dz = dest[2] - p.getValue()[2];

			coefs.make0();
			double L = 0;
//			if (i % 3 == 0)
			{
				L += dx * dx;
				coefs.setItem(0, 0, dx * p.getKey()[0]);
				coefs.setItem(1, 0, dx * p.getKey()[1]);
				coefs.setItem(2, 0, dx * p.getKey()[2]);
//				lsa.addMeasurement(coefs, 1, L, 0);
			}

			if (i % 3 != 2)
			{
				L += dy * dy;
				coefs.setItem(3, 0, dy * p.getKey()[0]);
				coefs.setItem(4, 0, dy * p.getKey()[1]);
				coefs.setItem(5, 0, dy * p.getKey()[2]);
//				lsa.addMeasurement(coefs, 1, L, 0);
			}
			if (i % 3 == 2)
			{
				L += dz * dz;
				coefs.setItem(6, 0, dz * p.getKey()[0]);
				coefs.setItem(7, 0, dz * p.getKey()[1]);
				coefs.setItem(8, 0, dz * p.getKey()[2]);
			}
			lsa.addMeasurement(coefs, 1, L, 0);

/*			coefs.make0();
			coefs.setItem(0, 0, dx * p.getKey()[0]);
			coefs.setItem(1, 0, dx * p.getKey()[1]);
			coefs.setItem(2, 0, dx * p.getKey()[2]);
			lsa.addMeasurement(coefs, 1, dx * dx, 0);

			coefs.make0();
			coefs.setItem(3, 0, dy * p.getKey()[0]);
			coefs.setItem(4, 0, dy * p.getKey()[1]);
			coefs.setItem(5, 0, dy * p.getKey()[2]);
			lsa.addMeasurement(coefs, 1, dy * dy, 0);

			coefs.make0();
			coefs.setItem(6, 0, dz * p.getKey()[0]);
			coefs.setItem(7, 0, dz * p.getKey()[1]);
			coefs.setItem(8, 0, dz * p.getKey()[2]);
			lsa.addMeasurement(coefs, 1, dz * dz, 0);*/

/*			coefs.make0();
			coefs.setItem(0, 0, p.getKey()[0]);
			coefs.setItem(1, 0, p.getKey()[1]);
			coefs.setItem(2, 0, p.getKey()[2]);
			lsa.addMeasurement(coefs, 1, dx, 0);

			coefs.make0();
			coefs.setItem(3, 0, p.getKey()[0]);
			coefs.setItem(4, 0, p.getKey()[1]);
			coefs.setItem(5, 0, p.getKey()[2]);
			lsa.addMeasurement(coefs, 1, dy, 0);

			coefs.make0();
			coefs.setItem(6, 0, p.getKey()[0]);
			coefs.setItem(7, 0, p.getKey()[1]);
			coefs.setItem(8, 0, p.getKey()[2]);
			lsa.addMeasurement(coefs, 1, dz, 0);*/
		}
		lsa.calculate();
		Matrix x = lsa.getUnknown();
		double xx[] = x.getVector();
		double jj[] = m.getVector();
		System.out.println("Unknowns " + arrayToString(xx));
		System.out.println("Before   " + arrayToString(jj));
		for (int i = 0; i < xx.length; i++)
			jj[i] -= xx[i];
		System.out.println("After    " + arrayToString(jj));
		System.out.println("Original " + arrayToString(initial.getVector()));
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
