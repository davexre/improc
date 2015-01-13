package com.test.math;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import com.slavi.io.xml.XMLMatrix;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.xml.XMLHelper;

public class PointsPair {
	public Matrix source;

	public Matrix target;
	
	private boolean bad;

	private double weight;
	
	/**
	 * The distance between target and sourceTransformed.
	 * The formula is:
	 * discrepancy = sqrt(sum(pow(target.getItem(i,0) - soruceTransformed.getItem(i,0), 2)))
	 */
	public double discrepancy; 

	public PointsPair() {
		source = null;
		target = null;
		bad = false;
		weight = 1.0;
	}

	public PointsPair(Matrix source, Matrix target, double weight) {
		this.source = source;
		this.target = target;
		this.bad = false;
		this.weight = weight;
	}

	public double getDiscrepancy() {
		return discrepancy;
	}

	public double getSourceCoord(int coordIndex) {
		return source.getItem(coordIndex, 0);
	}

	public double getTargetCoord(int coordIndex) {
		return target.getItem(coordIndex, 0);
	}

	public void setDiscrepancy(double discrepancy) {
		this.discrepancy = discrepancy;
	}
	
	public boolean isBad() {
		return bad;
	}

	public void setBad(boolean bad) {
		this.bad = bad;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}

	public void toXML(Element dest) {
		Element e;
		
		dest.addContent(XMLHelper.makeAttrEl("weight", Double.toString(getWeight())));
		dest.addContent(XMLHelper.makeAttrEl("bad", Boolean.toString(isBad())));
		
		e = new Element("source");
		XMLMatrix.instance.toXML(source, e);
		dest.addContent(e);
		
		e = new Element("target");
		XMLMatrix.instance.toXML(target, e);
		dest.addContent(e);
	}
	
	public static PointsPair fromXML(Element source) throws JDOMException {
		PointsPair r = new PointsPair();
		Element e;
		
		r.setWeight(Double.parseDouble(XMLHelper.getAttrEl(source, "weight")));
		r.setBad(Boolean.parseBoolean(XMLHelper.getAttrEl(source, "bad", "false")));

		e = source.getChild("source");
		r.source = XMLMatrix.instance.fromXML(e);

		e = source.getChild("target");
		r.target = XMLMatrix.instance.fromXML(e);
		return r;
	}
}
