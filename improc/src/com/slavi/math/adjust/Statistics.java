package com.slavi.math.adjust;

import java.util.Locale;


/**
 * Формулите са взети от "Теория на математическата обработка на геодезическите
 * измервания", Техника, 1988, проф.к.т.н.инж.Стефан Н. Атанасов.
 */
public class Statistics {

	/**
	 * Вероятността (по подразбиране) за попадане на случайната величина в
	 * интервал, симетричен спрямо математическото очакване.
	 */
	public static final double CDefaultStatB = 0.98;

	// Констати, определящи елементите, които ще се показват при
    // отпечатване (процедура Display). Изпозват се в комбинации с
    // побитово И (OR).

	// Доверителен интервал [J start, J end]
	public static final int CStatJ        = 0x0001;  
	// Асиметрия и ексцес
	public static final int CStatAE       = 0x0002;
	// Стойностите MinX и MaxX
	public static final int CStatMinMax   = 0x0004;
	// Стойностите Min(Abs(X)) и Max(Abs(X))
	public static final int CStatAbs      = 0x0008;
	// Стойността MaxX - MinX
	public static final int CStatDelta    = 0x0010;
	// Таблица със начален (M) и централен (D) моменти
	public static final int CStatMD       = 0x0020;
	// Съобщение 'There is(are) bad values'
	public static final int CStatErrors   = 0x0040;
	
	public static final int CStatAll      = CStatJ | CStatAE | CStatMinMax | CStatAbs | CStatDelta | CStatMD | CStatErrors;
	
	public static final int CStatShort    = CStatJ | CStatMinMax | CStatErrors;

	public static final int CStatDetail   = CStatJ | CStatMinMax | CStatAbs | CStatDelta | CStatErrors;
	
	public static final int CStatDefault  = CStatDetail;

    /**
	 * Доверителна вероятност, стр.53, стойности близки до 1, но по-малки
	 * обикновено са 0.9; 0.95 или 0.96. Използва се за определяне на
	 * Доверителния интервал на средно тежестното, стр.53,285.
	 */
    protected double B = CDefaultStatB;

    // Брой на "добрите", с които е изчислявано.
    protected double MinX;
    protected double MaxX;
    protected double AbsMinX;
    protected double AbsMaxX;
    /**
	 * Начален момент от s-ти ред (M[1] = Математическо очакване Математическо
	 * очакване - средно тежестно, стр.26,48
	 * Average = M[1]
	 */
    protected double M1;
    protected double M2;
    protected double M3;
    protected double M4;

    /**
	 * Централен момент от s-ти ред (D[2] = Дисперсия, стр.26,48
	 * Variance = D[2] / Items.Count - 1
	 * Standard deviation = Sqrt(Variance) 
	 */
    protected double D2;
    protected double D3;
    protected double D4;

    /**
	 * Асиметрия
	 */
    protected double  A;

    /**
	 * Ексцес
	 */
    protected double E;
    
    /**
	 * Доверителен интервал. Всички X от списъка Items, които попадат извън този
	 * интервал се маркират с вдигнат флаг.
	 */
    protected double J_Start;
    
    /**
	 * @see #J_Start
	 */
    protected double  J_End;
	
    protected double sumValues1;
    protected double sumValues2;
    protected double sumValues3;
    protected double sumValues4;
	
	protected double sumWeight;
	
	protected int itemsCount;
	
	public void addValue(double value) {
		addValue(value, 1.0);
	}
	
	public void addValue(double value, double weight) {
		if (weight < 0.0)
			throw new IllegalArgumentException("Negative weight received by Statistics."); 
		
		double absValue = Math.abs(value);
		if (itemsCount == 0) {
			MaxX = MinX = value;
			AbsMinX = AbsMaxX = absValue;
		} else {
			if (value < MinX)
				MinX = value;
			if (value > MaxX) 
				MaxX = value;
			if (absValue < AbsMinX)
				AbsMinX = absValue;
			if (absValue > AbsMaxX)
				AbsMaxX = absValue;
		}
		itemsCount++;
		sumWeight += weight;
		sumValues1 += value * weight;
		sumValues2 += value * value * weight;
		sumValues3 += value * value * value * weight;
		sumValues4 += value * value * value * value * weight;
	}

	public void start() {
		resetCalculations();
	}
	
	public void stop() {
		if (sumWeight <= 0.0)
			return;
		// стр.26,48
		// Пресмятане на Начален момент от 1,2,3 и 4 ред. (1-ви ред = средно тежестно).
		M1 = sumValues1 / sumWeight;
		M2 = sumValues2 / sumWeight;
		M3 = sumValues3 / sumWeight;
		M4 = sumValues4 / sumWeight;

		// стр.26,48
		// Пресмятане на Централен момент от 2,3 и 4 ред. (2-ри ред = дисперсия)
		double m1_2 = M1 * M1;
		D2 = M2 - m1_2;
		D3 = M3 - 3.0 * M1 * M2 + 2.0 * (m1_2 * M1);
		D4 = M4 - 4.0 * M1 * M3 + 6.0 * m1_2 * M2 - 3.0 * (m1_2 * m1_2);

		// стр.27,48
		// Пресмятане на Асиметрия и Ексцес.
		A = D3 == 0 ? 0 : D3 / Math.sqrt(Math.abs(D3));
		E = D4 == 0 ? 0 : (D4 / Math.sqrt(Math.abs(D4))) - 3.0;  

		// стр.54,285
		// Определяне на Доверителния интервал.
		double r = Laplas.get_T_from_Laplas(B) * Math.sqrt(Math.abs(D2));
		J_Start = M1 - r;
		J_End = M1 + r;
	}
	
	public int getItemsCount() {
		return itemsCount;
	}

    public void resetCalculations() {
    	M1 = 0.0;
    	M2 = 0.0;
    	M3 = 0.0;
    	M4 = 0.0;
    	D2 = 0.0;
    	D3 = 0.0;
    	D4 = 0.0;
    	J_Start = J_End = A = E = MaxX = MinX = 0.0;
    	if ((B < 0.0) || (B > 1.0))
    		B = CDefaultStatB;
    	sumValues1 = 0.0;
    	sumValues2 = 0.0;
    	sumValues3 = 0.0;
    	sumValues4 = 0.0;
    	sumWeight = 0.0;
    	itemsCount = 0;
    }
    
    public double getA() {
		return A;
	}

	public double getAbsMaxX() {
		return AbsMaxX;
	}

	public double getAbsMinX() {
		return AbsMinX;
	}

	public double getB() {
		return B;
	}
	
	public void setB(double B) {
		this.B = B;
	}

	public double getD(int index) {
		switch (index) {
			case 2: return D2;
			case 3: return D3;
			case 4: return D4;
			default: throw new IllegalArgumentException("Index out of range [2..4]");
		}
	}

	public double getE() {
		return E;
	}

	public double getJ_End() {
		return J_End;
	}

	public double getJ_Start() {
		return J_Start;
	}

	public double getM(int index) {
		switch (index) {
			case 1: return M1;
			case 2: return M2;
			case 3: return M3;
			case 4: return M4;
			default: throw new IllegalArgumentException("Index out of range [1..4]");
		}
	}

	public double getMaxX() {
		return MaxX;
	}

	public double getMinX() {
		return MinX;
	}

	/**
	 * Средно тежестна стойност
	 */
    public double getAvgValue() {
    	return M1;
    }
    
    public double getStdDeviation() {
    	return Math.sqrt(D2 / (getItemsCount() - 1));
    }
	
    public boolean hasBadValues() {
    	return (MinX < J_Start) || (MaxX > J_End);
    }
    
    public boolean isBad(double value) {
    	return (value < J_Start) || (value > J_End);
    }
    
    public String toString(int style) {
    	StringBuilder b = new StringBuilder();
    	b.append(String.format(Locale.US,
			"Average           = %.4f\n" +
			"Count             = %d\n" +
			"B                 = %.4f",
			new Object[] { new Double(this.getAvgValue()), new Integer(this.getItemsCount()), new Double(this.B) } ));
    	if ((style & CStatJ) != 0)
			b.append(String.format(Locale.US, "\n" +
				"J start           = %.4f\n" +
				"J end             = %.4f", 
				new Object[] { new Double(this.J_Start), new Double(this.J_End) } ));
    	if ((style & CStatAE) != 0)
    		b.append(String.format(Locale.US, "\n" +
				"A                 = %.4f\n" +
				"E                 = %.4f", 
				new Object[] { new Double(this.A), new Double(this.E) } ));
    	if ((style & CStatMinMax) != 0)
    		b.append(String.format(Locale.US, "\n" +
				"min               = %.4f\n" +
				"max               = %.4f", 
				new Object[] { new Double(this.MinX), new Double(this.MaxX) } ));
    	if ((style & CStatAbs) != 0)
    		b.append(String.format(Locale.US, "\n" +
				"min(abs(X))       = %.4f\n" +
				"max(abs(X))       = %.4f", 
				new Object[] { new Double(this.AbsMinX), new Double(this.AbsMaxX) } ));
    	if ((style & CStatDelta) != 0)
    		b.append(String.format(Locale.US, "\n" +
				"max-min           = %.4f", 
				new Object[] { new Double(this.MaxX - this.MinX) } ));
    	if ((style & CStatMD) != 0) {
	    	for (int i = 2; i <= 4; i++)
	    		b.append(String.format(Locale.US, "\nM[%d]              = %.4f", 
	    				new Object[] { new Integer(i), new Double(getM(i)) } ));
	    	for (int i = 2; i <= 4; i++)
	    		b.append(String.format(Locale.US, "\nD[%d]              = %.4f", 
	    				new Object[] { new Integer(i), new Double(getD(i)) } ));
    	}
    	if (((style & CStatErrors) != 0) && hasBadValues()) 
    		b.append("\n*** There is/are BAD value(s) ***");
		return b.toString();
    }
    
    public static String toString2Header(int style) {
    	StringBuilder b = new StringBuilder();
    	b.append("Average\tCount\tB");
    	if ((style & CStatJ) != 0)
			b.append("\tJ start\tJ end");
    	if ((style & CStatAE) != 0)
    		b.append("\tA\tE");
    	if ((style & CStatMinMax) != 0)
    		b.append("\tmin\tmax");
    	if ((style & CStatAbs) != 0)
    		b.append("\tmin(abs(X))\tmax(abs(X))");
    	if ((style & CStatDelta) != 0)
    		b.append("\tmax-min");
    	if ((style & CStatMD) != 0) {
	    	for (int i = 2; i <= 4; i++)
	    		b.append("\tM[" + i + "]");
	    	for (int i = 2; i <= 4; i++)
	    		b.append("\tD[" + i + "]");
    	}
    	if ((style & CStatErrors) != 0) 
    		b.append("\tResult");
		return b.toString();
    }
    
    public static String toString2Header() {
    	return toString2Header(CStatDefault);
    }
    
    public String toString2(int style) {
    	StringBuilder b = new StringBuilder();
    	b.append(Double.toString(this.getAvgValue()) + "\t" + Double.toString(this.getItemsCount()) + "\t" + Double.toString(this.B));
    	if ((style & CStatJ) != 0)
			b.append("\t" + Double.toString(this.J_Start) + "\t" + Double.toString(this.J_End));
    	if ((style & CStatAE) != 0)
    		b.append("\t" + Double.toString(this.A) + "\t" + Double.toString(this.E));
    	if ((style & CStatMinMax) != 0)
    		b.append("\t" + Double.toString(this.MinX) + "\t" + Double.toString(this.MaxX));
    	if ((style & CStatAbs) != 0)
    		b.append("\t" + Double.toString(this.AbsMinX) + "\t" + Double.toString(this.AbsMaxX));
    	if ((style & CStatDelta) != 0)
    		b.append("\t" + Double.toString(this.MaxX - this.MinX));
    	if ((style & CStatMD) != 0) {
	    	for (int i = 2; i <= 4; i++)
	    		b.append("\t" + Double.toString(getM(i)));
	    	for (int i = 2; i <= 4; i++)
	    		b.append("\t" + Double.toString(getD(i)));
    	}
    	if (((style & CStatErrors) != 0) && hasBadValues()) 
    		b.append("\tFAILED");
    	else
    		b.append("\tok");
		return b.toString();
    }
    
    public String toString2() {
    	return toString2(CStatDefault);
    }
    
    public String toString() {
    	return toString(CStatDefault);
    }
}
