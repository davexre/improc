package com.slavi.math.adjust;

import com.slavi.math.matrix.Matrix;

public class MatrixStatistics {

	/**
	 * Доверителна вероятност, стр.53, стойности близки до 1, но по-малки
	 * обикновено са 0.9; 0.95 или 0.96. Използва се за определяне на
	 * Доверителния интервал на средно тежестното, стр.53,285.
	 */
	protected double B = Statistics.CDefaultStatB;

	protected Matrix MinX;
	protected Matrix MaxX;
	protected Matrix AbsMinX;
	protected Matrix AbsMaxX;
	/**
	 * Начален момент от s-ти ред (M[1] = Математическо очакване Математическо
	 * очакване - средно тежестно, стр.26,48
	 * Average = M[1]
	 */
	protected Matrix M1;
	protected Matrix M2;
	protected Matrix M3;
	protected Matrix M4;

	/**
	 * Централен момент от s-ти ред (D[2] = Дисперсия, стр.26,48
	 * Variance = D[2] / Items.Count - 1
	 * Standard deviation = Sqrt(Variance)
	 */
	protected Matrix D2;
	protected Matrix D3;
	protected Matrix D4;

	/**
	 * Асиметрия
	 */
	protected Matrix A;

	/**
	 * Ексцес
	 */
	protected Matrix E;

	/**
	 * Доверителен интервал. Всички X от списъка Items, които попадат извън този
	 * интервал се маркират с вдигнат флаг.
	 */
	protected Matrix J_Start;
	protected Matrix stdDev;
	/**
	 * @see #J_Start
	 */
	protected Matrix J_End;

	protected Matrix sumValues1;
	protected Matrix sumValues2;
	protected Matrix sumValues3;
	protected Matrix sumValues4;

	protected double sumWeight;

	protected int itemsCount;

	public void addValue(Matrix m) {
		addValue(m, 1.0);
	}

	public void addValue(Matrix m, double weight) {
		if (weight < 0.0)
			throw new IllegalArgumentException("Negative weight received by Statistics.");

		if (itemsCount == 0) {
			sumWeight = 0;

			sumValues1 = new Matrix(m.getSizeX(), m.getSizeY());
			sumValues2 = new Matrix(m.getSizeX(), m.getSizeY());
			sumValues3 = new Matrix(m.getSizeX(), m.getSizeY());
			sumValues4 = new Matrix(m.getSizeX(), m.getSizeY());
			stdDev = new Matrix(m.getSizeX(), m.getSizeY());
			M1 = new Matrix(m.getSizeX(), m.getSizeY());
			M2 = new Matrix(m.getSizeX(), m.getSizeY());
			M3 = new Matrix(m.getSizeX(), m.getSizeY());
			M4 = new Matrix(m.getSizeX(), m.getSizeY());
			D2 = new Matrix(m.getSizeX(), m.getSizeY());
			D3 = new Matrix(m.getSizeX(), m.getSizeY());
			D4 = new Matrix(m.getSizeX(), m.getSizeY());
			A = new Matrix(m.getSizeX(), m.getSizeY());
			E = new Matrix(m.getSizeX(), m.getSizeY());
			J_Start = new Matrix(m.getSizeX(), m.getSizeY());
			J_End = new Matrix(m.getSizeX(), m.getSizeY());

			MinX = m.makeCopy();
			MaxX = m.makeCopy();
			AbsMinX = new Matrix(m.getSizeX(), m.getSizeY());
			AbsMaxX = new Matrix(m.getSizeX(), m.getSizeY());
			for (int i = m.getVectorSize() - 1; i >= 0; i--) {
				double v = Math.abs(m.getVectorItem(i));
				AbsMinX.setVectorItem(i, v);
				AbsMaxX.setVectorItem(i, v);
			}
		} else {
			for (int i = m.getVectorSize() - 1; i >= 0; i--) {
				double value = m.getVectorItem(i);
				double absValue = Math.abs(value);
				if (value < MinX.getVectorItem(i))
					MinX.setVectorItem(i, value);
				if (value > MaxX.getVectorItem(i))
					MaxX.setVectorItem(i, value);
				if (absValue < AbsMinX.getVectorItem(i))
					AbsMinX.setVectorItem(i, absValue);
				if (absValue > AbsMaxX.getVectorItem(i))
					AbsMaxX.setVectorItem(i, absValue);
			}
		}
		itemsCount++;
		sumWeight += weight;
		for (int i = m.getVectorSize() - 1; i >= 0; i--) {
			double value = m.getVectorItem(i);
			double tmp = weight;
			sumValues1.vectorItemAdd(i, tmp *= value);
			sumValues2.vectorItemAdd(i, tmp *= value);
			sumValues3.vectorItemAdd(i, tmp *= value);
			sumValues4.vectorItemAdd(i, tmp *= value);
		}
	}
	
	public void start() {
		resetCalculations();
	}

	public void stop() {
		if (itemsCount == 0 || sumWeight <= 0.0)
			return;
		// стр.26,48
		// Пресмятане на Начален момент от 1,2,3 и 4 ред. (1-ви ред = средно тежестно).
		double tmp = 1.0 / sumWeight;
		for (int i = sumValues1.getVectorSize() - 1; i >= 0; i--) {
			M1.setVectorItem(i, sumValues1.getVectorItem(i) * tmp);
			M2.setVectorItem(i, sumValues2.getVectorItem(i) * tmp);
			M3.setVectorItem(i, sumValues3.getVectorItem(i) * tmp);
			M4.setVectorItem(i, sumValues4.getVectorItem(i) * tmp);
		}

		for (int i = sumValues1.getVectorSize() - 1; i >= 0; i--) {
			// стр.26,48
			// Пресмятане на Централен момент от 2,3 и 4 ред. (2-ри ред = дисперсия)
			double m1 = M1.getVectorItem(i);
			double m2 = M2.getVectorItem(i);
			double m3 = M3.getVectorItem(i);
			double m4 = M4.getVectorItem(i);
			double m1_2 = m1 * m1;
			double d2, d3, d4;
			D2.setVectorItem(i, d2 = m2 - m1_2);
			D3.setVectorItem(i, d3 = m3 - 3.0 * m1 * m2 + 2.0 * (m1_2 * m1));
			D4.setVectorItem(i, d4 = m4 - 4.0 * m1 * m3 + 6.0 * m1_2 * m2 - 3.0 * (m1_2 * m1_2));

			// стр.27,48
			// Пресмятане на Асиметрия и Ексцес.
			A.setVectorItem(i, d3 == 0 ? 0 : d3 / Math.sqrt(Math.abs(d3)));
			E.setVectorItem(i, d4 == 0 ? 0 : (d4 / Math.sqrt(Math.abs(d4))) - 3.0);
			
			// стр.54,285
			// Определяне на Доверителния интервал.
			double r = Laplas.get_T_from_Laplas(B) * Math.sqrt(Math.abs(d2));
			J_Start.setVectorItem(i, m1 - r);
			J_End.setVectorItem(i, m1 + r);

			double sv1 = sumValues1.getVectorItem(i);
			double sv2 = sumValues2.getVectorItem(i);
			stdDev.setVectorItem(i, Math.sqrt(
					Math.abs(itemsCount * sv2 - sv1 * sv1) /
					(itemsCount * (itemsCount - 1))));
		}
	}

	public int getItemsCount() {
		return itemsCount;
	}

	public void resetCalculations() {
		itemsCount = 0;
	}

	public Matrix getA() {
		return A;
	}

	public Matrix getAbsMaxX() {
		return AbsMaxX;
	}

	public Matrix getAbsMinX() {
		return AbsMinX;
	}

	public double getB() {
		return B;
	}

	public void setB(double B) {
		this.B = B;
	}

	public Matrix getD(int index) {
		switch (index) {
			case 2: return D2;
			case 3: return D3;
			case 4: return D4;
			default: throw new IllegalArgumentException("Index out of range [2..4]");
		}
	}

	public Matrix getE() {
		return E;
	}

	public Matrix getJ_End() {
		return J_End;
	}

	public Matrix getJ_Start() {
		return J_Start;
	}

	public Matrix getM(int index) {
		switch (index) {
			case 1: return M1;
			case 2: return M2;
			case 3: return M3;
			case 4: return M4;
			default: throw new IllegalArgumentException("Index out of range [1..4]");
		}
	}

	public Matrix getMaxX() {
		return MaxX;
	}

	public Matrix getMinX() {
		return MinX;
	}

	/**
	 * Средно тежестна стойност
	 */
	public Matrix getAvgValue() {
		return M1;
	}

	public Matrix getStdDeviation() {
		return stdDev;
	}

	public boolean hasBadValues() {
		for (int i = sumValues1.getVectorSize() - 1; i >= 0; i--) {
			if ((MinX.getVectorItem(i) < J_Start.getVectorItem(i)) ||
				(MaxX.getVectorItem(i) > J_End.getVectorItem(i)))
				return true;
		}
		return false;
	}

	public boolean isBad(Matrix m) {
		if (m.getSizeX() != sumValues1.getSizeX() ||
			m.getSizeY() != sumValues1.getSizeY())
			throw new Error("Bad matrix size");
		for (int i = sumValues1.getVectorSize() - 1; i >= 0; i--) {
			if ((m.getVectorItem(i) < J_Start.getVectorItem(i)) ||
				(m.getVectorItem(i) > J_End.getVectorItem(i)))
				return true;
		}
		return false;
	}

	public String toString(int style) {
		StringBuilder r = new StringBuilder();
		if ((style & Statistics.CStatAvg) != 0) {
			r.append("Count: ").append(getItemsCount()).append("\n");
			r.append("Average\n").append(getAvgValue());
		}
		if ((style & Statistics.CStatStdDev) != 0) {
			r.append("B    : ").append(getB()).append("\n");
			r.append("Std deviation\n").append(getStdDeviation());
		}
		if ((style & Statistics.CStatJ) != 0) {
			r.append("J start\n").append(getJ_Start());
			r.append("J end\n").append(getJ_End());
		}
		if ((style & Statistics.CStatAE) != 0) {
			r.append("A\n").append(getA());
			r.append("E\n").append(getE());
		}
		if ((style & Statistics.CStatMinMax) != 0) {
			r.append("min\n").append(getMinX());
			r.append("max\n").append(getMaxX());
		}
		if ((style & Statistics.CStatAbs) != 0) {
			r.append("min(abs(X))\n").append(getAbsMinX());
			r.append("max(abs(X))\n").append(getAbsMaxX());
		}
		if ((style & Statistics.CStatDelta) != 0) {
			Matrix d = new Matrix();
			getMaxX().mSub(getMinX(), d);
			r.append("max-min\n").append(d);
		}
		if ((style & Statistics.CStatMD) != 0) {
			for (int i = 2; i <= 4; i++) {
				r.append("M[" + i + "]\n").append(getM(i));
			}
			for (int i = 2; i <= 4; i++) {
				r.append("D[" + i + "]\n").append(getD(i));
			}
		}
		if (((style & Statistics.CStatErrors) != 0) && hasBadValues())
			r.append("*** There is/are BAD value(s) ***\n");
		return r.toString().trim();
	}

	public String toString() {
		return toString(Statistics.CStatDefault);
	}
}
