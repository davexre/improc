package com.test.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.slavi.math.MathUtil;
import com.slavi.util.ColorConversion;
import com.slavi.util.Util;

public class TestColorConversion {

	static abstract class Dummy {
		public abstract double getValue(double DRGB[]);
	}
	
	public static void doit(BufferedImage bi, BufferedImage bo, String fouName, Dummy op) throws IOException {
		System.out.println(fouName);
		double DRGB[] = new double[3];
		for (int j = 0; j < bi.getHeight(); j++)
			for (int i = 0; i < bi.getWidth(); i++) {
				int rgb = bi.getRGB(i, j);
				ColorConversion.RGB.fromRGB(rgb, DRGB);
				double val = op.getValue(DRGB);
				int c = (int) MathUtil.clipValue(val, 0.0, 255.0);
				c = (c << 16) | (c << 8) | c;
				bo.setRGB(i, j, c);
			}
		ImageIO.write(bo, "png", new File(fouName));		
	}

	public static void processOneImage(String fname, String fouDir) throws IOException {
		File fin = new File(fname);
		String baseName = Util.changeFileExtension(fin.getName(), "");
		
		BufferedImage bi = ImageIO.read(fin);
		BufferedImage bo = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		FileInputStream fis = new FileInputStream(fin);
		FileOutputStream fos = new FileOutputStream(fouDir + fin.getName());
		byte buf[] = new byte[1000];
		while (fis.available() > 0) {
			int read = fis.read(buf);
			fos.write(buf, 0, read);
		}
		fis.close();
		fos.close();

		doit(bi, bo, fouDir + baseName + " HSL L.png", new Dummy() {
			public double getValue(double[] DRGB) {
				ColorConversion.HSL.fromDRGB(DRGB, DRGB);
				return DRGB[2] * 255.0;
			}
		});

		doit(bi, bo, fouDir + baseName + " HSL S.png", new Dummy() {
			public double getValue(double[] DRGB) {
				ColorConversion.HSL.fromDRGB(DRGB, DRGB);
				return DRGB[1] * 255.0;
			}
		});
		
		doit(bi, bo, fouDir + baseName + " HSL H.png", new Dummy() {
			public double getValue(double[] DRGB) {
				ColorConversion.HSL.fromDRGB(DRGB, DRGB);
				return DRGB[0] * 255.0 / MathUtil.C2PI;
			}
		});
		
		doit(bi, bo, fouDir + baseName + " HSV H.png", new Dummy() {
			public double getValue(double[] DRGB) {
				ColorConversion.HSV.fromDRGB(DRGB, DRGB);
				return DRGB[0] * 255.0 / MathUtil.C2PI;
			}
		});
		
		doit(bi, bo, fouDir + baseName + " HSV S.png", new Dummy() {
			public double getValue(double[] DRGB) {
				ColorConversion.HSV.fromDRGB(DRGB, DRGB);
				return DRGB[1] * 255.0;
			}
		});
		
		doit(bi, bo, fouDir + baseName + " HSV V.png", new Dummy() {
			public double getValue(double[] DRGB) {
				ColorConversion.HSV.fromDRGB(DRGB, DRGB);
				return DRGB[2] * 255.0;
			}
		});

		doit(bi, bo, fouDir + baseName + " BW.png", new Dummy() {
			public double getValue(double[] DRGB) {
				return 255.0 * (DRGB[0] + DRGB[1] + DRGB[2]) / 3.0;
			}
		});

		doit(bi, bo, fouDir + baseName + " RGB R.png", new Dummy() {
			public double getValue(double[] DRGB) {
				return 255.0 * DRGB[0];
			}
		});

		doit(bi, bo, fouDir + baseName + " RGB G.png", new Dummy() {
			public double getValue(double[] DRGB) {
				return 255.0 * DRGB[1];
			}
		});

		doit(bi, bo, fouDir + baseName + " RGB B.png", new Dummy() {
			public double getValue(double[] DRGB) {
				return 255.0 * DRGB[2];
			}
		});
	}
	
	public static void main(String[] args) throws Exception {
		String fouDir = "/home/slavian/S/java/Images/work/1/";
		processOneImage("/home/slavian/S/java/Images/Image data/20090801 Vodopad Skaklia/Skaklia 2/P1100395.jpg", fouDir);
		processOneImage("/home/slavian/S/java/Images/Image data/20090801 Vodopad Skaklia/Skaklia 2/P1100403.jpg", fouDir);
		processOneImage("/home/slavian/S/java/Images/Image data/20090801 Vodopad Skaklia/Skaklia 2/P1100404.jpg", fouDir);
		processOneImage("/home/slavian/S/java/Images/Image data/20090801 Vodopad Skaklia/Skaklia 2/P1100410.jpg", fouDir);
	}
}
