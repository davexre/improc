package com.slavi.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.slavi.math.MathUtil;

public class ColorNames {

	List<ColorNameData> colors = new ArrayList<>();

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colors.size(); i++) {
			if (i > 0)
				sb.append('\n');
			sb.append(colors.get(i).toString());
		}
		return sb.toString();
	}

	public void loadDefault() throws IOException {
		try (InputStream is = getClass().getResourceAsStream("ColorNames-full.properties")) {
			load(is);
		}
	}

	public void loadShortList() throws IOException {
		try (InputStream is = getClass().getResourceAsStream("ColorNames.properties")) {
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
		Collections.sort(colors, (o1, o2) -> {
			int r = Double.compare(o1.h, o2.h);
			if (r == 0)
				r = Double.compare(o1.s, o2.s);
			if (r == 0)
				r = Double.compare(o1.l, o2.l);
			return r;
		});
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

		ColorNameData r = null;
		double dist = Double.MAX_VALUE;
		for (ColorNameData i : colors) {
			double d =
					Math.pow(MathUtil.fixAnglePI(i.h - tmp[0]) + 0.001, 2.5) +
					Math.pow(Math.abs(i.s - tmp[1]) + 0.001, 2) +
					Math.pow(Math.abs(i.l - tmp[2]) + 0.001, 1.5);
			if (d < dist) {
				dist = d;
				r = i;
			}
		}
		return r;
	}
}
