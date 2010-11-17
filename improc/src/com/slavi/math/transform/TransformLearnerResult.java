package com.slavi.math.transform;

import com.slavi.math.MathUtil;
import com.slavi.math.adjust.Statistics;

public class TransformLearnerResult {

	public int iteration = 0;
	
	public int dataCount = -1;
	
	public int minGoodRequired = -1;
	
	public int oldGoodCount = -1;
	
	public int oldBadCount = -1;
	
	public int newGoodCount = -1;
	
	public int newBadCount = -1;
	
	public int oldBadNowGood = -1;
	
	public int oldGoodNowBad = -1;
	
	public boolean adjustFailed = true;
	
	public double discrepancyThreshold = 0;
	
	public double recoverDiscrepancy = 0;
	
	public double maxAllowedDiscrepancy = 0;
	
	public final Statistics discrepancyStatistics = new Statistics();
	
	public double getGoodDataRatio() {
		return dataCount <= 0 ? 0 : 100.0 * oldGoodCount / dataCount; 
	}
	
	public boolean isAdjustFailed() {
		return adjustFailed;
	}
	
	public boolean isAdjusted() {
		if (isAdjustFailed())
			return false;
		if (newGoodCount < minGoodRequired)
			return false;
		if (discrepancyThreshold > 0) {
			return (discrepancyStatistics.getMaxX() <= discrepancyThreshold);
		}
		return oldGoodNowBad == 0;
	}
	
	public String toString() {
		String res = 
				"Iteration:                  " + iteration +
				"\nData count:                 " + dataCount + 
				"\nMinimum good required:      " + minGoodRequired +
				"\nBefore adjust good count:   " + oldGoodCount +
				"\nBefore adjust bad count:    " + oldBadCount +
				"\nBefore adjust good ratio:   " + MathUtil.d2(getGoodDataRatio()) + "%";
		if (isAdjustFailed()) {
			res = "Adjust FAILED\n" + res;
		} else {
			res = res + 
				"\nAfter adjust good count:    " + newGoodCount +
				"\nAfter adjust bad count:     " + newBadCount +
				"\nBad before adjust now good: " + oldBadNowGood +
				"\nGood before adjust now bad: " + oldGoodNowBad +
				"\nDicrepancy threshold:       " + MathUtil.d4(discrepancyThreshold) +
				"\nRecover dicrepancy:         " + MathUtil.d4(recoverDiscrepancy) +
				"\nMax allowed dicrepancy:     " + MathUtil.d4(maxAllowedDiscrepancy) +
				"\nDiscrepancy statistics:\n" + discrepancyStatistics;				
		}
		return res;			
	}
}
