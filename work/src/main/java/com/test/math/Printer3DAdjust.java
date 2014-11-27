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
import com.slavi.util.testUtil.TestUtil;
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
		double offsetX;			// Millimeter paper offset X
		double offsetY;			// Millimeter paper offset Y
		ArrayList<Measurement> measurements = new ArrayList<Printer3DAdjust.Measurement>();
	}

	static Data generateData() {
		Data result = new Data();
		
		Random rnd = new Random();
		for (int i = 0; i < result.rotationAngles.length; i++) {
			result.rotationAngles[i] = rnd.nextDouble() / 10;
		}
		result.alpha = rnd.nextDouble() / 10;
		result.beta = rnd.nextDouble() / 10;
		result.offsetX = rnd.nextInt(2000);
		result.offsetY = rnd.nextInt(2000);
		
		Matrix M = RotationXYZ.instance.makeAngles(result.rotationAngles);
		double tgAlpha = Math.tan(result.alpha);
		double tgBeta = Math.tan(result.beta);

		double tmp[] = new double[3];
		for (int i = 0; i < 13; i++) {
			Measurement d = new Measurement();
			d.p[0] = rnd.nextInt(3000);
			d.p[1] = rnd.nextInt(3000);
			d.p[2] = rnd.nextInt(3000);
			
			RotationXYZ.instance.transformForward(M, d.p, tmp);
			d.hx = tmp[0] + tmp[2] * tgAlpha; // + rnd.nextDouble() - 0.5;
			d.hy = tmp[1] + tmp[2] * tgBeta ; // + rnd.nextDouble() - 0.5;
			result.measurements.add(d);
		}
		return result;
	}
	
	static void toXML(Data d, String dataFile) throws Exception {
		Element data = new Element("data");
		ObjectToXML.Write toxml = new ObjectToXML.Write(data);
		toxml.objectToXML(data, d);
		XMLHelper.writeXML(new File(dataFile), data, null);
	}
	
	static Data fromXML(String dataFile) throws Exception {
		Element data = XMLHelper.readXML(new File(dataFile));
		ObjectToXML.Read fromxml = new ObjectToXML.Read(data);
		Data result = (Data) fromxml.xmlToObject(data);
		return result;
	}
	
	Data data;
	
	double adjRotationAngles[] = new double[3];
	double adjAlpha;
	double adjBeta;
	double adjOffsetX;
	double adjOffsetY;
	
	void adjust() {
		double tgAlpha1 = Math.tan(adjAlpha);
		double tgBeta1 = Math.tan(adjBeta);
		double dFdAlpha1 = Math.cos(adjAlpha);
		dFdAlpha1 = 1.0 / (dFdAlpha1 * dFdAlpha1);
		double dFdBeta1 = Math.cos(adjBeta);
		dFdBeta1 = 1.0 / (dFdBeta1 * dFdBeta1);

		Matrix adjM = RotationXYZ.instance.makeAngles(adjRotationAngles[0], adjRotationAngles[1], adjRotationAngles[2]);
		Matrix dF_dR1 = RotationXYZ.instance.make_dF_dR1(adjRotationAngles[0], adjRotationAngles[1], adjRotationAngles[2]);
		Matrix dF_dR2 = RotationXYZ.instance.make_dF_dR2(adjRotationAngles[0], adjRotationAngles[1], adjRotationAngles[2]);
		Matrix dF_dR3 = RotationXYZ.instance.make_dF_dR3(adjRotationAngles[0], adjRotationAngles[1], adjRotationAngles[2]);
		
		double tmp[] = new double[3];
		double tmp1[] = new double[3];
		double tmp2[] = new double[3];
		double tmp3[] = new double[3];
		double L;
		Matrix m = new Matrix(5, 1);
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(5);
		lsa.clear();
		for (Measurement d : data.measurements) {
			RotationXYZ.instance.transformForward(adjM, d.p, tmp);
			RotationXYZ.instance.transformForward(dF_dR1, d.p, tmp1);
			RotationXYZ.instance.transformForward(dF_dR2, d.p, tmp2);
			RotationXYZ.instance.transformForward(dF_dR3, d.p, tmp3);
			
			m.setItem(0, 0, tmp1[0] + tmp1[2] * tgAlpha1);
			m.setItem(1, 0, tmp2[0] + tmp2[2] * tgAlpha1);
			m.setItem(2, 0, tmp3[0] + tmp3[2] * tgAlpha1);
			m.setItem(3, 0, tmp[2] * dFdAlpha1);
			m.setItem(4, 0, 0);
//			m.setItem(5, 0, 1);
//			m.setItem(6, 0, 0);
			L = tmp[0] + tmp[2] * tgAlpha1 - d.hx;
			lsa.addMeasurement(m, 1.0, -L, 0);
			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);

			///////////////////////////////////
			
			m.setItem(0, 0, tmp1[1] + tmp1[2] * tgBeta1);
			m.setItem(1, 0, tmp2[1] + tmp2[2] * tgBeta1);
			m.setItem(2, 0, tmp3[1] + tmp3[2] * tgBeta1);
			m.setItem(3, 0, 0);
			m.setItem(4, 0, tmp[2] * dFdBeta1);
//			m.setItem(5, 0, 0);
//			m.setItem(6, 0, 1);
			L = tmp[1] + tmp[2] * tgBeta1 - d.hy;
			lsa.addMeasurement(m, 1.0, -L, 0);
			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);
		}
		
		if (!lsa.calculate()) {
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
		
/*		adjRotationAngles[0] = MathUtil.fixAngle2PI(adjRotationAngles[0] + U.getItem(0, 0));
		adjRotationAngles[1] = MathUtil.fixAngle2PI(adjRotationAngles[1] + U.getItem(0, 1));
		adjRotationAngles[2] = MathUtil.fixAngle2PI(adjRotationAngles[2] + U.getItem(0, 2));
		adjAlpha = MathUtil.fixAngle2PI(adjAlpha + U.getItem(0, 3));
		adjBeta  = MathUtil.fixAngle2PI(adjBeta  + U.getItem(0, 4));
*/		
		adjRotationAngles[0] = (adjRotationAngles[0] + U.getItem(0, 0));
		adjRotationAngles[1] = (adjRotationAngles[1] + U.getItem(0, 1));
		adjRotationAngles[2] = (adjRotationAngles[2] + U.getItem(0, 2));
		adjAlpha = (adjAlpha + U.getItem(0, 3));
		adjBeta  = (adjBeta  + U.getItem(0, 4));

		U.printM("U");
		TestUtil.dumpAngles("   rotationAngles", data.rotationAngles);
		TestUtil.dumpAngles("ADJrotationAngles", adjRotationAngles);

		System.out.println("   Alpha = " + MathUtil.rad2degStr(data.alpha));
		System.out.println("adjAlpha = " + MathUtil.rad2degStr(adjAlpha));
		System.out.println("   Beta  = " + MathUtil.rad2degStr(data.beta));
		System.out.println("adjBeta  = " + MathUtil.rad2degStr(adjBeta));
//		System.out.println("Squared Deviation from E: " + dest.getSquaredDeviationFromE());
	}
	
	private void doIt() throws Exception {
		System.out.println("Data file is " + dataFile);

		data = generateData();
/*
		try {
			data = fromXML(dataFile);
		} catch (Exception e) {
			data = generateData();
			toXML(data, dataFile);
		}

		data = generateData();
		toXML(data, dataFile);
*/		
		adjRotationAngles[0] = 0;
		adjRotationAngles[1] = 0;
		adjRotationAngles[2] = 0;
		
		adjAlpha = 0.01; //data.alpha;
		adjBeta = 0.01; //data.beta;

		for (int i = 1; i < 16; i++) {
			System.out.println("\n\n=========== Iteration " + i);
			adjust();
		}
	}
	
	public static void main(String[] args) throws Exception {
		new Printer3DAdjust().doIt();
		System.out.println("Done.");
	}
}
