package com.slavi.math.adjust;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import com.slavi.math.matrix.Matrix;

public class MatrixUtil {
	public static ArrayRealVector toApacheVector(Matrix m) {
		if (m.getSizeX() == 1 || m.getSizeY() == 1) {
			double mm[] = new double[m.getVectorSize()];
			for (int i = m.getVectorSize() - 1; i >= 0; i--)
				mm[i] = m.getVectorItem(i);
			return new ArrayRealVector(mm);
		} else {
			throw new Error("Invalid size [" + m.getSizeX() + ", " + m.getSizeY() + "]");
		}
	}

	public static BlockRealMatrix toApacheMatrix(Matrix m) {
		return new BlockRealMatrix(m.toArray());
	}

	public static Matrix fromApacheMatrix(RealMatrix m, Matrix dest) {
		if (dest == null)
			dest = new Matrix(m.getColumnDimension(), m.getRowDimension());
		else
			dest.resize(m.getColumnDimension(), m.getRowDimension());
		for (int i = dest.getSizeX() - 1; i >= 0; i--)
			for (int j = dest.getSizeY() - 1; j >= 0; j--)
				dest.setItem(i, j, m.getEntry(j, i));
		return dest;
	}

	public static Matrix fromApacheVector(RealVector v, Matrix dest) {
		if (dest == null)
			dest = new Matrix(v.getDimension(), 1);
		else
			dest.resize(v.getDimension(), 1);
		for (int i = dest.getVectorSize() - 1; i >= 0; i--)
				dest.setItem(i, 0, v.getEntry(i));
		return dest;
	}
}
