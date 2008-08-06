package com.slavi.improc;

import java.util.StringTokenizer;

public class PanoPair {
	public double sx, sy, tx, ty, discrepancy, weight;
	
	public PanoPair() {}
	
	public PanoPair(KeyPointPair kpp) {
		sx = kpp.sourceSP.doubleX;
		sy = kpp.sourceSP.doubleY;
		tx = kpp.targetSP.doubleX;
		ty = kpp.targetSP.doubleY;
		discrepancy = kpp.getValue();
		weight = kpp.getComputedWeight();
	}
	
	public static PanoPair fromString(String s) {
		PanoPair result = new PanoPair();
		StringTokenizer st = new StringTokenizer(s, "\t");
		result.sx = Double.parseDouble(st.nextToken());
		result.sy = Double.parseDouble(st.nextToken());
		result.tx = Double.parseDouble(st.nextToken());
		result.ty = Double.parseDouble(st.nextToken());
		result.discrepancy = Double.parseDouble(st.nextToken());
		result.weight = Double.parseDouble(st.nextToken());
		return result;
	}
	
	public String toString() {
		return 
			Double.toString(sx) + "\t" +
			Double.toString(sy) + "\t" +
			Double.toString(tx) + "\t" +
			Double.toString(ty) + "\t" +
			Double.toString(discrepancy) + "\t" +
			Double.toString(weight);
	}
}
