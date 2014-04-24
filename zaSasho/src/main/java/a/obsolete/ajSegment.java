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

	public static void drawLineSegment(Graphics g, float ax, float ay, float bx, float by) {
		float dx = bx - ax;
		float dy = by - ay;
		if (Math.abs(dx) >= Math.abs(dy)) {
			if (ax < -10000F) {
				if (bx < -10000F)
					return;
				ay = (dy / dx) * (-10000F - ax) + ay;
				ax = -10000F;
			} else if (ax > 10000F) {
				if (bx > 10000F)
					return;
				ay = (dy / dx) * (10000F - ax) + ay;
				ax = 10000F;
			}
			if (bx < -10000F) {
				by = (dy / dx) * (-10000F - bx) + by;
				bx = -10000F;
			} else if (bx > 10000F) {
				by = (dy / dx) * (10000F - bx) + by;
				bx = 10000F;
			}
			if (ay < -10000F && by < -10000F || ay > 10000F && by > 10000F)
				return;
		} else {
			if (ay < -10000F) {
				if (by < -10000F)
					return;
				ax = (dx / dy) * (-10000F - ay) + ax;
				ay = -10000F;
			} else if (ay > 10000F) {
				if (by > 10000F)
					return;
				ax = (dx / dy) * (10000F - ay) + ax;
				ay = 10000F;
			}
			if (by < -10000F) {
				bx = (dx / dy) * (-10000F - by) + bx;
				by = -10000F;
			} else if (by > 10000F) {
				bx = (dx / dy) * (10000F - by) + bx;
				by = 10000F;
			}
			if (ax < -10000F && bx < -10000F || ax > 10000F && bx > 10000F)
				return;
		}
		if (ax < bx)
			g.drawLine(Math.round(ax), Math.round(ay), Math.round(bx), Math.round(by));
		else
			g.drawLine(Math.round(bx), Math.round(by), Math.round(ax), Math.round(ay));
		int i = Math.round(ax);
		int j = Math.round(ay);
		int k = Math.round(bx);
		int l = Math.round(by);
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
