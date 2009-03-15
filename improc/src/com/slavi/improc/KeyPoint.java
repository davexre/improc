package com.slavi.improc;

import java.util.StringTokenizer;

public class KeyPoint {
	public KeyPointList keyPointList = null;
	
	public int id = hashCode();
	
	public static final int numDirections = 8;

	public static final int descriptorSize = 4;

	public static final int descriptorPixelSize = 4;
	
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
	
	byte[][][] featureVector = new byte[descriptorSize][descriptorSize][numDirections];

	public byte getItem(int atX, int atY, int atOrientation) {
		return featureVector[atX][atY][atOrientation];
	}

	public void setItem(int atX, int atY, int atOrientation, byte aValue) {
		featureVector[atX][atY][atOrientation] = aValue;
	}

	public int getNumberOfNonZero() {
		int result = 0;
		for (int i = 0; i < descriptorSize; i++)
			for (int j = 0; j < descriptorSize; j++)
				for (int k = 0; k < numDirections; k++)
					if (featureVector[i][j][k] != 0)
						result++;
		return result;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(imgX);
		result.append("\t");
		result.append(imgY);
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
		result.append(id);
		result.append("\t");

		boolean first = true;
		for (int k = 0; k < numDirections; k++) {
			for (int j = 0; j < descriptorSize; j++) {
				for (int i = 0; i < descriptorSize; i++) {
					if (first)
						first = false;
					else
						result.append("\t");
					result.append(Integer.toString(featureVector[i][j][k]));
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
		r.doubleX = Double.parseDouble(st.nextToken());
		r.doubleY = Double.parseDouble(st.nextToken());
		r.dogLevel = (int) Double.parseDouble(st.nextToken());
		r.adjS = Double.parseDouble(st.nextToken());
		r.kpScale = Double.parseDouble(st.nextToken());
		r.degree = Double.parseDouble(st.nextToken());
		r.id = Integer.parseInt(st.nextToken());
		for (int k = 0; k < numDirections; k++) 
			for (int j = 0; j < descriptorSize; j++) 
				for (int i = 0; i < descriptorSize; i++) {
					int tmp = Integer.parseInt(st.nextToken());
					if (tmp > Byte.MAX_VALUE)
						tmp = Byte.MAX_VALUE;
					if (tmp < Byte.MIN_VALUE)
						tmp = Byte.MIN_VALUE;
					r.featureVector[i][j][k] = (byte)tmp;
				}
		return r;
	}

	public boolean equalsFeatureVector(KeyPoint sp) {
		for (int k = 0; k < numDirections; k++) {
			for (int j = 0; j < descriptorSize; j++) {
				for (int i = 0; i < descriptorSize; i++) {
					if (sp.featureVector[i][j][k] != 
						featureVector[i][j][k])
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
			(sp.dogLevel != dogLevel) || 
			((int)(sp.degree * multiply) != (int)(degree * multiply)) ||
			((int)(sp.kpScale * multiply) != (int)(kpScale * multiply)) || 
			((int)(sp.doubleX * multiply) != (int)(doubleX * multiply)) ||
			((int)(sp.doubleY * multiply) != (int)(doubleY * multiply)) )
			return false;
		return equalsFeatureVector(sp);
	}

	public static final int linearFeatureVectorDimension = descriptorSize * descriptorSize * numDirections;
	
	public int getDimensions() {
		return linearFeatureVectorDimension;
	}

	public double getValue(int dimensionIndex) {
		int x = dimensionIndex % descriptorSize;
		dimensionIndex /= descriptorSize;
		int y = dimensionIndex % descriptorSize;
		int o = dimensionIndex / descriptorSize;
		return featureVector[x][y][o];
	}
}
