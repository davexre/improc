package com.slavi.math.matrix;

import com.slavi.math.MathUtil;

public class DiagonalMatrix <T extends DiagonalMatrix<T>> extends Vector<T> implements IMatrix<T> {

	/**
	 * Number of columns
	 */
	private int sizeX;

	/**
	 * Number of rows
	 */
	private int sizeY;

	public DiagonalMatrix() {}

	public DiagonalMatrix(int sizeX, int sizeY) {
		resize(sizeX, sizeY);
	}

	/**
	 * Resizes the matrix if the new size differs from the current matrix size.
	 * @return this
	 */
	public DiagonalMatrix resize(int newSizeX, int newSizeY) {
		if ((newSizeX < 0) || (newSizeY < 0)) {
			throw new Error("Invalid matrix size");
		}
		sizeX = newSizeX;
		sizeY = newSizeY;
		int newSize = Math.min(sizeX, sizeY);
		if (m == null || m.length != newSize)
			m = newSize == 0 ? null : new double[newSize];
		return this;
	}

	/**
	 * Returns the number of columns in the matrix.
	 */
	@Override
	public int getSizeX() {
		return sizeX;
	}

	/**
	 * Returns the number of rows in the matrix.
	 */
	@Override
	public int getSizeY() {
		return sizeY;
	}

	private void asserValidIndexes(int atX, int atY) {
		if (atX < 0 || atX >= getSizeX() ||
			atY < 0 || atY >= getSizeY())
			throw new IndexOutOfBoundsException("Indexes (" + atX + "," + atY + ") not in range (" + getSizeX() + "," + getSizeY() + ")");
	}

	@Override
	public double getItem(int atX, int atY) {
		asserValidIndexes(atX, atY);
		return atX == atY ? m[atX] : 0;
	}

	@Override
	public void setItem(int atX, int atY, double aValue) {
		asserValidIndexes(atX, atY);
		if (atX == atY) {
			m[atX] = aValue;
		} else {
			if (Math.abs(aValue) > MathUtil.eps) {
				throw new Error("Invalid argument. Setting value at index (" + atX + "," + atY +
						") to " + aValue + " of a diagonal matrix.");
			}
		}
	}

	@Override
	public T transpose() {
		int tmp = sizeX;
		sizeX = sizeY;
		sizeY = tmp;
		return (T) this;
	}

	/**
	 * Exchanges the column atX1 with column atX2.
	 * @return this
	 */
	public DiagonalMatrix exchangeX(int atX1, int atX2) {
		double D = getVectorItem(atX1);
		setVectorItem(atX1, getVectorItem(atX2));
		setVectorItem(atX2, D);
		return this;
	}

	/**
	 * Exchanges the row atY1 with row atY2.
	 * @return this
	 */
	public DiagonalMatrix exchangeY(int atY1, int atY2) {
		double D = getVectorItem(atY1);
		setVectorItem(atY1, getVectorItem(atY2));
		setVectorItem(atY2, D);
		return this;
	}
}
