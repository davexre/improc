package com.slavi.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

import com.slavi.math.MathUtil;
import com.slavi.util.file.FileUtil;

public class ColorNamesTest {
	public static double colorDist(ColorNameData i, double tmp[]) {
		return
				MathUtil.fixAnglePI(i.h - tmp[0]) / 2 +
				Math.abs(i.s - tmp[1]) +
				Math.abs(i.l - tmp[2]);
	}

	static int numColors = 20;
	static int colHeight = 20;

	public static void drawColor(Graphics2D g, int row, int color, String name) {
		g.setColor(new Color(color));
		g.fillRect(0, row * colHeight, 50, colHeight);
		g.setColor(Color.black);
		g.drawString(name, 55, (row + 1) * colHeight - 5);
	}

	void doIt() throws Exception {
		ColorNames cn = new ColorNames();
		cn.loadDefault();
/*
		double tmp[] = { 6.23, 0.8, 0.5 };
		double tmp2[] = tmp.clone();
		ColorConversion.HSL.toDRGB(tmp, tmp2);
		int color = ColorConversion.RGB.toRGB(tmp2);
*/
		int color = 0xFFEBCD;
		double tmp[] = new double[3];
		ColorConversion.RGB.fromRGB(color, tmp);
		ColorConversion.HSL.instance.fromDRGB(tmp, tmp);

		ArrayList<ColorNameData> bak = new ArrayList<>(cn.colors);
		Collections.sort(bak, (a, b) -> {
			return Double.compare(
					colorDist(a, tmp),
					colorDist(b, tmp));
		});

		System.out.println(String.format("\t(0x%2$06X) h:%3$.2f s:%4$.2f, l:%5$.2f, %1$s", "test", color, tmp[0], tmp[1], tmp[2]));
		for (int i = 0; i < numColors; i++) {
			System.out.println(String.format("%.2f\t%s", 100000* colorDist(bak.get(i), tmp),bak.get(i)));
		}

		BufferedImage bi = new BufferedImage(200,  numColors * colHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		drawColor(g, 0, color, "TEST");
		for (int i = 1; i < numColors; i++) {
			ColorNameData c = bak.get(i - 1);
			drawColor(g, i, c.color, c.name);
		}

		String fouName = "target/tmp.png";
		FileUtil.rollFile(fouName);
		File fou = new File(fouName);
		ImageIO.write(bi, "png", fou);
		System.out.println(fou.getAbsolutePath());
	}

	void doIt2() throws Exception {
		ColorNames cn = new ColorNames();
		cn.loadDefault();

		ArrayList<ColorNameData> bak = new ArrayList<>(cn.colors);
		Collections.sort(bak, (a, b) -> {
			return Double.compare(a.h, b.h);
		});

		for (Object i : cn.colors)
			System.out.println(i);
/*
		int color = 0xCE2028;
		double tmp[] = new double[3];
		ColorConversion.RGB.fromRGB(color, tmp);
		ColorConversion.HSL.fromDRGB(tmp, tmp);
		*/
		double tmp[] = { 6.23, 0.8, 0.5 };
		double tmp2[] = tmp.clone();
		ColorConversion.HSL.instance.toDRGB(tmp, tmp2);
		int color = ColorConversion.RGB.toRGB(tmp2);

		System.out.println();
		System.out.println(String.format("(0x%2$06X) h:%3$.2f s:%4$.2f, l:%5$.2f, %1$s", "test", color, tmp[0], tmp[1], tmp[2]));
		System.out.println(cn.findClosestColor(color));
	}

	public static void main(String[] args) throws Exception {
		new ColorNamesTest().doIt();
//		System.out.println(String.format("%.20f", Math.pow(MathUtil.fixAnglePI(6.03-6.23) + 0.001, 2) * Math.pow(0.001, 2) * Math.pow(0.001, 1)));
//		System.out.println(String.format("%.20f", Math.pow(MathUtil.fixAnglePI(   0-6.23) + 0.001, 2) * Math.pow(0.001, 2) * Math.pow(0.011, 1)));
		System.out.println("Done.");
	}
}
