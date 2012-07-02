package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajLed.java

import java.awt.*;


public class ajLed extends Canvas implements ajAnimation {

	public ajLed() {
		duration = 1000L;
		basecolor = Color.red;
		width = 20;
		height = 7;
		last_sw = 0L;
		state = 0;
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
		g.draw3DRect(0, 0, width - 1, height - 1, true);
		switch (state) {
		case 0: // '\0'
		case 1: // '\001'
			g.setColor(Color.black);
			g.draw3DRect(1, 1, width - 3, height - 3, true);
			g.fillRect(2, 2, width - 4, height - 4);
			g.setColor(basecolor);
			g.drawLine(2, 2, 2, 2);
			g.drawLine(2, 4, 2, 4);
			g.drawLine(4, 2, 4, 2);
			g.drawLine(3, 3, 3, 3);
			g.drawLine(4, 4, 4, 4);
			g.drawLine(6, 2, 6, 2);
			g.drawLine(5, 3, 5, 3);
			g.drawLine(6, 4, 6, 4);
			g.drawLine(8, 2, 8, 2);
			g.drawLine(7, 3, 7, 3);
			g.drawLine(8, 4, 8, 4);
			g.drawLine(10, 2, 10, 2);
			g.drawLine(9, 3, 9, 3);
			g.drawLine(10, 4, 10, 4);
			g.drawLine(12, 2, 12, 2);
			g.drawLine(11, 3, 11, 3);
			g.drawLine(12, 4, 12, 4);
			g.drawLine(14, 2, 14, 2);
			g.drawLine(13, 3, 13, 3);
			g.drawLine(14, 4, 14, 4);
			g.drawLine(16, 2, 16, 2);
			g.drawLine(15, 3, 15, 3);
			g.drawLine(16, 4, 16, 4);
			g.drawLine(18, 2, 18, 2);
			g.drawLine(17, 3, 17, 3);
			g.drawLine(18, 4, 18, 4);
			return;

		case 2: // '\002'
		case 3: // '\003'
			g.setColor(basecolor);
			g.draw3DRect(1, 1, width - 3, height - 3, true);
			g.fillRect(2, 2, width - 4, height - 4);
			g.setColor(Color.white);
			g.drawLine(4, 2, 4, 2);
			g.drawLine(3, 3, 3, 3);
			g.drawLine(4, 4, 4, 4);
			g.drawLine(6, 2, 6, 2);
			g.drawLine(6, 4, 6, 4);
			g.drawLine(8, 2, 8, 2);
			g.drawLine(8, 4, 8, 4);
			g.drawLine(10, 2, 10, 2);
			g.drawLine(5, 3, 13, 3);
			g.drawLine(10, 4, 10, 4);
			g.drawLine(12, 2, 12, 2);
			g.drawLine(12, 4, 12, 4);
			g.drawLine(14, 2, 14, 2);
			g.drawLine(14, 4, 14, 4);
			g.drawLine(15, 3, 15, 3);
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

	public int width;
	public int height;
	public int state;
	public long last_sw;
	public long duration;
	public Color basecolor;
}
