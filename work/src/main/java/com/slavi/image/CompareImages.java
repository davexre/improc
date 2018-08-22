package com.slavi.image;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

import com.slavi.math.MathUtil;
import com.slavi.util.ColorConversion;

public class CompareImages {

	void err(String msg) {
		System.out.println(msg);
		System.exit(1);
	}

	void err(boolean test, String msg) {
		if (test)
			err(msg);
	}

	double unmatchedPrercent = 10;
	public void doIt(String[] args) throws Exception {
		String f1 = args[0];
		String f2 = args[1];
		BufferedImage i1 = ImageIO.read(new FileInputStream(f1));
		BufferedImage i2 = ImageIO.read(new FileInputStream(f2));

		err(i1.getWidth() != i2.getWidth(), "Width does not match");
		err(i1.getHeight() != i2.getHeight(), "Height does not match");

		double rgb1[] = new double[3];
		double rgb2[] = new double[3];
		double tmp1[] = new double[3];
		double tmp2[] = new double[3];
		double maxRgbDist = 0;
		double maxRGB[] = new double[3];
		double maxHSL[] = new double[3];
		double d;
		int unmatchedPixels = 0;
		for (int ix = 0; ix < i1.getWidth(); ix++) {
			for (int iy = 0; iy < i1.getHeight(); iy++) {
				boolean isSame = true;
				int c1 = i1.getRGB(ix, iy);
				int c2 = i2.getRGB(ix, iy);
				ColorConversion.RGB.fromRGB(c1, rgb1);
				ColorConversion.RGB.fromRGB(c2, rgb2);

				double dist = 0;
				for (int k = 0; k < 3; k++) {
					d = Math.abs(rgb1[k] - rgb2[k]);
					if (d > unmatchedPrercent/255.0) isSame = false;
					dist += d * d;
					if (maxRGB[k] < d)
						maxRGB[k] = d;
				}
				dist = Math.sqrt(dist);
				if (maxRgbDist < dist)
					maxRgbDist = dist;

				ColorConversion.HSL.fromDRGB(rgb1, tmp1);
				ColorConversion.HSL.fromDRGB(rgb2, tmp2);
				d = MathUtil.fixAnglePI(tmp1[0] - tmp2[0]);
				if (d > unmatchedPrercent/MathUtil.C2PI) isSame = false;
				if (maxHSL[0] < d) {
					maxHSL[0] = d;
					System.out.println("  " + MathUtil.d4(tmp1[0]) + " " + MathUtil.d4(tmp2[0]) + " d:" + MathUtil.d4(d) +
						" " + ColorConversion.RGB.toString(rgb1) +
						" -> " + ColorConversion.RGB.toString(rgb2));
				}
				for (int k = 1; k < 3; k++) {
					d = Math.abs(tmp1[k] - tmp2[k]);
					if (d > unmatchedPrercent/100.0) isSame = false;
					if (maxHSL[k] < d)
						maxHSL[k] = d;
				}

				if (!isSame)
					unmatchedPixels++;
			}
		}

		System.out.println("Max RGB dist:    " + MathUtil.d2(maxRgbDist));
		System.out.println("Max R dist:      " + MathUtil.d2(maxRGB[0]));
		System.out.println("Max G dist:      " + MathUtil.d2(maxRGB[1]));
		System.out.println("Max B dist:      " + MathUtil.d2(maxRGB[2]));
		System.out.println("Max H dist:      " + MathUtil.d4(maxHSL[0]));
		System.out.println("Max S dist:      " + MathUtil.d2(maxHSL[1]));
		System.out.println("Max L dist:      " + MathUtil.d2(maxHSL[2]));
		int allPixels = i1.getWidth() * i1.getHeight();
		System.out.println("Unmatched pixels:" + unmatchedPixels + "/" + allPixels);
		System.out.println("Unmatched %:     " + MathUtil.d2(100.0 * unmatchedPixels / allPixels));
	}

	public static void main(String[] args) throws Exception {
		args = new String[] {
			"/home/spetrov/temp/tp/DSC_3808.JPG",
			"/home/spetrov/temp/tp/a6.jpg",
		};
		new CompareImages().doIt(args);
		System.out.println("Done.");
	}
}
