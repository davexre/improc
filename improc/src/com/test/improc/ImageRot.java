package com.test.improc;

import java.awt.Graphics2D;
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
import com.slavi.math.transform.BaseTransformer;

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
		
		x = Math.atan2(x, z);
		y = Math.atan2(y, z);
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

	public static class ImageRotationTransformer extends BaseTransformer<Point2D.Double, Point2D.Double> {

		public double a, b, c;
		
		public int getInputSize() {
			return 2;
		}

		public int getNumberOfCoefsPerCoordinate() {
			return 3;
		}

		public int getOutputSize() {
			return 2;
		}

		private double getCoord(Point2D.Double item, int coordIndex) {
			switch (coordIndex) {
				case 0: return item.x;
				case 1: return item.y;
				default: throw new RuntimeException("Invalid coordinate");
			}
		}

		private void setCoord(Point2D.Double item, int coordIndex, double value) {
			switch (coordIndex) {
				case 0: 
					item.x = value;
					break;				
				case 1: 
					item.y = value;
					break;
				default: throw new RuntimeException("Invalid coordinate");
			}
		}

		public double getSourceCoord(Point2D.Double item, int coordIndex) {
			return getCoord(item, coordIndex);
		}

		public double getTargetCoord(Point2D.Double item, int coordIndex) {
			return getCoord(item, coordIndex);
		}

		public void setSourceCoord(Point2D.Double item, int coordIndex, double value) {
			setCoord(item, coordIndex, value);
		}

		public void setTargetCoord(Point2D.Double item, int coordIndex, double value) {
			setCoord(item, coordIndex, value);
		}

		public void transform(java.awt.geom.Point2D.Double source, java.awt.geom.Point2D.Double dest) {
			
		}
	}
	
	public static class ImageRotationTransformLearer {
		
		ImageRotationTransformer tr;
		
		ArrayList<Image> images;
		
		public ImageRotationTransformLearer(ArrayList<Image> images) {
			this.images = images;
			tr = new ImageRotationTransformer();
		}

		
		public void transformTwo(double sourceX, double sourceY, Point2D.Double dest) {
			double sb = Math.sin(tr.b);
			double cb = Math.cos(tr.b);

			double sa = Math.sin(sourceX - tr.a);
			double ca = Math.cos(sourceX - tr.a);
			
			double sy = Math.sin(sourceY);
			double cy = Math.cos(sourceY);
			
			double e = Math.asin(sy*cb - cy*sb*sa);
			double ce = Math.cos(e);
			double af = ce == 0.0 ? 0 : Math.asin(cy*ca/ce);
			dest.x = sourceX - tr.a > 0 ? af : -af;
			dest.y = e;
		}
		
		public boolean calculateTwo() {
			LeastSquaresAdjust lsa = new LeastSquaresAdjust(2, 1);
			
			double sb = Math.sin(tr.b);
			double cb = Math.cos(tr.b);
			
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

					double sa = Math.sin(src.x - tr.a);
					double ca = Math.cos(src.x - tr.a);
					
					double sy = Math.sin(src.y);
					double cy = Math.cos(src.y);
					
					double dFdA = -sy*sb - cy*cb*sa;
					double dFdB = cy*sb*ca;
					double F0 = sy*cb - cy*sb*sa;
					
					coefs.setItem(0, 0, dFdA);
					coefs.setItem(1, 0, dFdB);
					lsa.addMeasurement(coefs, 1.0, F0, 0);
				}
			}
			if (!lsa.calculate())
				return false;

			// Build transformer
			Matrix u = lsa.getUnknown(); 
			u.printM("U");
			tr.a = MathUtil.fixAngleMPI_PI(tr.a + u.getItem(0, 0));
			tr.b = MathUtil.fixAngleMPI_PI(tr.b + u.getItem(0, 1));
			
			System.out.println(
					"A=" + MathUtil.d4(tr.a*MathUtil.rad2deg) + 
					"\tB=" + MathUtil.d4(tr.b*MathUtil.rad2deg));
			return true;
		}
		
		public boolean calculateOne() {
			LeastSquaresAdjust lsa = new LeastSquaresAdjust(2, 1);

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
					
					coefs.setItem(0, 0, src.x);
					coefs.setItem(1, 0, 1.0);
					double L = src.y;
					lsa.addMeasurement(coefs, 1.0, L, 0);
				}
			}
			if (!lsa.calculate())
				return false;

			// Build transformer
			Matrix u = lsa.getUnknown(); 
			u.printM("U");
			tr.a = u.getItem(0, 0);
			tr.c = u.getItem(0, 1);
			
			System.out.println("A=" + MathUtil.d4(tr.a) + "\tB=" + MathUtil.d4(tr.b) + "\tC=" + MathUtil.d4(tr.c));
			return true;
		}
	}	
	
	ArrayList<Image> data;
	int outsize = 1000;
	
	public void worldToProj(double rx, double ry, Point2D.Double dest) {
		ry = MathUtil.fixAngleMPI_PI(ry);
		if (ry > MathUtil.PIover2) {
			ry = Math.PI - ry;
			rx += Math.PI;
		}
		if (ry < -MathUtil.PIover2) {
			ry = ry - Math.PI;
			rx += Math.PI;
		}
		rx = MathUtil.fixAngleMPI_PI(rx);
		
		dest.x = rx * outsize / (2.0 * Math.PI) + outsize / 2.0;
		dest.y = ry * outsize / (4.0 * Math.PI) + outsize / 4.0;
	}
	
	public void projToWorld(double sx, double sy, Point2D.Double dest) {
//		dest.x = (sx - outsize / 2.0) * (4.0 * Math.PI) / outsize;
//		dest.y = (sy - outsize / 2.0) * (4.0 * Math.PI) / outsize;
	}	
	
	public void doIt() throws Exception {
		data = read();
		ImageRotationTransformLearer learner = new ImageRotationTransformLearer(data);
		learner.tr.a = 0 * MathUtil.deg2rad;
		learner.tr.b = 90 * MathUtil.deg2rad;
//		boolean res = learner.calculateTwo();
//		System.out.println("RESULT is " + res);
		
		SafeImage img = new SafeImage(outsize, outsize / 2);
		Point2D.Double dest = new Point2D.Double();
		for (Image image : data) {
			int color = img.getNextColor();
			learner.transformTwo(image.tl.x, image.tl.y, dest);
			worldToProj(dest.x, dest.y, dest);
			int atX1 = (int) dest.x;
			int atY1 = (int) dest.y;
			img.drawCross(atX1, atY1, color);
			
			learner.transformTwo(image.tr.x, image.tr.y, dest);
			worldToProj(dest.x, dest.y, dest);
			int atX2 = (int) dest.x;
			int atY2 = (int) dest.y;
			img.drawCross(atX2, atY2, color);
			
			learner.transformTwo(image.bl.x, image.bl.y, dest);
			worldToProj(dest.x, dest.y, dest);
			int atX3 = (int) dest.x;
			int atY3 = (int) dest.y;
			img.drawCross(atX3, atY3, color);
			
			learner.transformTwo(image.br.x, image.br.y, dest);
			worldToProj(dest.x, dest.y, dest);
			int atX4 = (int) dest.x;
			int atY4 = (int) dest.y;
			img.drawCross(atX4, atY4, color);

			for (int i = 0; i < image.sizeX; i++) {
				imageToWorld(i, 0, image, dest);
				learner.transformTwo(dest.x, dest.y, dest);
				worldToProj(dest.x, dest.y, dest);
				img.setRGB((int) dest.x, (int) dest.y, color); 

				imageToWorld(i, image.sizeY-1, image, dest);
				learner.transformTwo(dest.x, dest.y, dest);
				worldToProj(dest.x, dest.y, dest);
				img.setRGB((int) dest.x, (int) dest.y, color); 
			}
				
			for (int j = 0; j < image.sizeY; j++) {
				imageToWorld(0, j, image, dest);
				learner.transformTwo(dest.x, dest.y, dest);
				worldToProj(dest.x, dest.y, dest);
				img.setRGB((int) dest.x, (int) dest.y, color); 

				imageToWorld(image.sizeX-1, j, image, dest);
				learner.transformTwo(dest.x, dest.y, dest);
				worldToProj(dest.x, dest.y, dest);
				img.setRGB((int) dest.x, (int) dest.y, color); 
			}
			
//			gr.setColor(new Color(color));
//			gr.drawLine(atX1, atY1, atX2, atY2);
//			gr.drawLine(atX4, atY4, atX2, atY2);
//			gr.drawLine(atX1, atY1, atX3, atY3);
//			gr.drawLine(atX4, atY4, atX3, atY3);
		}
		
		for (int i = -1000; i < 1000; i++) {
			double x = i * Math.PI / 1000;
			learner.transformTwo(x, 0.0, dest);
			worldToProj(dest.x, dest.y, dest);
//			worldToProj(x, 0, dest);
			int atX = (int) dest.x;
			int atY = (int) dest.y;
			img.setRGB(atX, atY, 0xffffff);
		}
		img.save();
	}
	
	public static void main(String[] args) throws Exception {
		new ImageRot().doIt();
	}
}
