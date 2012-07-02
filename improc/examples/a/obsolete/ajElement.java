package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajElement.java

import java.awt.*;


public abstract class ajElement extends ajListElement implements ajAnimation {

	protected ajElement() {
		this(pointColor);
	}

	protected ajElement(Color color1) {
		hidden = false;
		ident = setIdent();
		color = color1;
	}

	private synchronized int setIdent() {
		return ++maxIdent;
	}

	protected ajElement(Color color1, boolean flag) {
		this(color1);
		hidden = flag;
	}

	public String toString() {
		return new String("ajEl!?!");
	}

	public void draw(Graphics g) {
		draw(g, color);
	}

	public void draw(Graphics g, Color color1) {
	}

	public static Color brighter(Color color1) {
		return new Color(more(color1.getRed()), more(color1.getGreen()), more(color1.getBlue()));
	}

	public static Color darker(Color color1) {
		return new Color(less(color1.getRed()), less(color1.getGreen()), less(color1.getBlue()));
	}

	public static Color muchBrighter(Color color1) {
		return brighter(brighter(color1));
	}

	public static Color veryMuchBrighter(Color color1) {
		return muchBrighter(muchBrighter(color1));
	}

	public static Color muchDarker(Color color1) {
		return darker(darker(color1));
	}

	public static Color veryMuchDarker(Color color1) {
		return muchDarker(muchDarker(color1));
	}

	public static int more(int i) {
		return Math.min(255, i + 32);
	}

	public static int less(int i) {
		return Math.max(0, i - 32);
	}

	public void move(float f, float f1) {
	}

	public boolean match(int i, int j) {
		return false;
	}

	public void do_it(long l) {
	}

	public static int maxIdent;
	public static Color backgroundColor;
	public static Color pointColor;
	public static Color shiningColor;
	public static Color segmentColor;
	public static Color textColor;
	public static Color circleColor;
	public static int textSize;
	public static Font textFont;
	static final int right = 10000;
	static final int left = -10000;
	static final int upper = 10000;
	static final int lower = -10000;
	public int ident;
	public Color color;
	public boolean hidden;
	public int priority;

	static {
		backgroundColor = Color.white;
		pointColor = darker(Color.red);
		shiningColor = muchDarker(Color.orange);
		segmentColor = Color.black;
		textColor = Color.black;
		circleColor = darker(Color.green);
		textSize = 10;
		textFont = new Font("Times", 1, textSize);
	}
}
