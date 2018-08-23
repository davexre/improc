package com.slavi.math;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.ejml.data.DMatrixRBlock;
import org.ejml.simple.SimpleSVD;

import com.slavi.math.matrix.JLapack;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.MatrixUtil;
import com.test.math.SVD_Working;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;

public class TestMySvd2 {

	public static Matrix pseudoInverseS(Matrix s, Matrix dest) {
		if (dest == null)
			dest = new Matrix(s.getSizeY(), s.getSizeX());
		else
			dest.resize(s.getSizeY(), s.getSizeX());
		for (int i = s.getSizeX() - 1; i >= 0; i--)
			for (int j = s.getSizeY() - 1; j >= 0; j--) {
				double v = 0;
				if (i == j) {
					v = s.getItem(i, j);
					if (Math.abs(v) > JLapack.EPS)
						v = 1.0 / v;
					else
						v = 0;
				}
				dest.setItem(j, i, v);
			}
		return dest;
	}

	public static Matrix pinvOld(Matrix u, Matrix s, Matrix vt) {
		Matrix tmp1 = new Matrix();
		Matrix tmp2 = new Matrix();
		Matrix tmp3 = new Matrix();

		vt.transpose(tmp1);
		pseudoInverseS(s, tmp2);
		tmp1.mMul(tmp2, tmp3);
		u.transpose(tmp1);
		tmp3.mMul(tmp1, tmp2);
		return tmp2;
	}

	public static Matrix pinv(Matrix a) {
		JLapack jl = new JLapack();
		Matrix u = new Matrix();
		Matrix vt = new Matrix();
		Matrix s = new Matrix();
		jl.mysvd(a, u, vt, s);
		return pinvOld(u, s, vt);
	}

	public static Matrix pinv(Matrix u, Matrix s, Matrix vt) {
		Matrix pinv = new Matrix(u.getSizeY(), vt.getSizeX());
		int minXY = Math.min(pinv.getSizeX(), pinv.getSizeY());
		for (int j = pinv.getSizeY() - 1; j >= 0; j--)
			for (int i = minXY - 1; i >= 0; i--) {
				double d = s.getItem(i, i);
				if (Math.abs(d) < JLapack.EPS)
					continue;
				d = vt.getItem(j, i) / d;

				for (int k = pinv.getSizeX() - 1; k >= 0; k--) {
					pinv.itemAdd(k, j, d * u.getItem(i, k));
				}
			}
		return pinv;
	}

	public static void printSVD(Matrix a, Matrix u, Matrix s, Matrix vt) {
		System.out.println(a.toMatlabString("A"));
		System.out.println(u.toMatlabString("u"));
		System.out.println(s.toMatlabString("s"));
		System.out.println(vt.toMatlabString("vt"));
	}

	public static void checkSVD(Matrix a, Matrix u, Matrix s, Matrix vt) {
		Matrix tmp1 = new Matrix();
		Matrix tmp2 = new Matrix();
		u.mMul(s, tmp1);
		tmp1.mMul(vt, tmp2);
		System.out.println(tmp2.toMatlabString("U * S * V'"));
		tmp2.mSub(a, tmp2);
		System.out.println(tmp2.is0(1E-5));
	}

	public static void checkPInv(Matrix a, Matrix pinv) {
		Matrix tmp1 = new Matrix();
		Matrix tmp2 = new Matrix();
		System.out.println(pinv.toMatlabString("P+"));
		a.mMul(pinv, tmp1);
		tmp1.mMul(a, tmp2);
		System.out.println(tmp2.toMatlabString("A * P+ * A = A"));
		tmp2.mSub(a, tmp2);
		System.out.println(tmp2.is0(1E-5));
	}

	public void svdMy() {
		new JLapack().mysvd(a, u, vt, s);
	}

	public void svdApache() {
		BlockRealMatrix aa = MatrixUtil.toApacheMatrix(a);
		SingularValueDecomposition svd = new SingularValueDecomposition(aa);
		u = MatrixUtil.fromApacheMatrix(svd.getV(), null);
		vt = MatrixUtil.fromApacheMatrix(svd.getUT(), null);
		s = MatrixUtil.fromApacheMatrix(svd.getS(), null);
	}

	public void svdMy2() {
		BlockRealMatrix aa = MatrixUtil.toApacheMatrix(a);
		double au[][] = aa.getData();
		double aw[] = new double[a.getSizeX()];
		double av[][] = new double[a.getSizeX()][a.getSizeX()];
		SVD_Working.svd(au, aw, av);

		for (int i = a.getSizeY() - 1; i >= 0; i--)
			s.setItem(i, i, aw[i]);
		for (int j = u.getSizeY() - 1; j >= 0; j--)
			for (int i = u.getSizeX() - 1; i >= 0; i--)
				u.setItem(i, j, au[i][j]);
		for (int j = vt.getSizeY() - 1; j >= 0; j--)
			for (int i = vt.getSizeX() - 1; i >= 0; i--)
				vt.setItem(i, j, av[i][j]);
	}

	// MTJ - ok
	public void svdMTJ() throws NotConvergedException {
		DenseMatrix aa = new DenseMatrix(a.toArray());
		SVD svd = new SVD(a.getSizeX(), a.getSizeY());
		svd.factor(aa);

		double SS[] = svd.getS();
		for (int i = SS.length - 1; i >= 0; i--)
			s.setItem(i, i, svd.getS()[i]);
		for (int j = u.getSizeY() - 1; j >= 0; j--)
			for (int i = u.getSizeX() - 1; i >= 0; i--)
				u.setItem(i, j, svd.getVt().get(i, j));
		Matrix vt = new Matrix(a.getSizeX(), a.getSizeX());
		for (int j = vt.getSizeY() - 1; j >= 0; j--)
			for (int i = vt.getSizeX() - 1; i >= 0; i--)
				vt.setItem(i, j, svd.getU().get(i, j));
	}

	// Jama - NO
	public void svdJama() {
		Jama.Matrix aa = new Jama.Matrix(a.toArray());
		Jama.SingularValueDecomposition svd = new Jama.SingularValueDecomposition(aa);

		s = Matrix.fromArray(svd.getS().getArray());
		u = Matrix.fromArray(svd.getV().transpose().getArray());
		vt = Matrix.fromArray(svd.getU().transpose().getArray());
	}

	// EJML
	public void svdEJML_NONO() {
		DMatrixRBlock aa = new DMatrixRBlock(a.getSizeX(), a.getSizeY());
		for (int j = a.getSizeY() - 1; j >= 0; j--)
			for (int i = a.getSizeX() - 1; i >= 0; i--)
				aa.set(i, j, a.getItem(i, j));
		SimpleSVD svd = new SimpleSVD(aa, false);
/*
		SimpleBase<SimpleBase<T>> uu = svd.getU();
		s = Matrix.fromArray(svd.getS().getArray());
		u = Matrix.fromArray(svd.getV().transpose().getArray());
		vt = Matrix.fromArray(svd.getU().transpose().getArray());
		*/
	}

	Matrix a =
//			Matrix.fromOneLineString("1 2 3 1; 4 5 6 1; 7 8 8 1");
//			Matrix.fromOneLineString("1 2 3 1; 4 5 6 1; 7 8 9 1");
//			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8; 1 2 3");
//			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8");
//			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 9; 9 2 3");
			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 9");
	Matrix copyA = a.makeCopy();

	Matrix s = new Matrix(a.getSizeX(), a.getSizeY());
	Matrix u = new Matrix(a.getSizeY(), a.getSizeY());
	Matrix vt = new Matrix(a.getSizeX(), a.getSizeX());

	public void doIt(String[] args) throws Exception {
		System.out.println(a.toMatlabString("A"));

//		svdMy();
//		svdMy2();
		svdApache();
//		svdJama();
//		svdMTJ();
//		svdEJML_NONO();

		Matrix pinv = pinvOld(u,s,vt);
		printSVD(copyA, u, s, vt);
		System.out.println();
		checkPInv(copyA, pinv);
		checkSVD(copyA, u, s, vt);
	}

	public static void main(String[] args) throws Exception {
		new TestMySvd2().doIt(args);
		System.out.println("Done.");
	}
}
