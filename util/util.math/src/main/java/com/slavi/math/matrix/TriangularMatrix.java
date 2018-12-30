package com.slavi.math.matrix;

import com.slavi.math.MathUtil;

public class TriangularMatrix <T extends TriangularMatrix<T>> extends Vector<T> implements IMatrix<T> {

	/**
	 * Number of columns
	 */
	private int sizeX;

	/**
	 * Number of rows
	 */
	private int sizeY;

	boolean transposed = false;

	public TriangularMatrix() {
		this(0, 0, false);
	}

	public TriangularMatrix(int sizeX, int sizeY, boolean isUpper) {
		this.transposed = isUpper;
		resize(sizeX, sizeY);
	}

	public boolean isUpper() {
		return transposed;
	}

	@Override
	public T transpose() {
		transposed = !transposed;
		return (T) this;
	}

	/**
	 * Resizes the matrix if the new size differs from the current matrix size.
	 * @return this
	 */
	public TriangularMatrix resize(int newSizeX, int newSizeY) {
		if ((newSizeX < 0) || (newSizeY < 0)) {
			throw new Error("Invalid matrix size");
		}
		if (transposed) {
			sizeX = newSizeY;
			sizeY = newSizeX;
		} else {
			sizeX = newSizeX;
			sizeY = newSizeY;
		}
		int newSize = sizeY <= sizeX ?
			((sizeY + 1) * sizeY) >> 1 :
			(sizeX * ((sizeY << 1) - sizeX + 1)) >> 1;
		if (m == null || m.length != newSize)
			m = newSize == 0 ? null : new double[newSize];
		return this;
	}

	/**
	 * Returns the number of columns in the matrix.
	 */
	@Override
	public int getSizeX() {
		return transposed ? sizeY : sizeX;
	}

	/**
	 * Returns the number of rows in the matrix.
	 */
	@Override
	public int getSizeY() {
		return transposed ? sizeX : sizeY;
	}

	/*
	 * Returns the index in the array m of the matrix element (atX, atY)
	 * If the atX, atY is out of range throws an exception.
	 * If the element is in the area filled with 0 a negative value is returned.
	 */
	private int getItemIndex(int atX, int atY) {
		if (atX < 0 || atX >= getSizeX() ||
			atY < 0 || atY >= getSizeY())
			throw new IndexOutOfBoundsException("Indexes (" + atX + "," + atY + ") not in range (" + getSizeX() + "," + getSizeY() + ")");
		if (transposed) {
			return atX < atY ? -1 :
				atX + atY * sizeY - ((atY * (atY + 1)) >> 1);
		} else {
			return atY < atX ? -1 :
				atY + atX * sizeY - ((atX * (atX + 1)) >> 1);
		}
	}

	@Override
	public double getItem(int atX, int atY) {
		int index = getItemIndex(atX, atY);
		return index < 0 ? 0 : m[index];
	}

	@Override
	public void setItem(int atX, int atY, double aValue) {
		int index = getItemIndex(atX, atY);
		if (index < 0) {
			if (Math.abs(aValue) > MathUtil.eps) {
				throw new Error("Invalid argument. Setting value at index (" + atX + "," + atY +
						") to " + aValue + " of a " + (isUpper() ? "upper" : "lower") +
						" triangular matrix.");
			}
		} else {
			m[index] = aValue;
		}
	}
}
