package a;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajPoint.java

import java.awt.Color;
import java.awt.Graphics;

import a.obsolete.ajElement;
import a.obsolete.ajLine;

public class ajPoint extends ajElement {

	public ajPoint() {
	}

	public ajPoint(float f, float f1) {
		x = f;
		y = f1;
	}

	public ajPoint(int i, int j) {
		x = i;
		y = j;
	}

	public ajPoint(double d, double d1) {
		Float float1 = new Float(d);
		x = float1.floatValue();
		float1 = new Float(d1);
		y = float1.floatValue();
	}

	public ajPoint(ajPoint ajpoint) {
		x = ajpoint.x;
		y = ajpoint.y;
	}

	public ajPoint(ajPoint ajpoint, Color color) {
		this(ajpoint);
		super.color = color;
	}

	public float distance2(ajPoint ajpoint) {
		return (ajpoint.x - x) * (ajpoint.x - x) + (ajpoint.y - y) * (ajpoint.y - y);
	}

	public float distance2(float f, float f1) {
		return (f - x) * (f - x) + (f1 - y) * (f1 - y);
	}

	public static void SetSmallPoints(boolean flag) {
		smallPoints = flag;
		if (smallPoints) {
			distClose = 20F;
			return;
		} else {
			distClose = 57F;
			return;
		}
	}

	public void draw(Graphics g, Color color) {
		g.setColor(color);
		if (smallPoints) {
			g.fillRect(Math.round(x) - 2, Math.round(y) - 2, 5, 5);
			return;
		} else {
			g.fillRect(Math.round(x) - 2, Math.round(y) - 2, 6, 6);
			return;
		}
	}

	public boolean match(int i, int j) {
		return distance2(i, j) <= distClose;
	}

	public void move(int i, int j) {
		x = i;
		y = j;
	}

	public boolean isLess(ajPoint ajpoint) {
		return x < ajpoint.x || x == ajpoint.x && y < ajpoint.y;
	}

	public boolean isGreater(ajPoint ajpoint) {
		return x > ajpoint.x || x == ajpoint.x && y > ajpoint.y;
	}

	public boolean isEqual(ajPoint ajpoint) {
		return x == ajpoint.x && y == ajpoint.y;
	}

	public String toString() {
		return new String(" ajPt[" + x + "|" + y + "]");
	}

	public int pointToLine(ajPoint p1, ajPoint p2) {
		float dX21 = p2.x - p1.x;
		float dY21 = p2.y - p1.y;
		float f2 = dY21 * (x - p1.x) - dX21 * (y - p1.y);
		if (f2 < 0.0F)
			return 1;
		if (f2 > 0.0F)
			return 2;
		if (dX21 > 0.0F) {
			if (x < p1.x)
				return 3;
			return p2.x >= x ? 0 : 4;
		}
		if (dX21 < 0.0F) {
			if (x > p1.x)
				return 3;
			return p2.x <= x ? 0 : 4;
		}
		if (dY21 > 0.0F) {
			if (y < p1.y)
				return 3;
			return p2.y >= y ? 0 : 4;
		}
		if (dY21 < 0.0F) {
			if (y > p1.y)
				return 3;
			return p2.y <= y ? 0 : 4;
		} else {
			System.out.println("Error, pointLineTest with a=b");
			return 5;
		}
	}

	public boolean areCollinear(ajPoint ajpoint, ajPoint ajpoint1) {
		float f = ajpoint1.x - ajpoint.x;
		float f1 = ajpoint1.y - ajpoint.y;
		float f2 = f1 * (x - ajpoint.x) - f * (y - ajpoint.y);
		return f2 == 0.0F;
	}

	public ajLine Bisector(ajPoint ajpoint) {
		return Bisector(ajpoint, ajElement.segmentColor);
	}

	public ajLine Bisector(ajPoint ajpoint, Color color) {
		float f = (x + ajpoint.x) / 2.0F;
		float f1 = (y + ajpoint.y) / 2.0F;
		float f2 = ajpoint.x - x;
		float f3 = ajpoint.y - y;
		ajPoint ajpoint1 = new ajPoint(f - f3, f1 + f2);
		ajPoint ajpoint2 = new ajPoint(f + f3, f1 - f2);
		return new ajLine(ajpoint1, ajpoint2, color);
	}

	public ajPoint circumcenter(ajPoint b, ajPoint c) {
		float f = ((b.x - c.x) * (b.x + c.x) + (b.y - c.y)
				* (b.y + c.y)) / 2.0F;
		float f1 = ((c.x - x) * (c.x + x) + (c.y - y) * (c.y + y)) / 2.0F;
		float f2 = (b.x - c.x) * (c.y - y) - (c.x - x) * (b.y - c.y);
		if (f2 == 0.0F)
			System.out.println("circumcenter, degenerate case");
		return new ajPoint((f * (c.y - y) - f1 * (b.y - c.y)) / f2,
				(f1 * (b.x - c.x) - f * (c.x - x)) / f2);
	}

	public float x;
	public float y;
	protected static boolean smallPoints;
	protected static float distClose;
	public static final int ONSEGMENT = 0;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int INFRONTOFA = 3;
	public static final int BEHINDB = 4;
	public static final int ERROR = 5;

	static {
		SetSmallPoints(false);
	}
}
