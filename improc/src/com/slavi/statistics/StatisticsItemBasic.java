package com.slavi.statistics;

public class StatisticsItemBasic implements StatisticsItem {

	private double value = 0.0;

	private double weight = 1.0;

	private double computedWeight = 0.0;

	private boolean bad = false;

	public StatisticsItemBasic() {
	}
	
	public StatisticsItemBasic(double value) {
		this.value = value;
	}

	public StatisticsItemBasic(double value, double weight) {
		this.value = value;
		this.weight = weight;
	}

	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}

	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getComputedWeight() {
		return computedWeight;
	}

	public void setComputedWeight(double computedWeight) {
		this.computedWeight = computedWeight;
	}

	public boolean isBad() {
		return bad;
	}

	public void setBad(boolean bad) {
		this.bad = bad;
	}
	
	public String toString() {
		return 	(bad ? "BAD" : "   ") +
			" Value=" + Double.toString(value) + 
			"\tW=" + Double.toString(weight) + 
			"\tComputed wight=" + Double.toString(computedWeight); 
	}
}
