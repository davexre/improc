package com.slavi.statistics;

import org.jdom.Element;

import com.slavi.matrix.DiagonalMatrix;
import com.slavi.matrix.Matrix;
import com.slavi.utils.XMLHelper;

public class LeastSquaresAdjust {

	private int numCoefsPerCoordinate;

	private int numCoordinates;

	private DiagonalMatrix nm;

	private Matrix apl;

	private Matrix unknown;

	/**
	 * Брой на всички измервания, участващи в изравнението - N
	 */
	private int measurementCount;

	/**
	 * Сума тежест*поправка^2 [PLL]
	 */
	private double sumPLL;

	/**
	 * Сума от тежестите
	 */
	private double sumP;

	/**
	 * Сума от квадратите на несъвпаденията [LL]
	 */
	private double sumLL;

	/**
	 * Ср.кв.гр.на измерване с тежест единица sqrt([PLL]/(n-u))
	 */
	private double medianSquareError;

	/**
	 * "Смисълът" на параметрите numCoefsPerCoordinate и numCoordinates е "намаляване" на
	 * размера на нормалната матрица при повтарящи се коефициенти например: Ако
	 * матрицата на уравненията на поправките е:<br>
	 * <tt><table border=1>
	 * <tr><td>a1</td><td> 0</td><td> 0</td><td>a2</td><td> 0</td><td> 0</td><td>a3</td><td> 0</td><td> 0</td><td>a4</td><td> 0</td><td> 0</td></tr>
	 * <tr><td> 0</td><td>a1</td><td> 0</td><td> 0</td><td>a2</td><td> 0</td><td> 0</td><td>a3</td><td> 0</td><td> 0</td><td>a4</td><td> 0</td></tr>
	 * <tr><td> 0</td><td> 0</td><td>a1</td><td> 0</td><td> 0</td><td>a2</td><td> 0</td><td> 0</td><td>a3</td><td> 0</td><td> 0</td><td>a4</td></tr>
	 * <tr><td>b1</td><td> 0</td><td> 0</td><td>b2</td><td> 0</td><td> 0</td><td>b3</td><td> 0</td><td> 0</td><td>b4</td><td> 0</td><td> 0</td></tr>
	 * <tr><td> 0</td><td>b1</td><td> 0</td><td> 0</td><td>b2</td><td> 0</td><td> 0</td><td>b3</td><td> 0</td><td> 0</td><td>b4</td><td> 0</td></tr>
	 * <tr><td> 0</td><td> 0</td><td>b1</td><td> 0</td><td> 0</td><td>b2</td><td> 0</td><td> 0</td><td>b3</td><td> 0</td><td> 0</td><td>b4</td></tr>
	 * <tr><td colspan=16><center>... ... ...</center></td></tr>
	 * <tr><td>k1</td><td> 0</td><td> 0</td><td>k2</td><td> 0</td><td> 0</td><td>k3</td><td> 0</td><td> 0</td><td>k4</td><td> 0</td><td> 0</td></tr>
	 * <tr><td> 0</td><td>k1</td><td> 0</td><td> 0</td><td>k2</td><td> 0</td><td> 0</td><td>k3</td><td> 0</td><td> 0</td><td>k4</td><td> 0</td></tr>
	 * <tr><td> 0</td><td> 0</td><td>k1</td><td> 0</td><td> 0</td><td>k2</td><td> 0</td><td> 0</td><td>k3</td><td> 0</td><td> 0</td><td>k4</td></tr>
	 * </table></tt><br>
	 * то тя се "редуцира" до матрица от вида:<br>
	 * <tt><table border=1>
	 * <tr><td>a1</td><td>a2</td><td>a3</td><td>a4</td></tr>
	 * <tr><td>b1</td><td>b2</td><td>b3</td><td>b4</td></tr>
	 * <tr><td colspan=4><center>... ... ...</center></td></tr>
	 * <tr><td>k1</td><td>k2</td><td>k3</td><td>k4</td></tr>
	 * </table></tt><br>
	 * което е доста по-икономично ;)<br>
	 * <br>
	 * В този случай numCoefsPerCoordinate=4, numCoordinates=3
	 */
	public LeastSquaresAdjust(int numCoefsPerCoordinate, int numCoordinates) {
		this.numCoefsPerCoordinate = numCoefsPerCoordinate;
		this.numCoordinates = numCoordinates;
		nm = new DiagonalMatrix(numCoefsPerCoordinate);
		apl = new Matrix(numCoordinates, numCoefsPerCoordinate);
		unknown = new Matrix(numCoordinates, numCoefsPerCoordinate);
		clear();
	}

	public LeastSquaresAdjust(int numPoints) {
		this(numPoints, 1);
	}

	public void clear() {
		nm.make0();
		apl.make0();
		unknown.make0();
		measurementCount = 0;
		sumPLL = 0;
		sumP = 0;
		sumLL = 0;
		medianSquareError = 0;
	}

	public int getRequiredMeasurements() {
		return numCoefsPerCoordinate * numCoordinates;
	}

	public int getRequiredPoints() {
		return numCoefsPerCoordinate;
	}

	public boolean canCalculate() {
		return measurementCount >= getRequiredMeasurements();
	}

	public boolean calculate() {
		if (!canCalculate())
			return false;
		if (measurementCount == getRequiredMeasurements())
			medianSquareError = 0;
		else
			medianSquareError = Math.sqrt(sumPLL / (measurementCount - numCoefsPerCoordinate));

		if (!nm.inverse())
			return false;
		unknown.make0();
		for (int i = numCoefsPerCoordinate - 1; i >= 0; i--)
			for (int j = numCoefsPerCoordinate - 1; j >= 0; j--)
				for (int k = numCoordinates - 1; k >= 0; k--)
					unknown.setItem(k, i, unknown.getItem(k, i) + nm.getItem(i, j) * apl.getItem(k, j));
		return true;
	}

	public void addMeasurement(Matrix m, double weight, double L, int coordinate) {
		if ((coordinate < 0) || (coordinate >= numCoordinates) || (m.getSizeX() != numCoefsPerCoordinate) || (m.getSizeY() != 1))
			throw new Error("Invalid measurement added to Least Square Adjustment.");
		measurementCount++;
		double ll = L * L;
		sumPLL += weight * ll;
		sumP += weight;
		sumLL += ll;

		for (int i = 0; i < numCoefsPerCoordinate; i++) {
			double tmp = weight * m.getItem(i, 0);
			apl.setItem(coordinate, i, apl.getItem(coordinate, i) + tmp * L);
			if (coordinate == 0)
				for (int j = i; j < numCoefsPerCoordinate; j++)
					nm.setItem(i, j, nm.getItem(i, j) + tmp * m.getItem(j, 0));
		}
	}

	public Matrix getApl() {
		return apl;
	}

	public int getMeasurementCount() {
		return measurementCount;
	}

	public double getMedianSquareError() {
		return medianSquareError;
	}

	public DiagonalMatrix getNm() {
		return nm;
	}

	public int getNumCoordinates() {
		return numCoordinates;
	}

	public int getNumPoints() {
		return numCoefsPerCoordinate;
	}

	public double getSumLL() {
		return sumLL;
	}

	public double getSumP() {
		return sumP;
	}

	public double getSumPLL() {
		return sumPLL;
	}

	public Matrix getUnknown() {
		return unknown;
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("numCoefsPerCoordinate = " + Integer.toString(numCoefsPerCoordinate) + "\n");
		b.append("numCoordinates        = " + Integer.toString(numCoordinates) + "\n");
		b.append("measurementCount      = " + Integer.toString(measurementCount) + "\n");
		b.append("sumPLL                = " + Double.toString(sumPLL) + "\n");
		b.append("sumP                  = " + Double.toString(sumP) + "\n");
		b.append("sumLL                 = " + Double.toString(sumLL) + "\n");
		b.append("medianSquareError     = " + Double.toString(medianSquareError) + "\n");
		b.append("Normal matrix\n");
		b.append(nm.toString());
		b.append("APL\n");
		b.append(apl.toString());
		b.append("Unknown\n");
		b.append(unknown.toString());

		return b.toString();
	}

	public void toXML(Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("numCoefsPerCoordinate", Integer.toString(numCoefsPerCoordinate)));
		dest.addContent(XMLHelper.makeAttrEl("numCoordinates", Integer.toString(numCoordinates)));
		dest.addContent(XMLHelper.makeAttrEl("measurementCount", Integer.toString(measurementCount)));
		dest.addContent(XMLHelper.makeAttrEl("sumPLL", Double.toString(sumPLL)));
		dest.addContent(XMLHelper.makeAttrEl("sumP", Double.toString(sumP)));
		dest.addContent(XMLHelper.makeAttrEl("sumLL", Double.toString(sumLL)));
		dest.addContent(XMLHelper.makeAttrEl("medianSquareError", Double.toString(medianSquareError)));
		Element e;

		e = new Element("NormalMatrix");
		nm.toXML(e);
		dest.addContent(e);

		e = new Element("APL");
		apl.toXML(e);
		dest.addContent(e);

		e = new Element("Unknown");
		unknown.toXML(e);
		dest.addContent(e);
	}
}
