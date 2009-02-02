package com.slavi.improc;

import java.util.Map;
import java.util.StringTokenizer;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.util.XMLHelper;

public class KeyPointPair implements Map.Entry<KeyPoint, KeyPoint>{
	public int id = hashCode();
	
	public KeyPoint sourceSP;
	
	public KeyPoint targetSP;
	
	public double angle;
	public double d1;
	
	// Distance to nearest KeyPoint as reported by nearestNeighbourhood
	public double distanceToNearest;
	// Distance to second-nearest KeyPoint as reported by nearestNeighbourhood
	public double distanceToNearest2;

	public double overallFitness;	// TODO: obsolete !?!
	
	// targetReused = true means this pair is suspicious, posibly bad
	public boolean targetReused;	// TODO: obsolete !?!
	
	public KeyPointPair() {
		super();
		sourceSP = null;
		targetSP = null;
		distanceToNearest = 0;
		distanceToNearest2 = 0;
		overallFitness = 0;
		targetReused = false;
	}
	
	public KeyPointPair(KeyPoint sourceSP, KeyPoint targetSP, double distanceToNearest, double distanceToNearest2) {
		this.sourceSP = sourceSP;
		this.targetSP = targetSP;
		this.distanceToNearest = distanceToNearest;
		this.distanceToNearest2 = distanceToNearest2;
		double d = distanceToNearest2 - distanceToNearest;
		if (d > 0)
			this.overallFitness = distanceToNearest / d;
		else
			this.overallFitness = Double.MAX_VALUE;
		this.targetReused = false;
	}

	public int getUnmatchingCount() {
		int result = 0;
		for (int i = 0; i < KeyPoint.descriptorSize; i++)
			for (int j = 0; j < KeyPoint.descriptorSize; j++)
				for (int k = 0; k < KeyPoint.numDirections; k++) {
					if (
						(sourceSP.featureVector[i][j][k] == 0) ^ 
						(targetSP.featureVector[i][j][k] == 0)) 
						result++;
				}
		return result;
	}
	
	public int getMaxDifference() {
		int result = 0;
		for (int i = 0; i < KeyPoint.descriptorSize; i++)
			for (int j = 0; j < KeyPoint.descriptorSize; j++)
				for (int k = 0; k < KeyPoint.numDirections; k++) {
					int dif = Math.abs(sourceSP.featureVector[i][j][k] - targetSP.featureVector[i][j][k]);
					if (result < dif)
						result = dif;
				}
		return result;
	}

	public String toString() {
		return
			Double.toString(distanceToNearest) + ":" + 
			Double.toString(distanceToNearest2) + ":" +
			sourceSP.toString() + ":" + targetSP.toString();
	}
	
	public static KeyPointPair fromString(String str) {
		StringTokenizer st = new StringTokenizer(str, ":");
		double d1 = Double.parseDouble(st.nextToken());
		double d2 = Double.parseDouble(st.nextToken());
		return new KeyPointPair(
				KeyPoint.fromString(st.nextToken()),
				KeyPoint.fromString(st.nextToken()), d1, d2);
	}	
	
	public void toXML(Element dest) {
		Element e;
		
		dest.addContent(XMLHelper.makeAttrEl("dist1", Double.toString(distanceToNearest)));
		dest.addContent(XMLHelper.makeAttrEl("dist2", Double.toString(distanceToNearest2)));
		
		e = new Element("sourceSP");
		sourceSP.toXML(e);
		dest.addContent(e);

		e = new Element("targetSP");
		targetSP.toXML(e);
		dest.addContent(e);
	}
	
	public static KeyPointPair fromXML(Element source) throws JDOMException {
		KeyPointPair r = new KeyPointPair();
		Element e;

		r.distanceToNearest = Double.parseDouble(XMLHelper.getAttrEl(source, "dist1"));
		r.distanceToNearest2 = Double.parseDouble(XMLHelper.getAttrEl(source, "dist2"));
		
		e = source.getChild("sourceSP");
		r.sourceSP = KeyPoint.fromXML(e);

		e = source.getChild("targetSP");
		r.targetSP = KeyPoint.fromXML(e);
		return r;
	}

	double discrepancy;
	public double getDiscrepancy() {
		return discrepancy;
	}

	public double getSourceCoord(int coordIndex) {
		switch (coordIndex) {
			case 0: return sourceSP.doubleX;
			case 1: return sourceSP.doubleY;
			default: throw new IllegalArgumentException("Index out of range [0..1]");
		}
	}

	public double getTargetCoord(int coordIndex) {
		switch (coordIndex) {
			case 0: return targetSP.doubleX;
			case 1: return targetSP.doubleY;
			default: throw new IllegalArgumentException("Index out of range [0..1]");
		}
	}

	public double getWeight() {
		return 1.0;
	}

	boolean bad;
	public boolean isBad() {
		return bad;
	}

	public void setBad(boolean bad) {
		this.bad = bad;
	}

	public void setDiscrepancy(double discrepancy) {
		this.discrepancy = discrepancy;
	}

	public KeyPoint getKey() {
		return sourceSP;
	}

	public KeyPoint getValue() {
		return targetSP;
	}

	public KeyPoint setValue(KeyPoint value) {
		throw new RuntimeException("Method not allowed");
		//return targetSP = value;
	}
}
