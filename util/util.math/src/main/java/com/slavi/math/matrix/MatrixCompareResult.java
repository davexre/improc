package com.slavi.math.matrix;

public class MatrixCompareResult {

	/**
	 * AvgA = Sum(A[i]) / N
	 */
	public double AvgA;

	/**
	 * AvgB = Sum(B[i]) / N
	 */
	public double AvgB;

	/**
	 * SAA = Sum( SQR( A[i] - AvgA ) )
	 */
	public double SAA;

	/**
	 * SBB = Sum( SQR( B[i] - AvgB ) )
	 */
	public double SBB;

	/**
	 * SAB = Sum( ( A[i] - AvgA ) * ( B[i] - AvgB ) )
	 */
	public double SAB;

	/**
	 * Pearson's correlation coefficient.
	 */
	public double PearsonR;
	
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("AvgA     = " + Double.toString(AvgA) + "\n");
		b.append("AvgB     = " + Double.toString(AvgB) + "\n");
		b.append("SAA      = " + Double.toString(SAA) + "\n");
		b.append("SBB      = " + Double.toString(SBB) + "\n");
		b.append("SAB      = " + Double.toString(SAB) + "\n");
		b.append("PearsonR = " + Double.toString(PearsonR));
		return b.toString();
	}
}
