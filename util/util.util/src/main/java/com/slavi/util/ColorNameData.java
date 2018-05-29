package com.slavi.util;

public class ColorNameData {
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

	public String toString() {
		return String.format("(0x%2$06X) h:%3$.2f s:%4$.2f l:%5$.2f %1$s", name, color, h, s, l);
	}
}
