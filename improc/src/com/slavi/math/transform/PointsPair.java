package com.slavi.math.transform;

public interface PointsPair {
	public boolean isBad();

	public void setBad(boolean bad);

	public double getWeight();

	/**
	 * The distance between target and sourceTransformed. The formula is:
	 * discrepancy = sqrt(sum(pow(target.getItem(i,0) -
	 * soruceTransformed.getItem(i,0), 2)))
	 */
	public double getDiscrepancy();

	public void setDiscrepancy(double discrepancy);

	public double getSourceCoord(int coordIndex);

	public double getTargetCoord(int coordIndex);
}
