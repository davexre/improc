package com.slavi.improc.myadjust;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.slavi.util.ColorConversion;
import com.slavi.util.Const;

public class HistogramTest {
	public static void main1(String[] args) {
		Histogram h = new Histogram(10, 0, 10, true);
		h.addValue(0);
		h.addValue(2.5);
		h.addValue(9);
		h.addValue(9);
		h.addValue(10);
		System.out.println(Arrays.toString(h.getHistogram()));
		System.out.println(Arrays.toString(h.getCDF()));
		System.out.println(h.calcNoramlizedCDF());
		System.out.println(h.calcHistogramEqualization(2.5));
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Source: " + Const.sourceImage);
		System.out.println("Dest  : " + Const.outputImage);

		BufferedImage image = ImageIO.read(new File(Const.sourceImage));

		int valueIndex = 2;
		Histogram h = new Histogram(255, 0, 1, true);
		double tmp[] = new double[3];
		for (int j = 0; j < image.getHeight(); j++) {
			for (int i = 0; i < image.getWidth(); i++) {
				int color = image.getRGB(i, j);
				ColorConversion.RGB.fromRGB(color, tmp);
				ColorConversion.HSL.fromDRGB(tmp, tmp);
				h.addValue(tmp[valueIndex]);
			}
		}

		double scale = 1;
		Histogram h2 = new Histogram(255, 0, 1, true);
		BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int j = 0; j < image.getHeight(); j++) {
			for (int i = 0; i < image.getWidth(); i++) {
				int color = image.getRGB(i, j);
				ColorConversion.RGB.fromRGB(color, tmp);
				ColorConversion.HSL.fromDRGB(tmp, tmp);
				double d = h.calcHistogramEqualization(tmp[valueIndex]);
				tmp[valueIndex] = (d - tmp[valueIndex]) * scale + tmp[valueIndex];
				h2.addValue(d);
				ColorConversion.HSL.toDRGB(tmp, tmp);
				color = ColorConversion.RGB.toRGB(tmp);
				out.setRGB(i, j, color);
			}
		}

		System.out.print(h.calcNoramlizedCDF());
		System.out.println(h2.calcNoramlizedCDF());

		System.out.println(Arrays.toString(h.getHistogram()));
		System.out.println(Arrays.toString(h2.getHistogram()));
		ImageIO.write(out, "png", new File(Const.outputImage));
		System.out.println("Done.");
	}
}
