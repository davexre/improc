package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajText.java

import java.awt.*;

import a.ajPoint;

public class ajText extends ajElement {

	public ajText() {
		textSize = ajElement.textSize;
		textFont = ajElement.textFont;
		str = "";
	}

	public ajText(Color color) {
		super(color);
		textSize = ajElement.textSize;
		textFont = ajElement.textFont;
		str = "";
	}

	public ajText(Color color, boolean flag) {
		super(color, flag);
		textSize = ajElement.textSize;
		textFont = ajElement.textFont;
		str = "";
	}

	public ajText(String s) {
		textSize = ajElement.textSize;
		textFont = ajElement.textFont;
		str = "";
		str = s;
	}

	public ajText(String s, ajPoint ajpoint) {
		textSize = ajElement.textSize;
		textFont = ajElement.textFont;
		str = "";
		str = s;
		p = ajpoint;
	}

	public ajText(String s, Color color) {
		super(color);
		textSize = ajElement.textSize;
		textFont = ajElement.textFont;
		str = "";
		str = s;
	}

	public ajText(String s, Color color, boolean flag) {
		super(color, flag);
		textSize = ajElement.textSize;
		textFont = ajElement.textFont;
		str = "";
		str = s;
	}

	public void draw(Graphics g, Color color) {
		g.setFont(textFont);
		g.setColor(color);
		g.drawString(str, (int) p.x, (int) p.y);
	}

	public void move(float f, float f1) {
		p.move(f, f1);
	}

	public int textSize;
	public Font textFont;
	public String str;
	public ajPoint p;
}
