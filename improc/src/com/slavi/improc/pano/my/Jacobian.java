package com.slavi.improc.pano.my;

import com.slavi.math.matrix.Matrix;

public class Jacobian {
	
	static final double functionPrecision = 1e-15;

	static final double eps = Math.sqrt(functionPrecision);
	
	public static void jacobian(MFunctionsNVariables function, double atPoint[], 
			double functionValues[], Matrix result) throws Exception {
		int m = function.getNumberOfFunctions();
		int n = function.getNumberOfVariables();
		result.resize(n, m);
		
		for (int i = 0 ; i < n; i++) {
			double temp = atPoint[i];
			double h = eps * Math.abs(temp);
			if (h == 0.0) {
				h = eps;
			}
			atPoint[i] = temp + h;
			double fval[] = result.m[i];
			function.calculate(atPoint, fval);
			for (int j = 0; j < m; j++) {
				fval[j] = (fval[j] - functionValues[j]) / h;
			}
			atPoint[i] = temp;
		}		
	}
}
