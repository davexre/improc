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

public class Printer3DAdjust2_1 {

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
		double offsetX;			// Millimeter paper offset X
		double offsetY;			// Millimeter paper offset Y
		double paperAlpha;		// Millimeter paper rotation
		ArrayList<Measurement> measurements = new ArrayList<Printer3DAdjust2_1.Measurement>();
	}

	static Data generateData() {
		Data result = new Data();
		
		Random rnd = new Random();
		for (int i = 0; i < result.rotationAngles.length; i++) {
			result.rotationAngles[i] = rnd.nextDouble() / 10;
		}
		result.alpha1 = rnd.nextDouble() / 10;
		result.beta1 = rnd.nextDouble() / 10;
		result.alpha2 = rnd.nextDouble() / 10;
		result.beta2 = rnd.nextDouble() / 10;
		result.offsetX = rnd.nextInt(2000);
		result.offsetY = rnd.nextInt(2000);
		result.paperAlpha = rnd.nextDouble() * MathUtil.C2PI;
		
		Matrix M = RotationXYZ.instance.makeAngles(result.rotationAngles);
		double tgAlpha1 = Math.tan(result.alpha1);
		double tgBeta1 = Math.tan(result.beta1);
		double tgAlpha2 = Math.tan(result.alpha2);
		double tgBeta2 = Math.tan(result.beta2);
		double sinPaperAlpha = Math.sin(result.paperAlpha);
		double cosPaperAlpha = Math.cos(result.paperAlpha);

		double tmp[] = new double[3];
		for (int i = 0; i < 13; i++) {
			Measurement d = new Measurement();
			d.p[0] = rnd.nextInt(3000);
			d.p[1] = rnd.nextInt(3000);
			d.p[2] = rnd.nextInt(3000);
			
			RotationXYZ.instance.transformForward(M, d.p, tmp);
			d.hx1 = result.offsetX + cosPaperAlpha * (tmp[0] + tmp[2] * tgAlpha1); // + rnd.nextDouble() - 0.5;
			d.hy1 = result.offsetY + sinPaperAlpha * (tmp[1] + tmp[2] * tgBeta1 ); // + rnd.nextDouble() - 0.5;
			d.hx2 = result.offsetX + cosPaperAlpha * (tmp[0] + tmp[2] * tgAlpha2); // + rnd.nextDouble() - 0.5;
			d.hy2 = result.offsetY + sinPaperAlpha * (tmp[1] + tmp[2] * tgBeta2 ); // + rnd.nextDouble() - 0.5;
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
	
	public static double fixAngleMPIover2_PIover2(double angle) {
		angle += MathUtil.PIover2;
		angle %= Math.PI;
		return ((angle < 0) ? Math.PI + angle : angle) - MathUtil.PIover2;
	}

	static boolean shallRotate(double angle) {
		angle = MathUtil.fixAngle2PI(angle);
		return (angle > 0.5 * Math.PI) && (angle <= 1.5 * Math.PI);
	}

	static boolean shallRotate2(double angle) {
		angle = MathUtil.fixAngle2PI(angle);
		return (angle > 0.25 * Math.PI) && (angle <= 1.75 * Math.PI);
	}

	static void rotateAngles(double rotationAngles[], double dR1, double dR2, double dR3) {
		Matrix rot1 = RotationXYZ.instance.makeAngles(rotationAngles);
		Matrix rot2 = RotationXYZ.instance.makeAngles(dR1, dR2, dR3);
		Matrix res = new Matrix(3, 3);
		rot2.mMul(rot1, res);
		RotationXYZ.instance.getRotationAngles(res, rotationAngles);
	}
	
	Data data;
	
	double adjRotationAngles[] = new double[3];
	double adjAlpha1;
	double adjBeta1;
	double adjAlpha2;
	double adjBeta2;
	double adjOffsetX;
	double adjOffsetY;
	double adjPaperAlpha;
	
	void fixAngles() {
		double pi4 = 0.25 * Math.PI;
		double pi2 = 0.5 * Math.PI;
		double dR;
		int index;
		
		dR = 0;
		index = (int) (MathUtil.fixAngle2PI(adjAlpha1) / pi4);
		switch (index) {
		case 1: // [pi*1/4..pi*2/4]
		case 2: // [pi*2/4..pi*3/4]
			dR = -pi2;
			break;
		case 3: // [pi*3/4..pi*4/4]
		case 4: // [pi*4/4..pi*5/4]
			dR = Math.PI;
			break;
		case 5: // [pi*5/4..pi*6/4]
		case 6: // [pi*6/4..pi*7/4]
			dR = pi2;
			break;
		case 0: // [0..pi/4]
		case 7: // [-pi/4..0]
			// Do nothing
			dR = 0;
			break;
		}
		if (dR != 0) {
			System.out.println("Fixing adjAngle1 with " + dR);
			adjAlpha1 = MathUtil.fixAngleMPI_PI(adjAlpha1 + dR);
			adjAlpha2 = MathUtil.fixAngleMPI_PI(adjAlpha2 + dR);
			rotateAngles(adjRotationAngles, 0, dR, 0);
		}
		
		///////////////////////
		
		dR = 0;
		index = (int) (MathUtil.fixAngle2PI(adjAlpha2) / pi2);
		switch (index) {
		case 0: // [0..pi*1/2]
		case 1: // [pi*3/2..pi*4/2]
			// Do nothing
			dR = 0;
			break;
		case 2: // [pi*1/2..pi*2/2]
		case 3: // [pi*2/2..pi*3/2]
			dR = Math.PI;
			System.out.println("Fixing adjAlpha2");
			adjAlpha2 = MathUtil.fixAngleMPI_PI(adjAlpha2 + dR);
			adjBeta2 = MathUtil.fixAngleMPI_PI(adjBeta2 + dR);
			break;
		}

		////////////////////////
		
		dR = 0;
		index = (int) (MathUtil.fixAngle2PI(adjBeta1) / pi2);
		switch (index) {
		case 1: // [pi*1/4..pi*2/4]
		case 2: // [pi*2/4..pi*3/4]
			dR = -pi2;
			break;
		case 3: // [pi*3/4..pi*4/4]
		case 4: // [pi*4/4..pi*5/4]
			dR = Math.PI;
			break;
		case 5: // [pi*5/4..pi*6/4]
		case 6: // [pi*6/4..pi*7/4]
			dR = pi2;
			break;
		case 0: // [0..pi/4]
		case 7: // [-pi/4..0]
			// Do nothing
			dR = 0;
			break;
		}
		
	}
	
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
		
		double sinPaperAlpha = Math.sin(adjPaperAlpha);
		double cosPaperAlpha = Math.cos(adjPaperAlpha);

		Matrix adjM = RotationXYZ.instance.makeAngles(adjRotationAngles[0], adjRotationAngles[1], adjRotationAngles[2]);
		Matrix dF_dR1 = RotationXYZ.instance.make_dF_dR1(adjRotationAngles[0], adjRotationAngles[1], adjRotationAngles[2]);
		Matrix dF_dR2 = RotationXYZ.instance.make_dF_dR2(adjRotationAngles[0], adjRotationAngles[1], adjRotationAngles[2]);
		Matrix dF_dR3 = RotationXYZ.instance.make_dF_dR3(adjRotationAngles[0], adjRotationAngles[1], adjRotationAngles[2]);
		
		double tmp[] = new double[3];
		double tmp1[] = new double[3];
		double tmp2[] = new double[3];
		double tmp3[] = new double[3];
		double L;
		Matrix m = new Matrix(10, 1);
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(10);
		lsa.clear();
		for (Measurement d : data.measurements) {
			RotationXYZ.instance.transformForward(adjM, d.p, tmp);
			RotationXYZ.instance.transformForward(dF_dR1, d.p, tmp1);
			RotationXYZ.instance.transformForward(dF_dR2, d.p, tmp2);
			RotationXYZ.instance.transformForward(dF_dR3, d.p, tmp3);
			
			m.setItem(0, 0, cosPaperAlpha * (tmp1[0] + tmp1[2] * tgAlpha1));
			m.setItem(1, 0, cosPaperAlpha * (tmp2[0] + tmp2[2] * tgAlpha1));
			m.setItem(2, 0, cosPaperAlpha * (tmp3[0] + tmp3[2] * tgAlpha1));
			m.setItem(3, 0, cosPaperAlpha * tmp[2] * dFdAlpha1);
			m.setItem(4, 0, 0);
			m.setItem(5, 0, 0);
			m.setItem(6, 0, 0);
			m.setItem(7, 0, 1);
			m.setItem(8, 0, 0);
			m.setItem(9, 0, -sinPaperAlpha * (tmp[0] + tmp[2] * tgAlpha1));
			L = adjOffsetX + cosPaperAlpha * (tmp[0] + tmp[2] * tgAlpha1) - d.hx1;
			lsa.addMeasurement(m, 1.0, -L, 0);
//			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);

			///////////////////////////////////
			
			m.setItem(0, 0, sinPaperAlpha * (tmp1[1] + tmp1[2] * tgBeta1));
			m.setItem(1, 0, sinPaperAlpha * (tmp2[1] + tmp2[2] * tgBeta1));
			m.setItem(2, 0, sinPaperAlpha * (tmp3[1] + tmp3[2] * tgBeta1));
			m.setItem(3, 0, 0);
			m.setItem(4, 0, sinPaperAlpha * tmp[2] * dFdBeta1);
			m.setItem(5, 0, 0);
			m.setItem(6, 0, 0);
			m.setItem(7, 0, 0);
			m.setItem(8, 0, 1);
			m.setItem(9, 0,  cosPaperAlpha * (tmp[1] + tmp[2] * tgBeta1));
			L = adjOffsetY + sinPaperAlpha * (tmp[1] + tmp[2] * tgBeta1) - d.hy1;
			lsa.addMeasurement(m, 1.0, -L, 0);
//			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);

			///////////////////////////////////

			m.setItem(0, 0, cosPaperAlpha * (tmp1[0] + tmp1[2] * tgAlpha2));
			m.setItem(1, 0, cosPaperAlpha * (tmp2[0] + tmp2[2] * tgAlpha2));
			m.setItem(2, 0, cosPaperAlpha * (tmp3[0] + tmp3[2] * tgAlpha2));
			m.setItem(3, 0, 0);
			m.setItem(4, 0, 0);
			m.setItem(5, 0, cosPaperAlpha * tmp[2] * dFdAlpha2);
			m.setItem(6, 0, 0);
			m.setItem(7, 0, 1);
			m.setItem(8, 0, 0);
			m.setItem(9, 0, -sinPaperAlpha * (tmp[0] + tmp[2] * tgAlpha2));
			L = adjOffsetX + cosPaperAlpha * (tmp[0] + tmp[2] * tgAlpha2) - d.hx2;
			lsa.addMeasurement(m, 1.0, -L, 0);
//			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);

			///////////////////////////////////
			
			m.setItem(0, 0, sinPaperAlpha * (tmp1[1] + tmp1[2] * tgBeta2));
			m.setItem(1, 0, sinPaperAlpha * (tmp2[1] + tmp2[2] * tgBeta2));
			m.setItem(2, 0, sinPaperAlpha * (tmp3[1] + tmp3[2] * tgBeta2));
			m.setItem(3, 0, 0);
			m.setItem(4, 0, 0);
			m.setItem(5, 0, 0);
			m.setItem(6, 0, sinPaperAlpha * tmp[2] * dFdBeta2);
			m.setItem(7, 0, 0);
			m.setItem(8, 0, 1);
			m.setItem(9, 0,  cosPaperAlpha * (tmp[1] + tmp[2] * tgBeta2));
			L = adjOffsetY + sinPaperAlpha * (tmp[1] + tmp[2] * tgBeta2) - d.hy2;
			lsa.addMeasurement(m, 1.0, -L, 0);
//			System.out.print("L:"+MathUtil.d4(L) + "\tM:" + m);
		}
		
		if (!lsa.calculate()) {
			throw new RuntimeException("Calculation failed");
		}

		Matrix U = lsa.getUnknown();

		adjRotationAngles[0] = MathUtil.fixAngle2PI(adjRotationAngles[0] + U.getItem(0, 0));
		adjRotationAngles[1] = MathUtil.fixAngle2PI(adjRotationAngles[1] + U.getItem(0, 1));
		adjRotationAngles[2] = MathUtil.fixAngle2PI(adjRotationAngles[2] + U.getItem(0, 2));
		adjAlpha1 = MathUtil.fixAngleMPI_PI(adjAlpha1 + U.getItem(0, 3));
		adjBeta1  = MathUtil.fixAngleMPI_PI(adjBeta1  + U.getItem(0, 4));
		adjAlpha2 = MathUtil.fixAngleMPI_PI(adjAlpha2 + U.getItem(0, 5));
		adjBeta2  = MathUtil.fixAngleMPI_PI(adjBeta2  + U.getItem(0, 6));
		adjOffsetX += U.getItem(0, 7);
		adjOffsetY += U.getItem(0, 8);
		adjPaperAlpha = MathUtil.fixAngle2PI(adjPaperAlpha + U.getItem(0, 9));

		boolean rotated = false;
		/*
		
		if (shallRotate(adjAlpha1)) {
			adjAlpha1 = MathUtil.fixAngleMPI_PI(adjAlpha1 + Math.PI);
			adjBeta1 =  MathUtil.fixAngleMPI_PI(adjBeta1 + Math.PI);
			System.out.println("ROTATING adjAlpha1");
			rotated = true;
		}
		if (shallRotate(adjAlpha2)) {
			adjAlpha2 = MathUtil.fixAngleMPI_PI(adjAlpha2 + Math.PI);
			adjBeta2 =  MathUtil.fixAngleMPI_PI(adjBeta2 + Math.PI);
			System.out.println("ROTATING adjAlpha2");
			rotated = true;
		}
		if (shallRotate2(adjAlpha1)) {
			adjAlpha1 = MathUtil.fixAngleMPI_PI(adjAlpha1 + 0.5 * Math.PI);
			adjAlpha2 = MathUtil.fixAngleMPI_PI(adjAlpha1 + 0.5 * Math.PI);
			rotateAngles(adjRotationAngles, 0, 90 * MathUtil.deg2rad, 0);
			
			adjRotationAngles[1] = MathUtil.fixAngle2PI(adjRotationAngles[1] + Math.PI);
			adjBeta1 =  MathUtil.fixAngleMPI_PI(adjBeta1 + Math.PI);
			System.out.println("ROTATING adjAlpha1");
			rotated = true;
		}
		
		
		
		if (shallRotate(adjBeta1)) {
			adjBeta1 = MathUtil.fixAngleMPI_PI(adjBeta1 + Math.PI);
			adjBeta2 = MathUtil.fixAngleMPI_PI(adjBeta2 + Math.PI);
			adjRotationAngles[0] = MathUtil.fixAngle2PI(adjRotationAngles[0] + Math.PI);
			System.out.println("ROTATING adjBeta1");
			rotated = true;
		}
		if (shallRotate(adjBeta2)) {
			adjBeta2 = MathUtil.fixAngleMPI_PI(adjBeta2 + Math.PI);
			System.out.println("ROTATING adjBeta2");
			System.out.println("************************** WHY ???????????\n\n");
			rotated = true;
		}
		if (shallRotate(adjRotationAngles[0])) {
			System.out.println("ROTATING adjRotationAngles[0]");
			adjRotationAngles[0] = MathUtil.fixAngle2PI(adjRotationAngles[0] + Math.PI);
			adjRotationAngles[1] = MathUtil.fixAngle2PI(adjRotationAngles[1] + Math.PI);
			adjRotationAngles[2] = MathUtil.fixAngle2PI(adjRotationAngles[2] + Math.PI);
			rotated = true;
		}*/
		fixAngles();
		
		TestUtil.dumpAngles("   rotationAngles", data.rotationAngles);
		TestUtil.dumpAngles("ADJrotationAngles", adjRotationAngles);
		System.out.println();
		System.out.println("   PaperAlpha = " + MathUtil.rad2degStr(data.paperAlpha));
		System.out.println("adjPaperAlpha = " + MathUtil.rad2degStr(adjPaperAlpha));

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
		System.out.println();
		U.printM("U");
	}
	
	private void doIt() throws Exception {
		System.out.println("Data file is " + dataFile);

//		data = generateData();
//		toXML(data, dataFile);
		data = fromXML(dataFile);

/*
		try {
			data = fromXML(dataFile);
		} catch (Exception e) {
			data = generateData();
			toXML(data, dataFile);
		}

		data = generateData();
*/		
		adjRotationAngles[0] = 0;
		adjRotationAngles[1] = 0;
		adjRotationAngles[2] = 0;
		
		adjAlpha1  = 0.01;
		adjBeta1   = 0.01;
		adjAlpha2  = 0.01;
		adjBeta2   = 0.01;
		adjOffsetX = 0;
		adjOffsetY = 0;
		adjPaperAlpha = 0.01; //260 * MathUtil.deg2rad;

		for (int i = 1; i < 16; i++) {
			System.out.println("\n\n=========== Iteration " + i);
			adjust();
		}
	}
	
	public static void main(String[] args) throws Exception {
		new Printer3DAdjust2_1().doIt();
		System.out.println("Done.");
	}
}
