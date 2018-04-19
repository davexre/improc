package com.slavi.ann.test;

import java.util.ArrayList;
import java.util.List;

import com.slavi.math.matrix.Matrix;

public class TwoSpiralsData {
	public static class TwoSpiralsDataPoint implements DatapointPair {
		static final double A = 1;
		static final double B = 1;
		
		double angle;
		int type;
		
		public void toInputMatrix(Matrix dest) {
			double t = angle + type * Math.PI;
			double r = A + B * t;
			dest.resize(4, 1);
			dest.setItem(0, 0, Math.cos(t) * r);
			dest.setItem(1, 0, Math.sin(t) * r);
			dest.setItem(2, 0, t);
			dest.setItem(3, 0, r);
		}

		public void toOutputMatrix(Matrix dest) {
			dest.resize(2, 1);
			dest.setItem(0, 0, type == 0 ? 1 : 0);
			dest.setItem(1, 0, type == 1 ? 1 : 0);
		}
		
		public String toString() {
			return "Angle: " + angle + ", type: " + type;
		}
	}
	
	public static List<TwoSpiralsDataPoint> dataSet(int numberOfItems) {
		ArrayList<TwoSpiralsDataPoint> r = new ArrayList<>();
		double deltaAngle = 2.0 * Math.PI * numberOfItems;
		double angle = 0;
		for (int i = 0; i < numberOfItems; i++) {
			TwoSpiralsDataPoint d = new TwoSpiralsDataPoint();
			d.angle = angle;
			d.type = i % 2 == 0 ? 0 : 1;
			angle += deltaAngle;
			r.add(d);
		}
		return r;
	}
}
