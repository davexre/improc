package com.slavi.statistics;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.matrix.Matrix;
import com.slavi.utils.XMLHelper;

public class PointsPair extends StatisticsItem {
	
	public Matrix source;

	public Matrix target;
	
	public Matrix sourceTransformed;
	
	/**
	 * The distance between target and sourceTransformed.
	 * The formula is:
	 * discrepancy = sqrt(sum(pow(target.getItem(i,0) - soruceTransformed.getItem(i,0), 2)))
	 */
	public double discrepancy; 

	public boolean previousBadStatus;

	public PointsPair() {
		source = null;
		target = null;
		sourceTransformed = null;
		weight = 1;
		computedWeight = 0;
		bad = false;
		previousBadStatus = false;
	}

	public PointsPair(Matrix source, Matrix target, double weight) {
		this.source = source;
		this.target = target;
		this.sourceTransformed = new Matrix(target.getSizeX(), target.getSizeY());
		this.discrepancy = 0;
		this.weight = weight;
		this.computedWeight = 0;
		this.bad = false;
		this.previousBadStatus = false;
	}

	public double getValue() {
		return discrepancy;
	}

	public double getWeight() {
		return weight;
	}

	public double getComputedWeight() {
		return computedWeight;
	}

	public void setComputedWeight(double computedWeight) {
		this.computedWeight = computedWeight;
	}

	public boolean isBad() {
		return bad;
	}

	public void setBad(boolean bad) {
		this.bad = bad;
	}
	
	public void toXML(Element dest) {
		Element e;
		
		dest.addContent(XMLHelper.makeAttrEl("weight", Double.toString(weight)));
		dest.addContent(XMLHelper.makeAttrEl("bad", Boolean.toString(bad)));
		
		e = new Element("source");
		source.toXML(e);
		dest.addContent(e);
		
		e = new Element("target");
		target.toXML(e);
		dest.addContent(e);
	}
	
	public static PointsPair fromXML(Element source) throws JDOMException {
		PointsPair r = new PointsPair();
		Element e;
		
		r.weight = Double.parseDouble(XMLHelper.getAttrEl(source, "weight"));
		r.bad = Boolean.parseBoolean(XMLHelper.getAttrEl(source, "bad", "false"));

		e = source.getChild("source");
		r.source = Matrix.fromXML(e);

		e = source.getChild("target");
		r.target = Matrix.fromXML(e);

		r.sourceTransformed = new Matrix(r.source.getSizeX(), r.source.getSizeY());
		return r;
	}
}
