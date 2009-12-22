package com.slavi.math.transform;

import com.slavi.math.MathUtil;

/**
 * Helmert2DTransformer performs a 4-parametered affine transform
 * <pre>
 * X(target) = a * X(source) - b * Y(source) + c
 * Y(target) = b * X(source) + a * Y(source) + d
 * </pre>
 */
public abstract class Helmert2DTransformer<InputType, OutputType> extends BaseTransformer<InputType, OutputType> {
	
	public double a; // a = cos(Angle) * scale 
	public double b; // b = sin(Angle) * scale
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
	 * transformation coefficients.
	 * params[0] = scale;
	 * params[1] = angle [-pi..pi];
	 * params[2] = translate x
	 * params[3] = translate y
	 */
	public void getParams(double params[]) {
		params[0] = MathUtil.hypot(a, b);
		params[1] = Math.atan2(b, a);
		params[2] = c;
		params[3] = d;
	}
	
	/**
	 * Sets the coefficients a and b to the correct values using
	 * the specified scale and angle parameters. The translation 
	 * coefficients c and d are unchanged. 
	 */
	public void setParams(double scale, double angle, double translateX, double translateY) {
		a = scale * Math.cos(angle);
		b = scale * Math.sin(angle);
		c = translateX;
		d = translateY;
	}
	
	public String toString() {
		return 
			"A=" + MathUtil.d4(a) + 
			"\nB=" + MathUtil.d4(b) + 
			"\nC=" + MathUtil.d4(c) + 
			"\nD=" + MathUtil.d4(d); 
	}
}
