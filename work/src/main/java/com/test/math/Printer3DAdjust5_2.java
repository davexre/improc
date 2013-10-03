package com.test.math;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import org.jdom.Element;

import com.slavi.math.MathUtil;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Const;
import com.slavi.util.io.ObjectToXML;
import com.slavi.util.testUtil.TestUtil;
import com.slavi.util.xml.XMLHelper;

/*
 * Two light pens
 * Adjusting all parameters
 */
public class Printer3DAdjust5_2 {

	String dataFile = Const.workDir + "/" + this.getClass().getName() + ".xml";
	
	static ShearXYZ shear = new ShearXYZ();
	
	public static class Measurement {
		double p[] = new double[3];	// Printer coordinates
		double hx1, hy1;			// Measuerd on millimeter paper on the printer's bed (the light pen)
		double hx2, hy2;			// Measuerd on millimeter paper on the printer's bed (the light pen)
		
		public String toString() {
			return String.format("P(%10.2f, %10.2f, %10.2f) H1(%10.2f, %10.2f) H2(%10.2f, %10.2f)", p[0], p[1], p[2], hx1, hy1, hx2, hy2);
		}
	}
	
	public static class Data {
		double shearAngles[] = new double[3];
		double alpha1;			// lightPenAngleZX
		double beta1;			// lightPenAngleZY
		double alpha2;			// lightPenAngleZX
		double beta2;			// lightPenAngleZY
		double offsetX;			// Millimeter paper offset X
		double offsetY;			// Millimeter paper offset Y
		double paperAlpha;		// Millimeter paper rotation
		ArrayList<Measurement> measurements = new ArrayList<Printer3DAdjust5_2.Measurement>();
	}

	static Data generateData() {
		Data result = new Data();
		
		Random rnd = new Random();
		for (int i = 0; i < result.shearAngles.length; i++) {
			result.shearAngles[i] = MathUtil.deg2rad * (80 + rnd.nextDouble() * 20);
		}
		result.alpha1 = MathUtil.deg2rad * (rnd.nextDouble() * 20 - 10);
		result.beta1 = MathUtil.deg2rad * (rnd.nextDouble() * 20 - 10);
		result.alpha2 = MathUtil.deg2rad * (rnd.nextDouble() * 20 - 10);
		result.beta2 = MathUtil.deg2rad * (rnd.nextDouble() * 20 - 10);
		result.paperAlpha = MathUtil.deg2rad * (rnd.nextDouble() * 90 - 45);
		result.offsetX = rnd.nextInt(2000);
		result.offsetY = rnd.nextInt(2000);
		
		Matrix M = shear.makeAngles(result.shearAngles[0], result.shearAngles[1], result.shearAngles[2], result.paperAlpha);
		double tgAlpha1 = Math.tan(result.alpha1);
		double tgBeta1 = Math.tan(result.beta1);
		double tgAlpha2 = Math.tan(result.alpha2);
		double tgBeta2 = Math.tan(result.beta2);

		double tmp[] = new double[3];
		for (int i = 0; i < 13; i++) {
			Measurement d = new Measurement();
			d.p[0] = rnd.nextInt(3000);
			d.p[1] = rnd.nextInt(3000);
			d.p[2] = rnd.nextInt(3000);
			
			shear.transformForward(M, d.p, tmp);
			d.hx1 = result.offsetX + tmp[0] + tmp[2] * tgAlpha1; // + rnd.nextDouble() - 0.5;
			d.hy1 = result.offsetY + tmp[1] + tmp[2] * tgBeta1 ; // + rnd.nextDouble() - 0.5;
			d.hx2 = result.offsetX + tmp[0] + tmp[2] * tgAlpha2; // + rnd.nextDouble() - 0.5;
			d.hy2 = result.offsetY + tmp[1] + tmp[2] * tgBeta2 ; // + rnd.nextDouble() - 0.5;
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
	
	double adjShearAngles[] = new double[3];
	double adjAlpha1;
	double adjBeta1;
	double adjAlpha2;
	double adjBeta2;
	double adjOffsetX;
	double adjOffsetY;
	double adjPaperAlhpa;
	
	void adjust() {
		double tgAlpha1 = Math.tan(adjAlpha1);
		double tgBeta1 = Math.tan(adjBeta1);
		double tgAlpha2 = Math.tan(adjAlpha2);
		double tgBeta2 = Math.tan(adjBeta2);
		
		double dFdAlpha1 = Math.cos(adjAlpha1);
		dFdAlpha1 = 1.0 / (dFdAlpha1 * dFdAlpha1);
		double dFdBeta1 = Math.cos(adjBeta1);
		dFdBeta1 = 1.0 / (dFdBeta1 * dFdBeta1);

		double dFdAlpha2 = Math.cos(adjAlpha2);
		dFdAlpha2 = 1.0 / (dFdAlpha2 * dFdAlpha2);
		double dFdBeta2 = Math.cos(adjBeta2);
		dFdBeta2 = 1.0 / (dFdBeta2 * dFdBeta2);

		Matrix adjM    = shear.makeAngles  (adjShearAngles[0], adjShearAngles[1], adjShearAngles[2], adjPaperAlhpa);
		Matrix dF_dR1  = shear.make_dF_dR1 (adjShearAngles[0], adjShearAngles[1], adjShearAngles[2], adjPaperAlhpa);
		Matrix dF_dR2  = shear.make_dF_dR2 (adjShearAngles[0], adjShearAngles[1], adjShearAngles[2], adjPaperAlhpa);
		Matrix dF_dR3  = shear.make_dF_dR3 (adjShearAngles[0], adjShearAngles[1], adjShearAngles[2], adjPaperAlhpa);
		Matrix dF_dROT = shear.make_dF_dROT(adjShearAngles[0], adjShearAngles[1], adjShearAngles[2], adjPaperAlhpa);
		
		double tmp[] = new double[3];
		double tmp1[] = new double[3];
		double tmp2[] = new double[3];
		double tmp3[] = new double[3];
		double tmpROT[] = new double[3];
		double L;
		Matrix m = new Matrix(10, 1);
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(10);
		lsa.clear();
		for (Measurement d : data.measurements) {
			shear.transformForward(adjM, d.p, tmp);
			shear.transformForward(dF_dR1, d.p, tmp1);
			shear.transformForward(dF_dR2, d.p, tmp2);
			shear.transformForward(dF_dR3, d.p, tmp3);
			shear.transformForward(dF_dROT, d.p, tmpROT);
			
			m.setItem(0, 0, tmp1[0] + tmp1[2] * tgAlpha1);
			m.setItem(1, 0, tmp2[0] + tmp2[2] * tgAlpha1);
			m.setItem(2, 0, tmp3[0] + tmp3[2] * tgAlpha1);
			m.setItem(3, 0, tmpROT[0] + tmpROT[2] * tgAlpha1);
			m.setItem(4, 0, tmp[2] * dFdAlpha1);
			m.setItem(5, 0, 0);
			m.setItem(6, 0, 0);
			m.setItem(7, 0, 0);
			m.setItem(8, 0, 1);
			m.setItem(9, 0, 0);
			L = adjOffsetX + tmp[0] + tmp[2] * tgAlpha1 - d.hx1;
			lsa.addMeasurement(m, 1.0, -L, 0);
			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);

			///////////////////////////////////
			
			m.setItem(0, 0, tmp1[1] + tmp1[2] * tgBeta1);
			m.setItem(1, 0, tmp2[1] + tmp2[2] * tgBeta1);
			m.setItem(2, 0, tmp3[1] + tmp3[2] * tgBeta1);
			m.setItem(3, 0, tmpROT[1] + tmpROT[2] * tgBeta1);
			m.setItem(4, 0, 0);
			m.setItem(5, 0, tmp[2] * dFdBeta1);
			m.setItem(6, 0, 0);
			m.setItem(7, 0, 0);
			m.setItem(8, 0, 0);
			m.setItem(9, 0, 1);
			L = adjOffsetY + tmp[1] + tmp[2] * tgBeta1 - d.hy1;
			lsa.addMeasurement(m, 1.0, -L, 0);
			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);

			///////////////////////////////////
			
			m.setItem(0, 0, tmp1[0] + tmp1[2] * tgAlpha2);
			m.setItem(1, 0, tmp2[0] + tmp2[2] * tgAlpha2);
			m.setItem(2, 0, tmp3[0] + tmp3[2] * tgAlpha2);
			m.setItem(3, 0, tmpROT[0] + tmpROT[2] * tgAlpha2);
			m.setItem(4, 0, 0);
			m.setItem(5, 0, 0);
			m.setItem(6, 0, tmp[2] * dFdAlpha2);
			m.setItem(7, 0, 0);
			m.setItem(8, 0, 1);
			m.setItem(9, 0, 0);
			L = adjOffsetX + tmp[0] + tmp[2] * tgAlpha2 - d.hx2;
			lsa.addMeasurement(m, 1.0, -L, 0);
			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);

			///////////////////////////////////
			
			m.setItem(0, 0, tmp1[1] + tmp1[2] * tgBeta2);
			m.setItem(1, 0, tmp2[1] + tmp2[2] * tgBeta2);
			m.setItem(2, 0, tmp3[1] + tmp3[2] * tgBeta2);
			m.setItem(3, 0, tmpROT[1] + tmpROT[2] * tgBeta2);
			m.setItem(4, 0, 0);
			m.setItem(5, 0, 0);
			m.setItem(6, 0, 0);
			m.setItem(7, 0, tmp[2] * dFdBeta2);
			m.setItem(8, 0, 0);
			m.setItem(9, 0, 1);
			L = adjOffsetY + tmp[1] + tmp[2] * tgBeta2 - d.hy2;
			lsa.addMeasurement(m, 1.0, -L, 0);
			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);
		}
		
		if (!lsa.calculateWithDebug(true)) {
			throw new RuntimeException("Calculation failed");
		}

		Matrix U = lsa.getUnknown();
		
/*		adjRotationAngles[0] = MathUtil.fixAngle2PI(adjRotationAngles[0] + U.getItem(0, 0));
		adjRotationAngles[1] = MathUtil.fixAngle2PI(adjRotationAngles[1] + U.getItem(0, 1));
		adjRotationAngles[2] = MathUtil.fixAngle2PI(adjRotationAngles[2] + U.getItem(0, 2));
		adjAlpha = MathUtil.fixAngle2PI(adjAlpha + U.getItem(0, 3));
		adjBeta  = MathUtil.fixAngle2PI(adjBeta  + U.getItem(0, 4));
*/		
		adjShearAngles[0] = (adjShearAngles[0] + U.getItem(0, 0));
		adjShearAngles[1] = (adjShearAngles[1] + U.getItem(0, 1));
		adjShearAngles[2] = (adjShearAngles[2] + U.getItem(0, 2));
		adjPaperAlhpa = (adjPaperAlhpa + U.getItem(0, 3));
		adjAlpha1 = (adjAlpha1 + U.getItem(0, 4));
		adjBeta1  = (adjBeta1  + U.getItem(0, 5));
		adjAlpha2 = (adjAlpha2 + U.getItem(0, 6));
		adjBeta2  = (adjBeta2  + U.getItem(0, 7));
		adjOffsetX += U.getItem(0, 8);
		adjOffsetY += U.getItem(0, 9);

		U.printM("U");
		TestUtil.dumpAngles("   rotationAngles", data.shearAngles);
		TestUtil.dumpAngles("ADJrotationAngles", adjShearAngles);
		System.out.println();
		System.out.println("   PaperAlpha = " + MathUtil.rad2degStr(data.paperAlpha));
		System.out.println("adjPaperAlpha = " + MathUtil.rad2degStr(adjPaperAlhpa));
		System.out.println();
		System.out.println("   Alpha1 = " + MathUtil.rad2degStr(data.alpha1));
		System.out.println("adjAlpha1 = " + MathUtil.rad2degStr(adjAlpha1));
		System.out.println("   Beta1  = " + MathUtil.rad2degStr(data.beta1));
		System.out.println("adjBeta1  = " + MathUtil.rad2degStr(adjBeta1));
		System.out.println();
		System.out.println("   Alpha2 = " + MathUtil.rad2degStr(data.alpha2));
		System.out.println("adjAlpha2 = " + MathUtil.rad2degStr(adjAlpha2));
		System.out.println("   Beta2  = " + MathUtil.rad2degStr(data.beta2));
		System.out.println("adjBeta2  = " + MathUtil.rad2degStr(adjBeta2));
		System.out.println();
		System.out.println("   offX   = " + MathUtil.d4(data.offsetX));
		System.out.println("adjOffX   = " + MathUtil.d4(adjOffsetX));
		System.out.println("   offY   = " + MathUtil.d4(data.offsetY));
		System.out.println("adjOffY   = " + MathUtil.d4(adjOffsetY));
		
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
		adjShearAngles[0] = MathUtil.deg2rad * 90;
		adjShearAngles[1] = MathUtil.deg2rad * 90;
		adjShearAngles[2] = MathUtil.deg2rad * 90;
		adjPaperAlhpa = MathUtil.deg2rad * 1;
		
		adjAlpha1 = MathUtil.deg2rad * 1; //*/ data.alpha1;
		adjBeta1  = MathUtil.deg2rad * 1; //*/ data.beta1;
		adjAlpha2 = MathUtil.deg2rad * 1; //*/ data.alpha2;
		adjBeta2  = MathUtil.deg2rad * 1; //*/ data.beta2;
		adjOffsetX = 0;
		adjOffsetY = 0;

		adjShearAngles[0] = data.shearAngles[0];
//		adjShearAngles[1] = data.shearAngles[1];
		adjShearAngles[2] = data.shearAngles[2];
		adjPaperAlhpa = data.paperAlpha;
		
//		adjAlpha1 = data.alpha1;
//		adjBeta1  = data.beta1;
//		adjAlpha2 = data.alpha2;
		adjBeta2  = data.beta2;
//		adjOffsetX = data.offsetX;
//		adjOffsetY = data.offsetY;

		for (int i = 1; i < 16; i++) {
			System.out.println("\n\n=========== Iteration " + i);
			adjust();
		}
	}
	
	public static void main(String[] args) throws Exception {
		new Printer3DAdjust5_2().doIt();
		System.out.println("Done.");
	}
}
