package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajAnimator.java

import java.awt.Component;
import java.util.*;


public class ajAnimator implements Runnable {

	public ajAnimator(Component component) {
		this(component, 200);
	}

	public ajAnimator(Component component, int i) {
		sleepTime = i;
		host = component;
		animations = new Vector();
	}

	public void start() {
		Date date = new Date();
		zeroTime = date.getTime();
		if (animatorThread == null) {
			animatorThread = new Thread(this);
			animatorThread.start();
		}
	}

	public void stop() {
		if (animatorThread != null && animatorThread.isAlive())
			animatorThread.stop();
		animatorThread = null;
	}

	public void run() {
		do {
			Date date = new Date();
			if (animations.size() > 0) {
				for (Enumeration enumeration = animations.elements(); 
						enumeration.hasMoreElements(); 
						((ajAnimation) enumeration.nextElement()).do_it(date.getTime() - zeroTime))
					;
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException _ex) {
			}
		} while (true);
	}

	public synchronized void add(ajAnimation ajanimation) {
		animations.addElement(ajanimation);
	}

	public synchronized void remove(ajAnimation ajanimation) {
		animations.removeElement(ajanimation);
	}

	protected Thread animatorThread;
	protected long zeroTime;
	protected Component host;
	public int sleepTime;
	public Vector animations;
}
