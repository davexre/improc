package com.test.improc;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.slavi.improc.SafeImage;
import com.slavi.math.MathUtil;
import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;

/*

Разстояние от т.(x,y) до права (A,B,C):

f(x,y) = A*x + B*y + C

Да се намерят A,B,C такива, че:

SUM(POW(f(A,B,C, x(i), y(i)), 2)) -> min

F(A,B,C) = f(A,B,C) ^ 2;

dF/dA = 2 * f(A,B,C) * x
dF/dB = 2 * f(A,B,C) * y
dF/dC = 2 * f(A,B,C) * 1

dF = δA*dF/dA + δB*dF/dB + δC*dF/dC
dF = 2*f*x + 2*f*y + 2*f*1
dF = 2*f*(x + y + 1) 

 */


public class ImageRot {

	public static class Image {
		double scaleZ, rx, ry, rz, cameraScale;
		int sizeX, sizeY;
		double cameraOriginX, cameraOriginY;
		
		Matrix camera2real;
		Point2D.Double tl, tr, bl, br;
	}
	
	public static void imageToWorld(double sx, double sy, Image srcImage, Point2D.Double dest) {
		sx = (sx - srcImage.cameraOriginX) * srcImage.cameraScale;
		sy = (sy - srcImage.cameraOriginY) * srcImage.cameraScale;
		double sz = srcImage.scaleZ;
		
		double x = 
			sx * srcImage.camera2real.getItem(0, 0) +
			sy * srcImage.camera2real.getItem(1, 0) +
			sz * srcImage.camera2real.getItem(2, 0);
		double y = 
			sx * srcImage.camera2real.getItem(0, 1) +
			sy * srcImage.camera2real.getItem(1, 1) +
			sz * srcImage.camera2real.getItem(2, 1);
		double z = 
			sx * srcImage.camera2real.getItem(0, 2) +
			sy * srcImage.camera2real.getItem(1, 2) +
			sz * srcImage.camera2real.getItem(2, 2);
		
		double d = Math.sqrt(x*x + z*z); 
		x = Math.atan2(x, z);
		y = Math.atan2(y, d);
//		final double pi2 = Math.PI / 2.0; 
//		if (y > pi2) { 
//			y = Math.PI - y;
//			x += Math.PI;
//		}
//		if (y < -pi2) { 
//			y = - Math.PI - y;
//			x += Math.PI;
//		}
//		x = MathUtil.fixAngleMPI_PI(x);
//		if (x > Math.PI)
//			x -= 2*Math.PI;
		
		dest.x = x;
		dest.y = y;
	}
	
	public static void worldToImage(double rx, double ry, Image srcImage, Point2D.Double dest) {
		double sz = 1.0;
		double sx = Math.tan(rx) * sz;
		double sy = Math.tan(ry) * sz;
		
		double x = 
			sx * srcImage.camera2real.getItem(0, 0) +
			sy * srcImage.camera2real.getItem(0, 1) +
			sz * srcImage.camera2real.getItem(0, 2);
		double y = 
			sx * srcImage.camera2real.getItem(1, 0) +
			sy * srcImage.camera2real.getItem(1, 1) +
			sz * srcImage.camera2real.getItem(1, 2);
		double z = 
			sx * srcImage.camera2real.getItem(2, 0) +
			sy * srcImage.camera2real.getItem(2, 1) +
			sz * srcImage.camera2real.getItem(2, 2);
		
		x = srcImage.scaleZ * (x / z);
		y = srcImage.scaleZ * (y / z);
		
		dest.x = (x / srcImage.cameraScale) + srcImage.cameraOriginX;
		dest.y = (y / srcImage.cameraScale) + srcImage.cameraOriginY;
	}
	
	public void dumpPoint(Point2D.Double p) {
		System.out.println(
				MathUtil.d4(MathUtil.rad2deg * p.x) + "\t" +
				MathUtil.d4(MathUtil.rad2deg * p.y) + "\t");
	}
	
	ArrayList<Image> generate() {
		ArrayList<Image> result = new ArrayList<Image>();
		
		int sizeX = 2272;
		int sizeY = 1712;
		double cameraScale = 1.0 / Math.max(sizeX, sizeY);
		int maxI = 4;
		for (int i = 0; i < maxI; i++) {
			Image img = new Image();
			img.sizeX = sizeX;
			img.sizeY = sizeY;
			img.cameraOriginX = sizeX / 2.0;
			img.cameraOriginY = sizeY / 2.0;
			img.cameraScale = cameraScale;

			img.scaleZ = 0.5 / Math.tan(MathUtil.deg2rad * 40 / 2);
			img.ry = 2.0 * i * Math.PI / maxI;
			img.rx = 0 * MathUtil.deg2rad;
			img.rz = 0 * MathUtil.deg2rad;
			img.camera2real = RotationXYZ.makeAngles(img.rx, img.ry, img.rz);
			
			img.tl = new Point2D.Double();
			img.tr = new Point2D.Double();
			img.bl = new Point2D.Double();
			img.br = new Point2D.Double();
			
			imageToWorld(0, 0, img, img.tl);
			imageToWorld(sizeX-1, 0, img, img.tr);
			imageToWorld(0, sizeY-1, img, img.bl);
			imageToWorld(sizeX-1, sizeY-1, img, img.br);

			dumpPoint(img.tl);
			dumpPoint(img.tr);
			dumpPoint(img.bl);
			dumpPoint(img.br);
			
			System.out.println(
					MathUtil.d4(MathUtil.rad2deg * (MathUtil.fixAnglePI(img.tl.x - img.br.x))) + "\t" +
					MathUtil.d4(MathUtil.rad2deg * (MathUtil.fixAnglePI(img.tl.y - img.br.y))));
			System.out.println();
			
			result.add(img);
		}
		
		return result;
	}
	
	ArrayList<Image> read() throws Exception {
		BufferedReader fin = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("ImageRot.txt")));
		String str;
		str = fin.readLine();
		str = fin.readLine();
		StringTokenizer st = new StringTokenizer(str);
		int sizeX = Integer.parseInt(st.nextToken());
		int sizeY = Integer.parseInt(st.nextToken());
		double cameraScale = 1.0 / Math.max(sizeX, sizeY);
		str = fin.readLine();

		ArrayList<Image> result = new ArrayList<Image>();
		
		while (fin.ready()) {
			str = fin.readLine();
			if (str.equals(""))
				break;
			st = new StringTokenizer(str);
			Image img = new Image();
			img.sizeX = sizeX;
			img.sizeY = sizeY;
			img.cameraOriginX = sizeX / 2.0;
			img.cameraOriginY = sizeY / 2.0;
			img.cameraScale = cameraScale;

			img.scaleZ = Double.parseDouble(st.nextToken());
			img.rx = Double.parseDouble(st.nextToken()) * MathUtil.deg2rad;
			img.ry = Double.parseDouble(st.nextToken()) * MathUtil.deg2rad;
			img.rz = Double.parseDouble(st.nextToken()) * MathUtil.deg2rad;
			img.camera2real = RotationXYZ.makeAngles(img.rx, img.ry, img.rz);
			
			img.tl = new Point2D.Double();
			img.tr = new Point2D.Double();
			img.bl = new Point2D.Double();
			img.br = new Point2D.Double();
			
			imageToWorld(0, 0, img, img.tl);
			imageToWorld(sizeX-1, 0, img, img.tr);
			imageToWorld(0, sizeY-1, img, img.bl);
			imageToWorld(sizeX-1, sizeY-1, img, img.br);
			
			result.add(img);
		}
		return result;
	}

	public static class ImageRotationTransformLearer {
		public double a, b, c;
		
		ArrayList<Image> images;
		
		public ImageRotationTransformLearer(ArrayList<Image> images) {
			this.images = images;
		}
		
		public void transformTwo(double rx, double ry, Point2D.Double dest) {
			rx = rx - a;
			double sb = Math.sin(rx);
			double cb = Math.cos(rx);
			
			double sg = Math.sin(b);
			double cg = Math.cos(b);

			double sy = Math.sin(ry);
			double cy = Math.cos(ry);
			
			double e = Math.asin(sy*cg - cy*sg*sb);
			double af = Math.atan2(sy*sg + cy*cg*sb, cy*cb);
			dest.x = af;
			dest.y = e;
		}
		
		public boolean calculateTwo() {
			LeastSquaresAdjust lsa = new LeastSquaresAdjust(2, 1);
			
			double sg = Math.sin(b);
			double cg = Math.cos(b);
			
			Matrix coefs = new Matrix(2, 1);
			for (Image image : images) {
				for (int point = 0; point < 4; point++) {
					Point2D.Double src = null;
					switch (point) { 
					case 0: src = image.tl; break;
					case 1: src = image.tr; break;
					case 2: src = image.bl; break;
					case 3: src = image.br; break;
					}

					double sb = Math.sin(src.x - a);
					double cb = Math.cos(src.x - a);
					
					double sy = Math.sin(src.y);
					double cy = Math.cos(src.y);
					
					double dFdA = -cy*sg*cb;
					double dFdB = -sy*sg - cy*cg*sb;
					double F0 = sy*cg - cy*sg*sb;
					
					coefs.setItem(0, 0, dFdA);
					coefs.setItem(1, 0, dFdB);
					lsa.addMeasurement(coefs, 1.0, F0, 0);
				}
			}
			if (!lsa.calculate())
				return false;

			System.out.println(
					"A=" + MathUtil.d4(a*MathUtil.rad2deg) + 
					"\tB=" + MathUtil.d4(b*MathUtil.rad2deg));
			// Build transformer
			Matrix u = lsa.getUnknown(); 
			u.printM("U");
			u.rMul(-1);
			a = MathUtil.fixAngleMPI_PI(a + u.getItem(0, 0));
			b = MathUtil.fixAngleMPI_PI(b + u.getItem(0, 1));
//			a = a + u.getItem(0, 0);
//			b = b + u.getItem(0, 1);
			
			System.out.println(
					"A=" + MathUtil.d4(a*MathUtil.rad2deg) + 
					"\tB=" + MathUtil.d4(b*MathUtil.rad2deg));
			return true;
		}
	}	
	
	public static void worldToProj(double rx, double ry, SafeImage targetImg, Point2D.Double dest) {
		ry = MathUtil.fixAngleMPI_PI(ry);
		if (ry > MathUtil.PIover2) {
			ry = Math.PI - ry;
			rx += Math.PI;
		}
		if (ry < -MathUtil.PIover2) {
			ry = ry - Math.PI;
			rx += Math.PI;
		}
		rx = MathUtil.fixAngleMPI_PI(rx) + Math.PI;
		ry = ry + MathUtil.PIover2;
		
		int offset = 7;
		int sizeX = targetImg.sizeX - offset*2;
		int sizeY = targetImg.sizeY - offset*2;
		
		dest.x = offset + rx * sizeX / (MathUtil.C2PI);
		dest.y = offset + ry * sizeY / Math.PI;
	}
	
//	public void projToWorld(double sx, double sy, Point2D.Double dest) {
//		dest.x = (sx - outsize / 2.0) * (4.0 * Math.PI) / outsize;
//		dest.y = (sy - outsize / 2.0) * (4.0 * Math.PI) / outsize;
//	}	
	
	public void doIt() throws Exception {
		ArrayList<Image> data = read();
//		ArrayList<Image> data = generate();
		int outsizeX = 1500;
		int outsizeY = outsizeX / 2;
		ImageRotationTransformLearer learner = new ImageRotationTransformLearer(data);
		learner.a = 1 * MathUtil.deg2rad;
		learner.b = 179 * MathUtil.deg2rad;
		boolean res = learner.calculateTwo();
		res &= learner.calculateTwo();
		res &= learner.calculateTwo();
		res &= learner.calculateTwo();
		res &= learner.calculateTwo();
		res &= learner.calculateTwo();
		System.out.println("RESULT is " + res);
		
		SafeImage img = new SafeImage(outsizeX, outsizeY);
		Point2D.Double dest = new Point2D.Double();
		for (Image image : data) {
			int color = img.getNextColor();
			dest.x = image.tl.x;
			dest.y = image.tl.y;
			learner.transformTwo(dest.x, dest.y, dest);
			worldToProj(dest.x, dest.y, img, dest);
			int atX1 = (int) dest.x;
			int atY1 = (int) dest.y;
			img.drawCross(atX1, atY1, color);
			
			dest.x = image.tr.x;
			dest.y = image.tr.y;
			learner.transformTwo(dest.x, dest.y, dest);
			worldToProj(dest.x, dest.y, img, dest);
			int atX2 = (int) dest.x;
			int atY2 = (int) dest.y;
			img.drawCross(atX2, atY2, color);
			
			dest.x = image.bl.x;
			dest.y = image.bl.y;
			learner.transformTwo(dest.x, dest.y, dest);
			worldToProj(dest.x, dest.y, img, dest);
			int atX3 = (int) dest.x;
			int atY3 = (int) dest.y;
			img.drawCross(atX3, atY3, color);
			
			dest.x = image.br.x;
			dest.y = image.br.y;
			learner.transformTwo(dest.x, dest.y, dest);
			worldToProj(dest.x, dest.y, img, dest);
			int atX4 = (int) dest.x;
			int atY4 = (int) dest.y;
			img.drawCross(atX4, atY4, color);

			for (int i = 0; i < image.sizeX; i++) {
				imageToWorld(i, 0, image, dest);
				learner.transformTwo(dest.x, dest.y, dest);
				worldToProj(dest.x, dest.y, img, dest);
				img.setRGB((int) dest.x, (int) dest.y, color); 

				imageToWorld(i, image.sizeY-1, image, dest);
				learner.transformTwo(dest.x, dest.y, dest);
				worldToProj(dest.x, dest.y, img, dest);
				img.setRGB((int) dest.x, (int) dest.y, color); 
			}
				
			for (int j = 0; j < image.sizeY; j++) {
				imageToWorld(0, j, image, dest);
				learner.transformTwo(dest.x, dest.y, dest);
				worldToProj(dest.x, dest.y, img, dest);
				img.setRGB((int) dest.x, (int) dest.y, color); 

				imageToWorld(image.sizeX-1, j, image, dest);
				learner.transformTwo(dest.x, dest.y, dest);
				worldToProj(dest.x, dest.y, img, dest);
				img.setRGB((int) dest.x, (int) dest.y, color); 
			}
		}

		drawWorldMesh(img, learner);
		
		int crossColor = 0xffff00;
		img.drawCross(0, 0, crossColor);
		img.drawCross(0, outsizeY - 1, crossColor);
		img.drawCross(outsizeX - 1, 0, crossColor);
		img.drawCross(outsizeX - 1, outsizeY - 1, crossColor);
		img.save();
	}

	public static void drawWorldMesh(SafeImage img, ImageRotationTransformLearer learner) {
		int numDivisionsX = 24;
		int numDivisionsY = 8;
		int scaleX = img.sizeX * 3;
		int scaleY = img.sizeY * 3;

		Point2D.Double dest = new Point2D.Double();
		// draw meridians
		for (int i = numDivisionsX; i >= 0; i--) {
			double x = i * 2 * Math.PI / numDivisionsX;
			int colorX = x == 0 ? 0xff0000 : 0xffffff;
			for (int j = scaleY; j >= 0; j--) {
				double y = j * Math.PI / scaleY - MathUtil.PIover2;
				dest.x = x;
				dest.y = y;
				learner.transformTwo(dest.x, dest.y, dest);
				worldToProj(dest.x, dest.y, img, dest);
				img.setRGB((int) dest.x, (int) dest.y, colorX);
			}
		}
		// draw parallels
		for (int j = numDivisionsY; j >= 0; j--) {
			double y = j * Math.PI / numDivisionsY - MathUtil.PIover2;
			int colorY = y == 0 ? 0xff0000 : 0xffff00;
			for (int i = scaleX; i >= 0; i--) {
				double x = i * 2 * Math.PI / scaleX;
				dest.x = x;
				dest.y = y;
				learner.transformTwo(dest.x, dest.y, dest);
				worldToProj(dest.x, dest.y, img, dest);
				img.setRGB((int) dest.x, (int) dest.y, colorY);
			}
		}

		int colorDiag = 0x0000ff;
		for (int i = scaleX / 2 ; i >= 0; i--) {
			double x = i * 4 * Math.PI / scaleX;
			double y = i * Math.PI / scaleX;
			dest.x = x;
			dest.y = y;
			learner.transformTwo(dest.x, dest.y, dest);
			worldToProj(dest.x, dest.y, img, dest);
			img.setRGB((int) dest.x, (int) dest.y, colorDiag);
		}
	}
	
	public static void main(String[] args) throws Exception {
		new ImageRot().doIt();
		System.out.println("Done.");
	}
}
