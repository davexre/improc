package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajCircle.java

import java.awt.Color;
import java.awt.Graphics;

import a.ajPoint;

public class ajCircle extends ajElement {

	public ajCircle() {
		super.color = ajElement.circleColor;
	}

	public ajCircle(ajPoint ajpoint, float f) {
		c = ajpoint;
		r = f;
		super.color = ajElement.circleColor;
	}

	public ajCircle(ajPoint ajpoint, float f, Color color) {
		c = ajpoint;
		r = f;
		super.color = color;
	}

	public ajCircle(ajCircle ajcircle, Color color) {
		c = ajcircle.c;
		r = ajcircle.r;
		super.color = color;
	}

	public void draw(Graphics g, Color color) {
		int i = (int) Math.round(Math.sqrt(r));
		int j = Math.round(c.x);
		int k = Math.round(c.y);
		g.setColor(color);
		g.drawOval(j - i, k - i, i << 1, i << 1);
	}

	public String toString() {
		return new String(" ajCi[" + c.toString() + "|" + r + "|" + (int) Math.round(Math.sqrt(r)) + "]");
	}

	public ajPoint c;
	public float r;
}
