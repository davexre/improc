package com.slavi.math.adjust;

public class PTOTokenizer {

	String s;
	int i;
	int l;
	boolean previousBlank;
	
	public PTOTokenizer(String s) {
		this.s = s;
		this.i = 0;
		this.l = s.length();
		previousBlank = false;
	}
	
	public String nextToken() {
		if (i >= l)
			return null;
		if (!previousBlank && (s.charAt(i) == ',')) {
			i++;
		} else {
			while ((i < l) && (" \t".indexOf(s.charAt(i)) >= 0)) {
				previousBlank = true;
				i++;
			}
		}
		if (i >= l)
			return null;
		int start = i;
		i++;
		if (!previousBlank) {
			if (s.charAt(start) != ',')
				previousBlank = false;
			
			if (s.charAt(start) == '"') {
				start = i;
				while ((i < l) && (s.charAt(i) != '"'))
					i++;
			} else {
				while ((i < l) && (", \t".indexOf(s.charAt(i)) < 0))
					i++;
			}
		} else {
			char peekNext = i < l ? s.charAt(i) : ' ';
			if (peekNext == '=') {
				previousBlank = true;
				i++;
			} else if (" \t".indexOf(peekNext) >= 0) {
				previousBlank = true;
			} else
				previousBlank = false;
		}
		if (start == i)
			return null;
		String result = s.substring(start, i);
//		System.out.println("[" + result + "]");
		return result;
	}
	
	public static void main(String[] args) {
		String str = "i w1712 h22,72 f0 a0 b=0,1,2= c0 d0 e0 g0 p0 r0 t0 v38 y0  u10 n\"C:\\Users\\S\\ImageProcess\\Images\\Image data small set\\Liulin sunrise\\HPIM4666.JPG";
		System.out.println(str);
		PTOTokenizer st = new PTOTokenizer(str);
		String t;
		while ((t = st.nextToken()) != null) {
			System.out.println("[" + t + "]");
		}
	}
}
