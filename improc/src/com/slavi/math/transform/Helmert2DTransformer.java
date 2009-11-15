package com.slavi.math.transform;

import com.slavi.math.MathUtil;

/**
 * Helmert2DTransformer performs a 4-parametered affine transform
 * 
 * X(target) = a * X(source) - b * Y(source) + c
 * Y(target) = b * X(source) + a * Y(source) + d
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

	/**
	 * Extraxts the scale and angle parameters of the current 
	 * transformation coefficients. The scale parameter is
	 * returned in params[0] and the angle [-pi..pi] is returned
	 * in params[1].
	 */
	public void getParams(double params[]) {
		params[0] = MathUtil.hypot(a, b);
		params[1] = Math.atan2(b, a);
	}
	
	/**
	 * Sets the coefficients a and b to the correct values using
	 * the specified scale and angle parameters. The translation 
	 * coefficients c and d are unchanged. 
	 */
	public void setParams(double scale, double angle) {
		a = scale * Math.cos(angle);
		b = scale * Math.sin(angle);
	}
	
	public String toString() {
		return 
			"A=" + MathUtil.d4(a) + 
			"\nB=" + MathUtil.d4(b) + 
			"\nC=" + MathUtil.d4(c) + 
			"\nD=" + MathUtil.d4(d); 
	}
}
