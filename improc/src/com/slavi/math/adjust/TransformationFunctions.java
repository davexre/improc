package com.slavi.math.adjust;

import java.awt.geom.Point2D;

import com.slavi.math.matrix.Matrix;

public class TransformationFunctions {
	private static final double R_EPS = 1.0e-6;
	
	private static final int MAXITER = 100;
	
	/**
	 * Rotate equirectangular image
	 * @param rotX	180 degree turn(screen points)
	 * @param rotY	turn(screen points);
	 */
	public static void rotateErect(Point2D.Double p, double rotX, double rotY) {
		p.x = p.x + rotY;

		while (p.x < -rotX)
			p.x += 2.0 * rotX;
		while (p.x > rotX)
			p.x -= 2.0 * rotX;
		// src.y = dest.y;
	}
	
	/**
	 * Calculate inverse 4th order polynomial correction using Newton
	 * Don't use on large image (slow)!
	 */
	public static void invRadial(Point2D.Double p, double c0, double c1, 
			double c2, double c3, double c4) {
		double rd = (Math.sqrt(p.x * p.x + p.y * p.y )) / c4; // Normalized
		double rs = rd;

		double f = (((c3 * rs + c2) * rs + c1) * rs + c0) * rs;
		int iter = 0;
		while ((Math.abs(f - rd) > R_EPS) && (iter++ < MAXITER)) {
			rs = rs - (f - rd) / ((( 4 * c3 * rs + 3 * c2) * rs  + 2 * c1) * rs + c0);
			f 	= (((c3 * rs + c2) * rs + c1) * rs + c0) * rs;
		}

		double scale = rs / rd;
		p.x *= scale;
		p.y *= scale;
	}

	public static void invVertical(Point2D.Double p, double c0, double c1, 
			double c2, double c3, double c4) {
		double rd = Math.abs(p.y) / c4; // Normalized 
		double rs = rd;

		double f = (((c3 * rs + c2) * rs + c1) * rs + c0) * rs;
		int iter = 0;
		while ((Math.abs(f - rd) > R_EPS) && (iter++ < MAXITER)) {
			rs = rs - (f - rd) / ((( 4 * c3 * rs + 3 * c2) * rs  + 2 * c1) * rs + c0);
			f = (((c3 * rs + c2) * rs + c1) * rs + c0) * rs;
		}

		double scale = rs / rd;
		//src.x = dest.x;
		p.y *= scale;
	}
	
	public static void resize(Point2D.Double p, double scaleHorizontal, double scaleVertical) {
		p.x *= scaleHorizontal;
		p.y *= scaleVertical;
	}
	
	public static void shear(Point2D.Double p, double shearHorizontal, double shearVertical) {
		double x = p.x;
		p.x = x + p.y * shearHorizontal;
		p.y = p.y + x * shearVertical;
	}
	
	public static void horiz(Point2D.Double p, double shiftHorizontal) {
		p.x += shiftHorizontal;
		// src.y = dest.y;
	}
	
	public static void vert(Point2D.Double p, double shiftVertical) {
		// src.x = dest.x;
		p.y += shiftVertical;
	}
	
	public static void radial(Point2D.Double p, double c0, double c1, 
			double c2, double c3, double c4, double c5) {
		double r = Math.sqrt(p.x * p.x + p.y * p.y) / c4;
		double scale;
		if (r < c5)
			scale = ((c3 * r + c2) * r + c1) * r + c0;
		else
			scale = 1000.0;
		p.x *= scale;
		p.y *= scale;
	}
	
	public static void vertical(Point2D.Double p, double c0, double c1, 
			double c2, double c3, double c4) {
		// src.x = dest.x;
		double r = Math.abs(p.y / c4);
		p.y *= ((c3 * r + c2) * r + c1) * r + c0;
	}
	
	public static void deregister(Point2D.Double p, /*double c0,*/ double c1, 
			double c2, double c3, double c4) {
		double r = Math.abs(p.y / c4);
		double scale = (c3 * r + c2) * r + c1;
		p.x += Math.abs(p.y) * scale;
		// src.y = dest.y;
	}
	
	public static void perspSphere(Point2D.Double p, Matrix m, double distance) {
		double r = Math.sqrt(p.x * p.x + p.y * p.y);
		double theta = r / distance;
		double s;
		if( r == 0.0 )
			s = 0.0;
		else
			s = Math.sin(theta) / r;

		double v0 = s * p.x;
		double v1 = s * p.y;
		double v2 = Math.cos(theta);

		double vv0 = m.getItem(0, 0) * v0 + m.getItem(0, 1) * v1 + m.getItem(0, 2) * v2;
		double vv1 = m.getItem(1, 0) * v0 + m.getItem(1, 1) * v1 + m.getItem(1, 2) * v2;
		double vv2 = m.getItem(2, 0) * v0 + m.getItem(2, 1) * v1 + m.getItem(2, 2) * v2;

		r = Math.sqrt(vv0 * vv0 + vv1 * vv1);
		if (r == 0.0)
			theta = 0.0;
		else
			theta = distance * Math.atan2(r, vv2) / r;
		p.x = theta * vv0;
		p.y = theta * vv1;
	}
	
	public static void perspRect(Point2D.Double p, Matrix m, double distance, double offsetX, double offsetY) {
		double v0 = p.x + offsetX;
		double v1 = p.y + offsetY;
		double v2 = distance;

		double vv0 = m.getItem(0, 0) * v0 + m.getItem(0, 1) * v1 + m.getItem(0, 2) * v2;
		double vv1 = m.getItem(1, 0) * v0 + m.getItem(1, 1) * v1 + m.getItem(1, 2) * v2;
		double vv2 = m.getItem(2, 0) * v0 + m.getItem(2, 1) * v1 + m.getItem(2, 2) * v2;

		p.x = vv0 * distance / vv2;
		p.y = vv1 * distance / vv2;
	}
	
	public static void rectPano(Point2D.Double p, double distance) {
		p.y = p.y / Math.cos(p.x / distance);
		p.x = distance * Math.tan(p.x / distance);
	}
	
	public static void panoRect(Point2D.Double p, double distance) {
		p.y = p.y / Math.cos(p.x / distance);
		p.x = distance * Math.atan(p.x / distance);
	}
	
	public static void rectErect(Point2D.Double p, double distance) {
		double phi = p.x / distance;
		double theta = -p.y / distance + Math.PI / 2.0;
		if (theta < 0) {
			theta = - theta;
			phi += Math.PI;
		}
		if (theta > Math.PI) {
			theta = Math.PI - (theta - Math.PI);
			phi += Math.PI;
		}
		p.x = distance * Math.tan(phi);
		p.y = distance / (Math.tan(theta) * Math.cos(phi));
	}
	
	public static void panoErect(Point2D.Double p, double distance) {
		// src.x = dest.x;
		p.y = distance * Math.tan(p.y / distance);
	}
	
	public static void erectPano(Point2D.Double p, double distance) {
		// src.x = dest.x;
		p.y = distance * Math.atan(p.y / distance);
	}
	
	public static void sphereCPErect(Point2D.Double p, double distance, double b) {
		double phi = -p.x / (distance * Math.PI / 2.0);
		double theta =  -(p.y + b) / (Math.PI / 2.0) ;
		p.x = theta * Math.cos(phi);
		p.y = theta * Math.sin(phi);
	}
	
	public static void sphereTPErect(Point2D.Double p, double distance) {
		double phi = p.x / distance;
		double theta = -p.y / distance + Math.PI / 2;
		if (theta < 0) {
			theta = -theta;
			phi += Math.PI;
		}
		if (theta > Math.PI) {
			theta = Math.PI - (theta - Math.PI);
			phi += Math.PI;
		}
		double s = Math.sin(theta);
		double v0 = s * Math.sin(phi);	//  y' -> x
		double v1 = Math.cos(theta);	//  z' -> y
		
		double r = Math.sqrt(v1 * v1 + v0 * v0);	

		theta = distance * Math.atan2(r, s * Math.cos(phi));
		
		p.x = theta * v0 / r;
		p.y = theta * v1 / r;
	}
	
	public static void erectSphereCP(Point2D.Double p, double distance, double b) {
		double theta = Math.sqrt(p.x * p.x + p.y * p.y) ;
		double phi = Math.atan2(p.y, -p.x);
		p.x = distance * phi;
		p.y = theta - b;
	}
	
	public static void rectSphereTP(Point2D.Double p, double distance) {
		double r = Math.sqrt(p.x * p.x + p.y * p.y);
		double theta = r / distance;
		
		if (theta > Math.PI /2.0)
			theta = Math.PI /2.0;

		double rho;
		if (theta == 0.0)
			rho = 1.0;
		else
			rho = Math.tan(theta) / theta;
		p.x *= rho;
		p.y *= rho;
	}
	
	public static void sphereTPRect(Point2D.Double p, double distance) {
		double r = Math.sqrt(p.x * p.x + p.y * p.y) / distance;
		double theta;
		if( r== 0.0 )
			theta = 1.0;
		else
			theta = Math.atan(r) / r;
		p.x *= theta;
		p.y *= theta;
	}
	
	public static void sphereTPPano(Point2D.Double p, double distance) {
		double phi = p.x / distance;
		double s = distance * Math.sin(phi);	//  y' -> x
		double r = Math.sqrt(s*s + p.y * p.y);
		double theta = distance * Math.atan2(r, distance * Math.cos(phi)) / r;
		p.x = theta * s ;
		p.y *= theta;
	}
	
	public static void panoSphereTP(Point2D.Double p, double distance) {
		double r = Math.sqrt(p.x * p.x + p.y * p.y);
		double theta = r / distance;
		double s;
		if (theta == 0.0)
			s = 1.0 / distance;
		else
			s = Math.sin(theta) / r;
		double v1 = s * p.x;			// x' -> y
		double v0 = Math.cos(theta);	// z' -> x

		p.x = distance * Math.atan2(v1, v0);
		p.y = distance * s * p.y / Math.sqrt(v0 * v0 + v1 * v1);
	}
	
	public static void sphereCPPano(Point2D.Double p, double distance) {
		double phi = -p.x / (distance * Math.PI / 2.0) ;
		double theta = Math.PI /2.0 + Math.atan(p.y / (distance * Math.PI / 2.0));
		p.x = distance * theta * Math.cos(phi);
		p.y = distance * theta * Math.sin(phi);
	}
	
	public static void erectRect(Point2D.Double p, double distance) {
		p.y = distance * Math.atan2(p.y, Math.sqrt(distance * distance + p.x * p.x));
		p.x = distance * Math.atan2(p.x, distance);
	}
	
	public static void erectSphereTP(Point2D.Double p, double distance) {
		double r = Math.sqrt(p.x * p.x + p.y * p.y);
		double theta = r / distance;
		double s;
		if(theta == 0.0)
			s = 1.0 / distance;
		else
			s = Math.sin(theta) / r;
		double v1 = s * p.x;   
		double v0 = Math.cos(theta);				
		p.x = distance * Math.atan2(v1, v0);
		p.y = distance * Math.atan(s * p.y / Math.sqrt(v0 * v0 + v1 * v1)); 
	}
	
	public static void mirrorSphereCP(Point2D.Double p, double distance, double b) {
		double theta = Math.sqrt(p.x * p.x + p.y * p.y ) / distance;
		double phi = Math.atan2(p.y , p.x);
		double rho = b * Math.sin( theta / 2.0 );
		p.x = -rho * Math.cos(phi);
		p.y = rho * Math.sin(phi);
	}
	
	public static void mirrorErect(Point2D.Double p, double distance, double b, double b2) {
		double phi = p.x / (distance * Math.PI / 2.0);
		double theta = -(p.y + b2) / (distance * Math.PI / 2.0);
		double rho = b * Math.sin(theta / 2.0);
		p.x = -rho * Math.cos(phi);
		p.y = rho * Math.sin(phi);
	}
	
	public static void mirrorPano(Point2D.Double p, double distance, double b) {
		double phi = -p.x / (distance * Math.PI / 2.0) ;
		double theta = Math.PI / 2.0 + Math.atan(p.y / (distance * Math.PI / 2.0));
		double rho = b * Math.sin(theta / 2.0);
		p.x = rho * Math.cos(phi);
		p.y = rho * Math.sin(phi);
	}
	
	public static void sphereCPMirror(Point2D.Double p, double distance, double b) {
		double rho = Math.sqrt(p.x * p.x + p.y * p.y);
		double theta = 2.0 * Math.asin(rho / b);
		double phi = Math.atan2(p.y, p.x);
		p.x = distance * theta * Math.cos(phi);
		p.y = distance * theta * Math.sin(phi);
	}
	
	public static void shiftScaleRotate(Point2D.Double p, double shiftX, double shiftY, 
			double scale, double cosPhi, double sinPhi) {
		double x = p.x - shiftX;
		double y = p.y - shiftY;
		p.x = (x * cosPhi - y * sinPhi) * scale;
		p.y = (x * sinPhi + y * cosPhi) * scale;
	}
}
