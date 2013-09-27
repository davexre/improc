package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   mouseUpThread.java

import java.awt.Component;
import java.awt.Event;

public class mouseUpThread extends Thread {

	public mouseUpThread(Component component, long l, int i, int j, int k) {
		comp = component;
		x = i;
		y = j;
		when = l;
		mod = k;
	}

	public synchronized void run() {
		try {
			Thread.sleep(300L);
		} catch (InterruptedException _ex) {
		}
		Event event = new Event(comp, when + 300L, 502, x, y, 0, mod);
		comp.postEvent(event);
	}

	Component comp;
	int x;
	int y;
	int mod;
	long when;
}
