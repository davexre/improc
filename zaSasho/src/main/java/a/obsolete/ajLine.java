package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajLine.java

import java.awt.Color;
import java.awt.Graphics;

import a.ajPoint;

public class ajLine extends ajSegment {

	public ajLine(ajPoint ajpoint, ajPoint ajpoint1) {
		super(ajpoint, ajpoint1);
	}

	public ajLine(ajPoint ajpoint, ajPoint ajpoint1, Color color) {
		super(ajpoint, ajpoint1, color);
	}

	public void draw(Graphics g) {
		g.setColor(super.color);
		drawLine(g, super.a, super.b);
	}

	public void draw(Graphics g, Color color) {
		g.setColor(color);
		drawLine(g, super.a, super.b);
	}

	public static void drawLine(Graphics g, ajPoint ajpoint, ajPoint ajpoint1) {
		drawLine(g, ajpoint.x, ajpoint.y, ajpoint1.x, ajpoint1.y);
	}

	public static void drawLine(Graphics g, float f, float f1, float f2, float f3) {
		float f4 = f2 - f;
		float f5 = f3 - f1;
		if (Math.abs(f4) >= Math.abs(f5)) {
			ajSegment.drawLineSegment(g, -10000F, Math.round((f5 / f4) * (-10000F - f) + f1), 10000F,
					Math.round((f5 / f4) * (10000F - f) + f1));
			return;
		} else {
			ajSegment.drawLineSegment(g, Math.round((f4 / f5) * (-10000F - f1) + f), -10000F,
					Math.round((f4 / f5) * (10000F - f1) + f), 10000F);
			return;
		}
	}

	public static void drawBisector(Graphics g, ajPoint ajpoint, ajPoint ajpoint1) {
		float f = (ajpoint.x + ajpoint1.x) / 2.0F;
		float f1 = (ajpoint.y + ajpoint1.y) / 2.0F;
		float f2 = ajpoint1.x - ajpoint.x;
		float f3 = ajpoint1.y - ajpoint.y;
		drawLine(g, f - f3, f1 + f2, f + f3, f1 - f2);
	}
}
