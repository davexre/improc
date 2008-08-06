package com.slavi.improc.working;

import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.util.XMLHelper;

public class ScalePoint extends KDNodeBase {
	public static final int numDirections = 8;

	public static final int descriptorSize = 4;

	public static final int descriptorPixelSize = 4;
	
	public static final int featureVectorLinearSize = descriptorSize * descriptorSize * numDirections;

	public int imgX;

	public int imgY;

	public double imgScale;
	
	public double doubleX;

	public double doubleY;

	public double level;

	public double adjS;

	public double kpScale;

	public double degree;

	double[][][] featureVector = new double[descriptorSize][descriptorSize][numDirections];

	public double getItem(int atX, int atY, int atOrientation) {
		return featureVector[atX][atY][atOrientation];
	}

	public void setItem(int atX, int atY, int atOrientation, double aValue) {
		featureVector[atX][atY][atOrientation] = aValue;
	}

	public void normalizeOriginal() {
		double norm = 0.0;
		for (int i = 0 ; i < descriptorSize; i++)
			for (int j = 0 ; j < descriptorSize; j++)
				for (int k = 0 ; k < numDirections; k++)
					norm += Math.pow(featureVector[i][j][k], 2.0);
		if (norm == 0.0)
			return;
		norm = Math.sqrt(norm);
		for (int i = 0 ; i < descriptorSize; i++)
			for (int j = 0 ; j < descriptorSize; j++)
				for (int k = 0 ; k < numDirections; k++)
					featureVector[i][j][k] /= norm;
	}
	
	public void normalize() {
		double norm = 0.0;
		double min = featureVector[0][0][0];
		for (int i = 0 ; i < descriptorSize; i++)
			for (int j = 0 ; j < descriptorSize; j++)
				for (int k = 0 ; k < numDirections; k++) {
					double value = featureVector[i][j][k];
					if (min > value)
						min = value;
					norm += Math.abs(value);
				}
		if (norm == 0.0)
			return;
		for (int i = 0 ; i < descriptorSize; i++)
			for (int j = 0 ; j < descriptorSize; j++)
				for (int k = 0 ; k < numDirections; k++) {
					featureVector[i][j][k] = (featureVector[i][j][k] - min) / norm;
				}
	}
	
	public void hiCap(double hiCapValue) {
		for (int i = 0 ; i < descriptorSize; i++)
			for (int j = 0 ; j < descriptorSize; j++)
				for (int k = 0 ; k < numDirections; k++)
					if (featureVector[i][j][k] > hiCapValue)
						featureVector[i][j][k] = hiCapValue;
	}
	
	public void initFeatureVector() {
		for (int i = 0 ; i < descriptorSize; i++)
			for (int j = 0 ; j < descriptorSize; j++)
				for (int k = 0 ; k < numDirections; k++)
					featureVector[i][j][k] = 0;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("imgX     ");
		result.append(imgX);
		result.append("\nimgY     ");
		result.append(imgY);
		result.append("\ndoubleX  ");
		result.append(Double.toString(doubleX));
		result.append("\ndoubleY  ");
		result.append(Double.toString(doubleY));
		result.append("\nlevel    ");
		result.append(Double.toString(level));
		result.append("\nadjS     ");
		result.append(Double.toString(adjS));
		result.append("\nkpScale  ");
		result.append(Double.toString(kpScale));
		result.append("\ndegree   ");
		result.append(Double.toString(degree));
		result.append("\nfeatureVector\n");

		for (int k = 0; k < numDirections; k++) {
			result.append("featureVector.direction.");
			result.append(k);
			result.append("\n");
			for (int j = 0; j < descriptorSize; j++) {
				for (int i = 0; i < descriptorSize; i++) {
					if (i != 0)
						result.append(" ");
						result.append(Double.toString(featureVector[i][j][k]));
				}
				if (j != descriptorSize - 1)
					result.append("\n");
			}
			if (k != numDirections - 1)
				result.append("\n");
		}
		return result.toString();
	}

	public boolean equals(Object o) {
		int multiply = 10;
		if (!(o instanceof ScalePoint))
			return false;
		ScalePoint sp = (ScalePoint)o;
		if (
//			(sp.imgX != imgX) || 
//			(sp.imgY != imgY) || 
//			((int)(sp.level * multiply) != (int)(level * multiply)) || 
//			((int)(sp.degree * multiply) != (int)(degree * multiply)) ||
//			((int)(sp.kpScale * multiply) != (int)(kpScale * multiply)) || 
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

	// Methods implementing KDNode
	
	public ScalePointList scalePointList = null;

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

	public boolean canFindDistanceToPoint(KDNode node) {
		return scalePointList != ((ScalePoint)node).scalePointList;
	}
	
	public void toXML(Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("imgX", Integer.toString(imgX)));
		dest.addContent(XMLHelper.makeAttrEl("imgY", Integer.toString(imgY)));
		dest.addContent(XMLHelper.makeAttrEl("doubleX", Double.toString(doubleX)));
		dest.addContent(XMLHelper.makeAttrEl("doubleY", Double.toString(doubleY)));
		dest.addContent(XMLHelper.makeAttrEl("level", Double.toString(level)));
		dest.addContent(XMLHelper.makeAttrEl("adjS", Double.toString(adjS)));
		dest.addContent(XMLHelper.makeAttrEl("kpScale", Double.toString(kpScale)));
		dest.addContent(XMLHelper.makeAttrEl("degree", Double.toString(degree)));
		for (int k = 0; k < numDirections; k++)
			for (int j = 0; j < descriptorSize; j++)
				for (int i = 0; i < descriptorSize; i++)
					dest.addContent(XMLHelper.makeEl("f", Double.toString(featureVector[i][j][k])));
	}

	public static ScalePoint fromXML(Element source) throws JDOMException {
		ScalePoint r = new ScalePoint();
		r.imgX = Integer.parseInt(XMLHelper.getAttrEl(source, "imgX"));
		r.imgY = Integer.parseInt(XMLHelper.getAttrEl(source, "imgY"));
		r.doubleX = Double.parseDouble(XMLHelper.getAttrEl(source, "doubleX"));
		r.doubleY = Double.parseDouble(XMLHelper.getAttrEl(source, "doubleY"));
		r.level = Double.parseDouble(XMLHelper.getAttrEl(source, "level"));
		r.adjS = Double.parseDouble(XMLHelper.getAttrEl(source, "adjS"));
		r.kpScale = Double.parseDouble(XMLHelper.getAttrEl(source, "kpScale"));
		r.degree = Double.parseDouble(XMLHelper.getAttrEl(source, "degree"));
		List fList = source.getChildren("f");
		if (fList.size() != ScalePoint.descriptorSize * ScalePoint.descriptorSize * ScalePoint.numDirections)
			throw new JDOMException("Number of feature elements goes not match.");
		int count = 0;
		for (int k = 0; k < numDirections; k++)
			for (int j = 0; j < descriptorSize; j++)				
				for (int i = 0; i < descriptorSize; i++)
					r.featureVector[i][j][k] = Double.parseDouble(((Element)fList.get(count++)).getTextTrim());
		return r;
	}
}
