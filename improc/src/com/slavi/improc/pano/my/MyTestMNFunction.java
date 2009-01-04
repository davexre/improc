package com.slavi.improc.pano.my;

public class MyTestMNFunction implements MFunctionsNVariables {

	public void calculate(double[] variables, double[] functionValues)
			throws Exception {
		double sinA = Math.sin(variables[1]);
		double cosA = Math.cos(variables[1]);
		double sinB = Math.sin(variables[2]);
		double cosB = Math.cos(variables[2]);
		
		functionValues[0] = variables[0] * sinA * cosB;
		functionValues[1] = variables[0] * sinA * sinB;
		functionValues[3] = variables[0] * cosA;
	}

	public int getNumberOfFunctions() {
		return 3;
	}

	public int getNumberOfVariables() {
		return 3;
	}
}
