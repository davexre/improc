package com.slavi.statistics;

import java.util.ArrayList;

/**
 * Формулите са взети от "Теория на математическата обработка на геодезическите
 * измервания", Техника, 1988, проф.к.т.н.инж.Стефан Н. Атанасов.
 */
public class Statistics extends StatisticsBase {

	protected int itemsCount;
    
	public Statistics() {
		itemsCount = 0;
	}
	
    public int calculateOne(ArrayList items, Statistician stat) {
    	resetCalculations();

		// Да намериме най-малката тежест, както и броя на
		// "Добрите" елементи.
    	for (int i = items.size() - 1; i >= 0; i--) {
    		Object item = items.get(i);
    		if (!stat.isBad(item)) {
    			double X = stat.getValue(item);
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

    	double sumWeight = 0.0;
    	for (int i = items.size() - 1; i >= 0; i--) {
    		Object item = items.get(i);
    		if (!stat.isBad(item)) {
    			double weight = stat.getWeight(item);
    			// Тежестите трябва ВИНАГИ да са положителни
    			if (weight < 0.0)
    				throw new Error("Negative weight received by TStatistics."); 
    			sumWeight += weight;
    		}
    	}

    	if (sumWeight == 0) {
    		double computedWeight = (double)1 / itemsCount;
    		for (int i = items.size() - 1; i >= 0; i--) {
    			Object item = items.get(i);
    			if (stat.isBad(item))
    				stat.setComputedWeight(item, 0.0);
    			else
    				stat.setComputedWeight(item, computedWeight);
    		}
    	} else {
		    // стр.22,48
		    // Сумата от тежестите = sum(NewP) = sum( abs(P)/sum(abs(P)) ) = 1
    		if (sumWeight != 1)
	    		for (int i = items.size() - 1; i >= 0; i--) {
	    			Object item = items.get(i);
	    			if (stat.isBad(item))
	    				stat.setComputedWeight(item, 0.0);
	    			else
	    				stat.setComputedWeight(item, stat.getWeight(item) / sumWeight);
	    		}
    	}

		for (int i = items.size() - 1; i >= 0; i--) {
			Object item = items.get(i);
			if (!stat.isBad(item)) {
		        // стр.26,48
		        // Пресмятане на Начален момент от 1,2,3 и 4 ред. (1-ви ред = средно тежестно).
				double value = stat.getValue(item);
				double weight = stat.getComputedWeight(item);
		        double r = value * weight;
		        double r1 = value * value;
		        M[1] = M[1] + r;
		        M[2] = M[2] + r1 * weight;
		        M[3] = M[3] + r1 * r;
		        M[4] = M[4] + r1 * r1 * weight;
			}				
		}
    	
		for (int i = items.size() - 1; i >= 0; i--) {
			Object item = items.get(i);
			if (!stat.isBad(item)) {
		        // стр.26,48
		        // Пресмятане на Централен момент от 2,3 и 4 ред. (2-ри ред = дисперсия)
				double weight = stat.getComputedWeight(item);
		        double r = stat.getValue(item) - M[1];
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
		for (int i = items.size() - 1; i >= 0; i--) {
			Object item = items.get(i);
			if (!stat.isBad(item)) {
				double value = stat.getValue(item);
				if ((value < J_Start) || (value > J_End)) {
					badCount++;
					stat.setBad(item, true);
				}
			}
		}
    	return badCount;
    }
    
    public void killBad(ArrayList items, Statistician stat) {
		for (int i = items.size() - 1; i >= 0; i--) 
			if (stat.isBad(items.get(i)))
				items.remove(i);
    }

	public int getItemsCount() {
		return itemsCount;
	}

    public void resetCalculations() {
    	super.resetCalculations();
    	itemsCount = 0;
    }
}
