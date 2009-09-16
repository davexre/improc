package com.slavi.math;

import java.util.Locale;

import com.slavi.math.matrix.Matrix;

public class MathUtil {
	public static double C2PI = Math.PI * 2.0;
	public static double PIover2 = Math.PI / 2.0;

	public static final double deg2rad = Math.PI / 180;
	
	public static final double rad2deg = 180 / Math.PI;
	
	public static final double rad2grad = 200 / Math.PI;

	public static final double grad2rad = Math.PI / 200;

	public static String d20(double d) {
		return String.format(Locale.US, "%1$1.20f", d);
	}
	
	public static String d4(double d) {
		return String.format(Locale.US, "%1$10.4f", d);
	}
	
	public static String d2(double d) {
		return String.format(Locale.US, "%1$1.2f", d);
	}
	
	public static String rad2radStr(double angle) {
		return String.format(Locale.US, "%+9.7f rad", angle);
	}
	
	public static String rad2gradStr(double angle) {
		return String.format(Locale.US, "%+1.5f", angle * rad2grad);
	}
	
	public static String rad2degStr(double angle) {
		angle = angle * rad2deg;
		double gr = (int) angle;
		if ((gr == 0) & (angle < 0))
			gr = -0.0000000000001;
		angle = Math.abs(angle - gr) * 60;
		int min = (int) Math.floor(angle);
		double sec = (angle - min) * 60;
		return String.format(Locale.US, "% 4.0f°%2d'%4.1f\"", gr, min, sec);
	}
	
	/**
	 * Returns the specified angle in the range [0..2*pi)
	 */
	public static double fixAngle2PI(double angle) {
		return Math.abs(angle - Math.floor(angle / C2PI) * C2PI);
	}

	/**
	 * Returns the specified angle in the range (-pi..pi] 
	 */
	public static double fixAngleMPI_PI(double angle) {
		angle += Math.PI;
		return Math.abs(angle - Math.floor(angle / C2PI) * C2PI) - Math.PI;
	}
	
	/**
	 * Returns the specified angle in the range [0..pi)
	 */
	public static double fixAnglePI(double angle) {
		return Math.abs(angle - Math.floor(angle / Math.PI) * Math.PI);
	}

	/**
	 * Set matrix elements based on Euler angles rx, ry, rz.
	 * @param type If true returns (mz*mx)*my otherwise returns (mx*mz)*my.
	 */
	public static Matrix makeAngles(double rx, double ry, double rz, boolean type) {
		/*
		 *  mx            my            mz
		 *  1   0   0     cb  0  sb     cc -sc  0
		 *  0  ca -sa      0  1  0      sc  cc  0
		 *  0  sa  ca    -sb  0  cb      0   0  1
		 */
		double sa = Math.sin(rx);
		double ca = Math.cos(rx);

		double sb = Math.sin(ry);
		double cb = Math.cos(ry);

		double sc = Math.sin(rz);
		double cc = Math.cos(rz);
		
		Matrix r = new Matrix(3, 3);
		if (type) {
			/*
			 *	mz*mx
			 *  cc		-sc*ca		sc*sa
			 *  sc		cc*ca		-cc*sa
			 *  0		sa			ca
			 *  
			 *  (mz*mx)*my
			 *  cc*cb+sb*sc*sa		-sc*ca		sb*cc+cb*sc*sa
			 *  sc*cb+sb*cc*sa		cc*ca		sc*sb-cb*cc*sa
			 *  -ca*sb				sa			ca*cb
			 */
			r.setItem(0, 0, cc*cb+sb*sc*sa);
			r.setItem(1, 0, sc*ca);
			r.setItem(2, 0, -sb*cc+cb*sc*sa);
			
			r.setItem(0, 1, -sc*cb+sb*cc*sa);
			r.setItem(1, 1, cc*ca);
			r.setItem(2, 1, sc*sb+cb*cc*sa);
			
			r.setItem(0, 2, ca*sb);
			r.setItem(1, 2, -sa);
			r.setItem(2, 2, ca*cb);
		} else {
			/*
			 *	mx*mz
			 *  cc		-sc		0
			 *  ca*sc	ca*cc	-sa
			 *  sa*sc	sa*cc	ca
			 *  
			 *  (mx*mz)*my
			 *  cc*cb				-sc			sb*cc
			 *  ca*sc*cb+sa*sb		ca*cc		ca*sc*sb-sa*cb
			 *  sa*sc*cb-ca*sb		sa*cc		sa*sc*sb+ca*cb
			 */
			r.setItem(0, 0, cc*cb);
			r.setItem(1, 0, sc);
			r.setItem(2, 0, -sb*cc);
			
			r.setItem(0, 1, -ca*sc*cb+sa*sb);
			r.setItem(1, 1, ca*cc);
			r.setItem(2, 1, ca*sc*sb+sa*cb);
			
			r.setItem(0, 2, sa*sc*cb+ca*sb);
			r.setItem(1, 2, -sa*cc);
			r.setItem(2, 2, -sa*sc*sb+ca*cb);
		}
		return r;
	}

	/**
	 * Transfer the sign of B on to A. 
	 */
	public static final double SIGN(double a, double b) {
		return ((b) >= 0.0 ? Math.abs(a) : -Math.abs(a));
	}
	
	/** 
	 * sqrt(a^2 + b^2) without under/overflow. 
	 */
	public static double hypot(double a, double b) {
		double r;
		double absA = Math.abs(a);
		double absB = Math.abs(b);
		if (absA > absB) {
			r = b / a;
			r = absA * Math.sqrt(1 + r * r);
		} else if (b != 0) {
			r = a / b;
			r = absB * Math.sqrt(1 + r * r);
		} else {
			r = 0.0;
		}
		return r;
	}
}
