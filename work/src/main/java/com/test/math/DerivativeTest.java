package com.test.math;

import com.slavi.math.MathUtil;

public class DerivativeTest {
	public static void main(String[] args) {
		double angle = MathUtil.deg2rad * 10;
		double dR = MathUtil.deg2rad * 0.1;
		double angle0 = angle + dR;
		double F = Math.sin(angle);
		double F0 = Math.sin(angle0);
		double F1 = F0 + Math.cos(angle0) * (-dR);
		double F2 = F1 - Math.sin(angle0) * Math.pow(-dR, 2) / 2;
		double F3 = F2 - Math.cos(angle0) * Math.pow(-dR, 3) / 6;
		double F4 = F3 + Math.sin(angle0) * Math.pow(-dR, 4) / 24;
		
		System.out.println(F);
		System.out.println(MathUtil.d20(F0) + "\t" + MathUtil.d20(F0 - F));
		System.out.println(MathUtil.d20(F1) + "\t" + MathUtil.d20(F1 - F));
		System.out.println(MathUtil.d20(F2) + "\t" + MathUtil.d20(F2 - F));
		System.out.println(MathUtil.d20(F3) + "\t" + MathUtil.d20(F3 - F));
		System.out.println(MathUtil.d20(F4) + "\t" + MathUtil.d20(F4 - F));
	}
}
