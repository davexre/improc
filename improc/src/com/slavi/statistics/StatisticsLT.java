package com.slavi.statistics;

/**
 * Формулите са взети от "Теория на математическата обработка на геодезическите
 * измервания", Техника, 1988, проф.к.т.н.инж.Стефан Н. Атанасов.
 */
public class StatisticsLT extends StatisticsBase {

	protected double sumValues[] = new double[5];
	
	protected int itemsCount;
	
	public void addValue(double value) {
		double absValue = Math.abs(value);
		if (itemsCount == 0) {
			MaxX = MinX = value;
			AbsMinX = AbsMaxX = absValue;
		} else {
			if (value < MinX)
				MinX = value;
			if (value > MaxX) 
				MaxX = value;
			if (absValue < AbsMinX)
				AbsMinX = absValue;
			if (absValue > AbsMaxX)
				AbsMaxX = absValue;
		}
		itemsCount++;
		sumValues[1] += value;
		sumValues[2] += Math.pow(value, 2.0);
		sumValues[3] += Math.pow(value, 3.0);
		sumValues[4] += Math.pow(value, 4.0);
	}

	public void start() {
		resetCalculations();
	}
	
	public void stop() {
		if (itemsCount == 0)
			return;
		// стр.26,48
		// Пресмятане на Начален момент от 1,2,3 и 4 ред. (1-ви ред = средно тежестно).
		M[1] = sumValues[1] / itemsCount;
		M[2] = sumValues[2] / itemsCount;
		M[3] = sumValues[3] / itemsCount;
		M[4] = sumValues[4] / itemsCount;

		// стр.26,48
		// Пресмятане на Централен момент от 2,3 и 4 ред. (2-ри ред = дисперсия)
		D[2] = M[2] - Math.pow(M[1], 2.0);
		D[3] = M[3] - 3.0*M[1]*M[2] + 2.0*Math.pow(M[1], 3.0);
		D[4] = M[4] - 4.0*M[1]*M[3] + 6.0*Math.pow(M[1], 2.0)*M[2] - 3.0*Math.pow(M[1], 4.0);

		// стр.27,48
		// Пресмятане на Асиметрия и Ексцес.
		A = D[3] == 0 ? 0 : D[3] / Math.sqrt(Math.abs(D[3]));
		E = D[4] == 0 ? 0 : (D[4] / Math.sqrt(Math.abs(D[4]))) - 3.0;  

		// стр.54,285
		// Определяне на Доверителния интервал.
		double r = Laplas.get_T_from_Laplas(B) * Math.sqrt(Math.abs(D[2]));
		J_Start = M[1] - r;
		J_End = M[1] + r;
	}
	
	public int getItemsCount() {
		return itemsCount;
	}

    public void resetCalculations() {
    	super.resetCalculations();
    	for (int i = sumValues.length - 1; i >= 0; i--)
    		sumValues[i] = 0.0;
    	itemsCount = 0;
    }
}
