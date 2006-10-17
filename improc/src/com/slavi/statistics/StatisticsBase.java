package com.slavi.statistics;

import org.jdom.Element;

import com.slavi.utils.XMLHelper;

/**
 * Формулите са взети от "Теория на математическата обработка на геодезическите
 * измервания", Техника, 1988, проф.к.т.н.инж.Стефан Н. Атанасов.
 */
public abstract class StatisticsBase {

	/**
	 * Вероятността (по подразбиране) за попадане на случайната величина в
	 * интервал, симетричен спрямо математическото очакване.
	 */
	public static final double CDefStatB = 0.98;

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
	// Таблица с всички стойности. Лошите стойности са
    // винаги маркирани като лоши, независимо от
    // флага CStatErrors. Има значение само за
    // TStatistics, но не и за TStatisticsLT.
	public static final int CStatDetails  = 0x0080;
	
	public static final int CStatAll      = 0x007f;
	
	public static final int CStatDefault = CStatAll; // CStatJ | CStatAE | CStatErrors;

    /**
	 * Доверителна вероятност, стр.53, стойности близки до 1, но по-малки
	 * обикновено са 0.9; 0.95 или 0.96. Използва се за определяне на
	 * Доверителния интервал на средно тежестното, стр.53,285.
	 */
    protected double B = CDefStatB;

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
    protected double M[] = new double[5];

    /**
	 * Централен момент от s-ти ред (D[2] = Дисперсия, стр.26,48
	 * Variance = D[2] / Items.Count - 1
	 * Standard deviation = Sqrt(Variance) 
	 */
    protected double D[] = new double [5];

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
		return D[index];
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
		return M[index];
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
    	return M[1];
    }
    
    public double getStdDeviation() {
    	return Math.sqrt(D[2] / (getItemsCount() - 1));
    }
	
    public boolean hasBadValues() {
    	return (MinX < J_Start) || (MaxX > J_End);
    }
    public abstract int getItemsCount();
    
    public boolean isBad(double value) {
    	return (value < J_Start) || (value > J_End);
    }
    
    protected void resetCalculations() {
    	for (int i = M.length - 1; i >= 0; i--)
    		M[i] = 0.0;
    	for (int i = D.length - 1; i >= 0; i--)
    		D[i] = 0.0;
    	J_Start = J_End = A = E = MaxX = MinX = 0.0;
    	if ((B < 0.0) || (B > 1.0))
    		B = CDefStatB;
    }
    
    public String toString(int style) {
    	StringBuffer b = new StringBuffer();
    	b.append(String.format(
			"Average           = %.4f\n" +
			"Count             = %d\n" +
			"B                 = %.4f",
			new Object[] { new Double(this.getAvgValue()), new Integer(this.getItemsCount()), new Double(this.B) } ));
    	if ((style & CStatJ) != 0)
			b.append(String.format("\n" +
				"J start           = %.4f\n" +
				"J end             = %.4f", 
				new Object[] { new Double(this.J_Start), new Double(this.J_End) } ));
    	if ((style & CStatAE) != 0)
    		b.append(String.format("\n" +
				"A                 = %.4f\n" +
				"E                 = %.4f", 
				new Object[] { new Double(this.A), new Double(this.E) } ));
    	if ((style & CStatMinMax) != 0)
    		b.append(String.format("\n" +
				"min               = %.4f\n" +
				"max               = %.4f", 
				new Object[] { new Double(this.MinX), new Double(this.MaxX) } ));
    	if ((style & CStatAbs) != 0)
    		b.append(String.format("\n" +
				"min(abs(X))       = %.4f\n" +
				"max(abs(X))       = %.4f", 
				new Object[] { new Double(this.AbsMinX), new Double(this.AbsMaxX) } ));
    	if ((style & CStatDelta) != 0)
    		b.append(String.format("\n" +
				"max-min           = %.4f", 
				new Object[] { new Double(this.MaxX - this.MinX) } ));
    	if ((style & CStatMD) != 0) {
	    	for (int i = 2; i <= 4; i++)
	    		b.append(String.format("\nM[%d]              = %.4f", 
	    				new Object[] { new Integer(i), new Double(M[i]) } ));
	    	for (int i = 2; i <= 4; i++)
	    		b.append(String.format("\nD[%d]              = %.4f", 
	    				new Object[] { new Integer(i), new Double(D[i]) } ));
    	}
    	if (((style & CStatErrors) != 0) && hasBadValues()) 
    		b.append("\n*** There is/are BAD value(s) ***");
		return b.toString();
    }
    
    public static String toString2Header(int style) {
    	StringBuffer b = new StringBuffer();
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
    	StringBuffer b = new StringBuffer();
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
	    		b.append("\t" + Double.toString(M[i]));
	    	for (int i = 2; i <= 4; i++)
	    		b.append("\t" + Double.toString(D[i]));
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
    
	public void toXML(Element dest) {
		Element conclusion = new Element("conclusion");
		conclusion.setText(hasBadValues() ? "*** There is/are BAD value(s)" : "All values are ok");
		dest.addContent(conclusion);
		dest.addContent(XMLHelper.makeAttrEl("Average", Double.toString(getAvgValue())));
		dest.addContent(XMLHelper.makeAttrEl("NumberOfItems", Integer.toString(getItemsCount())));
		dest.addContent(XMLHelper.makeAttrEl("B", Double.toString(B)));
		dest.addContent(XMLHelper.makeAttrEl("J_Start", Double.toString(J_Start)));
		dest.addContent(XMLHelper.makeAttrEl("J_End", Double.toString(J_End)));
		dest.addContent(XMLHelper.makeAttrEl("A", Double.toString(A)));
		dest.addContent(XMLHelper.makeAttrEl("E", Double.toString(E)));
		dest.addContent(XMLHelper.makeAttrEl("MinX", Double.toString(MinX)));
		dest.addContent(XMLHelper.makeAttrEl("MaxX", Double.toString(MaxX)));
		dest.addContent(XMLHelper.makeAttrEl("MinAbsX", Double.toString(AbsMinX)));
		dest.addContent(XMLHelper.makeAttrEl("MaxAbsX", Double.toString(AbsMaxX)));
		dest.addContent(XMLHelper.makeAttrEl("Delta", Double.toString(MaxX - MinX)));
		Element m = new Element("M");
    	for (int i = 2; i <= 4; i++)
    		m.addContent(XMLHelper.makeAttrEl("M" + i, Double.toString(M[i])));
    	dest.addContent(m);
		Element d = new Element("D");
    	for (int i = 2; i <= 4; i++)
    		d.addContent(XMLHelper.makeAttrEl("D" + i, Double.toString(D[i])));
    	dest.addContent(d);
	}
}
