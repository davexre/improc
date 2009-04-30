package com.test.improc;

import java.awt.geom.Point2D;
import java.io.FileInputStream;

import com.slavi.improc.SafeImage;
import com.slavi.util.Const;

public class BarrelDistortion {

	public static void cartesianToPolar(double x, double y, Point2D.Double dest) {
		dest.x = Math.sqrt(x*x + y*y);	// radius
		dest.y = Math.atan2(y, x);		// angle
	}
	
	public static class BarrelDistroctionFilter {
		double c0;
		double c1;
		double c2;
		double c3;
		
		public BarrelDistroctionFilter(double c0, double c1, double c2, double c3) {
			this.c0 = c0;
			this.c1 = c1;
			this.c2 = c2;
			this.c3 = c3;
		}
		
		public BarrelDistroctionFilter(double c1, double c2, double c3) {
			this.c0 = 1 - (c1 + c2 + c3);
			this.c1 = c1;
			this.c2 = c2;
			this.c3 = c3;
		}
		
		public void apply(double radius, double angle, Point2D.Double dest) {
			double r2 = radius*radius;
			if (radius != 0.0)
				dest.x = c0 * radius + c1 * r2 + c2 * (radius * r2) + c3 * (r2 * r2);
			else
				dest.x = 0.0;
			dest.y = angle;
		}
	}
	
	public static void polarToCartesian(double radius, double angle, Point2D.Double dest) {
		dest.x = Math.cos(angle) * radius;
		dest.y = Math.sin(angle) * radius;
	}
	
	public static void main(String[] args) throws Exception {
		FileInputStream fin = new FileInputStream(Const.sourceImage);
		SafeImage img = new SafeImage(fin);
		fin.close();
		SafeImage oi = new SafeImage(img.sizeX, img.sizeY);
//		BarrelDistroctionFilter f = new BarrelDistroctionFilter(1.1, 0, 0);
		BarrelDistroctionFilter f = new BarrelDistroctionFilter(1.1, 0.00005, 0, 0);
		
		double centerX = img.sizeX / 2.0;
		double centerY = img.sizeY / 2.0;
		double scale = Math.max(centerX, centerY);
		
		double k1 = -11110.0000018502;
		
		Point2D.Double d = new Point2D.Double();
		for (int atX = 0; atX < oi.sizeX; atX++) {
			for (int atY = 0; atY < oi.sizeY; atY++) {
				d.x = atX - centerX;
				d.y = atY - centerY;
				
				d.x /= scale;
				d.y /= scale;
				
				cartesianToPolar(d.x, d.y, d);
				f.apply(d.x, d.y, d);
				polarToCartesian(d.x, d.y, d);
//				double r2 = d.x*d.x + d.y*d.y;
//				r2 *= k1;
//				r2++;
//				d.x *= r2; 
//				d.y *= r2; 

				d.x *= scale;
				d.y *= scale;
				
				d.x += centerX;
				d.y += centerY;
				int color = img.getRGB((int) d.x, (int) d.y);
				
				oi.setRGB(atX, atY, color);
			}
		}
		oi.save();
		System.out.println("Done.");
	}	
}
