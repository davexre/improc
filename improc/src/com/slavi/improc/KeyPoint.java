package com.slavi.improc;

import java.util.List;
import java.util.StringTokenizer;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.util.XMLHelper;

public class KeyPoint {
	public int id = hashCode();
	
	public static final int numDirections = 8;

	public static final int descriptorSize = 4;

	public static final int descriptorPixelSize = 4;
	
	public static final int featureVectorLinearSize = descriptorSize * descriptorSize * numDirections;

	public int imgX;

	public int imgY;

	public double imgScale;
	
	public double doubleX;

	public double doubleY;

	public int dogLevel;

	public double adjS;

	public double kpScale;

	public double degree;
	
	byte[][][] featureVector = new byte[descriptorSize][descriptorSize][numDirections];

	public byte getItem(int atX, int atY, int atOrientation) {
		return featureVector[atX][atY][atOrientation];
	}

	public void setItem(int atX, int atY, int atOrientation, byte aValue) {
		featureVector[atX][atY][atOrientation] = aValue;
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
		r.dogLevel = Integer.parseInt(st.nextToken());
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

	public boolean equals(Object o) {
		int multiply = 10000;
		if (!(o instanceof KeyPoint))
			return false;
		KeyPoint sp = (KeyPoint)o;
		if (
			(sp.imgX != imgX) || 
			(sp.imgY != imgY) || 
			((int)(sp.dogLevel * multiply) != (int)(dogLevel * multiply)) || 
			((int)(sp.degree * multiply) != (int)(degree * multiply)) ||
			((int)(sp.kpScale * multiply) != (int)(kpScale * multiply)) || 
			((int)(sp.doubleX * multiply) != (int)(doubleX * multiply)) ||
			((int)(sp.doubleY * multiply) != (int)(doubleY * multiply)) )
			return false;
		for (int k = 0; k < numDirections; k++) {
			for (int j = 0; j < descriptorSize; j++) {
				for (int i = 0; i < descriptorSize; i++) {
					if ((int)(sp.featureVector[i][j][k]) != 
						(int)(featureVector[i][j][k]))
						return false;
				}
			}
		}
		return true;
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

	public void toXML(Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("id", Integer.toString(id)));
		dest.addContent(XMLHelper.makeAttrEl("imgX", Integer.toString(imgX)));
		dest.addContent(XMLHelper.makeAttrEl("imgY", Integer.toString(imgY)));
		dest.addContent(XMLHelper.makeAttrEl("doubleX", Double.toString(doubleX)));
		dest.addContent(XMLHelper.makeAttrEl("doubleY", Double.toString(doubleY)));
		dest.addContent(XMLHelper.makeAttrEl("dogLevel", Double.toString(dogLevel)));
		dest.addContent(XMLHelper.makeAttrEl("adjS", Double.toString(adjS)));
		dest.addContent(XMLHelper.makeAttrEl("kpScale", Double.toString(kpScale)));
		dest.addContent(XMLHelper.makeAttrEl("degree", Double.toString(degree)));
		for (int k = 0; k < numDirections; k++)
			for (int j = 0; j < descriptorSize; j++)
				for (int i = 0; i < descriptorSize; i++)
					dest.addContent(XMLHelper.makeEl("f", Integer.toString(featureVector[i][j][k])));
	}

	public static KeyPoint fromXML(Element source) throws JDOMException {
		KeyPoint r = new KeyPoint();
		r.id = Integer.parseInt(XMLHelper.getAttrEl(source, "id"));
		r.imgX = Integer.parseInt(XMLHelper.getAttrEl(source, "imgX"));
		r.imgY = Integer.parseInt(XMLHelper.getAttrEl(source, "imgY"));
		r.doubleX = Double.parseDouble(XMLHelper.getAttrEl(source, "doubleX"));
		r.doubleY = Double.parseDouble(XMLHelper.getAttrEl(source, "doubleY"));
		r.dogLevel = Integer.parseInt(XMLHelper.getAttrEl(source, "dogLevel"));
		r.adjS = Double.parseDouble(XMLHelper.getAttrEl(source, "adjS"));
		r.kpScale = Double.parseDouble(XMLHelper.getAttrEl(source, "kpScale"));
		r.degree = Double.parseDouble(XMLHelper.getAttrEl(source, "degree"));
		List fList = source.getChildren("f");
		if (fList.size() != KeyPoint.descriptorSize * KeyPoint.descriptorSize * KeyPoint.numDirections)
			throw new JDOMException("Number of feature elements goes not match.");
		int count = 0;
		for (int k = 0; k < numDirections; k++)
			for (int j = 0; j < descriptorSize; j++)				
				for (int i = 0; i < descriptorSize; i++) {
					int tmp = Integer.parseInt(((Element)fList.get(count++)).getTextTrim());
					if (tmp > Byte.MAX_VALUE)
						tmp = Byte.MAX_VALUE;
					if (tmp < Byte.MIN_VALUE)
						tmp = Byte.MIN_VALUE;
					r.featureVector[i][j][k] = (byte)tmp;
				}
		return r;
	}
}
