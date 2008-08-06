package com.slavi.math.transform;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.math.matrix.Matrix;
import com.slavi.util.XMLHelper;

/**
 * Helmert2DTransformer performs a 4-parametered affine transform
 * 
 * X(target) = a * X(source) - b * Y(source) + c
 * Y(target) = b * X(source) + a * Y(source) + d
 * 
 * @author Slavian Petrov
 */
public class Helmert2DTransformer extends BaseTransformer {
	
	public double a; // a = cos(Angle) * scale 
	public double b; // b = sin(Angle) * scale
	public double c; // c = translate x
	public double d; // d = translate y
	
	public Helmert2DTransformer() {
		this.inputSize = 2;
		this.outputSize = 2;
	}
	
	public int getNumberOfCoefsPerCoordinate() {
		return 4;
	}
	
	public void transform(Matrix source, Matrix dest) {
		if ((source.getSizeX() != inputSize) ||
			(source.getSizeY() != 1))
			throw new IllegalArgumentException("Transform received invalid point");
		dest.resize(outputSize, 1);
		dest.setItem(0, 0, c + a * source.getItem(0, 0) - b * source.getItem(1, 0));
		dest.setItem(1, 0, d + b * source.getItem(0, 0) + a * source.getItem(1, 0));
	}

	public String toString() {
		return 
			"A=" + Double.toString(a) + 
			"\nB=" + Double.toString(b) + 
			"\nC=" + Double.toString(c) + 
			"\nD=" + Double.toString(d); 
	}
	
	public void toXML(Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("A", Double.toString(a)));
		dest.addContent(XMLHelper.makeAttrEl("B", Double.toString(b)));
		dest.addContent(XMLHelper.makeAttrEl("C", Double.toString(c)));
		dest.addContent(XMLHelper.makeAttrEl("D", Double.toString(d)));
	}
	
	public static Helmert2DTransformer fromXML(Element source) throws JDOMException {
		Helmert2DTransformer r = new Helmert2DTransformer();
		r.a = Double.parseDouble(XMLHelper.getAttrEl(source, "A"));
		r.b = Double.parseDouble(XMLHelper.getAttrEl(source, "B"));
		r.c = Double.parseDouble(XMLHelper.getAttrEl(source, "C"));
		r.d = Double.parseDouble(XMLHelper.getAttrEl(source, "D"));
		return r;
	}
}
