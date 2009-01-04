package com.slavi.improc.pano.my;

public interface MFunctionsNVariables {
	
	public int getNumberOfFunctions();
	
	public int getNumberOfVariables();
	
	/**
	 * Calculates the M functions at the N variables point.
	 * @param variables			Input array. 
	 * @param functionValues	Output array.
	 */
	public void calculate(final double variables[], double functionValues[]) throws Exception;
}
