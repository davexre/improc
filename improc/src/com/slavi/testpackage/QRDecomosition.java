package com.slavi.testpackage;

import java.io.BufferedReader;
import java.io.FileReader;

import com.slavi.matrix.Matrix;

public class QRDecomosition {

	public static final double SIGN(double a, double b) {
		return ((b) >= 0. ? Math.abs(a) : -Math.abs(a));
	}

	public static void QR(Matrix A, Matrix R) {
		int minXY = Math.min(A.getSizeX(), A.getSizeY());
		R.resize(minXY, 1);
		for (int atIndex = 0; atIndex < minXY; atIndex++) {
			// DNRM2: Returns the euclidean norm of a vector DNRM2 := sqrt( x'*x )
			double alpha = A.getItem(atIndex, atIndex);
			double ssq = 1.0;
			double scale = 0.0;
			for (int i = atIndex + 1; i < A.getSizeX(); i++) {
				double absM = Math.abs(A.getItem(i, atIndex));
				if (absM == 0.0) 
					continue;
				if (scale < absM) {
					ssq = 1.0 + ssq * Math.pow(scale / absM, 2);
					scale = absM;
				} else
					ssq += Math.pow(absM / scale, 2);
			}
			double xnorm = scale / Math.sqrt(ssq);
			// End DNRM2
			// TODO: DLARFG:102 XNORM, BETA may be inaccurate; scale X and recompute them
			double beta = -SIGN(alpha, Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2)));
			double tau = (beta - alpha) / beta;
			scale = 1.0 / (alpha - beta);
			for (int i = atIndex + 1; i < A.getSizeX(); i++) 
				A.setItem(i, atIndex, scale * A.getItem(i, atIndex));
			A.setItem(atIndex, atIndex, beta);  // How to return BETA ??
			
			if (atIndex + 1 < A.getSizeX()) {
				// Apply H(i) to A(i:m,i+1:n) from the left
				double AII = A.getItem(atIndex, atIndex); 
				A.setItem(atIndex, atIndex, 1.0);
				for (int j = atIndex; j < A.getSizeY(); j++) {
					double sum = 0.0;
					for (int i = atIndex + 1; i < A.getSizeX(); i++) {
						sum += A.getItem(i, j) * A.getItem(i - 1, atIndex);
					}
					for (int i = atIndex + 1; i < A.getSizeX(); i++) {
						A.setItem(i, j, A.getItem(i, j) - tau * sum * A.getItem(i - 1, atIndex));
					}				
				}
				A.setItem(atIndex, atIndex, AII);			
			}
			R.setItem(atIndex, 0, tau);
		}
	}
	
	public static void printM(Matrix m, String title) {
		System.out.println(title);
		System.out.println(m.toString());
	}
	
	public static void main(String[] args) throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(
				SVD_Test.class.getResource(
					"SVD-A.txt").getFile()));
		Matrix a = new Matrix(3, 3);
		a.load(fin);
		fin.close();
		Matrix aa = a.makeCopy();
		Matrix u = new Matrix();
		Matrix v = new Matrix();
		Matrix s = new Matrix();

		printM(a, "A");
		QR(a, s);
		printM(a, "Q");
		printM(s, "R");
	}

}
