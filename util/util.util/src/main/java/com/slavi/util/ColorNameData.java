package com.slavi.util;

public class ColorNameData implements Comparable<ColorNameData> {
	public final String name;
	public final int color;
	public final double h;
	public final double s;
	public final double l;
	
	public ColorNameData(String name, int color, double hsl[]) {
		this.name = name;
		this.color = color;
		this.h = hsl[0];
		this.s = hsl[1];
		this.l = hsl[2];
	}

	public int compareTo(ColorNameData o) {
		int r = Double.compare(h, o.h);
		if (r == 0)
			r = Double.compare(s, o.s);
		if (r == 0)
			r = Double.compare(l, o.l);
		if (r == 0)
			r = name.compareTo(o.name);
		return r;
	}
	
	public String toString() {
		return String.format("%s (0x%06X)", name, color);
	}
}
