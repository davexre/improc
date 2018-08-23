package com.test.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import com.slavi.util.ColorConversion;
import com.slavi.util.Const;

public class ImageHistogram {

	public static void makeHistogram(BufferedImage bi, int lightHist[], int saturationHist[]) {
		int sizeX = bi.getWidth();
		int sizeY = bi.getHeight();
		int maxL = lightHist.length - 1;
		int maxS = saturationHist.length - 1;
		double DRGB[] = new double[3];
		double HSL[] = new double[3];
		Arrays.fill(lightHist, 0);
		for (int j = sizeY - 1; j >= 0; j--) {
			for (int i = sizeX - 1; i >= 0; i--) {
				int color = bi.getRGB(i, j);
				ColorConversion.RGB.fromRGB(color, DRGB);
				ColorConversion.HSL.instance.fromDRGB(DRGB, HSL);
				lightHist[(int) (HSL[2] * maxL)]++;
				saturationHist[(int) (HSL[1] * maxS)]++;
			}
		}
	}

	public static void makeCDF(int histogram[], double dest[]) {
		int size = histogram.length;
		if (size != dest.length) {
			throw new Error("Invalid argument");
		}
		double sum = 0;
		for (int i = 0; i < size; i++) {
			sum += histogram[i];
		}
		double c = 0;
		for (int i = 0; i < size; i++) {
			c += histogram[i];
			dest[i] = c / sum;
		}
	}

	public String histCDFToString(double cdf[]) {
		StringBuilder sb = new StringBuilder();
		for (double i : cdf) {
			sb.append(i);
			sb.append('\t');
		}
		return sb.toString();
	}

	public double[] fromString(String str) {
		StringTokenizer st = new StringTokenizer(str, "\t");
		int size = st.countTokens();
		double res[] = new double[size];
		for (int i = 0; i < size; i++) {
			String s = st.nextToken();
			double v = Double.parseDouble(s);
			res[i] = v;
		}
		return res;
	}
/*
	public static void main(String[] args) throws IOException {
		String thedir = "c:/users/s/java/images/";

		File fi1 = new File(thedir + "i1.jpg");
		File fi2 = new File(thedir + "i2.jpg");

		File fo1 = new File(thedir + "i1o.jpg");
		File fo2 = new File(thedir + "i2o.jpg");

		BufferedImage bi1 = ImageIO.read(fi1);
		BufferedImage bi2 = ImageIO.read(fi2);

		BufferedImage bo1 = new BufferedImage(bi1.getWidth(), bi1.getHeight(), BufferedImage.TYPE_INT_RGB);
		BufferedImage bo2 = new BufferedImage(bi2.getWidth(), bi2.getHeight(), BufferedImage.TYPE_INT_RGB);

		ImageHistogram hi1 = new ImageHistogram();
		ImageHistogram hi2 = new ImageHistogram();

		System.out.println(hi1);
		System.out.println(hi2);


		ImageHistogram ho1 = new ImageHistogram();
		ImageHistogram ho2 = new ImageHistogram();

		System.out.println(ho1);
		System.out.println(ho2);

		ImageIO.write(bo1, "jpg", fo1);
		ImageIO.write(bo2, "jpg", fo2);

		ImageIO.write(bo1, "jpg", new File(thedir + "a/i1o.jpg"));
		ImageIO.write(bo2, "jpg", new File(thedir + "a/i2o.jpg"));
	}
*/



	public static void main(String[] args) throws Exception {
		String finName = Const.sourceImage;
		String fouDir = Const.workDir;

		BufferedImage bi = ImageIO.read(new File(finName));
		BufferedImage bo = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);

		int lightHist[] = new int[256];
		int saturationHist[] = new int[256];
		makeHistogram(bi, lightHist, saturationHist);

		double lightCDF[] = new double[256];
		double saturationCDF[] = new double[256];
		makeCDF(lightHist, lightCDF);
		makeCDF(saturationHist, saturationCDF);

		double DRGB[] = new double[3];
		double HSL[] = new double[3];
		for (int j = bi.getHeight() - 1; j >= 0; j--) {
			for (int i = bi.getWidth() - 1; i >= 0; i--) {
				int color = bi.getRGB(i, j);
				ColorConversion.RGB.fromRGB(color, DRGB);
				ColorConversion.HSL.instance.fromDRGB(DRGB, HSL);
				double l = HSL[2];
				double l2 = lightCDF[(int) (l * 255.0)];
				HSL[2] = (l + l2) / 2.0;
				ColorConversion.HSL.instance.toDRGB(HSL, DRGB);
				color = ColorConversion.RGB.toRGB(DRGB);
				bo.setRGB(i, j, color);
			}
		}

		ImageIO.write(bi, "jpg", new File(fouDir + "/imageHistogram-source.jpg"));
		ImageIO.write(bo, "jpg", new File(fouDir + "/imageHistogram-transformed.jpg"));
	}
}
