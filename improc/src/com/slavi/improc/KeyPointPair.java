package com.slavi.improc;

import java.util.StringTokenizer;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.PointsPair;
import com.slavi.util.XMLHelper;

public class KeyPointPair extends PointsPair {
	public int id = hashCode();
	
	public KeyPoint sourceSP;
	
	public KeyPoint targetSP;
	
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
	
	private static Matrix getMatrixFromScalePoint(KeyPoint sp) {
		Matrix r = new Matrix(2, 1);
		r.setItem(0, 0, sp.doubleX);
		r.setItem(1, 0, sp.doubleY);
		return r;
	}
	
	public KeyPointPair(KeyPoint sourceSP, KeyPoint targetSP, double distanceToNearest, double distanceToNearest2) {
		super(getMatrixFromScalePoint(sourceSP), getMatrixFromScalePoint(targetSP), 1.0);
		this.sourceTransformed = new Matrix(this.target.getSizeX(), this.target.getSizeY());
		
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

	public String toString() {
		return
			Double.toString(distanceToNearest) + ":" + 
			Double.toString(distanceToNearest2) + ":" +
			source.toString() + ":" + target.toString();
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
		
		e = new Element("source");
		source.toXML(e);
		dest.addContent(e);
		
		e = new Element("target");
		target.toXML(e);
		dest.addContent(e);
		
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
		
		e = source.getChild("source");
		r.source = Matrix.fromXML(e);

		e = source.getChild("target");
		r.target = Matrix.fromXML(e);

		e = source.getChild("sourceSP");
		r.sourceSP = KeyPoint.fromXML(e);

		e = source.getChild("targetSP");
		r.targetSP = KeyPoint.fromXML(e);

		r.sourceTransformed = new Matrix(r.source.getSizeX(), r.source.getSizeY());
		return r;
	}
}
