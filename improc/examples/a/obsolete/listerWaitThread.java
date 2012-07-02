package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   listerWaitThread.java

import java.awt.Component;

import a.VoronoiApplet;

class listerWaitThread extends Thread {

	public listerWaitThread(VoronoiApplet voronoiapplet) {
		vapp = voronoiapplet;
	}

	public synchronized void run() {
		try {
			wait();
		} catch (InterruptedException _ex) {
		}
		synchronized (vapp) {
			vapp.enable();
			vapp.evRec.load_save();
		}
		stop();
	}

	VoronoiApplet vapp;
}
