package com.slavi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.slavi.math.MathUtil;

public class ColorNames {

	public static class ColorNameData implements Comparable<ColorNameData> {
		String name;
		int color;
		double h;
		double s;
		double l;
		
		public ColorNameData(String name, int color, double hsl[]) {
			this.name = name;
			this.color = color;
			this.h = hsl[0];
			this.s = hsl[1];
			this.l = hsl[2];
		}

		public int compareTo(ColorNameData o) {
			int r = Double.compare(h, o.h);
			if (r == 0)
				r = Double.compare(s, o.s);
			if (r == 0)
				r = Double.compare(l, o.l);
			if (r == 0)
				r = name.compareTo(o.name);
			return r;
		}
	}
	
	List<ColorNameData> colors = new ArrayList<>();

	public ColorNames() throws IOException {
		loadDefault();
	}
	
	public void loadDefault() throws IOException {
		try (InputStream is = getClass().getResourceAsStream("ColorNames.properties")) {
			Properties colorNamesMapping = new Properties();
			colorNamesMapping.load(is);
			load(colorNamesMapping);
		}
	}
	
	public void load(Properties colorNamesMapping) throws IOException {
		double tmp[] = new double[3];
		for (Map.Entry i : colorNamesMapping.entrySet()) {
			String k = (String) i.getKey();
			int color = Integer.decode(k);
			ColorConversion.RGB.fromRGB(color, tmp);
			ColorConversion.HSL.fromDRGB(tmp, tmp);
			colors.add(new ColorNameData((String) i.getValue(), color, tmp));
		}
		Collections.sort(colors);
	}
	
	public static final double C2PI = Math.PI * 2.0;
	
	public static double fixAngle2PI(double angle) {
		angle %= C2PI;
		return (angle < 0) ? C2PI + angle : angle;
	}
	
	/**
	 * Returns -1 - choose A, 1 - choose B, 0 - A equals B
	 */
	public static int snap(double A, double value, double B) {
		return Double.compare(A, B) == 0 ? 0 : Double.compare(Math.abs(value - A), Math.abs(value - B));
	}
	
	public String color2Name(int color) {
		if (colors.size() == 0)
			return "";
		double tmp[] = new double[3];
		ColorConversion.RGB.fromRGB(color, tmp);
		ColorConversion.HSL.fromDRGB(tmp, tmp);
		int index = Collections.binarySearch(colors, tmp, new Comparator() {
			public int compare(Object o1, Object o2) {
				ColorNameData cd = (ColorNameData) o1;
				double tmp[] = (double[]) o2;
				int r = Double.compare(cd.h, tmp[0]);
				if (r == 0)
					r = Double.compare(cd.s, tmp[1]);
				if (r == 0)
					r = Double.compare(cd.l, tmp[2]);
				return r;
			}
		});
		if (index < 0) {
			index = -index;
			ColorNameData cd2 = colors.get(index % colors.size());
			index--;
			ColorNameData cd1 = colors.get(index < 0 ? colors.size() - 1 : index);
			double a = fixAngle2PI(cd2.h - cd1.h);
			double b = fixAngle2PI(tmp[0] - cd1.h);
		
		}
		System.out.println(index);
		return "";
	}
	
	void doIt() throws Exception {
		color2Name(0x0000Fe);
	}

	public static void main(String[] args) throws Exception {
		new ColorNames().doIt();
		System.out.println("Done.");
	}
}
