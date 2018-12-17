package com.slavi.util;

import java.io.Serializable;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.slavi.math.matrix.Matrix;

public class ApacheMatrixWrapper extends AbstractRealMatrix implements Serializable {

	Matrix m;

	public ApacheMatrixWrapper(Matrix m) {
		this.m = m;
	}

	@Override
	public int getRowDimension() {
		return m.getSizeY();
	}

	@Override
	public int getColumnDimension() {
		return m.getSizeX();
	}

	@Override
	public RealMatrix createMatrix(int rowDimension, int columnDimension) throws NotStrictlyPositiveException {
		return new ApacheMatrixWrapper(new Matrix(columnDimension, rowDimension));
	}

	@Override
	public RealMatrix copy() {
		return new ApacheMatrixWrapper(m.makeCopy());
	}

	@Override
	public double getEntry(int row, int column) throws OutOfRangeException {
		return m.getItem(column, row);
	}

	@Override
	public void setEntry(int row, int column, double value) throws OutOfRangeException {
		m.setItem(column, row, value);
	}
}
