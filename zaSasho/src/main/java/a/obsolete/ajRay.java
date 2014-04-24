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

	public static void drawRay(Graphics g, float startX, float startY, float px, float py) {
		float dx = px - startX;
		float dy = py - startY;
		if (Math.abs(dx) >= Math.abs(dy)) {
			if (dx > 0.0F)
				if (startX > 10000F) {
					return;
				} else {
					ajSegment.drawLineSegment(g, startX, startY, 10000F, (dy / dx) * (10000F - startX) + startY);
					return;
				}
			if (startX < -10000F) {
				return;
			} else {
				ajSegment.drawLineSegment(g, startX, startY, -10000F, (dy / dx) * (-10000F - startX) + startY);
				return;
			}
		}
		if (dy > 0.0F)
			if (startY > 10000F) {
				return;
			} else {
				ajSegment.drawLineSegment(g, startX, startY, (dx / dy) * (10000F - startY) + startX, 10000F);
				return;
			}
		if (startY < -10000F) {
			return;
		} else {
			ajSegment.drawLineSegment(g, startX, startY, (dx / dy) * (-10000F - startY) + startX, -10000F);
			return;
		}
	}
}
