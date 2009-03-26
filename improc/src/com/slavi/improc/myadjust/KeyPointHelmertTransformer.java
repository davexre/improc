package com.slavi.improc.myadjust;

import com.slavi.improc.KeyPoint;
import com.slavi.math.MathUtil;
import com.slavi.math.transform.Helmert2DTransformer;

public class KeyPointHelmertTransformer extends Helmert2DTransformer<KeyPoint, KeyPoint>{

	public double getSourceCoord(KeyPoint item, int coordIndex) {
		switch (coordIndex) {
		case 0: return item.doubleX;
		case 1: return item.doubleY;
		default:
			throw new IllegalArgumentException();
		}
	}

	public double getTargetCoord(KeyPoint item, int coordIndex) {
		switch (coordIndex) {
		case 0: return item.doubleX;
		case 1: return item.doubleY;
		default:
			throw new IllegalArgumentException();
		}
	}

	public void setSourceCoord(KeyPoint item, int coordIndex, double value) {
		switch (coordIndex) {
		case 0: item.doubleX = value; break;
		case 1: item.doubleY = value; break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public void setTargetCoord(KeyPoint item, int coordIndex, double value) {
		switch (coordIndex) {
		case 0: item.doubleX = value; break;
		case 1: item.doubleY = value; break;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	public void getParams2(double params[]) {
		params[0] = MathUtil.hypot(a, b);
		double ca = Math.acos(params[0] == 0 ? 1.0 : a / params[0]);
		double sa = Math.asin(params[0] == 0 ? 0.0 : b / params[0]);
		if (ca <= MathUtil.PIover2) {
			params[1] = sa; 
		} else {
			if (sa >= 0)
				params[1] = ca;
			else
				params[1] = -ca;
		}
	}
	
	public static void main(String[] args) {
		KeyPointHelmertTransformer tr = new KeyPointHelmertTransformer();
		double angle = -200 * MathUtil.deg2rad;
		tr.setParams(1.0, angle);
		double params[] = new double[2];
		tr.getParams(params);
		System.out.println(MathUtil.d4(params[0]) + "\t" + MathUtil.d4(MathUtil.rad2deg * params[1]));
	}
}
