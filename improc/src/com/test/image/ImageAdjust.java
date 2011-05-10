package com.test.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.imageio.ImageIO;

import com.slavi.math.adjust.Statistics;
import com.slavi.util.ColorConversion;
import com.slavi.util.Const;
import com.slavi.util.Util;
import com.slavi.util.concurrent.TaskSetExecutor;
import com.slavi.util.file.FindFileIterator;

public class ImageAdjust {

	public abstract class Transform {
		public abstract void transform(double srcDRGB[], double destDRGB[]);
	}
	
	Statistics statS;
	Statistics statL;
	int light[] = new int[256];
	double lightCumul[] = new double[256];
	int saturation[] = new int[256];
	double saturationCumul[] = new double[256];
	
	public void makeStats(BufferedImage bi) {
		double srcDRGB[] = new double[3];
		double HSL[] = new double[3];
		Arrays.fill(light, 0);
		statS = new Statistics();
		statL = new Statistics();
		statS.start();
		statL.start();
		
		for (int j = 0; j < bi.getHeight(); j++)
			for (int i = 0; i < bi.getWidth(); i++) {
				int color = bi.getRGB(i, j);
				ColorConversion.RGB.fromRGB(color, srcDRGB);
				ColorConversion.HSL.fromDRGB(srcDRGB, HSL);
				statS.addValue(srcDRGB[1]);
				statL.addValue(HSL[2]);
				light[(int) (HSL[2] * 255)]++;
				saturation[(int) (HSL[1] * 255)]++;
			}
		statS.stop();
		statL.stop();
		
		double lightC = 0;
		double saturationC = 0;
		double sum = bi.getWidth() * bi.getHeight();
		for (int i = 0; i < light.length; i++) {
			lightC += light[i];
			lightCumul[i] = lightC / sum;
			saturationC += saturation[i];
			saturationCumul[i] = saturationC / sum;
		}
		for (int i = 0; i < light.length; i++) {
			int j = light.length - 1;
			double perfectCumul;
			do {
				light[i] = j;
				perfectCumul = j / 255.0;
				j--;
			} while ( (j >= 0) && (lightCumul[i] < perfectCumul) );
		}

		for (int i = 0; i < saturation.length; i++) {
			int j = saturation.length - 1;
			double perfectCumul;
			do {
				saturation[i] = j;
				perfectCumul = j / 255.0;
				j--;
			} while ( (j >= 0) && (saturationCumul[i] < perfectCumul) );
		}
		
		System.out.println("Stat S");
		System.out.println(statS);
		System.out.println("Stat L");
		System.out.println(statL);
	}
	
	public static abstract class ProcessImage implements Callable<Void> {
		BufferedImage bi;
		String fouName;
		
		public ProcessImage(BufferedImage bi, String fouBaseName, String fouNameEnding) {
			this.bi = bi;
			this.fouName = Util.changeFileExtension(fouBaseName, fouNameEnding + ".png");
		}

		public abstract void transform(double srcDRGB[], double destDRGB[]);

		public Void call() throws Exception {
			double srcDRGB[] = new double[3];
			double destDRGB[] = new double[3];

			System.out.println("Generating " + fouName);
			BufferedImage bo = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
			for (int j = 0; j < bi.getHeight(); j++)
				for (int i = 0; i < bi.getWidth(); i++) {
					int color = bi.getRGB(i, j);
					ColorConversion.RGB.fromRGB(color, srcDRGB);
					transform(srcDRGB, destDRGB);
					color = ColorConversion.RGB.toRGB(destDRGB);
					bo.setRGB(i, j, color);
				}
			ImageIO.write(bo, "png", new File(fouName));
			return null;
		}
	}
	
	public void doIt(ExecutorService exec, String finName, String fouDir) throws Exception {
		File fin = new File(finName);
		String fouBaseName = new File(fouDir + "/" + fin.getName()).getAbsolutePath();
		BufferedImage bi = ImageIO.read(fin);
		makeStats(bi);
		TaskSetExecutor taskSet = new TaskSetExecutor(exec);
		taskSet.add(new ProcessImage(bi, fouBaseName, "_a_original") {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				destDRGB[0] = srcDRGB[0];
				destDRGB[1] = srcDRGB[1];
				destDRGB[2] = srcDRGB[2];
			}
		});

		
/*		taskSet.add(new ProcessImage(bi, fouBaseName, "_SL.png") {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				double tmp = destDRGB[2];
//				destDRGB[2] = destDRGB[0] / MathUtil.C2PI;
				destDRGB[0] = tmp * MathUtil.C2PI;
				ColorConversion.HSL.toDRGB(destDRGB, destDRGB);
			}
		});*/

		taskSet.add(new ProcessImage(bi, fouBaseName, "_CDF_L") {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				destDRGB[2] = (destDRGB[2] + lightCumul[(int)(destDRGB[2] * 255)]) / 2.0;
				ColorConversion.HSL.toDRGB(destDRGB, destDRGB);
			}
		});
		
		taskSet.add(new ProcessImage(bi, fouBaseName, "_CDF_S") {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				destDRGB[1] = (destDRGB[1] + saturationCumul[(int)(destDRGB[1] * 255)]) / 2.0;
				ColorConversion.HSL.toDRGB(destDRGB, destDRGB);
			}
		});
		
		taskSet.add(new ProcessImage(bi, fouBaseName, "_CDF_SL") {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				destDRGB[1] = (destDRGB[1] + saturationCumul[(int)(destDRGB[1] * 255)]) / 2.0;
				destDRGB[2] = (destDRGB[2] + lightCumul[(int)(destDRGB[2] * 255)]) / 2.0;
				ColorConversion.HSL.toDRGB(destDRGB, destDRGB);
			}
		});
		
		taskSet.add(new ProcessImage(bi, fouBaseName, "_SL") {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				destDRGB[1] *= 0.35 / statS.getAvgValue();
				destDRGB[2] *= 0.45 / statL.getAvgValue();
				ColorConversion.HSL.toDRGB(destDRGB, destDRGB);
			}
		});
		
		final double scaleL = 0.8;
		final double scaleS = scaleL;
		
/*		taskSet.add(new ProcessImage(bi, fouBaseName, "_L2") {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				double delta = ((double) light[(int) (destDRGB[2] * 255.0)]) / 255.0 - destDRGB[2];
				destDRGB[2] += delta * scaleL;
				ColorConversion.HSL.toDRGB(destDRGB, destDRGB);
			}
		});*/

/*		taskSet.add(new ProcessImage(bi, fouBaseName, "_S2") {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				double delta = ((double) saturation[(int) (destDRGB[1] * 255.0)]) / 255.0 - destDRGB[1];
				destDRGB[1] += delta * scaleS;
				ColorConversion.HSL.toDRGB(destDRGB, destDRGB);
			}
		});*/

/*		taskSet.add(new ProcessImage(bi, fouBaseName, "_SL2") {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				double delta = ((double) light[(int) (destDRGB[2] * 255.0)]) / 255.0 - destDRGB[2];
				destDRGB[2] += delta * scaleL;
				delta = ((double) saturation[(int) (destDRGB[1] * 255.0)]) / 255.0 - destDRGB[1];
				destDRGB[1] += delta * scaleS;
				ColorConversion.HSL.toDRGB(destDRGB, destDRGB);
			}
		});*/

		taskSet.add(new ProcessImage(bi, fouBaseName, "_L") {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				destDRGB[2] *= 0.45 / statL.getAvgValue();
				ColorConversion.HSL.toDRGB(destDRGB, destDRGB);
			}
		});

/*		taskSet.add(new ProcessImage(bi, fouBaseName, "_S_BW") {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				destDRGB[0] = destDRGB[2] = destDRGB[1];
			}
		});*/
		
		taskSet.add(new ProcessImage(bi, fouBaseName, "_L_BW") {
			public void transform(double[] srcDRGB, double[] destDRGB) {
				ColorConversion.HSL.fromDRGB(srcDRGB, destDRGB);
				destDRGB[0] = destDRGB[1] = destDRGB[2];
			}
		});
		
		taskSet.addFinished();
		taskSet.get();
	}
	
	public static void main(String[] args) throws Exception {
		ImageAdjust t = new ImageAdjust();
		String finName = Const.sourceImage;
		String fouDir = Const.workDir + "/ImageAdjust";
//		String fouDir = "/home/slavian/S/temp/1";
		
		String finDir = "C:/Users/i047367/S/java/Images/Image data/20090801 Vodopad Skaklia/1/*.jpg";
//		String finDir = "/home/slavian/S/java/Images/Image data/20090801 Vodopad Skaklia/Skaklia 2/*.jpg";
		FindFileIterator ff = FindFileIterator.makeWithWildcard(finDir, true, true);
		
		ExecutorService exec = Util.newBlockingThreadPoolExecutor();
		while (ff.hasNext()) {
			finName = ff.next().getAbsolutePath();
			System.out.println(finName);
			t.doIt(exec, finName, fouDir);
		}
		exec.shutdown();
		System.out.println("Done.");
	}
}
