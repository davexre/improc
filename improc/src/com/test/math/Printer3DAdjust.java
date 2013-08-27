package com.test.math;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

import org.jdom.Element;

import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.SymmetricMatrix;

public class Printer3DAdjust {

	double rotationAngles[] = new double[3];
	double alpha;			// lightPenAngleZX
	double beta;			// lightPenAngleZY
	
	static class Data {
		double p[] = new double[3];	// Printer coordinates
		double hx, hy;				// Measuerd on millimeter paper on the printer's bed (the light pen)
		
		public String toString() {
			return String.format("P(%10.2f, %10.2f, %10.2f) H(%10.2f, %10.2f)", p[0], p[1], p[2], hx, hy);
		}
	}
	
	ArrayList<Data> data;
	
	Matrix M;
	double tgAlpha;
	double tgBeta;
	
	void generateData() {
		Random rnd = new Random();
		for (int i = 0; i < rotationAngles.length; i++) {
			rotationAngles[i] = 0.01; // rnd.nextDouble();
		}
		alpha = 0.01; // rnd.nextDouble();
		beta = 0.01; //rnd.nextDouble();
		
		M = RotationXYZ.instance.makeAngles(rotationAngles);
		tgAlpha = Math.tan(alpha);
		tgBeta = Math.tan(beta);
		
		double tmp[] = new double[3];
		data = new ArrayList<Printer3DAdjust.Data>();
		for (int i = 0; i < 13; i++) {
			Data d = new Data();
			d.p[0] = rnd.nextDouble(); // rnd.nextInt(3000);
			d.p[1] = rnd.nextDouble(); // rnd.nextInt(3000);
			d.p[2] = rnd.nextDouble(); // rnd.nextInt(3000);
			
			RotationXYZ.instance.transformForward(M, d.p, tmp);
			d.hx = tmp[0] + tmp[2] * tgAlpha; // + rnd.nextDouble() - 0.5;
			d.hy = tmp[1] + tmp[2] * tgBeta;  // + rnd.nextDouble() - 0.5;
			data.add(d);
			//System.out.println(d);
		}
	}
	
	void toXML(Element dest) {
		
	}
	
	Matrix adjM;
	double adjAlpha;
	double adjBeta;
	
	void adjust() {
		double tgAlpha1 = Math.tan(adjAlpha);
		double tgBeta1 = Math.tan(adjBeta);
		double dFdAlpha1 = Math.cos(adjAlpha);
		dFdAlpha1 = 1.0 / (dFdAlpha1 * dFdAlpha1);
		double dFdBeta1 = Math.cos(adjBeta);
		dFdBeta1 = 1.0 / (dFdBeta1 * dFdBeta1);

		double tmp[] = new double[3];
		double L;
		Matrix m = new Matrix(9, 1);
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(9);
		lsa.clear();
		for (Data d : data) {
			RotationXYZ.instance.transformForward(adjM, d.p, tmp);
			
			m.setItem(0, 0, d.p[0]);
			m.setItem(1, 0, d.p[1]);
			m.setItem(2, 0, d.p[2]);
			m.setItem(3, 0, 0);
			m.setItem(4, 0, 0);
			m.setItem(5, 0, 0);
			m.setItem(6, 0, d.p[0] * tgAlpha1);
			m.setItem(7, 0, d.p[1] * tgAlpha1);
			m.setItem(8, 0, d.p[2] * tgAlpha1);
//			m.setItem(9, 0, tmp[2] * dFdAlpha1);
//			m.setItem(10, 0, 0);
			L = tmp[0] + tmp[2] * tgAlpha1 - d.hx;
			lsa.addMeasurement(m, 1.0, -L, 0);
			System.out.print(L + "\t" + m);

			///////////////////////////////////
			
			m.setItem(0, 0, 0);
			m.setItem(1, 0, 0);
			m.setItem(2, 0, 0);
			m.setItem(3, 0, d.p[0]);
			m.setItem(4, 0, d.p[1]);
			m.setItem(5, 0, d.p[2]);
			m.setItem(6, 0, d.p[0] * tgBeta1);
			m.setItem(7, 0, d.p[1] * tgBeta1);
			m.setItem(8, 0, d.p[2] * tgBeta1);
//			m.setItem(9, 0, 0);
//			m.setItem(10, 0, tmp[2] * dFdBeta1);
			L = tmp[1] + tmp[2] * tgAlpha1 - d.hy;
			lsa.addMeasurement(m, 1.0, -L, 0);
			System.out.print(L + "\t" + m);
		}
		if (!lsa.calculateWithDebug(true)) {
			throw new RuntimeException("Calculation failed");
		}
/*
		lsa.getApl().printM("APL");
		SymmetricMatrix NM = lsa.getNm().makeCopy();
		lsa.getNm().printM("NM");
		if (!lsa.calculate()) {
			throw new RuntimeException("Calculation failed");
		}
		lsa.getNm().printM("NM inverse");
		SymmetricMatrix dest = NM.makeCopy();
		lsa.getNm().mMul(NM, dest);
		dest.printM("NM * NM inverse");
*/
		Matrix U = lsa.getUnknown();
		
		adjM.setItem(0, 0, adjM.getItem(0, 0) + U.getItem(0, 0));
		adjM.setItem(1, 0, adjM.getItem(1, 0) + U.getItem(0, 1));
		adjM.setItem(2, 0, adjM.getItem(2, 0) + U.getItem(0, 2));
		adjM.setItem(0, 1, adjM.getItem(0, 1) + U.getItem(0, 3));
		adjM.setItem(1, 1, adjM.getItem(1, 1) + U.getItem(0, 4));
		adjM.setItem(2, 1, adjM.getItem(2, 1) + U.getItem(0, 5));
		adjM.setItem(0, 2, adjM.getItem(0, 2) + U.getItem(0, 6));
		adjM.setItem(1, 2, adjM.getItem(1, 2) + U.getItem(0, 7));
		adjM.setItem(2, 2, adjM.getItem(2, 2) + U.getItem(0, 8));

//		adjAlpha += U.getItem(0, 9);
//		adjBeta += U.getItem(0, 10);
		
		U.printM("U");
		M.printM("M");
		adjM.printM("M1");
//		System.out.println("Squared Deviation from E: " + dest.getSquaredDeviationFromE());
	}
	
	private void doIt() {
		generateData();
		
		adjM = new Matrix(3, 3);
		adjM.makeE();
//		M.copyTo(adjM);
		
		adjAlpha = alpha;
		adjBeta = beta;
		
		adjust();
		adjust();
		adjust();
	}
	
	public static void main(String[] args) {
		new Printer3DAdjust().doIt();
		System.out.println("Done.");
	}
}
