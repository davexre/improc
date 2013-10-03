package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Slider.java

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics;

public class Slider extends Canvas {

	public Slider() {
		min = 1;
		max = 100;
		resize(132, 33);
		min_width = 132;
		min_height = 33;
		font = new Font("TimesRoman", 0, 12);
		backgroundColor = Color.lightGray;
		thumbColor = Color.lightGray;
		barColor = Color.lightGray.darker();
		slashColor = Color.black;
		textColor = Color.black;
		SetBarHeight(size().height);
		SetValue(1);
	}

	public void Motion() {
	}

	public void Release() {
	}

	public void SetMaximum(int i) {
		max = i;
		if (max < min) {
			int j = min;
			min = max;
			max = j;
		}
		SetValue(value);
	}

	public void SetMinimum(int i) {
		min = i;
		if (max < min) {
			int j = min;
			min = max;
			max = j;
		}
		SetValue(value);
	}

	public void SetValue(int i) {
		value = i;
		if (value < min)
			value = min;
		else if (value > max)
			value = max;
		if (value != min)
			pixel = (int) (Math.round(Math.abs((double) (value - min) / (double) (max - min))
					* (double) (pixelMax - pixelMin)) + (long) pixelMin);
		else
			pixel = pixelMin;
		repaint();
	}

	public void SetHeight(int i) {
		if (i < 24)
			i = 24;
		min_height = i;
		resize(size().width, i);
		repaint();
	}

	public void SetWidth(int i) {
		if (i < 66)
			i = 66;
		min_width = i;
		resize(i + 32, size().height);
		repaint();
	}

	public void SetBarHeight(int i) {
		if (i > size().height - 18)
			i = size().height - 18;
		bar_height = i;
		repaint();
	}

	public int GetValue() {
		return value;
	}

	public void SetBackgroundColor(Color color) {
		backgroundColor = color;
		repaint();
	}

	public void SetThumbColor(Color color) {
		thumbColor = color;
		repaint();
	}

	public void SetBarColor(Color color) {
		barColor = color;
		repaint();
	}

	public void SetSlashColor(Color color) {
		slashColor = color;
		repaint();
	}

	public void SetTextColor(Color color) {
		textColor = color;
		repaint();
	}

	public void SetFont(Font font1) {
		font = font1;
		repaint();
	}

	public void paint(Graphics g) {
		int i = size().width;
		int j = size().height;
		g.setColor(backgroundColor);
		g.fillRect(0, 0, i, j);
		int k = j - 18 - bar_height >> 1;
		g.setColor(barColor);
		g.fill3DRect(16, 18 + k, i - 32, bar_height, false);
		g.setColor(thumbColor);
		g.fill3DRect((16 + pixel) - 14, 20, 29, j - 4 - 18, true);
		g.fill3DRect(0, 18, 12, j - 18, true);
		g.fill3DRect(i - 12, 18, 12, j - 18, true);
		g.setColor(slashColor);
		g.drawLine(16 + pixel, 21, 16 + pixel, j - 4);
		g.setColor(textColor);
		g.setFont(font);
		String s = String.valueOf(value);
		g.drawString(s, (16 + pixel) - getFontMetrics(font).stringWidth(s) / 2, 15);
	}

	void HandleMouse(int i) {
		size();
		pixel = Math.max(i - 16, pixelMin);
		pixel = Math.min(pixel, pixelMax);
		double d;
		if (pixel != pixelMin)
			d = ((double) pixel - (double) pixelMin) / (double) (pixelMax - pixelMin);
		else
			d = 0.0D;
		value = (int) Math.round(d * (double) (max - min)) + min;
		paint(getGraphics());
	}

	public boolean mouseDown(Event event, int i, int j) {
		HandleMouse(i);
		Motion();
		return true;
	}

	public boolean mouseDrag(Event event, int i, int j) {
		HandleMouse(i);
		Motion();
		return true;
	}

	public boolean mouseUp(Event event, int i, int j) {
		HandleMouse(i);
		Release();
		return true;
	}

	public void reshape(int i, int j, int k, int l) {
		super.reshape(i, j, k, l);
		pixelMin = 16;
		pixelMax = k - 32 - 14 - 2 - 1;
		if (value != min) {
			pixel = (int) (Math.round(Math.abs((double) (value - min) / (double) (max - min))
					* (double) (pixelMax - pixelMin)) + (long) pixelMin);
			return;
		} else {
			pixel = pixelMin;
			return;
		}
	}

	public Dimension minimumSize() {
		Dimension dimension = new Dimension(min_width, min_height);
		return dimension;
	}

	public Dimension preferredSize() {
		Dimension dimension = new Dimension(min_width, min_height);
		return dimension;
	}

//	private static final int THUMB_SIZE = 14;
//	private static final int BUFFER = 2;
//	private static final int BUTTON_WIDTH = 12;
//	private static final int BUTTON_SIZE = 16;
//	private static final int TEXT_HEIGHT = 18;
//	private static final int TEXT_BUFFER = 3;
//	private static final int DEFAULT_WIDTH = 132;
//	private static final int DEFAULT_HEIGHT = 15;
//	private static final int MIN_WIDTH = 66;
//	private static final int MIN_HEIGHT = 6;
//	private static final int DEFAULT_MIN = 1;
//	private static final int DEFAULT_MAX = 100;
	int min;
	int max;
	int value;
	int pixel;
	int pixelMin;
	int pixelMax;
	int bar_height;
	int min_width;
	int min_height;
	Color backgroundColor;
	Color thumbColor;
	Color barColor;
	Color slashColor;
	Color textColor;
	Font font;
}
