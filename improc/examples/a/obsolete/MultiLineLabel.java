package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MultiLineLabel.java

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.StringTokenizer;

public class MultiLineLabel extends Canvas {

	protected void newLabel(String s) {
		StringTokenizer stringtokenizer = new StringTokenizer(s, "\n");
		num_lines = stringtokenizer.countTokens();
		lines = new String[num_lines];
		line_widths = new int[num_lines];
		for (int i = 0; i < num_lines; i++)
			lines[i] = stringtokenizer.nextToken();

	}

	protected void measure() {
		FontMetrics fontmetrics = getFontMetrics(getFont());
		if (fontmetrics == null)
			return;
		line_height = fontmetrics.getHeight();
		line_ascent = fontmetrics.getAscent();
		max_width = 0;
		for (int i = 0; i < num_lines; i++) {
			line_widths[i] = fontmetrics.stringWidth(lines[i]);
			if (line_widths[i] > max_width)
				max_width = line_widths[i];
		}

	}

	public MultiLineLabel(String s, int i, int j, int k) {
		newLabel(s);
		margin_width = i;
		margin_height = j;
		alignment = k;
	}

	public MultiLineLabel(String s, int i, int j) {
		this(s, i, j, 0);
	}

	public MultiLineLabel(String s, int i) {
		this(s, 10, 10, i);
	}

	public MultiLineLabel(String s) {
		this(s, 10, 10, 0);
	}

	public void setLabel(String s) {
		newLabel(s);
		measure();
		repaint();
	}

	public void setFont(Font font) {
		super.setFont(font);
		measure();
		repaint();
	}

	public void setForeground(Color color) {
		super.setForeground(color);
		repaint();
	}

	public void setAlignment(int i) {
		alignment = i;
		repaint();
	}

	public void setMarginWidth(int i) {
		margin_width = i;
		repaint();
	}

	public void setMarginHeight(int i) {
		margin_height = i;
		repaint();
	}

	public int getAlignment() {
		return alignment;
	}

	public int getMarginWidth() {
		return margin_width;
	}

	public int getMarginHeight() {
		return margin_height;
	}

	public void addNotify() {
		super.addNotify();
		measure();
	}

	public Dimension preferredSize() {
		return new Dimension(max_width + 2 * margin_width, num_lines * line_height + 2 * margin_height);
	}

	public Dimension minimumSize() {
		return new Dimension(max_width, num_lines * line_height);
	}

	public void paint(Graphics g) {
		Dimension dimension = size();
		int j = line_ascent + (dimension.height - num_lines * line_height) / 2;
		for (int k = 0; k < num_lines;) {
			int i;
			switch (alignment) {
			case 0: // '\0'
				i = margin_width;
				break;

			case 1: // '\001'
			default:
				i = (dimension.width - line_widths[k]) / 2;
				break;

			case 2: // '\002'
				i = dimension.width - margin_width - line_widths[k];
				break;
			}
			g.drawString(lines[k], i, j);
			k++;
			j += line_height;
		}

	}

	public static final int LEFT = 0;
	public static final int CENTER = 1;
	public static final int RIGHT = 2;
	protected String lines[];
	protected int num_lines;
	protected int margin_width;
	protected int margin_height;
	protected int line_height;
	protected int line_ascent;
	protected int line_widths[];
	protected int max_width;
	protected int alignment;
}
