package com.slavi.math;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import com.slavi.math.matrix.JLapack;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.MatrixUtil;

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

	public static Matrix pinv2(Matrix a) {
		JLapack jl = new JLapack();
		Matrix u = new Matrix();
		Matrix vt = new Matrix();
		Matrix s = new Matrix();
		jl.mysvd(a, u, vt, s);

		int minXY = Math.min(a.getSizeX(), a.getSizeY());
		Matrix pinv = new Matrix(a.getSizeY(), a.getSizeX());
		for (int j = pinv.getSizeY() - 1; j >= 0; j--)
			for (int i = pinv.getSizeX() - 1; i >= 0; i--) {
				if (i >= minXY || j >= minXY)
					continue;
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

	public static Matrix pinv(Matrix a) {
		JLapack jl = new JLapack();
		Matrix u = new Matrix();
		Matrix vt = new Matrix();
		Matrix s = new Matrix();
		jl.mysvd(a, u, vt, s);

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

	public static void main(String[] args) {

		Matrix a = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 8; 1 2 3");
//		Matrix a = Matrix.fromOneLineString("1 2 3; 4 5 6; 1 2 3");
		Matrix copyA = a.makeCopy();
/*		JLapack jl = new JLapack();
		Matrix u = new Matrix();
		Matrix v = new Matrix();
		Matrix s = new Matrix();
		System.out.println(a.toMatlabString("A"));
		jl.mysvd(a, u, v, s);
		System.out.println(u.toMatlabString("U"));
		System.out.println(s.toMatlabString("S"));
		System.out.println(v.toMatlabString("V"));

		Matrix tmp1 = new Matrix();
		Matrix tmp2 = new Matrix();
		Matrix tmp3 = new Matrix();

		v.transpose(tmp1);
		pseudoInverseS(s, tmp2);
		System.out.println(tmp2.toMatlabString("S+"));
		tmp1.mMul(tmp2, tmp3);
		u.transpose(tmp1);
		tmp3.mMul(tmp1, tmp2);
		*/
		Matrix tmp1 = new Matrix();
		Matrix tmp2 = pinv2(a);
		Matrix tmp3 = new Matrix();
		System.out.println(tmp2.toMatlabString("P+"));
		copyA.mMul(tmp2, tmp1);
		tmp1.mMul(copyA, tmp3);
		System.out.println(tmp3.toMatlabString("A * P+ * A = A"));
		tmp3.mSub(copyA, tmp3);
		System.out.println(tmp3.is0(1E-5));
/*
		u.mMul(s, tmp1);
		tmp1.mMul(v, tmp2);
		System.out.println(tmp2.toMatlabString("A"));*/
	}

	public static void main0(String[] args) {
		Matrix a = Matrix.fromOneLineString("1 2 3; 4 5 6; 7 8 9");
		BlockRealMatrix aa = MatrixUtil.toApacheMatrix(a);
		SingularValueDecomposition svd = new SingularValueDecomposition(aa);
		System.out.println(svd.getS());

		//svd.getV().multiply(svd)
	}
}
