package com.slavi.statistics;

import java.util.StringTokenizer;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.slavi.matrix.Matrix;
import com.slavi.utils.XMLHelper;

/**
 * AffineTransformer performs a 6-parametered affine transform in 
 * case of a 2-dimensional point space.
 * @author Slavian Petrov
 */
public class AffineTransformer extends BaseTransformer {

	public Matrix affineCoefs;
	
	public Matrix origin;

	protected AffineTransformer() {
	}
	
	public AffineTransformer(int inputSize, int outputSize) {
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.affineCoefs = new Matrix(inputSize, outputSize);
		this.origin = new Matrix(outputSize, 1);
	}
	
	public int getNumberOfCoefsPerCoordinate() {
		return inputSize + 1;
	}
	
	public void transform(Matrix source, Matrix dest) {
		if ((source.getSizeX() != inputSize) ||
			(source.getSizeY() != 1))
			throw new IllegalArgumentException("Transform received invalid point");
		dest.resize(outputSize, 1);
		
		for (int j = outputSize - 1; j >= 0; j--) {
			double t = 0;
			for (int i = inputSize - 1; i >= 0; i--)
				t += source.getItem(i, 0) * affineCoefs.getItem(j, i);
			dest.setItem(j, 0, t + origin.getItem(j, 0));
		}
	}

	/**
	 * Fills the array d with parameters for use with java.awt.geom.AffineTransform.<br/>
	 * <big><b>This method can be used ONLY if the number of coordinates is 2!</b></big><br/>
	 * Usage:<br/>
	 * <tt>
	 * AffineTransformer atr = new AffineTransformer(2); // 2D affine transofrm!!!
	 * ...
	 * double[] d = new double[6];
	 * atr.getMatrix(d);
	 * java.awt.geom.AffineTransform at = new java.awt.geom.AffineTransform(d);
	 * ...
	 * </tt>
	 * @param d Array of double[6]
	 */
	public void getMatrix(double[] d) {
		if (
			(d.length != 6) || 
			(inputSize != 2) ||
			(outputSize != 2))
			throw new IllegalArgumentException("AffineTransformer.getMatrix requires a double[6] array and AffineTransformer must be 2D");
		d[0] = affineCoefs.getItem(0, 0);
		d[1] = affineCoefs.getItem(1, 0);
		d[2] = affineCoefs.getItem(0, 1);
		d[3] = affineCoefs.getItem(1, 1);
		d[4] = origin.getItem(0, 0);
		d[5] = origin.getItem(1, 0);
	}
	
	/**
	 * Sets this transformer to the parameters returned by java.awt.geom.AffineTransform.<br/>
	 * <big><b>This method can be used ONLY if the number of coordinates is 2!</b></big><br/>
	 * Usage:<br/>
	 * <tt>
	 * AffineTransformer atr = new AffineTransformer(2); // 2D affine transofrm!!!
	 * java.awt.geom.AffineTransform at;
	 * ...
	 * double[] d = new double[6];
	 * at.getMatrix(d);
	 * atr.setMatrix(d);
	 * ...
	 * </tt>
	 * @param d Array of double[6]
	 */
	public void setMatrix(double[] d) {
		if (
			(d.length != 6) || 
			(inputSize != 2) ||
			(outputSize != 2))
			throw new IllegalArgumentException("AffineTransformer.setMatrix requires a double[6] array and AffineTransformer must be 2D");
		affineCoefs.setItem(0, 0, d[0]);
		affineCoefs.setItem(1, 0, d[1]);
		affineCoefs.setItem(0, 1, d[2]);
		affineCoefs.setItem(1, 1, d[3]);
		origin.setItem(0, 0, d[4]);
		origin.setItem(1, 0, d[5]);
	}
	
	public Matrix getMatrix() {
		Matrix result = new Matrix(inputSize + 1, outputSize + 1);
		for (int i = inputSize - 1; i >= 0; i--) {
			for (int j = outputSize - 1; j >= 0; j--)
				result.setItem(i, j, affineCoefs.getItem(i, j));
			result.setItem(i, outputSize, 0.0);
		}
		for (int j = outputSize - 1; j >= 0; j--)
			result.setItem(inputSize, j, origin.getItem(j, 0));
		result.setItem(inputSize, outputSize, 1.0);
		return result;
	}
	
	public void setMatrix(Matrix src) {
		if ((src.getSizeX() != inputSize) || (src.getSizeY() != outputSize)) 
			throw new IllegalArgumentException("Invalid matrix size");
		for (int i = inputSize - 1; i >= 0; i--) 
			for (int j = outputSize - 1; j >= 0; j--)
				affineCoefs.setItem(i, j, src.getItem(i, j));
		for (int j = outputSize - 1; j >= 0; j--)
			origin.setItem(inputSize, j, src.getItem(j, 0));
	}
	
	public AffineTransformer getReverseTransformer() {
		if (inputSize != outputSize) 
			throw new IllegalArgumentException("Affine transformation irreversible");
		AffineTransformer result = new AffineTransformer(outputSize, inputSize);
		Matrix m = getMatrix();
		if (!m.inverse()) 
			throw new IllegalArgumentException("Reverse affine transformation calculation failed");
		result.setMatrix(m);
		return result;
	}
	
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Origin\n");
		b.append(origin.toString());
		b.append("Coefs\n");
		b.append(affineCoefs.toString());
		return b.toString();
	}
	
	public String toString2() {
		StringBuilder b = new StringBuilder();
		for (int j = 0; j < outputSize; j++) {
			for (int i = 0; i < inputSize; i++) {
				if ( (i != 0) && (j != 0) )
					b.append("\t");
				b.append(affineCoefs.getItem(i, j));
			}
			b.append("\t");
			b.append(origin.getItem(j, 0));
		}
		return b.toString();
	}
	
	public static AffineTransformer fromString2(int inputSize, int outputSize, String str) {
		StringTokenizer st = new StringTokenizer(str, "\t");
		AffineTransformer r = new AffineTransformer(inputSize, outputSize);
		for (int j = 0; j < outputSize; j++) {
			for (int i = 0; i < inputSize; i++) 
				r.affineCoefs.setItem(i, j, Double.parseDouble(st.nextToken()));
			r.origin.setItem(j, 0, Double.parseDouble(st.nextToken()));
		}
		return r;
	}
	
	public void toXML(Element dest) {
		Element e;
		dest.addContent(XMLHelper.makeAttrEl("inputSize", Integer.toString(inputSize)));
		dest.addContent(XMLHelper.makeAttrEl("outputSize", Integer.toString(outputSize)));
		
		e = new Element("Origin");
		origin.toXML(e);
		dest.addContent(e);

		e = new Element("Coefs");
		affineCoefs.toXML(e);
		dest.addContent(e);
	}
	
	public static AffineTransformer fromXML(Element source) throws JDOMException {
		AffineTransformer r = new AffineTransformer();
		Element e;

		r.inputSize = Integer.parseInt(XMLHelper.getAttrEl(source, "inputSize"));
		r.outputSize = Integer.parseInt(XMLHelper.getAttrEl(source, "outputSize"));

		e = source.getChild("Origin");
		r.origin = Matrix.fromXML(e);

		e = source.getChild("Coefs");
		r.affineCoefs = Matrix.fromXML(e);
		
		if (
			(r.affineCoefs.getSizeX() != r.inputSize) ||
			(r.affineCoefs.getSizeY() != r.outputSize) ||
			(r.origin.getSizeX() != r.outputSize) ||
			(r.origin.getSizeY() != 1))
			throw new IllegalArgumentException("Invalid data for affine transformer");
		return r;
	}
}
