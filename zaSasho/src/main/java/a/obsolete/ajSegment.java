package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajSegment.java

import java.awt.Color;
import java.awt.Graphics;

import a.ajPoint;

public class ajSegment extends ajElement {

	public ajSegment() {
		super.color = ajElement.segmentColor;
	}

	public ajSegment(ajPoint ajpoint, ajPoint ajpoint1) {
		a = ajpoint;
		b = ajpoint1;
		super.color = ajElement.segmentColor;
	}

	public ajSegment(ajPoint ajpoint, ajPoint ajpoint1, Color color) {
		a = ajpoint;
		b = ajpoint1;
		super.color = color;
	}

	public String toString() {
		return new String(" ajSg[" + a.toString() + "|" + b.toString() + "]");
	}

	public static void SetThinLines(boolean flag) {
		thinLines = flag;
	}

	public void draw(Graphics g) {
		g.setColor(super.color);
		drawLineSegment(g, a, b);
	}

	public void draw(Graphics g, Color color) {
		g.setColor(color);
		drawLineSegment(g, a, b);
	}

	public static void drawLineSegment(Graphics g, ajPoint ajpoint, ajPoint ajpoint1) {
		drawLineSegment(g, ajpoint.x, ajpoint.y, ajpoint1.x, ajpoint1.y);
	}

	public static void drawLineSegment(Graphics g, float f, float f1, float f2, float f3) {
		float f4 = f2 - f;
		float f5 = f3 - f1;
		if (Math.abs(f4) >= Math.abs(f5)) {
			if (f < -10000F) {
				if (f2 < -10000F)
					return;
				f1 = (f5 / f4) * (-10000F - f) + f1;
				f = -10000F;
			} else if (f > 10000F) {
				if (f2 > 10000F)
					return;
				f1 = (f5 / f4) * (10000F - f) + f1;
				f = 10000F;
			}
			if (f2 < -10000F) {
				f3 = (f5 / f4) * (-10000F - f2) + f3;
				f2 = -10000F;
			} else if (f2 > 10000F) {
				f3 = (f5 / f4) * (10000F - f2) + f3;
				f2 = 10000F;
			}
			if (f1 < -10000F && f3 < -10000F || f1 > 10000F && f3 > 10000F)
				return;
		} else {
			if (f1 < -10000F) {
				if (f3 < -10000F)
					return;
				f = (f4 / f5) * (-10000F - f1) + f;
				f1 = -10000F;
			} else if (f1 > 10000F) {
				if (f3 > 10000F)
					return;
				f = (f4 / f5) * (10000F - f1) + f;
				f1 = 10000F;
			}
			if (f3 < -10000F) {
				f2 = (f4 / f5) * (-10000F - f3) + f2;
				f3 = -10000F;
			} else if (f3 > 10000F) {
				f2 = (f4 / f5) * (10000F - f3) + f2;
				f3 = 10000F;
			}
			if (f < -10000F && f2 < -10000F || f > 10000F && f2 > 10000F)
				return;
		}
		if (f < f2)
			g.drawLine(Math.round(f), Math.round(f1), Math.round(f2), Math.round(f3));
		else
			g.drawLine(Math.round(f2), Math.round(f3), Math.round(f), Math.round(f1));
		int i = Math.round(f);
		int j = Math.round(f1);
		int k = Math.round(f2);
		int l = Math.round(f3);
		if (i < k) {
			g.drawLine(i, j, k, l);
			if (!thinLines) {
				g.drawLine(i + 1, j, k + 1, l);
				g.drawLine(i, j + 1, k, l + 1);
				g.drawLine(i + 1, j + 1, k + 1, l + 1);
				return;
			}
		} else {
			g.drawLine(k, l, i, j);
			if (!thinLines) {
				g.drawLine(k + 1, l, i + 1, j);
				g.drawLine(k, l + 1, i, j + 1);
				g.drawLine(k + 1, l + 1, i + 1, j + 1);
			}
		}
	}

	ajPoint a;
	ajPoint b;
	protected static boolean thinLines;
}
