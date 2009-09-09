package com.slavi.math.transform;

import com.slavi.math.adjust.Statistics;

public class TransformLearnerResult {

	public int dataCount = -1;
	
	public int minGoodRequired = -1;
	
	public int oldGoodCount = -1;
	
	public int oldBadCount = -1;
	
	public int newGoodCount = -1;
	
	public int newBadCount = -1;
	
	public int oldBadNowGood = -1;
	
	public int oldGoodNowBad = -1;
	
	public boolean adjustFailed = true;
	
	public final Statistics discrepancyStatistics = new Statistics();
	
	public boolean isAdjustFailed() {
		return adjustFailed;
	}
	
	public boolean isAdjusted() {
		if (newGoodCount < 0)
			return false;
		if (newGoodCount < minGoodRequired)
			return false;
		return oldGoodNowBad == 0;
	}
	
	public String toString() {
		String res = 
				"Data count:                 " + dataCount + 
				"\nMinimum good required:      " + minGoodRequired +
				"\nBefore adjust good count:   " + oldGoodCount +
				"\nBefore adjust bad count:    " + oldBadCount;
		if (isAdjustFailed()) {
			res = "Adjust FAILED\n" + res;
		} else {
			res = res + 
				"\nAfter adjust good count:    " + newGoodCount +
				"\nAfter adjust bad count:     " + newBadCount +
				"\nBad before adjust now good: " + oldBadNowGood +
				"\nGood before adjust now bad: " + oldGoodNowBad +
				"\nDiscrepancy statistics:\n" + discrepancyStatistics;				
		}
		return res;			
	}
}
