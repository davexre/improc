package com.slavi.math.transform;

import com.slavi.math.matrix.Matrix;

public abstract class PolynomialTransformer<InputType, OutputType> extends BaseTransformer<InputType, OutputType> {

	public int polynomPower;
	
	public Matrix sourceOrigin;
	
	public Matrix polynomPowers;
	
	public Matrix polynomCoefs;
	
	protected int numPoints;
	
	private double tmpSrc[];	// used by transform() method
	private double tmpDest[]; 	// used by transform() method
	
	public PolynomialTransformer(int polynomPower) {
		int inputSize = getInputSize();
		int outputSize = getOutputSize();
		this.polynomPower = polynomPower;
		buildPolynomPowers();

		sourceOrigin = new Matrix(inputSize, 1);
		polynomCoefs = new Matrix(outputSize, numPoints);
		tmpSrc = new double[inputSize];
		tmpDest = new double[outputSize];
	}	

	public int getNumberOfCoefsPerCoordinate() {
		return numPoints;
	}
	
	private static final String coefNames[] = {"X", "Y", "Z"};
	
	public String getCoefIndexText(int polynomCoefIndex) {
		int inputSize = getInputSize();
		StringBuilder b = new StringBuilder();
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
		int inputSize = getInputSize();
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

	public void transform(InputType source, OutputType dest) {
		int inputSize = getInputSize();
		int outputSize = getOutputSize();
		
		for (int i = 0; i < inputSize; i++) {
			tmpSrc[i] = getSourceCoord(source, i) - sourceOrigin.getItem(i, 0);
		}
		for (int i = 0; i < outputSize; i++) {
			tmpDest[i] = 0.0;
		}
		
		for (int j = 0; j < numPoints; j++) {
			double t = 1;
			for (int i = 0; i < inputSize; i++) {
				t *= Math.pow(tmpSrc[i], polynomPowers.getItem(i, j));
			}
			for (int i = 0; i < outputSize; i++) {
				tmpDest[i] += t * polynomCoefs.getItem(i, j);
			}
		}
		// Copy the result
		for (int i = 0; i < outputSize; i++) {
			setTargetCoord(dest, i, tmpDest[i]);
		}
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
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
}
