package com.slavi.math.statistics;

/**
 * Единична стойност на изследваната величина.
 */
public interface StatisticsItem {
	/**
	 * Стойност на величината.
	 */
	public double getValue();

	/**
	 * Тежест на величината. Произволно число (не е задължително да е в
	 * интервала 0-1), дори може да е и отрицателно.
	 */
	public double getWeight();

	/**
	 * Тежест, изчислена по формулата P/sum(P) или тежестта, с която са
	 * извършени сметките.
	 */
	public double getComputedWeight();

	/**
	 * @see #getComputedWeight(Object)
	 */
	public void setComputedWeight(double computedWeight);

	/**
	 * Попълва се от CalculateONE. Показва дали дадената стойност попада извън
	 * доверителния интервал. Ако флагът е вдигнат, а това означава "лоша"
	 * стойност - тя ще се изгнорира при следващите извиквания на CalculateONE.
	 */
	public boolean isBad();

	/**
	 * @see #isBad
	 */
	public void setBad(boolean bad);
}
