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
		a = 1.0;
		b = 0.0;
		c = 0.0;
		d = 0.0;
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

	public void transformBackward(OutputType source, InputType dest) {
		double x = getTargetCoord(source, 0) - c;
		double y = getTargetCoord(source, 1) - d;
		double d2 = a * a + b * b;
		if (d2 == 0.0) {
			setSourceCoord(dest, 0, 0.0);
			setSourceCoord(dest, 1, 0.0);
		} else {
			setSourceCoord(dest, 0, (a * x + b * y) / d2);
			setSourceCoord(dest, 1, (a * y - b * x) / d2);
		}
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
	 * Extraxts the scale and angle parameters of the reverse
	 * transformation coefficients.
	 * params[0] = scale;
	 * params[1] = angle [-pi..pi];
	 * params[2] = translate x
	 * params[3] = translate y
	 */
	public void getParamsBackward(double params[]) {
		double d2 = a * a + b * b;
		if (d2 == 0.0) {
			params[0] = 0.0;
			params[1] = 0.0;
			params[2] = -c;
			params[3] = -d;
		} else {
			double A = a / d2;
			double B = - b / d2;
			params[0] = MathUtil.hypot(A, B);
			params[1] = Math.atan2(B, A);
			params[2] = - (a * c + b * d) / d2;
			params[3] = (b * c - a * d) / d2;
			
		}
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

	public void setParams(double params[]) {
		setParams(params[0], params[1], params[2], params[3]);
	}
	
	public String toString() {
		return 
			"A=" + MathUtil.d4(a) + 
			"\nB=" + MathUtil.d4(b) + 
			"\nC=" + MathUtil.d4(c) + 
			"\nD=" + MathUtil.d4(d); 
	}
}
