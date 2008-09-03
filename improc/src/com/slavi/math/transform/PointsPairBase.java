package com.slavi.math.transform;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.math.matrix.Matrix;
import com.slavi.util.XMLHelper;

public class PointsPairBase implements PointsPair {
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

	public PointsPairBase() {
		source = null;
		target = null;
		bad = false;
		weight = 1.0;
	}

	public PointsPairBase(Matrix source, Matrix target, double weight) {
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
		source.toXML(e);
		dest.addContent(e);
		
		e = new Element("target");
		target.toXML(e);
		dest.addContent(e);
	}
	
	public static PointsPairBase fromXML(Element source) throws JDOMException {
		PointsPairBase r = new PointsPairBase();
		Element e;
		
		r.setWeight(Double.parseDouble(XMLHelper.getAttrEl(source, "weight")));
		r.setBad(Boolean.parseBoolean(XMLHelper.getAttrEl(source, "bad", "false")));

		e = source.getChild("source");
		r.source = Matrix.fromXML(e);

		e = source.getChild("target");
		r.target = Matrix.fromXML(e);
		return r;
	}
}
