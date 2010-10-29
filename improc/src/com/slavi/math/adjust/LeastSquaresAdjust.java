package com.slavi.math.adjust;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.SymmetricMatrix;

public class LeastSquaresAdjust {

	private int numCoefsPerCoordinate;

	private int numCoordinates;

	private SymmetricMatrix nm;

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
		nm = new SymmetricMatrix(numCoefsPerCoordinate);
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

	public boolean calculateWithDebug() {
		if (!canCalculate()) {
			System.out.println("Can not calculate. Not enough data.");
			return false;
		}
		SymmetricMatrix nmCopy = nm.makeCopy();
		if (!nm.inverse()) {
			System.out.println("Inverse of normal matrix failed.");
			return false;
		}
		SymmetricMatrix tmp = new SymmetricMatrix(nm.getSizeM());
		nmCopy.mMul(nm, tmp);
		double deviation = tmp.getSquaredDeviationFromE();
		System.out.println("Inverse of normal matrix precision (squared deviation from E) is: " + MathUtil.d4(deviation));

		unknown.make0();
		for (int i = numCoefsPerCoordinate - 1; i >= 0; i--)
			for (int j = numCoefsPerCoordinate - 1; j >= 0; j--)
				for (int k = numCoordinates - 1; k >= 0; k--)
					unknown.setItem(k, i, unknown.getItem(k, i) + nm.getItem(i, j) * apl.getItem(k, j));
		return true;
	}
	
	public boolean calculate() {
		if (!canCalculate())
			return false;
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
			throw new IllegalArgumentException("Invalid measurement added to Least Square Adjustment.");
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

	/**
	 * Ср.кв.гр.на измерване с тежест единица sqrt([PLL]/(n-u))
	 */
	public double getMedianSquareError() {
		return measurementCount == getRequiredMeasurements() ? 0.0 : 
			Math.sqrt(sumPLL / (measurementCount - numCoefsPerCoordinate));
	}

	public SymmetricMatrix getNm() {
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
		StringBuilder b = new StringBuilder();
		b.append("numCoefsPerCoordinate = " + Integer.toString(numCoefsPerCoordinate) + "\n");
		b.append("numCoordinates        = " + Integer.toString(numCoordinates) + "\n");
		b.append("measurementCount      = " + Integer.toString(measurementCount) + "\n");
		b.append("sumPLL                = " + Double.toString(sumPLL) + "\n");
		b.append("sumP                  = " + Double.toString(sumP) + "\n");
		b.append("sumLL                 = " + Double.toString(sumLL) + "\n");
		b.append("medianSquareError     = " + Double.toString(getMedianSquareError()) + "\n");
		b.append("Normal matrix\n");
		b.append(nm.toString());
		b.append("APL\n");
		b.append(apl.toString());
		b.append("Unknown\n");
		b.append(unknown.toString());

		return b.toString();
	}
}
