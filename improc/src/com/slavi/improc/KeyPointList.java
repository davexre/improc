package com.slavi.improc;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FileStamp;

public class KeyPointList {
	public static final String fileHeader = "KeyPoint file version 1.2";
	
	public final ArrayList<KeyPoint> items = new ArrayList<KeyPoint>();
	
	public FileStamp imageFileStamp = null;
	
	public int imageSizeX;

	public int imageSizeY;
	
	// My adjust
	public int imageId = -1;
	public double rx = 0.0, ry = 0.0, rz = 0.0;

	public Matrix camera2real;
	public Matrix dMdX, dMdY, dMdZ;
	public Point2D.Double min, max;
	
	public double cameraOriginX, cameraOriginY, cameraScale; 
	public double scaleZ;
	// My adjust

	public static final double defaultCameraFieldOfView = MathUtil.deg2rad * 40;
	public static final double defaultCameraFOV_to_ScaleZ = 1.0 / 
			(2.0 * Math.tan(defaultCameraFieldOfView / 2.0));
	
	public KeyPointList() {
	}

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
		while (fin.ready()) {
			String str = fin.readLine().trim();
			if ((str.length() > 0) && (str.charAt(0) != '#')) {
				KeyPoint kp = KeyPoint.fromString(str);
				kp.keyPointList = r;
				r.items.add(kp);
			}
		}
		return r;
	}

	public void toTextStream(PrintWriter fou) {
		fou.println(imageFileStamp.toString());
		fou.println(imageSizeX + "\t" + imageSizeY);
		for (KeyPoint item : items)
			fou.println(item.toString());
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
