package com.slavi.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ColorNames {

	public static class ColorNameData implements Comparable<ColorNameData> {
		public final String name;
		public final int color;
		public final double h;
		public final double s;
		public final double l;
		
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
		
		public String toString() {
			return String.format("%s (0x%06X)", name, color);
		}
	}
	
	List<ColorNameData> colors = new ArrayList<>();

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colors.size(); i++) {
			sb.append(i);
			sb.append(" ");
			sb.append(colors.get(i).toString());
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public ColorNames() throws IOException {
		loadDefault();
	}
	
	public void loadDefault() throws IOException {
		try (InputStream is = getClass().getResourceAsStream("ColorNames-full.properties")) {
			load(is);
		}
	}

	public void load(InputStream is) throws IOException {
		Properties colorNamesMapping = new Properties();
		colorNamesMapping.load(new InputStreamReader(is));
		load(colorNamesMapping);
	}
	
	public void load(Properties colorNamesMapping) throws IOException {
		double tmp[] = new double[3];
		HashSet<Integer> usedColors = new HashSet<>(); // used to remove duplicate colors
		for (Map.Entry i : colorNamesMapping.entrySet()) {
			String k = (String) i.getKey();
			int color = Integer.decode(k);
			if (usedColors.add(color)) {
				ColorConversion.RGB.fromRGB(color, tmp);
				ColorConversion.HSL.fromDRGB(tmp, tmp);
				colors.add(new ColorNameData((String) i.getValue(), color, tmp));
			}
		}
		Collections.sort(colors);
	}
	
	public static final double C2PI = Math.PI * 2.0;
	
	public static double fixAngle2PI(double angle) {
		angle %= C2PI;
		return (angle < 0) ? C2PI + angle : angle;
	}
	
	public static int fixIndex(int index, int size) {
		if (size <= 0)
			return -1;
		index %= size;
		if (index < 0)
			index += size;
		return index;
	}
	
	/**
	 * Returns -1 - choose A, 1 - choose B, 0 - A equals B
	 */
	public static int snap(double A, double value, double B) {
		return Double.compare(Math.abs(value - A), Math.abs(value - B));
	}
	

	public String color2Name(int color) {
		ColorNameData cd = findClosestColor(color);
		return cd == null ? "" : cd.name;
	}
	
	public ColorNameData findClosestColor(int color) {
		if (colors.size() == 0)
			return null;
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
		ColorNameData cd;
		if (index < 0) {
			index = -index - 1;
			ColorNameData cd2 = colors.get(index % colors.size());
			index--;
			cd = colors.get(index < 0 ? colors.size() - 1 : index);
			double a = fixAngle2PI(tmp[0] - cd.h);
			double b = fixAngle2PI(cd2.h - tmp[0]);
			int r = snap(0, a, a + b);
			if (r == 0) {
				r = snap(cd.s, tmp[1], cd2.s);
				if (r == 0)
					r = snap(cd.l, tmp[2], cd2.l);
			}
			if (r > 0)
				cd = cd2;
		} else {
			cd = colors.get(index);
		}
		return cd;
	}
}
