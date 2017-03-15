package com.slavi.util;

public class ColorNamesTest {
	
	void doIt() throws Exception {
		ColorNames cn = new ColorNames();
		
		System.out.println(cn);
		int color = 0x0010Fe;
		double tmp[] = new double[3];
		ColorConversion.RGB.fromRGB(color, tmp);
		ColorConversion.HSL.fromDRGB(tmp, tmp);
		System.out.println(ColorConversion.HSL.toString(tmp));
		System.out.println(cn.findClosestColor(color));
	}

	public static void main(String[] args) throws Exception {
		new ColorNamesTest().doIt();
		System.out.println("Done.");
	}
}
