package com.test.math;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Locale;

import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.AffineTransformer;

public class AffineAdjust {

	private static double degreeToRad = Math.PI / 180;

	static class AdjImage {
		String name;
		Matrix toWorld, fromWorld;
		public AdjImage(String name) {
			this.name = name;
		}
	}
	
	static class AdjImagePair {
		AdjImage source, target;
		
		Matrix toTarget, fromTarget;
	}
	
	ArrayList<AdjImage>il = new ArrayList<AdjImage>();
	ArrayList<AdjImagePair>ipl = new ArrayList<AdjImagePair>();
	AffineTransform jTransform = new AffineTransform();
	AffineTransformer sTransform = new AffineTransformer(2, 2);
	double d6[] = new double[6];
	
	void addData(AdjImage source, AdjImage target, double angleInDegree) {
		jTransform.setToIdentity();
		jTransform.rotate(angleInDegree * degreeToRad);
//		jTransform.scale(123.456, 789.123);
//		jTransform.shear(1.234, 2.345);
//		jTransform.translate(100.567, 200.123);
		jTransform.getMatrix(d6);
		sTransform.setMatrix(d6);
		AdjImagePair d = new AdjImagePair();
		d.source = source;
		d.target = target;
		d.toTarget = sTransform.getMatrix();
		d.fromTarget = d.toTarget.makeCopy();
		if (!d.fromTarget.inverse()) 
			throw new RuntimeException("Matrix inverse failed");
		ipl.add(d);
	}	

	void makeData() {
		il.add(new AdjImage("0"));
		il.add(new AdjImage("1"));
		il.add(new AdjImage("2"));
		il.add(new AdjImage("3"));
		//
		//   1 2
		//   0 3
		//
		addData(il.get(0), il.get(1), 91);
		addData(il.get(0), il.get(2), 44);
		addData(il.get(0), il.get(3), 1);
		addData(il.get(1), il.get(2), -89);
		addData(il.get(1), il.get(3), -136);
		addData(il.get(2), il.get(3), -134);
	}
	
	void calcToWorldUsingOriginImage(AdjImage image) {
		// Clear previous calculations
		for (AdjImage i : il)
			i.toWorld = null;
		image.toWorld = new Matrix(3, 3);
		image.toWorld.makeE();
		
		// Calculate the approximate toWorld matrices
		boolean loopMore = true;
		do {
			loopMore = false;
			for (AdjImagePair p : ipl) {
				if ((p.source.toWorld != null) && (p.target.toWorld == null)) {
					p.target.toWorld = new Matrix(3, 3);
					p.fromTarget.mMul(p.source.toWorld, p.target.toWorld);
					p.target.toWorld.setItem(0, 2, 0.0);
					p.target.toWorld.setItem(1, 2, 0.0);
					p.target.toWorld.setItem(2, 2, 1.0);
					loopMore = true;
				} else if ((p.source.toWorld == null) && (p.target.toWorld != null)) {
					p.source.toWorld = new Matrix(3, 3);
					p.toTarget.mMul(p.target.toWorld, p.source.toWorld);
					p.source.toWorld.setItem(0, 2, 0.0);
					p.source.toWorld.setItem(1, 2, 0.0);
					p.source.toWorld.setItem(2, 2, 1.0);
					loopMore = true;
				} 
			}
		} while(loopMore);
	}
	
	private void calcFromWorld() {
		// Check if there are any non-computed images
		// Calculate all the "fromWorld" matrices
		for (AdjImage i : il) { 
			if (i.toWorld == null)
				throw new IllegalArgumentException("There are not connected images");
			i.fromWorld = i.toWorld.makeCopy();
			if (!i.fromWorld.inverse())
				throw new IllegalArgumentException("Could not compute the fromWorld matrix");
			i.fromWorld.setItem(0, 2, 0.0);
			i.fromWorld.setItem(1, 2, 0.0);
			i.fromWorld.setItem(2, 2, 1.0);
//			i.fromWorld2 = new Matrix(3, 3);
//			i.fromWorld.mMul(i.fromWorld, i.fromWorld2);
		}
	}
	
	private Matrix getRotationMatrix(Matrix m) {
		Matrix r = m.makeCopy();
		r.setItem(2, 0, 0.0);
		r.setItem(2, 1, 0.0);
		r.setItem(2, 2, 1.0);
		r.setItem(0, 2, 0.0);
		r.setItem(1, 2, 0.0);
		return r;
	}

	private void printSourceToTarget() {
		System.out.println("*** source to target");
		for (AdjImagePair p : ipl) {
			System.out.println(p.toTarget.toMatlabString(
					"a" + il.indexOf(p.source) + 
					"a" + il.indexOf(p.target)));
		}
	}

	private void printImageToWorld() {
		System.out.println("*** Image toWorld");
		for (int i = 0; i < il.size(); i++) {
			AdjImage ii = il.get(i);
			System.out.println(ii.toWorld.toMatlabString("a" + i));
		}
		System.out.println("*** Alfa");
		for (int i = 0; i < il.size(); i++) {
			AdjImage ii = il.get(i);
			System.out.format(Locale.US, "al%d=%8.3f\n", i, (Math.acos(ii.toWorld.getItem(0, 0)) / degreeToRad) );
		}
	}
	
	private void calcUsingOriginPoint(AdjImage image) {
		printSourceToTarget();

		System.out.println("*** Calc using origin image " + il.indexOf(image));
		calcToWorldUsingOriginImage(image);
		calcFromWorld();

		printImageToWorld();
		
		// Least square adjust the rotation of the images 
		LeastSquaresAdjust lsa = new LeastSquaresAdjust(il.size() * 4);
		Matrix coefs = new Matrix(il.size() * 4, 1);
		Matrix L = new Matrix(3, 3);
		Matrix Q = new Matrix(3, 3);
		Matrix a = new Matrix(3, 3);
		Matrix b = new Matrix(3, 3);
		for (AdjImagePair p : ipl) {
			int iA = il.indexOf(p.source) * 4;
			int iB = il.indexOf(p.target) * 4;
			// Discrepancy
			Matrix rotSrcToWorld = getRotationMatrix(p.source.toWorld);
			Matrix rotTargetFromWorld = getRotationMatrix(p.target.fromWorld);
			Matrix rotMul = new Matrix();
			rotSrcToWorld.mMul(rotTargetFromWorld, rotMul);
			rotMul.mSub(getRotationMatrix(p.toTarget), L);
			System.out.println("------ Processing pair " + il.indexOf(p.source) + "-" + il.indexOf(p.target));
			L.printM("L");

			for (int i = 0; i < 2; i++) { 
				for (int j = 0; j < 2; j++) {
					Q.make0();
					Q.setItem(i, j, 1.0);
		
					rotMul.mMul(Q, a);
					a.mMul(rotTargetFromWorld, rotMul);
					Q.mMul(rotTargetFromWorld, a);
					coefs.make0();
		
					coefs.setItem(iA + 0, 0, a.getItem(0, 0));
					coefs.setItem(iA + 1, 0, a.getItem(1, 0));
					coefs.setItem(iA + 2, 0, a.getItem(0, 1));
					coefs.setItem(iA + 3, 0, a.getItem(1, 1));
		
					coefs.setItem(iB + 0, 0, -b.getItem(0, 0));
					coefs.setItem(iB + 1, 0, -b.getItem(1, 0));
					coefs.setItem(iB + 2, 0, -b.getItem(0, 1));
					coefs.setItem(iB + 3, 0, -b.getItem(1, 1));
					a.printM("a " + i + "-" + j);
					b.printM("b " + i + "-" + j);
					System.out.println("L=" + L.getItem(i, j));
					System.out.print("Coefs " + i + "-" + j + " " + String.format(Locale.US, "%10.4f |", L.getItem(i, j)) + coefs.toString());
					lsa.addMeasurement(coefs, 1.0, L.getItem(i, j), 0);
				}
			}
		}
		System.out.println("NM");
		System.out.println(lsa.getNm());
		if (!lsa.calculate())
			throw new RuntimeException("Adjust failed");

//		printImageToWorld();
		// Recreate the toWorld and fromWorld matrices
		Matrix u = lsa.getUnknown();
		for (int i = il.size() - 1; i >= 0; i--) {
			AdjImage im = il.get(i);
			int iA = i * 4;
			im.toWorld.setItem(0, 0, u.getItem(0, iA + 0));
			im.toWorld.setItem(1, 0, u.getItem(0, iA + 1));
			im.toWorld.setItem(0, 1, u.getItem(0, iA + 2));
			im.toWorld.setItem(1, 1, u.getItem(0, iA + 3));
		}
		printImageToWorld();
		calcFromWorld();
/*		dump4matlab();

		// Least square adjust the translation of the images 
		lsa = new LeastSquaresAdjust(il.size() * 2, 2);
		coefs = new Matrix(il.size() * 2, 1);
		a = new Matrix(1, 3);
		a.setItem(0, 2, 0.0);
		b = new Matrix(1, 3);
		L = new Matrix(1, 3);
		for (AdjImagePair p : ipl) {
			int iA = il.indexOf(p.source);
			int iB = il.indexOf(p.target);
			// Discrepancy
			a.setItem(0, 0, p.toTarget.getItem(2, 0));
			a.setItem(0, 1, p.toTarget.getItem(2, 1));
			p.source.toWorld.mMul(a, b);
			
			coefs.make0();
			coefs.setItem(iA, 0, 1.0);
			coefs.setItem(iB, 0, -1.0);
			lsa.addMeasurement(coefs, 1.0, p.source.toWorld.getItem(2, 0) - p.target.toWorld.getItem(2, 0) + b.getItem(0, 0), 0);
			lsa.addMeasurement(coefs, 1.0, p.source.toWorld.getItem(2, 1) - p.target.toWorld.getItem(2, 1) + b.getItem(0, 1), 1);
		}
		lsa.calculate();
		
		// Recreate the toWorld and fromWorld matrices
		u = lsa.getUnknown();
		for (int iA = il.size() - 1; iA >= 0; iA--) {
			AdjImage im = il.get(iA);
			im.toWorld.setItem(0, 0, u.getItem(0, iA));
			im.toWorld.setItem(1, 0, u.getItem(1, iA));
		}
		calcFromWorld();
*/
	}
	
	
//	void calcUsingOriginPoint(AdjImagePair origin) {
//		
//	}
	
	public static void main(String[] args) {
		try {
			AffineAdjust t = new AffineAdjust();
			t.makeData();
			t.calcUsingOriginPoint(t.il.get(0));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
}
