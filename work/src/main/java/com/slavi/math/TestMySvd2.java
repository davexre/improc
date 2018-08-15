package com.slavi.math;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.ejml.data.DMatrixRBlock;
import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleMatrix;
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

	public static void mainMy(String[] args) {
		//Matrix a = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8; 1 2 3");
		Matrix a = Matrix.fromOneLineString("1 2 3 1; 4 5 6 2; 7 8 8 3");
		Matrix copyA = a.makeCopy();

		Matrix u = new Matrix();
		Matrix s = new Matrix();
		Matrix vt = new Matrix();
		JLapack jl = new JLapack();
		jl.mysvd(a, u, vt, s);

		Matrix pinv = pinv(u,s,vt);
		checkPInv(copyA, pinv);
		checkSVD(copyA, u, s, vt);
	}

	public static void mainApache(String[] args) {
		//Matrix a = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8; 1 2 3");
		Matrix a = Matrix.fromOneLineString("1 2 3 1; 4 5 6 2; 7 8 8 3");
		Matrix copyA = a.makeCopy();
		System.out.println(a.toMatlabString("A"));

		BlockRealMatrix aa = MatrixUtil.toApacheMatrix(a);
		SingularValueDecomposition svd = new SingularValueDecomposition(aa);
		Matrix u = MatrixUtil.fromApacheMatrix(svd.getV(), null);
		System.out.println(u.toMatlabString("u"));
		Matrix vt = MatrixUtil.fromApacheMatrix(svd.getUT(), null);
		System.out.println(vt.toMatlabString("vt"));
		Matrix s = MatrixUtil.fromApacheMatrix(svd.getS(), null);
		System.out.println(s.toMatlabString("s"));

		Matrix pinv = pinv(u,s,vt);
		checkPInv(copyA, pinv);
	}

	public static void mainMy2(String[] args) {
		Matrix a = Matrix.fromOneLineString("1 2 3 1; 4 5 6 1; 7 8 8 1");
		Matrix copyA = a.makeCopy();

		BlockRealMatrix aa = MatrixUtil.toApacheMatrix(a);
		double au[][] = aa.getData();
		double aw[] = new double[a.getSizeX()];
		double av[][] = new double[a.getSizeX()][a.getSizeX()];
		SVD_Working.svd(au, aw, av);

		Matrix s = new Matrix(a.getSizeX(), a.getSizeY());
		for (int i = a.getSizeY() - 1; i >= 0; i--)
			s.setItem(i, i, aw[i]);
		Matrix u = new Matrix(a.getSizeY(), a.getSizeY());
		for (int j = u.getSizeY() - 1; j >= 0; j--)
			for (int i = u.getSizeX() - 1; i >= 0; i--)
				u.setItem(i, j, au[i][j]);
		Matrix vt = new Matrix(a.getSizeX(), a.getSizeX());
		for (int j = vt.getSizeY() - 1; j >= 0; j--)
			for (int i = vt.getSizeX() - 1; i >= 0; i--)
				vt.setItem(i, j, av[i][j]);

		Matrix pinv = pinv(u,s,vt);
		checkPInv(copyA, pinv);
		checkSVD(copyA, u, s, vt);
	}

	// MTJ - ok
	public static void mainMTJ(String[] args) throws NotConvergedException {
		Matrix a = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 9");
		Matrix copyA = a.makeCopy();

		System.out.println(a.toMatlabString("A"));
		DenseMatrix aa = new DenseMatrix(a.toArray());
		SVD svd = new SVD(a.getSizeX(), a.getSizeY());
		svd.factor(aa);

		Matrix s = new Matrix(a.getSizeX(), a.getSizeY());
		double SS[] = svd.getS();
		for (int i = SS.length - 1; i >= 0; i--)
			s.setItem(i, i, svd.getS()[i]);
		System.out.println(s.toMatlabString("s"));
		Matrix u = new Matrix(a.getSizeY(), a.getSizeY());
		for (int j = u.getSizeY() - 1; j >= 0; j--)
			for (int i = u.getSizeX() - 1; i >= 0; i--)
				u.setItem(i, j, svd.getVt().get(i, j));
		System.out.println(u.toMatlabString("u"));
		Matrix vt = new Matrix(a.getSizeX(), a.getSizeX());
		for (int j = vt.getSizeY() - 1; j >= 0; j--)
			for (int i = vt.getSizeX() - 1; i >= 0; i--)
				vt.setItem(i, j, svd.getU().get(i, j));
		System.out.println(vt.toMatlabString("vt"));

		Matrix pinv = pinv(u,s,vt);
		checkPInv(copyA, pinv);
		checkSVD(copyA, u, s, vt);
	}

	// Jama - NO
	public static void mainJama(String[] args) {
		Matrix a = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8");
		Matrix copyA = a.makeCopy();

		System.out.println(a.toMatlabString("A"));
		Jama.Matrix aa = new Jama.Matrix(a.toArray());
		Jama.SingularValueDecomposition svd = new Jama.SingularValueDecomposition(aa);

		Matrix s = Matrix.fromArray(svd.getS().getArray());
		System.out.println(s.toMatlabString("s"));
		Matrix u = Matrix.fromArray(svd.getV().transpose().getArray());
		System.out.println(u.toMatlabString("u"));
		Matrix vt = Matrix.fromArray(svd.getU().transpose().getArray());
		System.out.println(vt.toMatlabString("vt"));

		Matrix pinv = pinv(u,s,vt);
		checkPInv(copyA, pinv);
		checkSVD(copyA, u, s, vt);
	}

	// EJML
	public static void main(String[] args) {
		Matrix a = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8");
		Matrix copyA = a.makeCopy();

		System.out.println(a.toMatlabString("A"));
		DMatrixRBlock aa = new DMatrixRBlock(a.getSizeX(), a.getSizeY());
		for (int j = a.getSizeY() - 1; j >= 0; j--)
			for (int i = a.getSizeX() - 1; i >= 0; i--)
				aa.set(i, j, a.getItem(i, j));
		SimpleSVD svd = new SimpleSVD(aa, false);
/*
		SimpleBase<SimpleBase<T>> uu = svd.getU();
		Matrix s = Matrix.fromArray(svd.getS().getArray());
		System.out.println(s.toMatlabString("s"));
		Matrix u = Matrix.fromArray(svd.getV().transpose().getArray());
		System.out.println(u.toMatlabString("u"));
		Matrix vt = Matrix.fromArray(svd.getU().transpose().getArray());
		System.out.println(vt.toMatlabString("vt"));

		Matrix pinv = pinv(u,s,vt);
		checkPInv(copyA, pinv);
		checkSVD(copyA, u, s, vt);*/
	}
}
