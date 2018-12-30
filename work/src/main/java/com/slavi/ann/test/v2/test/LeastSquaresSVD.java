package com.slavi.ann.test.v2.test;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import com.slavi.math.MathUtil;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.JLapack;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.MatrixUtil;

public class LeastSquaresSVD extends LeastSquaresAdjust {
	public LeastSquaresSVD(int numCoefsPerCoordinate, int numCoordinates) {
		super(numCoefsPerCoordinate, numCoordinates);
	}

	public LeastSquaresSVD(int numPoints) {
		super(numPoints);
	}

	static Matrix pseudoInverseS(Matrix s, Matrix dest) {
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

	public boolean calculate() {
		if (log.isInfoEnabled()) {
			double sumP = getSumP();
			if (sumP == 0.0)
				sumP = 1E-100;
			log.info("Measurements: " + measurementCount +
					", [P]: " + MathUtil.d4(getSumP()) +
					", Sqrt([PLL]/[P]): " + MathUtil.d4(getRootMeanSquareError())
			);
		}
		if (log.isTraceEnabled()) {
			log.trace("Normal matrix\n" + nm.toString());
			log.trace("APL\n" + apl.toString());
		}

		Matrix u = new Matrix();
		Matrix vt = new Matrix();
		Matrix s = new Matrix();
		Matrix A = nm.toMatrix();
//		new JLapack().mysvd(A, u, vt, s);

		BlockRealMatrix aa = MatrixUtil.toApacheMatrix(A);
		SingularValueDecomposition svd = new SingularValueDecomposition(aa);
		u = MatrixUtil.fromApacheMatrix(svd.getV(), null);
		vt = MatrixUtil.fromApacheMatrix(svd.getUT(), null);
		s = MatrixUtil.fromApacheMatrix(svd.getS(), null);

		Matrix tmp1 = new Matrix();
		Matrix tmp2 = new Matrix();
		Matrix tmp3 = new Matrix();

		vt.transpose(tmp1);
		pseudoInverseS(s, tmp2);
		tmp1.mMul(tmp2, tmp3);
		u.transpose(tmp1);
		tmp3.mMul(tmp1, tmp2);

		// calculateUnknowns
		unknown.make0();
		for (int i = numCoefsPerCoordinate - 1; i >= 0; i--)
			for (int j = numCoefsPerCoordinate - 1; j >= 0; j--)
				for (int k = numCoordinates - 1; k >= 0; k--)
					unknown.itemAdd(k, i, tmp2.getItem(i, j) * apl.getItem(k, j));
		if (log.isDebugEnabled()) {
			log.debug("UNKNOWNS\n" + unknown.toString());
		}

		return true;
	}
}
