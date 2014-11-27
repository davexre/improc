package com.slavi.math.adjust;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.SymmetricMatrix;

public class LeastSquaresAdjust {
	private Logger log = LoggerFactory.getLogger(getClass());

	private Logger log_measurements = LoggerFactory.getLogger(getClass().getName() + ".measurements");
	
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

	public double precision = 1.0 / 10000.0;

	public boolean calculate() {
		if (!canCalculate()) {
			log.error("Can not calculate. Not enough data.");
			return false;
		}
		if (log.isInfoEnabled()) {
			double sumP = getSumP();
			if (sumP == 0.0)
				sumP = 1E-100;
			log.info("Measurements: " + measurementCount +
					", [P]: " + MathUtil.d4(getSumP()) + 
					", Sqrt([PLL]/[P]): " + MathUtil.d4(getMedianSquareError())
			);
		}
		SymmetricMatrix nmCopy = nm.makeCopy();
		if (!nm.inverse()) {
			log.error("Inverse of normal matrix failed.");
			if (log.isDebugEnabled()) {
				log.debug("Normal matrix\n" + nmCopy.toString());
				log.debug("APL\n" + apl.toString());
			}
			return false;
		}
		SymmetricMatrix tmp = new SymmetricMatrix(nm.getSizeM());
		nmCopy.mMul(nm, tmp);
		double deviation = tmp.getSquaredDeviationFromE();
		log.info("Inverse of normal matrix precision (squared deviation from E) is: " + MathUtil.d4(deviation));
		if (deviation > precision) {
			log.error("Inverse of normal matrix non-reliable.");
		}
		if (log.isTraceEnabled() || (deviation > precision)) {
			log.info("Normal matrix\n" + nmCopy.makeSquareMatrix().toString());
			log.info("Inverted Normal Matrix\n" + nm.toString());
			log.info("NM * (NM')\n" + tmp.toString());
			log.info("APL\n" + apl.toString());
		}
		if (deviation > precision) {
			return false;
		}
		calculateUnknowns();
		if (log.isDebugEnabled()) {
			log.debug("UNKNOWNS\n" + unknown.toString());
		}
		return true;
	}
	
	public boolean calculateNoNm_Validation() {
		if (!canCalculate())
			return false;
		if (!nm.inverse())
			return false;
		calculateUnknowns();
		return true;
	}
	
	private void calculateUnknowns() {
		unknown.make0();
		for (int i = numCoefsPerCoordinate - 1; i >= 0; i--)
			for (int j = numCoefsPerCoordinate - 1; j >= 0; j--)
				for (int k = numCoordinates - 1; k >= 0; k--)
					unknown.setItem(k, i, unknown.getItem(k, i) + nm.getItem(i, j) * apl.getItem(k, j));
	}

	public void addMeasurement(Matrix m, double weight, double L, int coordinate) {
		if ((coordinate < 0) || (coordinate >= numCoordinates) || (m.getSizeX() != numCoefsPerCoordinate) || (m.getSizeY() != 1) || (weight <= 0.0))
			throw new IllegalArgumentException("Invalid measurement added to Least Square Adjustment.");
		measurementCount++;
		if (log_measurements.isDebugEnabled()) {
			log_measurements.debug(String.format("N:%1$3d, L:%2$10.4f, A:%3$s", measurementCount, L, m.toOneLineString()));
		}
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
	 * Ср.кв.гр.на измерване с тежест единица sqrt([PLL]/[P])
	 */
	public double getMedianSquareError() {
		if (sumP == 0.0)
			return Double.POSITIVE_INFINITY;
		return Math.sqrt(sumPLL / sumP);
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
