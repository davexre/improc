package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajButton.java

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageObserver;


public class ajButton extends Canvas implements ajAnimation, ImageObserver {

	public ajButton(int i, Image image, Image image1) {
		duration = 1000L;
		basecolor = Color.black;
		img_on = image1;
		img_off = image;
		type = i;
		width = img_on.getWidth(this) + 4;
		height = img_on.getHeight(this) + 4;
		last_sw = 0L;
		state = 0;
		GrayFilter grayfilter = new GrayFilter();
		FilteredImageSource filteredimagesource = new FilteredImageSource(img_off.getSource(), grayfilter);
		img_dis = createImage(filteredimagesource);
	}

	public void switch_on() {
		state = 3;
		repaint();
	}

	public void switch_off() {
		state = 0;
		repaint();
	}

	public void blinking() {
		state = 1;
	}

	public void paint(Graphics g) {
		g.setColor(Color.darkGray);
		g.drawRect(0, 0, width - 1, height - 1);
		g.setColor(Color.gray);
		switch (state) {
		case -1:
			g.drawImage(img_dis, 2, 2, this);
			return;

		case 0: // '\0'
		case 1: // '\001'
			g.drawImage(img_off, 2, 2, this);
			return;

		case 2: // '\002'
		case 3: // '\003'
			g.drawImage(img_on, 2, 2, this);
			return;
		}
	}

	public void do_it(long l) {
		if (l - last_sw > duration) {
			last_sw = l;
			switch (state) {
			case 1: // '\001'
				state = 2;
				break;

			case 2: // '\002'
				state = 1;
				break;
			}
			repaint();
		}
	}

	public Dimension minimumSize() {
		return new Dimension(width, height);
	}

	public Dimension preferredSize() {
		return minimumSize();
	}

	public boolean mouseDown(Event event, int i, int j) {
		if (type == 1) {
			if (state == 0)
				state = 3;
			else if (state == 3)
				state = 0;
		} else {
			state = 3;
		}
		repaint();
		Toolkit.getDefaultToolkit().sync();
		Event event1 = new Event(this, event.when, 1001, i, j, event.key, event.modifiers, "Image");
		postEvent(event1);
		return true;
	}

	public boolean mouseUp(Event event, int i, int j) {
		if (type == 1) {
			return true;
		} else {
			state = 0;
			repaint();
			Toolkit.getDefaultToolkit().sync();
			return true;
		}
	}

	public boolean imageUpdate(Image image, int i, int j, int k, int l, int i1) {
		if ((i & 1) != 0) {
			width = l + 4;
			resize(width, height);
			invalidate();
			if (getParent() != null)
				getParent().layout();
			repaint();
		}
		if ((i & 2) != 0) {
			height = i1 + 4;
			resize(width, height);
			invalidate();
			if (getParent() != null)
				getParent().layout();
			repaint();
		}
		repaint();
		return true;
	}

	public void enable() {
		super.enable();
		state = save_state;
		repaint();
	}

	public void disable() {
		if (state != -1) {
			save_state = state;
			state = -1;
			repaint();
			super.disable();
		}
	}

	public static final int PUSH = 0;
	public static final int TOGGLE = 1;
	int width;
	int height;
	int type;
	public int state;
	public int save_state;
	long last_sw;
	public long duration;
	public Color basecolor;
	Image img_on;
	Image img_off;
	Image img_dis;
}
