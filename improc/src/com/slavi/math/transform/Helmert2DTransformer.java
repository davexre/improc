package com.slavi.math.transform;

import java.awt.geom.Point2D;

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
	 * transformation coefficients. In the scale parameter is
	 * returned in params.x and the angle (0..pi) is returned
	 * in params.y.
	 */
	public void getParams(Point2D.Double params) {
		params.x = MathUtil.hypot(a, b);
		params.y = params.x == 0 ? Double.NaN : Math.acos(a / params.x); 
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
			"A=" + Double.toString(a) + 
			"\nB=" + Double.toString(b) + 
			"\nC=" + Double.toString(c) + 
			"\nD=" + Double.toString(d); 
	}
}
