package com.slavi.improc.old;

import java.util.StringTokenizer;

import com.slavi.improc.KeyPointPair;

public class PanoPair {
	public PanoPairList list = null;
	public double sx, sy, tx, ty, discrepancy;
	public double distance1, distance2;
	public boolean bad;
	public double weight;
	
	public PanoPair() {}
	
	public PanoPair(KeyPointPair kpp) {
		sx = kpp.sourceSP.doubleX;
		sy = kpp.sourceSP.doubleY;
		tx = kpp.targetSP.doubleX;
		ty = kpp.targetSP.doubleY;
		discrepancy = kpp.discrepancy;
		distance1 = kpp.distanceToNearest;
		distance2 = kpp.distanceToNearest2;
	}
	
	public static PanoPair fromString(String s) {
		PanoPair result = new PanoPair();
		StringTokenizer st = new StringTokenizer(s, "\t");
		result.sx = Double.parseDouble(st.nextToken());
		result.sy = Double.parseDouble(st.nextToken());
		result.tx = Double.parseDouble(st.nextToken());
		result.ty = Double.parseDouble(st.nextToken());
		result.discrepancy = Double.parseDouble(st.nextToken());
		return result;
	}
	
	public String toString() {
		return 
			Double.toString(sx) + "\t" +
			Double.toString(sy) + "\t" +
			Double.toString(tx) + "\t" +
			Double.toString(ty) + "\t" +
			Double.toString(discrepancy);
	}
}
