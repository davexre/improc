package com.test.image;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.slavi.math.adjust.Statistics;
import com.slavi.util.ColorConversion;
import com.slavi.util.Const;
import com.slavi.util.Util;
import com.slavi.util.file.FindFileIterator;

public class ImageAdjust {

	public abstract class Transform {
		public abstract void transform(double srcDRGB[], double destDRGB[]);
	}
	
	Statistics statS;
	Statistics statL;
	
	public void makeStats(BufferedImage bi) {
		double srcDRGB[] = new double[3];
		statS = new Statistics();
		statL = new Statistics();
		statS.start();
		statL.start();
		for (int j = 0; j < bi.getHeight(); j++)
			for (int i = 0; i < bi.getWidth(); i++) {
				int color = bi.getRGB(i, j);
				ColorConversion.RGB.fromRGB(color, srcDRGB);
				ColorConversion.HSL.fromDRGB(srcDRGB, srcDRGB);
				statS.addValue(srcDRGB[1]);
				statL.addValue(srcDRGB[2]);
			}
		statS.stop();
		statL.stop();
		System.out.println("Stat S");
		System.out.println(statS);
		System.out.println("Stat L");
		System.out.println(statL);
	}
	
	public void hist(BufferedImage bi, String fouName, Transform tr) throws Exception {
		double srcDRGB[] = new double[3];
		double destDRGB[] = new double[3];

		System.out.println("Generating " + fouName);
		BufferedImage bo = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int j = 0; j < bi.getHeight(); j++)
			for (int i = 0; i < bi.getWidth(); i++) {
				int color = bi.getRGB(i, j);
				ColorConversion.RGB.fromRGB(color, srcDRGB);
				tr.transform(srcDRGB, destDRGB);
				color = ColorConversion.RGB.toRGB(destDRGB);
				bo.setRGB(i, j, color);
			}
		ImageIO.write(bo, "png", new File(fouName));
	}
	
	public void doIt(String finName, String fouDir) throws Exception {
		File fin = new File(finName);
		String fouBaseName = new File(fouDir + fin.getName()).getAbsolutePath();
		Util.copyFileIfDifferent(fin, new File(Util.changeFileExtension(fouBaseName, "_original.jpg")));
		BufferedImage bi = ImageIO.read(fin);
		makeStats(bi);
/*
 		hist(bi, Util.changeFileExtension(fouBaseName, "_SL.jpg"), new Transform() {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				double tmp = destDRGB[2];
//				destDRGB[2] = destDRGB[0] / MathUtil.C2PI;
				destDRGB[0] = tmp * MathUtil.C2PI;
				ColorConversion.HSL.toDRGB(destDRGB, destDRGB);
			}
		});
*/

 		hist(bi, Util.changeFileExtension(fouBaseName, "_SL.jpg"), new Transform() {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				destDRGB[1] *= 0.35 / statS.getAvgValue();
				destDRGB[2] *= 0.45 / statL.getAvgValue();
				ColorConversion.HSL.toDRGB(destDRGB, destDRGB);
			}
		});
		hist(bi, Util.changeFileExtension(fouBaseName, "_L.jpg"), new Transform() {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				destDRGB[2] *= 0.45 / statL.getAvgValue();
				ColorConversion.HSL.toDRGB(destDRGB, destDRGB);
			}
		});
/*		hist(bi, Util.changeFileExtension(fouBaseName, "_S_BW.jpg"), new Transform() {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				destDRGB[0] = destDRGB[2] = destDRGB[1];
			}
		});
		hist(bi, Util.changeFileExtension(fouBaseName, "_L_BW.jpg"), new Transform() {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				destDRGB[0] = destDRGB[1] = destDRGB[2];
			}
		});
*/		
	}
	
	public static void main(String[] args) throws Exception {
		String finName = Const.sourceImage;
		String fouDir = "d:/temp/1/";

		String finDir = "D:/Users/S/Java/Images/Image data/Evgeni panorama/1/*.jpg";
		FindFileIterator ff = FindFileIterator.makeWithWildcard(finDir, true, true);
		
		ImageAdjust t = new ImageAdjust();
		while (ff.hasNext()) {
			finName = ff.next().getAbsolutePath();
			t.doIt(finName, fouDir);
		}
		System.out.println("Done.");
	}
}
