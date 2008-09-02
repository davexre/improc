package com.slavi.math.statistics;

import com.slavi.math.adjust.Laplas;

/**
 * Формулите са взети от "Теория на математическата обработка на геодезическите
 * измервания", Техника, 1988, проф.к.т.н.инж.Стефан Н. Атанасов.
 */
public class Statistics extends StatisticsBase {

	protected int itemsCount;
	
	protected double oneOverSumWeights = 1.0;
    
	public Statistics() {
		itemsCount = 0;
	}

	public int getItemsCount() {
		return itemsCount;
	}

    public void resetCalculations() {
    	super.resetCalculations();
    	itemsCount = 0;
    }
	
    public double getComputedWeight(StatisticsItem item) {
    	return item.isBad() ? 0.0 : item.getWeight() * oneOverSumWeights;
    }
    
    public int calculateOne(Iterable<? extends StatisticsItem> data) {
    	resetCalculations();

		// Да намериме най-малката тежест, както и броя на
		// "Добрите" елементи.
    	for (StatisticsItem item : data) {
    		if (!item.isBad()) {
    			double X = item.getValue();
    			double absX = Math.abs(X);
    			if (itemsCount == 0) {
    				MaxX = MinX = X;
    				AbsMinX = AbsMaxX = absX;
    			} else {
    				if (X < MinX)
    					MinX = X;
    				if (X > MaxX) 
    					MaxX = X;
    				if (absX < AbsMinX)
    					AbsMinX = absX;
    				if (absX > AbsMaxX)
    					AbsMaxX = absX;
    			}    				
    			itemsCount++;
    		}
    	}
    	
    	if (itemsCount == 0) 
    		return 0;       // Няма елементи - няма изчисления.

		int goodCount = 0;
    	double sumWeight = 0.0;
    	for (StatisticsItem item : data) {
    		if (!item.isBad()) {
    			double weight = item.getWeight();
    			// Тежестите трябва ВИНАГИ да са положителни
    			if (weight < 0.0)
    				throw new IllegalArgumentException("Negative weight received by Statistics."); 
    			goodCount++;
    			sumWeight += weight;
    		}
    	}
    	
    	// стр.22,48
    	// Сумата от тежестите = sum(NewP) = sum( abs(P)/sum(abs(P)) ) = 1
    	// Тежест, изчислена по формулата P/sum(P) или тежестта, с която са
    	// извършени сметките.
		if (sumWeight == 0.0) {
			oneOverSumWeights = 1.0 / goodCount;
		} else {
			oneOverSumWeights = 1.0 / sumWeight;
		}

    	for (StatisticsItem item : data) {
			if (!item.isBad()) {
		        // стр.26,48
		        // Пресмятане на Начален момент от 1,2,3 и 4 ред. (1-ви ред = средно тежестно).
				double value = item.getValue();
				double weight = item.getWeight() * oneOverSumWeights;
		        double r = value * weight;
		        double r1 = value * value;
		        M[1] = M[1] + r;
		        M[2] = M[2] + r1 * weight;
		        M[3] = M[3] + r1 * r;
		        M[4] = M[4] + r1 * r1 * weight;
			}				
		}
    	
    	for (StatisticsItem item : data) {
			if (!item.isBad()) {
		        // стр.26,48
		        // Пресмятане на Централен момент от 2,3 и 4 ред. (2-ри ред = дисперсия)
				double weight = item.getWeight() * oneOverSumWeights;
		        double r = item.getValue() - M[1];
		        double r1 = r * r;
		        D[2] = D[2] + r1 * weight;
		        D[3] = D[3] + r1 * r * weight;
		        D[4] = D[4] + r1 * r1 * weight;
			}
		}

		// стр.27,48
		// Пресмятане на Асиметрия и Ексцес.
		if (D[3] != 0)
			A = D[3] / Math.sqrt(Math.abs(D[3]));
		if (D[4] != 0)
			E = (D[4] / Math.sqrt(D[4])) - 3.0;

		// стр.54,285
		// Определяне на Доверителния интервал.
		double r = Laplas.get_T_from_Laplas(B) * Math.sqrt(D[2]);
		J_Start = M[1] - r;
		J_End = M[1] + r;

		// И сега да маркираме тези, които попадат ИЗВЪН доверителния интервал.
		int badCount = 0;
    	for (StatisticsItem item : data) {
			if (!item.isBad()) {
				double value = item.getValue();
				if ((value < J_Start) || (value > J_End)) {
					badCount++;
					item.setBad(true);
				}
			}
		}
    	return badCount;
    }
}
