package com.slavi.statistics;

public class Laplas {

	/**
	 * Вероятността (по подразбиране) за попадане на случайната величина в
	 * интервал, симетричен спрямо математическото очакване.
	 */
	public static final double defaultStatB = 0.9;

	public static final double[] laplasF = {
		0.0000, 0.0800, 0.1590, 0.2360, 0.3110,
		0.3830, 0.4510, 0.5160, 0.5760, 0.6320,
		0.6830, 0.7290, 0.7700, 0.8060, 0.8380,
		0.8660, 0.8900, 0.9110, 0.9280, 0.9430,
		0.9550, 0.9640, 0.9720, 0.9790, 0.9840,
		0.9880, 0.9910, 0.9930, 0.9950, 0.9960,
		0.9973, 0.9981, 0.9986, 0.9990, 0.9993,
		0.9995, 0.9997, 0.9998, 0.9999, 0.99997,
		0.99999};
  
	/**
	 * Смята функцията, при зададен аргумент
	 * 
	 * @param t
	 * @return Връща стойността на функцията
	 */
	public static final double get_Laplas(double t) {
		if ((t < 0.0) || (t > 4.0))
			throw new Error("getLaplas: Got a bad parameter");
		int tt = (int) (t * 10.0);
		double r = laplasF[tt];
		if (tt + 1 < laplasF.length)
			r = r + (laplasF[tt + 1] - r) * (t - tt / 10.0);
		return r;
	}

	public static final double get_T_from_Laplas(double f_t) {
		if ((f_t < 0.0) || (f_t >= 1.0))
			throw new Error("get_T_from_Laplas: Got a bad parameter");
		int i = laplasF.length - 1;
		while ((i >= 0.0) && (f_t < laplasF[i]))
			i--;
		if (i + 1 < laplasF.length)
			return (i / 10.0 + (f_t - laplasF[i]) * (laplasF[i + 1] - laplasF[i]));
		return 4.0;
	}
}
