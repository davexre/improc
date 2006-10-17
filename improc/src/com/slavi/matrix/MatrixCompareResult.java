package com.slavi.matrix;

import org.jdom.Element;

import com.slavi.utils.XMLHelper;

public class MatrixCompareResult {

	/**
	 * First Matrix/DiagonalMatrix compared against the second matrix
	 */
	Object A;

	/**
	 * Second Matrix/DiagonalMatrix
	 */
	Object B;

	/**
	 * AvgA = Sum(A[i]) / N
	 */
	double AvgA;

	/**
	 * AvgB = Sum(B[i]) / N
	 */
	double AvgB;

	/**
	 * SAA = Sum( SQR( A[i] - AvgA ) )
	 */
	double SAA;

	/**
	 * SBB = Sum( SQR( B[i] - AvgB ) )
	 */
	double SBB;

	/**
	 * SAB = Sum( ( A[i] - AvgA ) * ( B[i] - AvgB ) )
	 */
	double SAB;

	/**
	 * Pearson's correlation coefficient.
	 */
	double PearsonR;
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("AvgA     = " + Double.toString(AvgA) + "\n");
		b.append("AvgB     = " + Double.toString(AvgB) + "\n");
		b.append("SAA      = " + Double.toString(SAA) + "\n");
		b.append("SBB      = " + Double.toString(SBB) + "\n");
		b.append("SAB      = " + Double.toString(SAB) + "\n");
		b.append("PearsonR = " + Double.toString(PearsonR));
		return b.toString();
	}
	
	public void toXML(Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("AvgA", Double.toString(AvgA)));
		dest.addContent(XMLHelper.makeAttrEl("AvgB", Double.toString(AvgB)));
		dest.addContent(XMLHelper.makeAttrEl("SAA", Double.toString(SAA)));
		dest.addContent(XMLHelper.makeAttrEl("SBB", Double.toString(SBB)));
		dest.addContent(XMLHelper.makeAttrEl("SAB", Double.toString(SAB)));
		dest.addContent(XMLHelper.makeAttrEl("PearsonR", Double.toString(PearsonR)));
	}
}
