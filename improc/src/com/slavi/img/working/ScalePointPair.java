package com.slavi.img.working;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.matrix.Matrix;
import com.slavi.statistics.PointsPair;
import com.slavi.utils.XMLHelper;

public class ScalePointPair extends PointsPair {
	public int id;
	
	public ScalePoint sourceSP;
	
	public ScalePoint targetSP;
	
	// Distance to nearest ScalePoint as reported by nearestNeighbourhood
	public double distanceToNearest;
	// Distance to second-nearest ScalePoint as reported by nearestNeighbourhood
	public double distanceToNearest2;

	public double overallFitness;
	
	// targetReused = true means this pair is suspicious, posibly bad
	public boolean targetReused;
	
	public ScalePointPair() {
		super();
		sourceSP = null;
		targetSP = null;
		distanceToNearest = 0;
		distanceToNearest2 = 0;
		overallFitness = 0;
		targetReused = false;
	}
	
	private static Matrix getMatrixFromScalePoint(ScalePoint sp) {
		Matrix r = new Matrix(2, 1);
		r.setItem(0, 0, sp.doubleX);
		r.setItem(1, 0, sp.doubleY);
		return r;
	}
	
	public ScalePointPair(ScalePoint sourceSP, ScalePoint targetSP, double distanceToNearest, double distanceToNearest2) {
		this.source = getMatrixFromScalePoint(sourceSP);
		this.target = getMatrixFromScalePoint(targetSP);
		this.sourceTransformed = new Matrix(this.target.getSizeX(), this.target.getSizeY());
		this.previousBadStatus = false;
		
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

	public void toXML(Element dest) {
		Element e;
		
		dest.addContent(XMLHelper.makeAttrEl("dist1", Double.toString(distanceToNearest)));
		dest.addContent(XMLHelper.makeAttrEl("dist2", Double.toString(distanceToNearest2)));
		dest.addContent(XMLHelper.makeAttrEl("targetReused", Boolean.toString(targetReused)));
		dest.addContent(XMLHelper.makeAttrEl("weight", Double.toString(getWeight())));
		
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
	
	public static ScalePointPair fromXML(Element source) throws JDOMException {
		ScalePointPair r = new ScalePointPair();
		Element e;

		r.distanceToNearest = Double.parseDouble(XMLHelper.getAttrEl(source, "dist1"));
		r.distanceToNearest2 = Double.parseDouble(XMLHelper.getAttrEl(source, "dist2"));
		r.targetReused = Boolean.parseBoolean(XMLHelper.getAttrEl(source, "targetReused"));
		r.setWeight(Double.parseDouble(XMLHelper.getAttrEl(source, "weight", "1")));
		
		e = source.getChild("source");
		r.source = Matrix.fromXML(e);

		e = source.getChild("target");
		r.target = Matrix.fromXML(e);

		e = source.getChild("sourceSP");
		r.sourceSP = ScalePoint.fromXML(e);

		e = source.getChild("targetSP");
		r.targetSP = ScalePoint.fromXML(e);

		r.sourceTransformed = new Matrix(r.source.getSizeX(), r.source.getSizeY());
		return r;
	}
}
