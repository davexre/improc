package com.slavi.statistics;

public class StatisticianImpl implements Statistician {

	protected static final StatisticianImpl instance = new StatisticianImpl();
	
	public double getComputedWeight(Object ob) {
		return ((StatisticsItem) ob).getComputedWeight();
	}

	public double getValue(Object ob) {
		return ((StatisticsItem) ob).getValue();
	}

	public double getWeight(Object ob) {
		return ((StatisticsItem) ob).getWeight();
	}

	public boolean isBad(Object ob) {
		return ((StatisticsItem) ob).isBad();
	}

	public void setBad(Object ob, boolean bad) {
		((StatisticsItem) ob).setBad(bad);
	}

	public void setComputedWeight(Object ob, double computedWeight) {
		((StatisticsItem) ob).setComputedWeight(computedWeight);
	}
	
	public static StatisticianImpl getInstance() {
		return instance;
	}
	
	protected StatisticianImpl() {
	}
}
