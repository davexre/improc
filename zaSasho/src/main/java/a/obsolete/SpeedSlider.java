package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SpeedSlider.java

import java.awt.Color;

import a.VoronoiApplet;

public class SpeedSlider extends Slider {

	public SpeedSlider(VoronoiApplet voronoiapplet, int i, int j, int k, int l, int i1) {
		SetWidth(i);
		SetHeight(j);
		SetMinimum(k);
		SetMaximum(l);
		SetValue(i1);
		SetBarHeight(5);
		SetBarColor(Color.gray);
		app = voronoiapplet;
	}

	public void Motion() {
		app.evRec.recordEvent(11, GetValue());
		app.stepper.set_time(GetValue());
	}

	public void Release() {
		app.evRec.recordEvent(11, GetValue());
		app.stepper.set_time(GetValue());
	}

	VoronoiApplet app;
}
