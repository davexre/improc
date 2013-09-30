package com.test.math;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import org.jdom.Element;

import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Const;
import com.slavi.util.io.ObjectToXML;
import com.slavi.util.xml.XMLHelper;

public class Printer3DAdjust {

	String dataFile = Const.workDir + "/" + this.getClass().getName() + ".xml";
	
	public static class Measurement {
		double p[] = new double[3];	// Printer coordinates
		double hx, hy;				// Measuerd on millimeter paper on the printer's bed (the light pen)
		
		public String toString() {
			return String.format("P(%10.2f, %10.2f, %10.2f) H(%10.2f, %10.2f)", p[0], p[1], p[2], hx, hy);
		}
	}
	
	public static class Data {
		double rotationAngles[] = new double[3];
		double alpha;			// lightPenAngleZX
		double beta;			// lightPenAngleZY
		ArrayList<Measurement> measurements = new ArrayList<Printer3DAdjust.Measurement>();
	}

	Data data;
	
	Matrix M;
	double tgAlpha;
	double tgBeta;
	
	void precompute(Data data) {
		M = RotationXYZ.instance.makeAngles(data.rotationAngles);
		tgAlpha = Math.tan(data.alpha);
		tgBeta = Math.tan(data.beta);
	}
	
	Data generateData() {
		Data result = new Data();
		
		Random rnd = new Random();
		for (int i = 0; i < result.rotationAngles.length; i++) {
			result.rotationAngles[i] = 0.01; // rnd.nextDouble();
		}
		result.alpha = 0.01; // rnd.nextDouble();
		result.beta = 0.03; //rnd.nextDouble();
		
		precompute(result);
		
		double tmp[] = new double[3];
		for (int i = 0; i < 13; i++) {
			Measurement d = new Measurement();
			d.p[0] = rnd.nextDouble(); // rnd.nextInt(3000);
			d.p[1] = rnd.nextDouble(); // rnd.nextInt(3000);
			d.p[2] = rnd.nextDouble(); // rnd.nextInt(3000);
			
			RotationXYZ.instance.transformForward(M, d.p, tmp);
			d.hx = tmp[0] + tmp[2] * tgAlpha; // + rnd.nextDouble() - 0.5;
			d.hy = tmp[1] + tmp[2] * tgBeta;  // + rnd.nextDouble() - 0.5;
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
		result.beta = 0.3; //TODO:
		precompute(result);
		return result;
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
		Matrix m = new Matrix(11, 1);
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(11);
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
			L = tmp[0] + tmp[2] * tgAlpha1 - d.hx;
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
			L = tmp[1] + tmp[2] * tgBeta1 - d.hy;
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

		adjAlpha += U.getItem(0, 9);
		adjBeta += U.getItem(0, 10);
		
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
		
		adjAlpha = data.alpha;
		adjBeta = data.beta;
		
		adjust();
		adjust();
		adjust();
	}
	
	public static void main(String[] args) throws Exception {
		new Printer3DAdjust().doIt();
		System.out.println("Done.");
	}
}
