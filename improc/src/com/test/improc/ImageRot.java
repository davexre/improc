package com.test.improc;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.matrix.SymmetricMatrix;
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
		
		dest.x = Math.atan2(x, z);
		dest.y = Math.atan2(y, z);
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
			st = new StringTokenizer(str);
			Image img = new Image();
			img.sizeX = sizeX;
			img.sizeY = sizeY;
			img.cameraOriginX = sizeX / 2.0;
			img.cameraOriginY = sizeY / 2.0;
			img.cameraScale = cameraScale;

			img.scaleZ = Double.parseDouble(st.nextToken());
			img.rx = Double.parseDouble(st.nextToken());
			img.ry = Double.parseDouble(st.nextToken());
			img.rz = Double.parseDouble(st.nextToken());
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
		
		ArrayList<Image> images;
		
		public ImageRotationTransformLearer(ArrayList<Image> images) {
			this.images = images;
		}
		
		public void calculateOne() {
			LeastSquaresAdjust lsa = new LeastSquaresAdjust(3, 1);

			Matrix coefs = new Matrix(3, 1);
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
					coefs.setItem(0, 0, src.y);
					coefs.setItem(0, 0, 1.0);
					lsa.addMeasurement(coefs, 1.0, 0.0, 0);
				}
			}
			SymmetricMatrix nm = lsa.getNm();
			if (!nm.inverse())
				return;
			
		}
	}	
	
	ArrayList<Image> data;
	
	public void doIt() throws Exception {
		data = read();
	}
	
	public static void main(String[] args) throws Exception {
		new ImageRot().doIt();
	}
}
