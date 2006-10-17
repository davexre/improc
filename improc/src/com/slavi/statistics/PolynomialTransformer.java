package com.slavi.statistics;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.matrix.Matrix;
import com.slavi.utils.XMLHelper;

public class PolynomialTransformer extends BaseTransformer {

	public int polynomPower;
	
	public Matrix sourceOrigin;
	
	public Matrix polynomPowers;
	
	public Matrix polynomCoefs;
	
	protected int numPoints;
	
	private Matrix point; // used by transform() method
	
	private PolynomialTransformer() {
	}
	
	public PolynomialTransformer(int polynomPower, int inputSize, int outputSize) {
		this.polynomPower = polynomPower;
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		buildPolynomPowers();

		sourceOrigin = new Matrix(inputSize, 1);
		polynomCoefs = new Matrix(outputSize, numPoints);
		point = new Matrix(inputSize, 1);
	}	

	public int getNumberOfCoefsPerCoordinate() {
		return numPoints;
	}
	
	private static final String coefNames[] = {"X", "Y", "Z"};
	
	public String getCoefIndexText(int polynomCoefIndex) {
		StringBuffer b = new StringBuffer();
		String prefix = "";
		boolean useCoefNames = (inputSize <= coefNames.length);
		for (int i = 0; i < inputSize; i++) {
			b.append(prefix);
			if (useCoefNames) {
				b.append(coefNames[i]);
			} else {
				b.append("Coord[");
				b.append(i);
				b.append("]");
			}
			b.append("^");
			b.append(Math.round(polynomPowers.getItem(i, polynomCoefIndex)));
			prefix = " * ";
		}
		return b.toString();
	}

	protected void buildPolynomPowers() {
		numPoints = (int) Math.pow(polynomPower, inputSize);
		polynomPowers = new Matrix(inputSize, numPoints);
		for (int j = numPoints - 1; j >= 0; j--) {
			int j2 = j;
			for (int i = inputSize - 1; i >= 0; i--) {
				int tmp = (int)Math.pow(polynomPower, i);
				polynomPowers.setItem(i, j, j2 / tmp);
				j2 %= tmp;
			}
		}
	}

	public void transform(Matrix source, Matrix dest) {
		if ((source.getSizeX() != inputSize) ||
				(source.getSizeY() != 1))
				throw new Error("Transform received invalid point");
		dest.resize(outputSize, 1);
		
		source.mSub(sourceOrigin, point);
		dest.make0();
		for (int j = 0; j < numPoints; j++) {
			double t = 1;
			for (int i = 0; i < inputSize; i++) {
				t *= Math.pow(point.getItem(i, 0), polynomPowers.getItem(i, j));
			}
			for (int i = 0; i < outputSize; i++) {
				dest.setItem(i, 0, dest.getItem(i, 0) +
						t * polynomCoefs.getItem(i, j));
			}
		}
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Origin source\n");
		b.append(sourceOrigin.toString());
		b.append("Coefs\n");
		for (int j = 0; j < polynomCoefs.getSizeY(); j++) {
			b.append(getCoefIndexText(j));
			b.append("\t");
			for (int i = 0; i < polynomCoefs.getSizeX(); i++) {
				b.append(Double.toString(polynomCoefs.getItem(i, j)));
				b.append("\t");
			}
			b.append("\n");
		}
		
		//b.append(polynomCoefs.toString());
		return b.toString();
	}
		
	public void toXML(Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("polynomPower", Integer.toString(polynomPower)));
		dest.addContent(XMLHelper.makeAttrEl("inputSize", Integer.toString(inputSize)));
		dest.addContent(XMLHelper.makeAttrEl("outputSize", Integer.toString(outputSize)));

		Element e;
		
		e = new Element("SourceOrigin");
		sourceOrigin.toXML(e);
		dest.addContent(e);
		
		e = new Element("Coefs");
		polynomCoefs.toXML(e);
		dest.addContent(e);

		e = new Element("Powers");
		polynomPowers.toXML(e);
		dest.addContent(e);
	}

	public static PolynomialTransformer fromXML(Element source) throws JDOMException {
		PolynomialTransformer r = new PolynomialTransformer();

		r.polynomPower = Integer.parseInt(XMLHelper.getAttrEl(source, "polynomPower"));
		r.inputSize = Integer.parseInt(XMLHelper.getAttrEl(source, "inputSize"));
		r.outputSize = Integer.parseInt(XMLHelper.getAttrEl(source, "outputSize"));
		r.buildPolynomPowers();
		r.sourceOrigin = Matrix.fromXML(source.getChild("SourceOrigin"));
		r.polynomCoefs = Matrix.fromXML(source.getChild("polynomCoefs"));
		r.point = new Matrix(r.inputSize, 1);
		
		if (
			(r.sourceOrigin.getSizeX() != r.inputSize) ||
			(r.sourceOrigin.getSizeY() != 1) ||
			(r.polynomCoefs.getSizeX() != r.outputSize) ||
			(r.polynomCoefs.getSizeY() != r.numPoints) )
			throw new Error("XML file contains malformed PolynomialTransformer data");
		return r;
	}
}
