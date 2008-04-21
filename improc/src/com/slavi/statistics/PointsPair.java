package com.slavi.statistics;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.matrix.Matrix;
import com.slavi.utils.XMLHelper;

public class PointsPair extends StatisticsItemBasic {
	
	public Matrix source;

	public Matrix target;
	
	public Matrix sourceTransformed;
	
	/**
	 * The distance between target and sourceTransformed.
	 * The formula is:
	 * discrepancy = sqrt(sum(pow(target.getItem(i,0) - soruceTransformed.getItem(i,0), 2)))
	 * 
	 * public double discrepancy; 
	 */

	public boolean previousBadStatus;

	public PointsPair() {
		super(0.0, 1.0);
		source = null;
		target = null;
		sourceTransformed = null;
		previousBadStatus = false;
	}

	public PointsPair(Matrix source, Matrix target, double weight) {
		super(0.0, weight);
		this.source = source;
		this.target = target;
		this.sourceTransformed = new Matrix(target.getSizeX(), target.getSizeY());
		this.previousBadStatus = false;
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
	
	public static PointsPair fromXML(Element source) throws JDOMException {
		PointsPair r = new PointsPair();
		Element e;
		
		r.setWeight(Double.parseDouble(XMLHelper.getAttrEl(source, "weight")));
		r.setBad(Boolean.parseBoolean(XMLHelper.getAttrEl(source, "bad", "false")));

		e = source.getChild("source");
		r.source = Matrix.fromXML(e);

		e = source.getChild("target");
		r.target = Matrix.fromXML(e);

		r.sourceTransformed = new Matrix(r.source.getSizeX(), r.source.getSizeY());
		return r;
	}
}
