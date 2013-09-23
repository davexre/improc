package com.test.math;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import org.jdom.Element;

import com.slavi.io.ObjectToXML;
import com.slavi.io.xml.XMLHelper;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.SymmetricMatrix;
import com.slavi.util.Const;

public class Printer3DAdjust2 {

	String dataFile = Const.workDir + "/" + this.getClass().getName() + ".xml";
	
	public static class Measurement {
		double p[] = new double[3];	// Printer coordinates
		double hx1, hy1;				// Measuerd on millimeter paper on the printer's bed (the light pen)
		double hx2, hy2;				// Measuerd on millimeter paper on the printer's bed (the light pen)
		
		public String toString() {
			return String.format("P(%10.2f, %10.2f, %10.2f) H1(%10.2f, %10.2f) H2(%10.2f, %10.2f)", p[0], p[1], p[2], hx1, hy1, hx2, hy2);
		}
	}
	
	public static class Data {
		double rotationAngles[] = new double[3];
		double alpha1;			// lightPenAngleZX
		double beta1;			// lightPenAngleZY
		double alpha2;			// lightPenAngleZX
		double beta2;			// lightPenAngleZY
		ArrayList<Measurement> measurements = new ArrayList<Measurement>();
	}

	Data data;
	
	Matrix M;
	double tgAlpha1;
	double tgBeta1;
	double tgAlpha2;
	double tgBeta2;
	
	void precompute(Data data) {
		M = RotationXYZ.instance.makeAngles(data.rotationAngles);
		tgAlpha1 = Math.tan(data.alpha1);
		tgBeta1 = Math.tan(data.beta1);
		tgAlpha2 = Math.tan(data.alpha2);
		tgBeta2 = Math.tan(data.beta2);
	}
	
	Data generateData() {
		Data result = new Data();
		
		Random rnd = new Random();
		for (int i = 0; i < result.rotationAngles.length; i++) {
			result.rotationAngles[i] = 0.01; // rnd.nextDouble();
		}
		result.alpha1 = 0.01; // rnd.nextDouble();
		result.beta1 = 0.03; //rnd.nextDouble();
		result.alpha2 = 0.2; // rnd.nextDouble();
		result.beta2 = 0.1; //rnd.nextDouble();
		
		precompute(result);
		
		double tmp[] = new double[3];
		for (int i = 0; i < 13; i++) {
			Measurement d = new Measurement();
			d.p[0] = rnd.nextDouble(); // rnd.nextInt(3000);
			d.p[1] = rnd.nextDouble(); // rnd.nextInt(3000);
			d.p[2] = rnd.nextDouble(); // rnd.nextInt(3000);
			
			RotationXYZ.instance.transformForward(M, d.p, tmp);
			d.hx1 = tmp[0] + tmp[2] * tgAlpha1; // + rnd.nextDouble() - 0.5;
			d.hy1 = tmp[1] + tmp[2] * tgBeta1;  // + rnd.nextDouble() - 0.5;
			d.hx2 = tmp[0] + tmp[2] * tgAlpha2; // + rnd.nextDouble() - 0.5;
			d.hy2 = tmp[1] + tmp[2] * tgBeta2;  // + rnd.nextDouble() - 0.5;
			result.measurements.add(d);
		}
		return result;
	}
	
	void toXML(Data d) throws Exception {
		Element data = new Element("data");
		ObjectToXML.Write toxml = new ObjectToXML.Write(data);
		toxml.objectToXML(data, d);
		XMLHelper.writeXML(new File(dataFile), data, null);
	}
	
	Data fromXML() throws Exception {
		Element data = XMLHelper.readXML(new File(dataFile));
		ObjectToXML.Read fromxml = new ObjectToXML.Read(data);
		Data result = (Data) fromxml.xmlToObject(data);
		precompute(result);
		return result;
	}
	
	Matrix adjM;
	double adjAlpha1;
	double adjBeta1;
	double adjAlpha2;
	double adjBeta2;
	
	void adjust() {
		double tgAlpha1 = Math.tan(adjAlpha1);
		double tgBeta1 = Math.tan(adjBeta1);
		double tgAlpha2 = Math.tan(adjAlpha1);
		double tgBeta2 = Math.tan(adjBeta1);

		double dFdAlpha1 = Math.cos(adjAlpha1);
		dFdAlpha1 = 1.0 / (dFdAlpha1 * dFdAlpha1);
		double dFdBeta1 = Math.cos(adjBeta1);
		dFdBeta1 = 1.0 / (dFdBeta1 * dFdBeta1);

		double dFdAlpha2 = Math.cos(adjAlpha2);
		dFdAlpha2 = 1.0 / (dFdAlpha2 * dFdAlpha2);
		double dFdBeta2 = Math.cos(adjBeta2);
		dFdBeta2 = 1.0 / (dFdBeta2 * dFdBeta2);
		
		double tmp[] = new double[3];
		double L;
		Matrix m = new Matrix(13, 1);
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(13);
		lsa.clear();
		for (Measurement d : data.measurements) {
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
			m.setItem(9, 0, tmp[2] * dFdAlpha1);
			m.setItem(10, 0, 0);
			m.setItem(11, 0, 0);
			m.setItem(12, 0, 0);
			L = tmp[0] + tmp[2] * tgAlpha1 - d.hx1;
			lsa.addMeasurement(m, 1.0, -L, 0);
			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);

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
			m.setItem(9, 0, 0);
			m.setItem(10, 0, tmp[2] * dFdBeta1);
			m.setItem(11, 0, 0);
			m.setItem(12, 0, 0);
			L = tmp[1] + tmp[2] * tgBeta1 - d.hy1;
			lsa.addMeasurement(m, 1.0, -L, 0);
			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);

			///////////////////////////////////

			m.setItem(0, 0, d.p[0]);
			m.setItem(1, 0, d.p[1]);
			m.setItem(2, 0, d.p[2]);
			m.setItem(3, 0, 0);
			m.setItem(4, 0, 0);
			m.setItem(5, 0, 0);
			m.setItem(6, 0, d.p[0] * tgAlpha2);
			m.setItem(7, 0, d.p[1] * tgAlpha2);
			m.setItem(8, 0, d.p[2] * tgAlpha2);
			m.setItem(9, 0, 0);
			m.setItem(10, 0, 0);
			m.setItem(11, 0, tmp[2] * dFdAlpha2);
			m.setItem(12, 0, 0);
			L = tmp[0] + tmp[2] * tgAlpha2 - d.hx2;
			lsa.addMeasurement(m, 1.0, -L, 0);
			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);

			///////////////////////////////////
			
			m.setItem(0, 0, 0);
			m.setItem(1, 0, 0);
			m.setItem(2, 0, 0);
			m.setItem(3, 0, d.p[0]);
			m.setItem(4, 0, d.p[1]);
			m.setItem(5, 0, d.p[2]);
			m.setItem(6, 0, d.p[0] * tgBeta2);
			m.setItem(7, 0, d.p[1] * tgBeta2);
			m.setItem(8, 0, d.p[2] * tgBeta2);
			m.setItem(9, 0, 0);
			m.setItem(10, 0, 0);
			m.setItem(11, 0, 0);
			m.setItem(12, 0, tmp[2] * dFdBeta2);
			L = tmp[1] + tmp[2] * tgBeta2 - d.hy2;
			lsa.addMeasurement(m, 1.0, -L, 0);
			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);
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

		adjAlpha1 += U.getItem(0, 9);
		adjBeta1 += U.getItem(0, 10);
		adjAlpha2 += U.getItem(0, 11);
		adjBeta2 += U.getItem(0, 12);
		
		U.printM("U");
		M.printM("M");
		adjM.printM("M1");
//		System.out.println("Squared Deviation from E: " + dest.getSquaredDeviationFromE());
	}
	
	private void doIt() throws Exception {
		try {
			data = fromXML();
		} catch (Exception e) {
			data = generateData();
			toXML(data);
		}
		
		adjM = new Matrix(3, 3);
		adjM.makeE();
//		M.copyTo(adjM);
		
		adjAlpha1 = data.alpha1;
		adjBeta1 = data.beta1;
		adjAlpha2 = data.alpha2;
		adjBeta2 = data.beta2;
		
		adjust();
		adjust();
		adjust();
	}
	
	public static void main(String[] args) throws Exception {
		new Printer3DAdjust2().doIt();
		System.out.println("Done.");
	}
}
