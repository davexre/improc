package com.slavi.math.matrix;

import java.util.Arrays;

public class BitMatrix {

	private byte m[];
	
	/**
	 * Number of columns
	 */
	private int sizeX;

	/**
	 * Number of rows
	 */
	private int sizeY;

	/**
	 * Creates a matrix with sizeX = sizeY = 0.
	 */
	public BitMatrix() {
		resize(0, 0);
	}
	
	/**
	 * Creates a matrix with aSizeX columns and aSizeY rows and sets all
	 * elements to 0.
	 */
	public BitMatrix(int aSizeX, int aSizeY) {
		resize(aSizeX, aSizeY);
	}

	/**
	 * Compares two matrices element by element.
	 * 
	 * @return Returns true if all the elements of the matrices are equal.
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || !(obj instanceof BitMatrix))
			return false;
		BitMatrix a = (BitMatrix) obj;
		if ((a.sizeX != sizeX) || (a.sizeY != sizeY))
			return false;
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (getItem(i, j) != a.getItem(i, j))
					return false;
		return true;
	}

	/**
	 * Returns the number of columns in the matrix.
	 */
	public int getSizeX() {
		return sizeX;
	}

	/**
	 * Returns the number of rows in the matrix.
	 */
	public int getSizeY() {
		return sizeY;
	}

	/**
	 * Resizes the matrix if the new size differs from the current matrix size.
	 */
	public void resize(int newSizeX, int newSizeY) {
		if ((newSizeX < 0) || (newSizeY < 0)) {
			throw new Error("Invalid matrix size");
		}
		if ((newSizeX == sizeX) && (newSizeY == sizeY)) {
			return;
		}
		sizeX = newSizeX;
		sizeY = newSizeY;
		int sizeM = (int) Math.ceil((double) (sizeX * sizeY) / 8.0);
		m = new byte[sizeM];
		Arrays.fill(m, (byte) 0);
	}

	public void make0() {
		Arrays.fill(m, (byte) 0);
	}
	
	/**
	 * Returns true if all elements are 0 (false). 
	 */
	public boolean is0() {
		for (int i = sizeX - 1; i >= 0; i--)
			for (int j = sizeY - 1; j >= 0; j--)
				if (getItem(i, j))
					return false;
		return true;
	}

	public void make1() {
		Arrays.fill(m, (byte) 0xff);
	}
	
	public void setItem(int atX, int atY, boolean aValue) {
		if ((atX < 0) || (atX > sizeX) || 
			(atY < 0) || (atY > sizeY)) {
			throw new IllegalArgumentException("Invalid argument");
		}
		int index = atY * sizeX + atX;
		int indexM = index / 8;
		byte mask = (byte) (1 << (index % 8));
		if (aValue) {
			m[indexM] |= mask;
		} else {
			m[indexM] &= ~mask;
		}
	}
	
	public boolean itemNot(int atX, int atY) {
		if ((atX < 0) || (atX > sizeX) || 
			(atY < 0) || (atY > sizeY)) {
			throw new IllegalArgumentException("Invalid argument");
		}
		int index = atY * sizeX + atX;
		int indexM = index / 8;
		byte mask = (byte) (1 << (index % 8));
		if ((m[indexM] & mask) == 0) {
			m[indexM] |= mask;
			return true;
		} else {
			m[indexM] &= ~mask;
			return false;
		}
	}
	
	public boolean getItem(int atX, int atY) {
		if ((atX < 0) || (atX > sizeX) || 
			(atY < 0) || (atY > sizeY)) {
			throw new IllegalArgumentException("Invalid argument");
		}
		int index = atY * sizeX + atX;
		int indexM = index / 8;
		byte mask = (byte) (1 << (index % 8));
		return (m[indexM] & mask) != 0;
	}
	
	public void copyTo(BitMatrix dest) {
		dest.resize(sizeX, sizeY);
		System.arraycopy(m, 0, dest.m, 0, m.length);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < sizeY; j++) {
			for (int i = 0; i < sizeX; i++) {
				sb.append(getItem(i, j) ? "1" : "0");
				if (i % 4 == 3)
					sb.append(" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
