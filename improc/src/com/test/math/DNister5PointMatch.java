package com.test.math;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import com.slavi.math.matrix.JLapack;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.PointsPair;
import com.slavi.math.transform.PointsPairBase;

public class DNister5PointMatch {

	ArrayList<PointsPair> pairs = new ArrayList<PointsPair>();
	
	public void checkSVD(Matrix source, Matrix u, Matrix v, Matrix s) {
		final double precision = 1000000.0;
//		source.printM("Source matrix");
//		u.printM("U");
//		v.printM("V");
//		s.printM("S");
		
		Matrix a = new Matrix();
		Matrix b = new Matrix();
		u.mMul(s, a);
		a.mMul(v, b);
		
//		b.printM("U*S*V'");
		b.mSub(source, b);
//		b.printM("(U*S*V') - source");
		b.rMul(precision);
		if (b.maxAbs() > 1.0)
			System.out.println("INVALID RESULT FROM SVD!!!!!!");
		u.transpose(a);
		u.mMul(a, b);
		for (int i = b.getSizeX() - 1; i >= 0; i--)
			b.setItem(i, i, b.getItem(i, i) - 1.0);
		b.rMul(precision);
		if (b.maxAbs() > 1.0)
			System.out.println("INVALID RESULT FROM SVD!!!!!!");
	}	
	
	private void copyRow2Matrix(Matrix v, int atRow, Matrix dest) {
		dest.setItem(0, 0, v.getItem(0, atRow));
		dest.setItem(1, 0, v.getItem(1, atRow));
		dest.setItem(2, 0, v.getItem(2, atRow));
		
		dest.setItem(0, 1, v.getItem(3, atRow));
		dest.setItem(1, 1, v.getItem(4, atRow));
		dest.setItem(2, 1, v.getItem(5, atRow));

		dest.setItem(0, 2, v.getItem(6, atRow));
		dest.setItem(1, 2, v.getItem(7, atRow));
		dest.setItem(2, 2, v.getItem(8, atRow));
	}
	
	public static void svdSortResult(Matrix U, Matrix s, Matrix V) {
		// Check data
//		if ((U.getSizeX() != U.getSizeY()) ||		// TODO: Enable this check
//			(V.getSizeX() != V.getSizeY()) ||
//			(V.getSizeX() != s.getSizeX()) ||
//			(U.getSizeY() != s.getSizeY()) )
//			throw new IllegalArgumentException("Invalid arguments");
		int minXY = Math.min(U.getSizeX(), V.getSizeX());
		// Make all singular values positive
		for (int j = 0; j < minXY; j++) {
			if (s.getItem(j, 0) < 0.0) {
				s.setItem(j, 0, -s.getItem(j, 0));
				for (int i = V.getSizeX() - 1; i >= 0; i--)
					V.setItem(i, j, -V.getItem(i, j));
			}
		}
		// Sort the singular values into decreasing order (insertion sort on
		// singular values, but only one transposition per singular vector)
		for (int j = 0; j < minXY; j++) {
			// Scan for smallest D(I)
			int minJindex = j;
			double minJvalue = s.getItem(j, 0);
			for (int i = j + 1; i < minXY; i++) {
				if (s.getItem(i, 0) > minJvalue) {
					minJindex = i;
					minJvalue = s.getItem(i, 0);						
				}				
			}
			if (minJindex != j) {
				// Swap singular values and vectors
				double tmp = s.getItem(j, 0);
				s.setItem(j, 0, s.getItem(minJindex, 0));
				s.setItem(minJindex, 0, tmp);
				for (int i = V.getSizeX() - 1; i >= 0; i--) {
					tmp = V.getItem(i, j);
					V.setItem(i, j, V.getItem(i, minJindex));
					V.setItem(i, minJindex, tmp);
				}
				for (int i = U.getSizeX() - 1; i >= 0; i--) {   
					tmp = U.getItem(j, i);
					U.setItem(j, i, U.getItem(minJindex, i));
					U.setItem(minJindex, i, tmp);
				}
			}
		}
	}
	
	Matrix X, Y, Z, W;

	private void polymult11(int atY1, int atX1, int atY2, int atX2, Matrix dest) {
		dest.resize(10, 1);
		
		dest.setItem(0, 0, X.getItem(atX1, atY1) * X.getItem(atX2, atY2));
		dest.setItem(1, 0, Y.getItem(atX1, atY1) * Y.getItem(atX2, atY2));
		dest.setItem(2, 0, Z.getItem(atX1, atY1) * Z.getItem(atX2, atY2));
		
		dest.setItem(3, 0, Y.getItem(atX1, atY1) * X.getItem(atX2, atY2) + X.getItem(atX1, atY1) * Y.getItem(atX2, atY2));
		dest.setItem(4, 0, Z.getItem(atX1, atY1) * X.getItem(atX2, atY2) + X.getItem(atX1, atY1) * Z.getItem(atX2, atY2));
		dest.setItem(5, 0, Z.getItem(atX1, atY1) * Y.getItem(atX2, atY2) + Y.getItem(atX1, atY1) * Z.getItem(atX2, atY2));
		
		dest.setItem(6, 0, W.getItem(atX1, atY1) * X.getItem(atX2, atY2) + X.getItem(atX1, atY1) * W.getItem(atX2, atY2));
		dest.setItem(7, 0, W.getItem(atX1, atY1) * Y.getItem(atX2, atY2) + Y.getItem(atX1, atY1) * W.getItem(atX2, atY2));
		dest.setItem(8, 0, W.getItem(atX1, atY1) * Z.getItem(atX2, atY2) + Z.getItem(atX1, atY1) * W.getItem(atX2, atY2));
		
		dest.setItem(9, 0, W.getItem(atX1, atY1) * W.getItem(atX2, atY2));
	}
	
	private void polymult21(int atY, int atX, Matrix a, Matrix dest) {
		dest.resize(20, 1);
		
		dest.setItem( 0, 0, a.getItem(0, 0) * X.getItem(atX, atY));
		dest.setItem( 1, 0, a.getItem(1, 0) * Y.getItem(atX, atY));
		dest.setItem( 2, 0, a.getItem(2, 0) * Z.getItem(atX, atY));
		
		dest.setItem( 3, 0, a.getItem(0, 0) * Y.getItem(atX, atY) + a.getItem(3, 0) * X.getItem(atX, atY));
		dest.setItem( 4, 0, a.getItem(0, 0) * Z.getItem(atX, atY) + a.getItem(4, 0) * X.getItem(atX, atY));
		dest.setItem( 5, 0, a.getItem(1, 0) * X.getItem(atX, atY) + a.getItem(3, 0) * Y.getItem(atX, atY));
		
		dest.setItem( 6, 0, a.getItem(1, 0) * Z.getItem(atX, atY) + a.getItem(5, 0) * Y.getItem(atX, atY));
		dest.setItem( 7, 0, a.getItem(2, 0) * X.getItem(atX, atY) + a.getItem(4, 0) * Z.getItem(atX, atY));
		dest.setItem( 8, 0, a.getItem(2, 0) * Y.getItem(atX, atY) + a.getItem(5, 0) * Z.getItem(atX, atY));

		dest.setItem( 9, 0, a.getItem(3, 0) * Z.getItem(atX, atY) + a.getItem(4, 0) * Y.getItem(atX, atY) + a.getItem(5, 0) * X.getItem(atX, atY));

		dest.setItem(10, 0, a.getItem(0, 0) * W.getItem(atX, atY) + a.getItem(6, 0) * X.getItem(atX, atY));
		dest.setItem(11, 0, a.getItem(1, 0) * W.getItem(atX, atY) + a.getItem(7, 0) * Y.getItem(atX, atY));
		dest.setItem(12, 0, a.getItem(2, 0) * W.getItem(atX, atY) + a.getItem(8, 0) * Z.getItem(atX, atY));

		dest.setItem(13, 0, a.getItem(3, 0) * W.getItem(atX, atY) + a.getItem(6, 0) * Y.getItem(atX, atY) + a.getItem(7, 0) * X.getItem(atX, atY));
		dest.setItem(14, 0, a.getItem(4, 0) * W.getItem(atX, atY) + a.getItem(6, 0) * Z.getItem(atX, atY) + a.getItem(8, 0) * X.getItem(atX, atY));
		dest.setItem(15, 0, a.getItem(5, 0) * W.getItem(atX, atY) + a.getItem(7, 0) * Z.getItem(atX, atY) + a.getItem(8, 0) * Y.getItem(atX, atY));

		dest.setItem(16, 0, a.getItem(6, 0) * W.getItem(atX, atY) + a.getItem(9, 0) * X.getItem(atX, atY));
		dest.setItem(17, 0, a.getItem(7, 0) * W.getItem(atX, atY) + a.getItem(9, 0) * Y.getItem(atX, atY));
		dest.setItem(18, 0, a.getItem(8, 0) * W.getItem(atX, atY) + a.getItem(9, 0) * Z.getItem(atX, atY));
		
		dest.setItem(19, 0, a.getItem(9, 0) * W.getItem(atX, atY));
	}
	
	private static void build10DegreePolynomial(int destY, Matrix src, Matrix dest) {
		int Y = destY << 1;
		int Y1 = Y + 1;
		dest.setItem( 0, destY,                   - src.getItem(0, Y1));
		dest.setItem( 1, destY, src.getItem(0, Y) - src.getItem(1, Y1));
		dest.setItem( 2, destY, src.getItem(1, Y) - src.getItem(2, Y1));
		dest.setItem( 3, destY, src.getItem(2, Y)                     );

		dest.setItem( 4, destY,                   - src.getItem(3, Y1));
		dest.setItem( 5, destY, src.getItem(3, Y) - src.getItem(4, Y1));
		dest.setItem( 6, destY, src.getItem(4, Y) - src.getItem(5, Y1));
		dest.setItem( 7, destY, src.getItem(5, Y)                     );

		dest.setItem( 8, destY,                   - src.getItem(6, Y1));
		dest.setItem( 9, destY, src.getItem(6, Y) - src.getItem(7, Y1));
		dest.setItem(10, destY, src.getItem(7, Y) - src.getItem(8, Y1));
		dest.setItem(11, destY, src.getItem(8, Y) - src.getItem(9, Y1));
		dest.setItem(12, destY, src.getItem(9, Y)                     );
	}

	private static void polymult0b(
			Matrix L, int LX, int LY, int LS,
			Matrix R, int RX, int RY, int RS,
			Matrix D, int DY) { 
		int DS = LS + RS - 1;
		for (int i = 0; i < DS; i++)
			D.setItem(i, DY, 0.0);
		for (int iR = 0; iR < RS; iR++) {
			for (int iL = 0; iL < LS; iL++) {
				D.setItem(iR + iL, DY, D.getItem(iR + iL, DY) +
					R.getItem(RX + iR, RY) * L.getItem(LX + iL, LY));
			}
		}
	}

	public void computeIt() throws Exception {
		if (pairs.size() < 5) 
			throw new ArithmeticException("At least 5 matching points required");
		
		// step 1: get 4 matrices X,Y,Z,W spanning the subspace of solutions

		Matrix Q = new Matrix(9, pairs.size());
		
		for (int i = pairs.size() - 1; i >= 0 ; i--) {
			PointsPair pp = pairs.get(i);
			Q.setItem(0, i, pp.getSourceCoord(0) * pp.getTargetCoord(0));
			Q.setItem(1, i, pp.getSourceCoord(1) * pp.getTargetCoord(0));
			Q.setItem(2, i,                        pp.getTargetCoord(0));
			Q.setItem(3, i, pp.getSourceCoord(0) * pp.getTargetCoord(1));
			Q.setItem(4, i, pp.getSourceCoord(1) * pp.getTargetCoord(1));
			Q.setItem(5, i,                        pp.getTargetCoord(1));
			Q.setItem(6, i, pp.getSourceCoord(0)                       );
			Q.setItem(7, i, pp.getSourceCoord(1)                       );
			Q.setItem(8, i, 1                                          );
		}
		
		Q.printM("Q");
		
		JLapack jl = new JLapack();
		Matrix u = new Matrix();
		Matrix s = new Matrix();
		Matrix v = new Matrix();
		
//		Q.svd1(s, v);
//		svdSortResult(v, s, Q);
//		Q.printM("V");
//		s.printM("s");
//		v.printM("U");

		Matrix backupQ = Q.makeCopy();
//		Q.mysvd(u, v, s);
		jl.mysvd(Q, u, v, s);
		u.printM("U");
		v.printM("V");
		s.printM("S");
		
		checkSVD(backupQ, u, v, s);


		u.resize(9, 9);
		u.load(new BufferedReader(new FileReader(
				DNister5PointMatch.class.getResource(
					"DNister5PointMatch_mat.txt").getFile())));
		u.transpose(v);
		v.printM("V");
		
		X = new Matrix(3, 3);
		Y = new Matrix(3, 3);
		Z = new Matrix(3, 3);
		W = new Matrix(3, 3);
		
		copyRow2Matrix(v, 5, X);
		copyRow2Matrix(v, 6, Y);
		copyRow2Matrix(v, 7, Z);
		copyRow2Matrix(v, 8, W);

		// Check 
		X.printM("X");
		Y.printM("Y");
		Z.printM("Z");
		W.printM("W");

		// step 2: expand the essential matrix constraints
		
		// 2.1 fundamental matrix constraint det(E) = 0
		Matrix a = new Matrix();
		Matrix b = new Matrix();
		Matrix detE = new Matrix();
		Matrix d = new Matrix();
		Matrix e = new Matrix();
		
		polymult11(0, 1, 1, 2, a);
		polymult11(0, 2, 1, 1, b);
		a.mSub(b, a);
		polymult21(2, 0, a, detE);
		
		polymult11(0, 2, 1, 0, a);
		polymult11(0, 0, 1, 2, b);
		a.mSub(b, a);
		polymult21(2, 1, a, d);
		
		detE.mSum(d, detE);
		
		polymult11(0, 0, 1, 1, a);
		polymult11(0, 1, 1, 0, b);
		a.mSub(b, a);
		polymult21(2, 2, a, d);
		
		detE.mSum(d, detE);
		
		// Check detE
		detE.printM("detE");
		
		// 2.2 EE'
		Matrix EEt[][] = new Matrix[3][3];
		for (int i = 2; i >= 0; i--)
			for (int j = 2; j >= 0; j--)
				EEt[i][j] = new Matrix(10,1);
		for (int i = 0; i < 3; i++)
			for (int j = i+1; j < 3; j++) {
				polymult11(i, 0, j, 0, a);
				polymult11(i, 1, j, 1, b);
				a.mSum(b, a);
				polymult11(i, 2, j, 2, b);
				a.mSum(b, EEt[i][j]);
				EEt[i][j].copyTo(EEt[j][i]);				
			}

		for (int k = 0; k < 10; k++) {
			System.out.println("EEt(:,:," + (k+1) + ")");
			for (int j = 0; j < 3; j++) {
				for (int i = 0; i < 3; i++) {
					System.out.print(( ((int)(EEt[i][j].getItem(k, 0) * 10000.0)) / 10000.0) + "\t");
				}
				System.out.println();
			}
			System.out.println();
		}

		
		// 2.3 L = EE'-0.5*trace(EE')I
		Matrix traceEEt = new Matrix(10, 1);
		for (int i = 0; i < 10; i++)
			traceEEt.setItem(i, 0, 0.5 * (
				EEt[0][0].getItem(i, 0) + 
				EEt[1][1].getItem(i, 0) + 
				EEt[2][2].getItem(i, 0) ));

		traceEEt.printM("traceEEt");
		
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 3; j++)
				EEt[j][j].setItem(i, 0, EEt[j][j].getItem(i, 0) - traceEEt.getItem(i, 0));  
		}
		
		// 2.4 essential matrix constraint LE = 0
		Matrix LE[] = new Matrix[10];
		for (int i = 8; i >= 0; i--)
			LE[i] = new Matrix(20, 1);
		LE[9] = detE;
		for (int lc = 0, i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				polymult21(0, j, EEt[i][0], d);
				polymult21(1, j, EEt[i][1], e);
				d.mSum(e, d);
				polymult21(2, j, EEt[i][2], e);
				d.mSum(e, LE[lc]);
				lc++;
			}

		// step 3: build equation system from constraints and reduce it
		System.out.println("LE");
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 20; j++) {
				System.out.print(String.format(Locale.US, "%7.4f ", LE[i].getItem(j, 0)));
			}
			System.out.println("");
		}

		Matrix A = new Matrix(20, 10);
		for (int j = 0; j < 10; j++) {
			// x^3 y^3 x^2y xy^2
			A.setItem(0, j, LE[j].getItem(0, 0));
			A.setItem(1, j, LE[j].getItem(1, 0));
			A.setItem(2, j, LE[j].getItem(3, 0));
			A.setItem(3, j, LE[j].getItem(5, 0));
			
			// x^2z x^2 y^2z y^2 xyz xy
			A.setItem( 4, j, LE[j].getItem( 4, 0));
			A.setItem( 5, j, LE[j].getItem(10, 0));
			A.setItem( 6, j, LE[j].getItem( 6, 0));
			A.setItem( 7, j, LE[j].getItem(11, 0));
			A.setItem( 8, j, LE[j].getItem( 9, 0));
			A.setItem( 9, j, LE[j].getItem(13, 0));
			
			// x [z^2 z 1]
			A.setItem(10, j, LE[j].getItem( 7, 0));
			A.setItem(11, j, LE[j].getItem(14, 0));
			A.setItem(12, j, LE[j].getItem(16, 0));

			// y [z^2 z 1]
			A.setItem(13, j, LE[j].getItem( 8, 0));
			A.setItem(14, j, LE[j].getItem(15, 0));
			A.setItem(15, j, LE[j].getItem(17, 0));

			// 1 [z^3 z^2 z 1]
			A.setItem(16, j, LE[j].getItem( 2, 0));
			A.setItem(17, j, LE[j].getItem(12, 0));
			A.setItem(18, j, LE[j].getItem(18, 0));
			A.setItem(19, j, LE[j].getItem(19, 0));
		}

		System.out.println("A");
		for (int j = 0; j < A.getSizeY(); j++) {
			for (int i = 0; i < A.getSizeX(); i++) {
				System.out.print(String.format(Locale.US, "%7.4f ", A.getItem(i, j)));
			}
			System.out.println("");
		}
		
//		A.printM("A");
		
		// Gauss-Jordan elimination with partial pivoting on a 10x20 matrix
		for (int aRow = 0; aRow < 9; aRow++) {
			// pivoting
			double theValue = Math.abs(A.getItem(aRow, aRow));
			double tmp;
			int theIndex = aRow;
			for (int j = aRow + 1; j < 10; j++) 
				if ((tmp = Math.abs(A.getItem(aRow, j))) > theValue) {
					theValue = tmp; 
					theIndex = j;
				}
			// Swap rows
			if (aRow != theIndex)
				A.exchangeY(aRow, theIndex);

			theValue = A.getItem(aRow, aRow);
			// normalize current row
			if (theValue != 0.0)
				for (int i = 0; i < 20; i++)
					A.setItem(i, aRow, A.getItem(i, aRow) / theValue);
			// nullify rest of column
			for (int j = aRow + 1; j < 10; j++) {
				tmp = A.getItem(aRow, j);
				for (int i = 0; i < 20; i++)
					A.setItem(i, j, A.getItem(i, j) - A.getItem(i, aRow) * tmp);
			}
			// diagonalize row 5-9
			for (int j = 4; j < aRow; j++) {
				tmp = A.getItem(aRow, j);
				for (int i = 0; i < 20; i++)
					A.setItem(i, j, A.getItem(i, j) - A.getItem(i, aRow) * tmp);
			}
		}
		// test for rank deficiency (e.g., pure translation)
		if (Math.abs(A.getItem(9, 9)) < 1e-7) {
			for (int i = 0; i < 20; i++) {
				A.setItem(i, 8, A.getItem(i, 9));
				A.setItem(i, 9, 0.0);
			}
		} else {
			// complete elimination for last row
			double tmp = A.getItem(9, 9);
			for (int i = 0; i < 20; i++)
				A.setItem(i, 9, A.getItem(i, 9) / tmp);
			for (int j = 4; j < 9; j++) {
				tmp = A.getItem(9, j);
				for (int i = 0; i < 20; i++)
					A.setItem(i, j, A.getItem(i, j) - A.getItem(i, 9) * tmp);
			}
		}
		
		// display/return only required part
		Matrix A2 = new Matrix(10, 6);
		System.out.println("A2");
		for (int i = 0, i2 = 10; i < 10; i++, i2++)
			for (int j = 0, j2 = 4; j < 6; j++, j2++)
				A2.setItem(i, j, A.getItem(i2, j2));
		
		for (int j = 0; j < A2.getSizeY(); j++) {
			for (int i = 0; i < A2.getSizeX(); i++) {
				System.out.print(String.format(Locale.US, "%7.4f ", A2.getItem(i, j)));
				//System.out.print(A2.getItem(i, j) + " ");
			}
			System.out.println(";");
		}
		
		// step 4: build 10th-degree polynomial
		Matrix B = new Matrix(13, 3);
		build10DegreePolynomial(0, A2, B);
		build10DegreePolynomial(1, A2, B);
		build10DegreePolynomial(2, A2, B);
		
		B.printM("B");
		
		Matrix p1 = new Matrix(8, 3);
		Matrix p2 = new Matrix(8, 3);
				
		polymult0b(B, 4, 0, 4, B, 8, 1, 5, p1, 0);
		polymult0b(B, 8, 0, 5, B, 4, 1, 4, p2, 0);
		
		polymult0b(B, 8, 0, 5, B, 0, 1, 4, p1, 1);
		polymult0b(B, 0, 0, 4, B, 8, 1, 5, p2, 1);
		
		polymult0b(B, 0, 0, 4, B, 4, 1, 4, p1, 2);
		polymult0b(B, 4, 0, 4, B, 0, 1, 4, p2, 2);
		p1.mSub(p2, p1);
				
		p1.printM("PPPP");

		Matrix pn = new Matrix(11, 1);
		Matrix pn2 = new Matrix(11, 1);
		
		polymult0b(p1, 0, 0, 8, B, 0, 2, 4, pn, 0);
		polymult0b(p1, 0, 1, 8, B, 4, 2, 4, pn2, 0);
		pn.mSum(pn2, pn);
		polymult0b(p1, 0, 2, 7, B, 8, 2, 5, pn2, 0);
		pn.mSum(pn2, pn);
		
		pn.printM("PN");
		
		Matrix z = new Matrix();
		jl.roots(pn, z);
		
		z.printM("Z");
		
		// Count the real roots
		int realRoots = 0;
		for (int i = z.getSizeX() - 1; i >= 0; i--)
			if (z.getItem(i, 1) == 0.0)
				realRoots++;
		if (realRoots == 0) {
			throw new ArithmeticException("No real roots!");
		}
		
		Matrix z7 = new Matrix(realRoots, 8);
		for (int i7 = realRoots - 1, i = z.getSizeX() - 1; i >= 0; i--) {
			if (z.getItem(i, 1) == 0.0) {
				double t1 = z.getItem(i, 0);
				double t2 = t1 * t1;
				double t3 = t1 * t2;
				double t4 = t2 * t2;
				z7.setItem(i7, 0, t4 * t3);
				z7.setItem(i7, 1, t4 * t2);
				z7.setItem(i7, 2, t4 * t1);
				z7.setItem(i7, 3, t4);
				z7.setItem(i7, 4, t3);
				z7.setItem(i7, 5, t2);
				z7.setItem(i7, 6, t1);
				z7.setItem(i7, 7, 1.0);
				i7--;
			}
		}
		
		z7.printM("z7");
		
		Matrix pnum = new Matrix(realRoots, 3);
		for (int i = realRoots - 1; i >= 0; i--) {
			double sum1 = 0.0;
			double sum2 = 0.0;
			for (int j = 0; j < 8; j++) {
				sum1 += p1.getItem(j, 0) * z7.getItem(i, j);
				sum2 += p1.getItem(j, 1) * z7.getItem(i, j);
			}
			pnum.setItem(i, 0, sum1);
			pnum.setItem(i, 1, sum2);
			sum1 = 0.0;
			for (int j = 1; j < 8; j++)
				sum1 += p1.getItem(j - 1, 2) * z7.getItem(i, j);
			pnum.setItem(i, 2, sum1);
		}
		pnum.printM("pnum");

		Matrix xy = new Matrix(realRoots,2);
		for (int i = xy.getSizeX() - 1; i >= 0; i--) {
			xy.setItem(i, 0, pnum.getItem(i, 0) / pnum.getItem(i, 2));
			xy.setItem(i, 1, pnum.getItem(i, 1) / pnum.getItem(i, 2));
		}

		xy.printM("xy");
		
		// compute essential matrix
		Matrix E[] = new Matrix[realRoots];
		for (int ei = realRoots - 1; ei >= 0; ei--) {
			double xi = xy.getItem(ei, 0);
			double yi = xy.getItem(ei, 1);
			double zi = z.getItem(ei, 0);
			Matrix Ei = new Matrix(3, 3);
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++) {
					Ei.setItem(i, j, 
							xi * X.getItem(i, j) +
							yi * Y.getItem(i, j) +
							zi * Z.getItem(i, j) +
							W.getItem(i, j));
				}
			E[ei] = Ei;
		}
		
		for (int i = 0; i < E.length; i++)
			E[i].printM("E[" + i + "]");
	}
	
	public void doTheJob() throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(
				DNister5PointMatch.class.getResource(
					"DNister5PointMatch.txt").getFile()));
		fin.readLine();
		while (fin.ready()) {
			StringTokenizer st = new StringTokenizer(fin.readLine());
			Matrix src = new Matrix(2, 1);
			Matrix dest = new Matrix(2, 1);
			src.setItem(0, 0, Double.parseDouble(st.nextToken()));
			src.setItem(1, 0, Double.parseDouble(st.nextToken()));
			dest.setItem(0, 0, Double.parseDouble(st.nextToken()));
			dest.setItem(1, 0, Double.parseDouble(st.nextToken()));
			PointsPair pp = new PointsPairBase(src, dest, 1.0);
			pairs.add(pp);
		}
		fin.close();
		
		computeIt();
	}

	public static void main(String[] args) throws Exception {
		(new DNister5PointMatch()).doTheJob();
	}

}
