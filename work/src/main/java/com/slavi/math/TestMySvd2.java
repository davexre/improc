package com.slavi.math;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.ejml.data.DMatrixRBlock;
import org.ejml.simple.SimpleSVD;

import com.slavi.math.matrix.DiagonalMatrix;
import com.slavi.math.matrix.JLapack;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.MatrixFactorization;
import com.slavi.math.matrix.SVD_Obsolete;
import com.slavi.util.MatrixUtil;
import com.test.math.SVD_Working;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;

public class TestMySvd2 {

	static final double eps = 1e-10;

	public static Matrix inverseDiagonalMatrix(Matrix s, Matrix dest) {
		if (dest == null)
			dest = new Matrix(s.getSizeY(), s.getSizeX());
		else
			dest.resize(s.getSizeY(), s.getSizeX());
		for (int i = s.getSizeX() - 1; i >= 0; i--)
			for (int j = s.getSizeY() - 1; j >= 0; j--) {
				double v = 0;
				if (i == j) {
					v = s.getItem(i, j);
					if (Math.abs(v) > eps)
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
		inverseDiagonalMatrix(s, tmp2);
		tmp1.mMul(tmp2, tmp3);
		u.transpose(tmp1);
		tmp3.mMul(tmp1, tmp2);
		return tmp2;
	}

	public static Matrix pinv(Matrix u, Matrix s, Matrix vt) {
		int minXY = Math.min(u.getSizeX(), vt.getSizeX());
		DiagonalMatrix ss = new DiagonalMatrix(vt.getSizeX(), u.getSizeX());
		for (int i = 0; i < minXY; i++)
			ss.setItem(i, i, s.getItem(i, i));
		vt.transpose();
		return MatrixFactorization.pinv(u, ss, vt, null);

/*
		int min = Math.min(u.getSizeX(), vt.getSizeX());
		vt.transpose();
		Matrix pinv = new Matrix(u.getSizeX(), vt.getSizeX());
		pinv.make0();

		for (int i = min - 1; i >= 0; i--) {
			double d = s.getItem(i, i);
			if (Math.abs(d) < eps)
				continue;
			for (int j = pinv.getSizeY() - 1; j >= 0; j--) {
				double scale = vt.getItem(i, j) / d;
				for (int k = pinv.getSizeX() - 1; k >= 0; k--) {
					pinv.itemAdd(k, j, scale * u.getItem(i, k));
				}
			}
		}
		return pinv;*/
	}

	public static Matrix pinvLEFT(Matrix u, Matrix s, Matrix vt) {
		int minXY = Math.min(u.getSizeX(), vt.getSizeX());
		DiagonalMatrix ss = new DiagonalMatrix(vt.getSizeX(), u.getSizeX());
		for (int i = 0; i < minXY; i++)
			ss.setItem(i, 0, s.getItem(i, i));
		vt.transpose();
		return MatrixFactorization.pinv(vt, ss, u, null);
/*
		vt.transpose();
		int min = Math.min(u.getSizeX(), vt.getSizeX());
		Matrix pinv = new Matrix(vt.getSizeX(), u.getSizeX());
		pinv.make0();

		for (int i = min - 1; i >= 0; i--) {
			double d = s.getItem(i, i);
			if (Math.abs(d) < eps)
				continue;
			for (int j = pinv.getSizeY() - 1; j >= 0; j--) {
				double scale = vt.getItem(i, j) / d;
				for (int k = pinv.getSizeX() - 1; k >= 0; k--) {
					pinv.itemAdd(k, j, scale * u.getItem(i, k));
				}
			}
		}
		return pinv;*/
	}

	public static void printSVD(Matrix a, Matrix u, Matrix s, Matrix vt) {
		System.out.println(a.toMatlabString("A"));
		System.out.println(u.toMatlabString("u"));
		System.out.println(s.toMatlabString("s"));
		System.out.println(vt.toMatlabString("vt"));
	}

	public static void checkSVD(Matrix a, Matrix u, Matrix s, Matrix vt) {
		double e;
		Matrix tmp1 = new Matrix();
		Matrix tmp2 = new Matrix();

		u.transpose(tmp1);
		u.mMul(tmp1, tmp2);
		e = tmp2.getSquaredDeviationFromE();
		if (e > eps)
			throw new Error("U * UT != I => " + e);

		u.transpose(tmp1);
		tmp1.mMul(u, tmp2);
		e = tmp2.getSquaredDeviationFromE();
		if (e > eps)
			throw new Error("UT * U != I => " + e);

		vt.transpose(tmp1);
		vt.mMul(tmp1, tmp2);
		e = tmp2.getSquaredDeviationFromE();
		if (e > eps)
			throw new Error("VT * V != I => " + e);

		vt.transpose(tmp1);
		tmp1.mMul(vt, tmp2);
		e = tmp2.getSquaredDeviationFromE();
		if (e > eps)
			throw new Error("V * VT != I => " + e);

		u.mMul(s, tmp1);
		tmp1.mMul(vt, tmp2);
		tmp2.printM("T2");
		tmp2.mSub(a, tmp2);
		e = tmp2.getSquaredDeviationFrom0();
		if (e > eps)
			throw new Error("A - U*S*VT != 0 => " + e);

		Matrix pinv = pinv(u,s,vt);
		pinv.printM("P+");
		a.mMul(pinv, tmp1);
		tmp1.mMul(a, tmp2);
		tmp2.mSub(a, tmp2);
		e = tmp2.getSquaredDeviationFrom0();
		if (e > eps)
			throw new Error("A * P+ * A - A != 0 => " + e);
/*
		pinv = pinvLEFT(u,s,vt);
		pinv.printM("LEFT P+");
		pinv.mMul(a, tmp1);
		tmp1.mMul(pinv, tmp2);
		tmp2.mSub(a, tmp2);
		e = tmp2.getSquaredDeviationFrom0();
		if (e > eps)
			throw new Error("P+ * A * P+ - A != 0 => " + e);*/
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

	public void svdGolubReinsch() {
		Matrix v = new Matrix();
		DiagonalMatrix q = new DiagonalMatrix();
		com.slavi.math.matrix.MatrixFactorization.svd(a, u, q, v, true);

		v.transpose(vt);
		q.toMatrix(s);
	}

	public void svdObsolete1() {
		SVD_Obsolete svd = new SVD_Obsolete(a.getSizeX(), a.getSizeY());
		a.copyTo(svd);
		Matrix w = new Matrix();
		svd.svd(w, u);
		u.transpose();
		svd.copyTo(vt);
		s.resize(vt.getSizeY(), u.getSizeX());
		s.make0();
		for (int i = 0; i < w.getSizeX(); i++) {
			double d = w.getItem(i, 0);
			s.setItem(i, i, d);
		}
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

		for (int j = u.getSizeY() - 1; j >= 0; j--)
			for (int i = u.getSizeX() - 1; i >= 0; i--)
				u.setItem(i, j, av[j][i]);

		for (int j = vt.getSizeY() - 1; j >= 0; j--)
			for (int i = vt.getSizeX() - 1; i >= 0; i--)
				vt.setItem(i, j, au[i][j]);

		for (int i = a.getSizeY() - 1; i >= 0; i--)
			s.setItem(i, i, aw[i]);
	}

	// MTJ - ok
	public void svdMTJ() throws NotConvergedException {
		DenseMatrix aa = new DenseMatrix(a.toArray());
		no.uib.cipr.matrix.SVD svd = new no.uib.cipr.matrix.SVD(a.getSizeX(), a.getSizeY());
		svd.factor(aa);

		u.resize(svd.getVt().numColumns(), svd.getVt().numRows());
		for (int j = u.getSizeY() - 1; j >= 0; j--)
			for (int i = u.getSizeX() - 1; i >= 0; i--)
				u.setItem(i, j, svd.getVt().get(i, j));

		vt.resize(svd.getU().numColumns(), svd.getU().numRows());
		for (int j = vt.getSizeY() - 1; j >= 0; j--)
			for (int i = vt.getSizeX() - 1; i >= 0; i--)
				vt.setItem(i, j, svd.getU().get(i, j));

		double SS[] = svd.getS();
		s.resize(vt.getSizeY(), u.getSizeX());
		s.make0();
		for (int i = SS.length - 1; i >= 0; i--)
			s.setItem(i, i, SS[i]);
	}

	// Jama - NO
	public void svdJama() {
		Jama.Matrix aa = new Jama.Matrix(a.toArray());
		Jama.SingularValueDecomposition svd = new Jama.SingularValueDecomposition(aa);

		u = Matrix.fromArray(svd.getV().transpose().getArray());
		vt = Matrix.fromArray(svd.getU().getArray());
		s.resize(vt.getSizeY(), u.getSizeX());
		s.make0();
		Matrix SS = Matrix.fromArray(svd.getS().getArray());
		for (int i = s.getSizeX() - 1; i >= 0; i--)
			for (int j = s.getSizeY() - 1; j >= 0; j--)
				s.setItem(i, j, SS.getItem(i, j));
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

	Matrix a = new Matrix();
	Matrix copyA = new Matrix();

	Matrix s = new Matrix();
	Matrix u = new Matrix();
	Matrix vt = new Matrix();

	Matrix testMatrices[] = new Matrix[] {
			Matrix.fromOneLineString("1 2 3; 1 2 3; 1 2 7; 1 2 8"),
			Matrix.fromOneLineString("1 0 0; 0 1 1"),
			Matrix.fromOneLineString("1 0; 0 1; 0 1"),
			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8"),
			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 9"),
			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8; 1 2 3"),
			Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 9; 9 2 3"),
			Matrix.fromOneLineString("1 2 3 1; 4 5 6 1; 7 8 8 1"),
			Matrix.fromOneLineString("1 2 3 1; 4 5 6 1; 7 8 9 1"),
		};

	public void doIt(String[] args) throws Exception {
		try {
			for (Matrix i : testMatrices) {
				i.copyTo(a);
				i.copyTo(copyA);
				svdGolubReinsch();
//				svdObsolete1();
//				svdMy();
//				svdMy2();	// Fails
//				svdApache();
//				svdMTJ();
//				svdJama();
//				svdEJML_NONO();
				checkSVD(copyA, u, s, vt);
				printSVD(copyA, u, s, vt);
			}
			System.out.println("All ok.");
		} catch (Throwable t) {
			t.printStackTrace();
			printSVD(copyA, u, s, vt);
		}
	}

	public static void main(String[] args) throws Exception {
		new TestMySvd2().doIt(args);
		System.out.println("Done.");
	}
}
