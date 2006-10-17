package com.slavi.statistics;

/**
 * Единична стойност на изследваната величина.
 */
public interface Statistician {
	/**
	 * Стойност на величината.
	 */
	public double getValue(Object ob);

	/**
	 * Тежест на величината. Произволно число (не е задължително да е в
	 * интервала 0-1), дори може да е и отрицателно.
	 */
	public double getWeight(Object ob);

	/**
	 * Тежест, изчислена по формулата P/sum(P) или тежестта, с която са
	 * извършени сметките.
	 */
	public double getComputedWeight(Object ob);

	/**
	 * @see #getComputedWeight(Object)
	 */
	public void setComputedWeight(Object ob, double computedWeight);

	/**
	 * Попълва се от CalculateONE. Показва дали дадената стойност попада извън
	 * доверителния интервал. Ако флагът е вдигнат, а това означава "лоша"
	 * стойност - тя ще се изгнорира при следващите извиквания на CalculateONE.
	 */
	public boolean isBad(Object ob);

	/**
	 * @see #isBad
	 */
	public void setBad(Object ob, boolean bad);
}
