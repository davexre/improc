package com.slavi.math;

import java.awt.Color;

public class ColorSetPick {
	public static final int colors[] = {
		0x00ff0000,
		0x0000ff00,
		0x000000ff,
		0x0000ffff,
		0x00ff00ff,
		0x00ffff00,
		0x00ff9600
	};

	int nextColor = 0;

	public int getNext() {
		int result = colors[nextColor++];
		if (nextColor >= colors.length)
			nextColor = 0;
		return result;
	}
	
	public Color getNextColor() {
		return new Color(getNext());
	}
	
	public Color getNextColor(int alpha) {
		alpha = MathUtil.clipValue(alpha, 0, 255);
		int c = getNext() | (alpha << 24);
		return new Color(c, true);
	}
	
	public void reset() {
		nextColor = 0;
	}
}
