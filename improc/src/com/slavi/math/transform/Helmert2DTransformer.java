package com.slavi.math.transform;


/**
 * Helmert2DTransformer performs a 4-parametered affine transform
 * 
 * X(target) = a * X(source) - b * Y(source) + c
 * Y(target) = b * X(source) + a * Y(source) + d
 * 
 * @author Slavian Petrov
 */
public abstract class Helmert2DTransformer<InputType, OutputType> extends BaseTransformer<InputType, OutputType> {
	
	public double a; // a = cos(Angle) * scaleX 
	public double b; // b = sin(Angle) * scaleY
	public double c; // c = translate x
	public double d; // d = translate y
	
	public Helmert2DTransformer() {
	}
	
	public int getInputSize() {
		return 2;
	}

	public int getOutputSize() {
		return 2;
	}

	public int getNumberOfCoefsPerCoordinate() {
		return 4;
	}
	
	public void transform(InputType source, OutputType dest) {
		double x = getSourceCoord(source, 0);
		double y = getSourceCoord(source, 1);
		setTargetCoord(dest, 0, c + a * x - b * y);
		setTargetCoord(dest, 1, d + b * x + a * y);
	}

	public String toString() {
		return 
			"A=" + Double.toString(a) + 
			"\nB=" + Double.toString(b) + 
			"\nC=" + Double.toString(c) + 
			"\nD=" + Double.toString(d); 
	}
}
