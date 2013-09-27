package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GrayFilter.java

import java.awt.image.RGBImageFilter;

public class GrayFilter extends RGBImageFilter {

	public GrayFilter() {
		super.canFilterIndexColorModel = true;
	}

	public int filterRGB(int i, int j, int k) {
		int l = k & 0xff000000;
		int i1 = (k & 0xff0000) + 0xff0000 >> 1;
		i1 &= 0xff0000;
		int j1 = (k & 0xff00) + 65280 >> 1;
		j1 &= 0xff00;
		int k1 = (k & 0xff) + 255 >> 1;
		k1 &= 0xff;
		return l | i1 | j1 | k1;
	}
}
