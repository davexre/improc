package com.slavi.math.transform;

import java.util.StringTokenizer;

import com.slavi.math.matrix.Matrix;

/**
 * AffineTransformer performs a 6-parametered affine transform in
 * case of a 2-dimensional point space.
 */
public abstract class AffineTransformer<InputType, OutputType> extends BaseTransformer<InputType, OutputType> {

	public Matrix affineCoefs;

	public Matrix origin;

	public AffineTransformer() {
		int inputSize = getInputSize();
		int outputSize = getOutputSize();
		this.affineCoefs = new Matrix(inputSize, outputSize);
		this.origin = new Matrix(outputSize, 1);
	}

	public int getNumberOfCoefsPerCoordinate() {
		return getInputSize() + 1;
	}

	public void transform(InputType source, OutputType dest) {
		int inputSize = getInputSize();
		int outputSize = getOutputSize();
		for (int j = outputSize - 1; j >= 0; j--) {
			double t = 0;
			for (int i = inputSize - 1; i >= 0; i--)
				t += getSourceCoord(source, i) * affineCoefs.getItem(j, i);
			setTargetCoord(dest, j, t + origin.getItem(j, 0));
		}
	}

	/**
	 * Fills the array d with parameters for use with java.awt.geom.AffineTransform.
	 * <p><big><b>This method can be used ONLY if the number of coordinates is 2!</b></big>
	 * <p>Usage:
	 * <p><tt>
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
			(getInputSize() != 2) ||
			(getOutputSize() != 2))
			throw new IllegalArgumentException("AffineTransformer.getMatrix requires a double[6] array and AffineTransformer must be 2D");
		d[0] = affineCoefs.getItem(0, 0);
		d[1] = affineCoefs.getItem(1, 0);
		d[2] = affineCoefs.getItem(0, 1);
		d[3] = affineCoefs.getItem(1, 1);
		d[4] = origin.getItem(0, 0);
		d[5] = origin.getItem(1, 0);
	}

	/**
	 * Sets this transformer to the parameters returned by java.awt.geom.AffineTransform.
	 * <p><big><b>This method can be used ONLY if the number of coordinates is 2!</b></big>
	 * <p>Usage:
	 * <p><tt>
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
			(getInputSize() != 2) ||
			(getOutputSize() != 2))
			throw new IllegalArgumentException("AffineTransformer.setMatrix requires a double[6] array and AffineTransformer must be 2D");
		affineCoefs.setItem(0, 0, d[0]);
		affineCoefs.setItem(1, 0, d[1]);
		affineCoefs.setItem(0, 1, d[2]);
		affineCoefs.setItem(1, 1, d[3]);
		origin.setItem(0, 0, d[4]);
		origin.setItem(1, 0, d[5]);
	}

	public Matrix getMatrix() {
		int inputSize = getInputSize();
		int outputSize = getOutputSize();
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
		int inputSize = getInputSize();
		int outputSize = getOutputSize();
		if ((src.getSizeX() != inputSize + 1) || (src.getSizeY() != outputSize + 1))
			throw new IllegalArgumentException("Invalid matrix size");
		for (int i = inputSize - 1; i >= 0; i--)
			for (int j = outputSize - 1; j >= 0; j--)
				affineCoefs.setItem(i, j, src.getItem(i, j));
		for (int j = outputSize - 1; j >= 0; j--)
			origin.setItem(j, 0, src.getItem(inputSize, j));
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
		int inputSize = getInputSize();
		int outputSize = getOutputSize();
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

	public void fromString2(String str) {
		int inputSize = getInputSize();
		int outputSize = getOutputSize();
		StringTokenizer st = new StringTokenizer(str, "\t");
		for (int j = 0; j < outputSize; j++) {
			for (int i = 0; i < inputSize; i++)
				affineCoefs.setItem(i, j, Double.parseDouble(st.nextToken()));
			origin.setItem(j, 0, Double.parseDouble(st.nextToken()));
		}
	}
}
