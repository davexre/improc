package com.test.math.RotationAdjust;

import java.util.Map;

public class MyPointPair implements Map.Entry<MyImagePoint, MyImagePoint> {
	public MyImagePoint srcPoint, destPoint;
	public MyPoint3D realPoint;
	
	public double discrepancy = 0.0;
	public double weight = 1.0;
	public boolean isBad = false;
	public double myDiscrepancy;
	
	public MyImagePoint getKey() {
		return srcPoint;
	}
	public MyImagePoint getValue() {
		return destPoint;
	}
	public MyImagePoint setValue(MyImagePoint value) {
		throw new UnsupportedOperationException();
	}
}
