package com.slavi.improc;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.ColorConversion;
import com.slavi.util.Util;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FileStamp;

public class KeyPointList {
	public static final String fileHeader = "KeyPoint file version 1.28";

	public final ArrayList<KeyPoint> items = new ArrayList<KeyPoint>();

	public KeyPointTree tree;

	public KeyPointTreeImageSpace imageSpaceTree;

	public FileStamp imageFileStamp = null;

	public int imageSizeX;

	public int imageSizeY;

	// Spherical pano adjust
	public double fov = defaultCameraFieldOfView; // Field of view
	public double sphereRZ1;
	public double sphereRY;
	public double sphereRZ2;
	public Matrix sphereCamera2real;

	// My adjust
	public int imageId = -1;
	public double rx = 0.0, ry = 0.0, rz = 0.0;
	// Translation in 7 params ZYZ
	public double tx = 0.0, ty = 0.0, tz = 0.0;
	// World origin (Focal point of first image) in the coordinate system of the image
	public double worldOrigin[] = new double[3];

	public Matrix camera2real;
	public Matrix dMdX, dMdY, dMdZ;
	public Point2D.Double min, max;

	public double cameraOriginX, cameraOriginY, cameraScale;
	public double scaleZ;
	public int calculatePrimsAtHop;

	// Affine pano adjust
	public double afa; // Affine Forward A
	public double afb; // Affine Forward B
	public double afc; // Affine Forward C (translateX)
	public double afd; // Affine Forward D
	public double afe; // Affine Forward E
	public double aff; // Affine Forward F (translateY)

	public double aba; // Affine Backward A
	public double abb; // Affine Backward B
	public double abc; // Affine Backward C (translateX)
	public double abd; // Affine Backward D
	public double abe; // Affine Backward E
	public double abf; // Affine Backward F (translateY)

	// Helmert pano adjust
	public double hfa; // Helmert Forward A
	public double hfb; // Helmert Forward B
	public double hfc; // Helmert Forward C
	public double hfd; // Helmert Forward D

	public double hba; // Helmert Backward A
	public double hbb; // Helmert Backward B
	public double hbc; // Helmert Backward C
	public double hbd; // Helmert Backward D
	public double hTranslateX;
	public double hTranslateY;

	public static final double defaultCameraFieldOfView = MathUtil.deg2rad * 40;
	public static final double defaultCameraFOV_to_ScaleZ = 1.0 /
			(2.0 * Math.tan(defaultCameraFieldOfView / 2.0));

	// Image histograms
	public static final int histogramSize = 256;
	public int lightHistogram[];
	public int saturationHistogram[];

	public double lightCDF[];
	public double saturationCDF[];

	public static KeyPointList fromTextStream(BufferedReader fin, AbsoluteToRelativePathMaker rootImagesDir) throws IOException {
		KeyPointList r = new KeyPointList();
		r.imageFileStamp = FileStamp.fromString(fin.readLine(), rootImagesDir);
		StringTokenizer st = new StringTokenizer(fin.readLine(), "\t");
		r.imageSizeX = Integer.parseInt(st.nextToken());
		r.imageSizeY = Integer.parseInt(st.nextToken());
		r.cameraOriginX = r.imageSizeX / 2.0;
		r.cameraOriginY = r.imageSizeY / 2.0;
		r.cameraScale = 1.0 / Math.max(r.imageSizeX, r.imageSizeY);
		r.scaleZ = defaultCameraFOV_to_ScaleZ;

		r.lightHistogram = Util.stringToIntArray(fin.readLine());
		r.saturationHistogram = Util.stringToIntArray(fin.readLine());
		r.buildCDFfromHistograms();

		while (fin.ready()) {
			String str = fin.readLine().trim();
			if ((str.length() > 0) && (str.charAt(0) != '#')) {
				KeyPoint kp = KeyPoint.fromString(r, str);
				r.items.add(kp);
			}
		}
		return r;
	}

	public void toTextStream(PrintWriter fou) {
		fou.println(imageFileStamp.toString());
		fou.println(imageSizeX + "\t" + imageSizeY);
		fou.println(Util.arrayToString(lightHistogram));
		fou.println(Util.arrayToString(saturationHistogram));
		for (KeyPoint item : items)
			fou.println(item.toString());
	}

	public void makeHistogram(BufferedImage bi) {
		lightHistogram = new int[histogramSize];
		saturationHistogram = new int[histogramSize];

		int sizeX = bi.getWidth() - 1;
		int sizeY = bi.getHeight() - 1;
		int sizeL = lightHistogram.length - 1;
		int sizeS = saturationHistogram.length - 1;
		double DRGB[] = new double[3];
		double HSL[] = new double[3];
		Arrays.fill(lightHistogram, 0);
		for (int j = sizeY; j >= 0; j--) {
			for (int i = sizeX; i >= 0; i--) {
				int color = bi.getRGB(i, j);
				ColorConversion.RGB.fromRGB(color, DRGB);
				ColorConversion.HSL.instance.fromDRGB(DRGB, HSL);
				lightHistogram[(int) (HSL[2] * sizeL)]++;
				saturationHistogram[(int) (HSL[1] * sizeS)]++;
			}
		}
		buildCDFfromHistograms();
	}

	private void buildCDFfromHistograms() {
		lightCDF = new double[histogramSize];
		saturationCDF = new double[histogramSize];
		makeCDF(lightHistogram, lightCDF);
		makeCDF(saturationHistogram, saturationCDF);
	}

	public static void makeCDF(int histogram[], double dest[]) {
		int size = histogram.length;
		if (size != dest.length) {
			throw new Error("Invalid argument");
		}
		double sum = 0.0;
		for (int i = 0; i < size; i++) {
			sum += histogram[i];
		}
		double c = 0.0;
		for (int i = 0; i < size; i++) {
			c += histogram[i];
			dest[i] = c / sum;
		}
	}

	public void compareToList(KeyPointList dest) {
		int matchedCount1 = 0;
		for (int i = items.size() - 1; i >= 0; i--) {
			KeyPoint sp1 = items.get(i);
			boolean matchingFound = false;
			for (int j = dest.items.size() - 1; j >= 0; j--) {
				KeyPoint sp2 = dest.items.get(j);
				if (sp1.equals(sp2)) {
					matchingFound = true;
					matchedCount1++;
					break;
				}
			}
			if (!matchingFound)
				System.out.println("Point No " + i + " from 1-st list has no match in 2-nd list");
		}

		int matchedCount2 = 0;
		for (int j = dest.items.size() - 1; j >= 0; j--) {
			KeyPoint sp2 = dest.items.get(j);
			boolean matchingFound = false;
			for (int i = items.size() - 1; i >= 0; i--) {
				KeyPoint sp1 = items.get(i);
				if (sp1.equals(sp2)) {
					matchingFound = true;
					matchedCount2++;
					break;
				}
			}
			if (!matchingFound)
				System.out.println("Point No " + j + " from 2-nd list has no match in 1-st list");
		}

		System.out.println("Matched 1-st list against 2-nd list: " + matchedCount1 + "/" + items.size());
		System.out.println("Matched 2-nd list against 1-st list: " + matchedCount2 + "/" + dest.items.size());
	}
}
