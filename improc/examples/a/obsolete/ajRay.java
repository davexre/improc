package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajRay.java

import java.awt.Color;
import java.awt.Graphics;

import a.ajPoint;

public class ajRay extends ajSegment {

	public ajRay(ajPoint ajpoint, ajPoint ajpoint1) {
		super(ajpoint, ajpoint1);
	}

	public ajRay(ajPoint ajpoint, ajPoint ajpoint1, Color color) {
		super(ajpoint, ajpoint1, color);
	}

	public void draw(Graphics g) {
		g.setColor(super.color);
		drawRay(g, super.a, super.b);
	}

	public void draw(Graphics g, Color color) {
		g.setColor(color);
		drawRay(g, super.a, super.b);
	}

	public static void drawRay(Graphics g, ajPoint ajpoint, ajPoint ajpoint1) {
		drawRay(g, ajpoint.x, ajpoint.y, ajpoint1.x, ajpoint1.y);
	}

	public static void drawRay(Graphics g, ajPoint ajpoint, float f, float f1) {
		drawRay(g, ajpoint.x, ajpoint.y, f, f1);
	}

	public static void drawRay(Graphics g, float f, float f1, float f2, float f3) {
		float f4 = f2 - f;
		float f5 = f3 - f1;
		if (Math.abs(f4) >= Math.abs(f5)) {
			if (f4 > 0.0F)
				if (f > 10000F) {
					return;
				} else {
					ajSegment.drawLineSegment(g, f, f1, 10000F, (f5 / f4) * (10000F - f) + f1);
					return;
				}
			if (f < -10000F) {
				return;
			} else {
				ajSegment.drawLineSegment(g, f, f1, -10000F, (f5 / f4) * (-10000F - f) + f1);
				return;
			}
		}
		if (f5 > 0.0F)
			if (f1 > 10000F) {
				return;
			} else {
				ajSegment.drawLineSegment(g, f, f1, (f4 / f5) * (10000F - f1) + f, 10000F);
				return;
			}
		if (f1 < -10000F) {
			return;
		} else {
			ajSegment.drawLineSegment(g, f, f1, (f4 / f5) * (-10000F - f1) + f, -10000F);
			return;
		}
	}
}
