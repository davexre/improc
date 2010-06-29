package com.slavi.improc;

import java.util.StringTokenizer;

public class KeyPoint {
	public KeyPointList keyPointList = null;
	
	public static final int numDirections = 4;	// originally 8

	public static final int descriptorSize = 4;	// originally 4

	public static final int featureVectorLinearSize = descriptorSize * descriptorSize * numDirections;

	public int imgX;		// TODO: Obsolete

	public int imgY;		// TODO: Obsolete

	public double imgScale; // TODO: Obsolete
	
	public double doubleX;

	public double doubleY;

	public int dogLevel; 	// TODO: Obsolete

	public double adjS;		// TODO: Obsolete

	public double kpScale;	// TODO: Obsolete

	public double degree;	// TODO: Obsolete
	
	byte[] featureVector = new byte[featureVectorLinearSize];

	public byte getItem(int atX, int atY, int atOrientation) {
		if ((atX < 0) || (atX >= descriptorSize) || 
			(atY < 0) || (atY >= descriptorSize) ||
			(atOrientation < 0) || (atOrientation >= numDirections))
			throw new ArrayIndexOutOfBoundsException("X=" + atX + " Y=" + atY + " O=" + atOrientation);
		return featureVector[((atX * descriptorSize) + atY) * numDirections + atOrientation];
	}

	public void setItem(int atX, int atY, int atOrientation, byte aValue) {
		if ((atX < 0) || (atX >= descriptorSize) || 
			(atY < 0) || (atY >= descriptorSize) ||
			(atOrientation < 0) || (atOrientation >= numDirections))
			throw new ArrayIndexOutOfBoundsException("X=" + atX + " Y=" + atY + " O=" + atOrientation);
		int atIndex = ((atX * descriptorSize) + atY) * numDirections + atOrientation; 
		try {
			featureVector[((atX * descriptorSize) + atY) * numDirections + atOrientation] = aValue;
		} catch (Throwable t) {
			t.printStackTrace();
			System.out.println("atX=" + atX);
			System.out.println("atY=" + atY);
			System.out.println("atO=" + atOrientation);
			System.out.println("atI=" + atIndex);
			System.out.println("atMax=" + featureVectorLinearSize);
			throw new Error(t);
		}
	}

	public int getNumberOfNonZero() {
		int result = 0;
		for (int i = 0; i < descriptorSize; i++)
			for (int j = 0; j < descriptorSize; j++)
				for (int k = 0; k < numDirections; k++)
					if (getItem(i, j, k) != 0)
						result++;
		return result;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(imgX);
		result.append("\t");
		result.append(imgY);
		result.append("\t");
		result.append(Double.toString(imgScale));
		result.append("\t");
		result.append(Double.toString(doubleX));
		result.append("\t");
		result.append(Double.toString(doubleY));
		result.append("\t");
		result.append(Double.toString(dogLevel));
		result.append("\t");
		result.append(Double.toString(adjS));
		result.append("\t");
		result.append(Double.toString(kpScale));
		result.append("\t");
		result.append(Double.toString(degree));
		result.append("\t");

		boolean first = true;
		for (int k = 0; k < numDirections; k++) {
			for (int j = 0; j < descriptorSize; j++) {
				for (int i = 0; i < descriptorSize; i++) {
					if (first)
						first = false;
					else
						result.append("\t");
					result.append(Integer.toString(getItem(i, j, k)));
				}
			}
		}
		return result.toString();
	}

	public static KeyPoint fromString(String str) {
		StringTokenizer st = new StringTokenizer(str, "\t");
		if (st.countTokens() != 9 + featureVectorLinearSize)
			throw new IllegalArgumentException("KeyPoint.fromString: Malformed source string.");
		KeyPoint r = new KeyPoint();
		r.imgX = Integer.parseInt(st.nextToken());
		r.imgY = Integer.parseInt(st.nextToken());
		r.imgScale = Double.parseDouble(st.nextToken());
		r.doubleX = Double.parseDouble(st.nextToken());
		r.doubleY = Double.parseDouble(st.nextToken());
		r.dogLevel = (int) Double.parseDouble(st.nextToken());
		r.adjS = Double.parseDouble(st.nextToken());
		r.kpScale = Double.parseDouble(st.nextToken());
		r.degree = Double.parseDouble(st.nextToken());
		for (int k = 0; k < numDirections; k++) 
			for (int j = 0; j < descriptorSize; j++) 
				for (int i = 0; i < descriptorSize; i++) {
					int tmp = Integer.parseInt(st.nextToken());
					if (tmp > Byte.MAX_VALUE)
						tmp = Byte.MAX_VALUE;
					if (tmp < Byte.MIN_VALUE)
						tmp = Byte.MIN_VALUE;
					r.setItem(i, j, k, (byte)tmp);
				}
		return r;
	}

	public boolean equalsFeatureVector(KeyPoint sp) {
		for (int k = 0; k < numDirections; k++) {
			for (int j = 0; j < descriptorSize; j++) {
				for (int i = 0; i < descriptorSize; i++) {
					if (sp.getItem(i, j, k) != getItem(i, j, k))
						return false;
				}
			}
		}
		return true;
	}
	
	public boolean equals(Object o) {
		int multiply = 10000;
		if (!(o instanceof KeyPoint))
			return false;
		KeyPoint sp = (KeyPoint)o;
		if (
			(sp.imgX != imgX) || 
			(sp.imgY != imgY) || 
			(sp.imgScale != imgScale) || 
			(sp.dogLevel != dogLevel) || 
			((int)(sp.degree * multiply) != (int)(degree * multiply)) ||
			((int)(sp.kpScale * multiply) != (int)(kpScale * multiply)) || 
			((int)(sp.doubleX * multiply) != (int)(doubleX * multiply)) ||
			((int)(sp.doubleY * multiply) != (int)(doubleY * multiply)) )
			return false;
		return equalsFeatureVector(sp);
	}

	public int getDimensions() {
		return featureVectorLinearSize;
	}

	public double getValue(int dimensionIndex) {
		return featureVector[dimensionIndex];
	}
}
