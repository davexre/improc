package com.slavi.math.adjust;

import java.awt.geom.Point2D;
import java.util.List;

import com.slavi.math.matrix.Matrix;

public class TransformationFunctions {
	private static final double R_EPS = 1.0e-6;
	
	private static final int MAXITER = 100;
	
	public interface TransformationFunction {
		public void transform(Point2D.Double src, Point2D.Double dest);
	}

	public static void execute_stack(List<TransformationFunctions.TransformationFunction>stack, Point2D.Double src, Point2D.Double dest) {
		Point2D.Double tmp = new Point2D.Double(src.x, src.y);
		dest.x = 0;
		dest.y = 0;
		for (TransformationFunctions.TransformationFunction f : stack) {
			f.transform(tmp, dest);
			tmp.x = dest.x;
			tmp.y = dest.y;
		}
	}	
	
	/**
	 * Rotate equirectangular image
	 */
	public static class RotateErect implements TransformationFunction {
		double var0;	// double 180degree_turn(screenpoints)
		double var1;	// double turn(screenpoints);
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			src.x = dest.x + var1;

			while (src.x < -var0)
				src.x += 2.0 * var0;
			while (src.x > var0)
				src.x -= 2.0 * var0;
			src.y = dest.y;
		}
		
		public RotateErect(double var0, double var1) {
			this.var0 = var0;
			this.var1 = var1;
		}
	}

	/**
	 * Calculate inverse 4th order polynomial correction using Newton
	 * Don't use on large image (slow)!
	 */
	public static class InvRadial implements TransformationFunction {
		double c0;
		double c1;
		double c2;
		double c3;
		double c4;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double rd = (Math.sqrt(dest.x * dest.x + dest.y * dest.y )) / c4; // Normalized
			double rs = rd;

			double f = (((c3 * rs + c2) * rs + c1) * rs + c0) * rs;
			int iter = 0;
			while ((Math.abs(f - rd) > R_EPS) && (iter++ < MAXITER)) {
				rs = rs - (f - rd) / ((( 4 * c3 * rs + 3 * c2) * rs  + 2 * c1) * rs + c0);
				f 	= (((c3 * rs + c2) * rs + c1) * rs + c0) * rs;
			}

			double scale = rs / rd;
			src.x = dest.x * scale;
			src.y = dest.y * scale;
		}
		
		public InvRadial(double c0, double c1, double c2, double c3, double c4) {
			this.c0 = c0;
			this.c1 = c1;
			this.c2 = c2;
			this.c3 = c3;
			this.c4 = c4;
		}
	}
	
	public static class InvVertical implements TransformationFunction {
		double c0;
		double c1;
		double c2;
		double c3;
		double c4;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double rd = Math.abs(dest.y) / c4; // Normalized 
			double rs = rd;

			double f = (((c3 * rs + c2) * rs + c1) * rs + c0) * rs;
			int iter = 0;
			while ((Math.abs(f - rd) > R_EPS) && (iter++ < MAXITER)) {
				rs = rs - (f - rd) / ((( 4 * c3 * rs + 3 * c2) * rs  + 2 * c1) * rs + c0);
				f = (((c3 * rs + c2) * rs + c1) * rs + c0) * rs;
			}

			double scale = rs / rd;
			src.x = dest.x;
			src.y = dest.y * scale;
		}
		
		public InvVertical(double c0, double c1, double c2, double c3, double c4) {
			this.c0 = c0;
			this.c1 = c1;
			this.c2 = c2;
			this.c3 = c3;
			this.c4 = c4;
		}
	}

	public static class Resize implements TransformationFunction {
		double scaleHorizontal;
		double scaleVertical;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			src.x = dest.x * scaleHorizontal;
			src.y = dest.y * scaleVertical;
		}
		
		public Resize(double scaleHorizontal, double scaleVertical) {
			this.scaleHorizontal = scaleHorizontal;
			this.scaleVertical = scaleVertical;
		}
	}

	public static class Shear implements TransformationFunction {
		double shearHorizontal;
		double shearVertical;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			src.x = dest.x + dest.y * shearHorizontal;
			src.y = dest.y + dest.x * shearVertical;
		}
		
		public Shear(double shearHorizontal, double shearVertical) {
			this.shearHorizontal = shearHorizontal;
			this.shearVertical = shearVertical;
		}
	}

	public static class Horiz implements TransformationFunction {
		double shiftHorizontal;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			src.x = dest.x + shiftHorizontal;
			src.y = dest.y;
		}
		
		public Horiz(double shiftHorizontal) {
			this.shiftHorizontal = shiftHorizontal;
		}
	}

	public static class Vert implements TransformationFunction {
		double shiftVertical;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			src.x = dest.x;
			src.y = dest.y + shiftVertical;
		}
		
		public Vert(double shiftVertical) {
			this.shiftVertical = shiftVertical;
		}
	}

	public static class Radial implements TransformationFunction {
		double c0;
		double c1;
		double c2;
		double c3;
		double c4;
		double c5;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double r = Math.sqrt(dest.x * dest.x + dest.y * dest.y) / c4;
			double scale;
			if (r < c5)
				scale = ((c3 * r + c2) * r + c1) * r + c0;
			else
				scale = 1000.0;
			src.x = dest.x * scale;
			src.y = dest.y * scale;
		}
		
		public Radial(double c0, double c1, double c2, double c3, double c4, double c5) {
			this.c0 = c0;
			this.c1 = c1;
			this.c2 = c2;
			this.c3 = c3;
			this.c4 = c4;
			this.c5 = c5;
		}
	}

	public static class Vertical implements TransformationFunction {
		double c0;
		double c1;
		double c2;
		double c3;
		double c4;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double r = Math.abs(dest.y / c4);
			double scale = ((c3 * r + c2) * r + c1) * r + c0;
			src.x = dest.x;
			src.y = dest.y * scale;
		}
		
		public Vertical(double c0, double c1, double c2, double c3, double c4) {
			this.c0 = c0;
			this.c1 = c1;
			this.c2 = c2;
			this.c3 = c3;
			this.c4 = c4;
		}
	}

	public static class Deregister implements TransformationFunction {
		double c0; // TODO: Obsolete
		double c1;
		double c2;
		double c3;
		double c4;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double r = Math.abs(dest.y / c4);
			double scale = (c3 * r + c2) * r + c1;
			src.x = dest.x + Math.abs(dest.y) * scale;
			src.y = dest.y;
		}
		
		public Deregister(double c0, double c1, double c2, double c3, double c4) {
			this.c0 = c0;
			this.c1 = c1;
			this.c2 = c2;
			this.c3 = c3;
			this.c4 = c4;
		}
	}

	public static class PerspSphere implements TransformationFunction {
		Matrix m;
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double r = Math.sqrt(dest.x * dest.x + dest.y * dest.y);
			double theta = r / distance;
			double s;
			if( r == 0.0 )
				s = 0.0;
			else
				s = Math.sin(theta) / r;

			double v0 = s * dest.x;
			double v1 = s * dest.y;
			double v2 = Math.cos(theta);

			double vv0 = m.getItem(0, 0) * v0 + m.getItem(0, 1) * v1 + m.getItem(0, 2) * v2;
			double vv1 = m.getItem(1, 0) * v0 + m.getItem(1, 1) * v1 + m.getItem(1, 2) * v2;
			double vv2 = m.getItem(2, 0) * v0 + m.getItem(2, 1) * v1 + m.getItem(2, 2) * v2;

			r = Math.sqrt(vv0 * vv0 + vv1 * vv1);
			if (r == 0.0)
				theta = 0.0;
			else
				theta = distance * Math.atan2(r, vv2) / r;
			src.x = theta * vv0;
			src.y = theta * vv1;
		}
		
		public PerspSphere(Matrix m, double distance) {
			this.m = m;
			this.distance = distance;
		}
	}
	
	public static class PerspRect implements TransformationFunction {
		Matrix m;
		double distance;
		double offsetX;
		double offsetY;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double v0 = dest.x + offsetX;
			double v1 = dest.y + offsetY;
			double v2 = distance;

			double vv0 = m.getItem(0, 0) * v0 + m.getItem(0, 1) * v1 + m.getItem(0, 2) * v2;
			double vv1 = m.getItem(1, 0) * v0 + m.getItem(1, 1) * v1 + m.getItem(1, 2) * v2;
			double vv2 = m.getItem(2, 0) * v0 + m.getItem(2, 1) * v1 + m.getItem(2, 2) * v2;

			src.x = vv0 * distance / vv2;
			src.y = vv1 * distance / vv2;
		}
		
		public PerspRect(Matrix m, double distance, double offsetX, double offsetY) {
			this.m = m;
			this.distance = distance;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}
	}
	
	public static class RectPano implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			src.x = distance * Math.tan(dest.x / distance);
			src.y = dest.y / Math.cos(dest.x / distance);
		}
		
		public RectPano(double distance) {
			this.distance = distance;
		}
	}

	public static class PanoRect implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			src.x = distance * Math.atan(dest.x / distance);
			src.y = dest.y / Math.cos(src.x / distance);
		}
		
		public PanoRect(double distance) {
			this.distance = distance;
		}
	}

	public static class RectErect implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double phi = dest.x / distance;
			double theta = -dest.y / distance + Math.PI / 2.0;
			if (theta < 0) {
				theta = - theta;
				phi += Math.PI;
			}
			if (theta > Math.PI) {
				theta = Math.PI - (theta - Math.PI);
				phi += Math.PI;
			}
			src.x = distance * Math.tan(phi);
			src.y = distance / (Math.tan(theta) * Math.cos(phi));
		}
		
		public RectErect(double distance) {
			this.distance = distance;
		}
	}

	public static class PanoErect implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			src.x = dest.x;
			src.y = distance * Math.tan(dest.y / distance);
		}
		
		public PanoErect(double distance) {
			this.distance = distance;
		}
	}
	
	public static class ErectPano implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			src.x = dest.x;
			src.y = distance * Math.atan(dest.y / distance);
		}
		
		public ErectPano(double distance) {
			this.distance = distance;
		}
	}

	public static class SphereCPErect implements TransformationFunction {
		double distance;
		double b;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double phi = -dest.x / (distance * Math.PI / 2.0);
			double theta =  -(dest.y + b) / (Math.PI / 2.0) ;
			src.x = theta * Math.cos(phi);
			src.y = theta * Math.sin(phi);
		}
		
		public SphereCPErect(double distance, double b) {
			this.distance = distance;
			this.b = b;
		}
	}

	public static class SphereTPErect implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double phi = dest.x / distance;
			double theta = -dest.y / distance + Math.PI / 2;
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
			
			src.x = theta * v0 / r;
			src.y = theta * v1 / r;
		}
		
		public SphereTPErect(double distance) {
			this.distance = distance;
		}
	}

	public static class ErectSphereCP implements TransformationFunction {
		double distance;
		double b;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double theta = Math.sqrt(dest.x * dest.x + dest.y * dest.y) ;
			double phi = Math.atan2(dest.y, -dest.x);
			
			src.x = distance * phi;
			src.y = theta - b;
		}
		
		public ErectSphereCP(double distance, double b) {
			this.distance = distance;
			this.b = b;
		}
	}

	public static class RectSphereTP implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double r = Math.sqrt(dest.x * dest.x + dest.y * dest.y);
			double theta = r / distance;
			
			if (theta > Math.PI /2.0)
				theta = Math.PI /2.0;

			double rho;
			if (theta == 0.0)
				rho = 1.0;
			else
				rho = Math.tan(theta) / theta;
			src.x = rho * dest.x ;
			src.y = rho * dest.y ;
		}
		
		public RectSphereTP(double distance) {
			this.distance = distance;
		}
	}
	
	public static class SphereTPRect implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double r = Math.sqrt(dest.x * dest.x + dest.y * dest.y) / distance;
			double theta;
			if( r== 0.0 )
				theta = 1.0;
			else
				theta = Math.atan(r) / r;
			
			src.x = theta * dest.x;
			src.y = theta * dest.y;
		}
		
		public SphereTPRect(double distance) {
			this.distance = distance;
		}
	}
	
	public static class SphereTPPano implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double phi = dest.x / distance;
			double s = distance * Math.sin(phi);	//  y' -> x
			double r = Math.sqrt(s*s + dest.y * dest.y);
			double theta = distance * Math.atan2(r, distance * Math.cos(phi)) / r;
			src.x = theta * s ;
			src.y = theta * dest.y;
		}
		
		public SphereTPPano(double distance) {
			this.distance = distance;
		}
	}
	
	public static class PanoSphereTP implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double r = Math.sqrt(dest.x * dest.x + dest.y * dest.y);
			double theta = r / distance;
			double s;
			if (theta == 0.0)
				s = 1.0 / distance;
			else
				s = Math.sin(theta) / r;
			double v1 = s * dest.x;			// x' -> y
			double v0 = Math.cos(theta);	// z' -> x

			src.x = distance * Math.atan2(v1, v0);
			src.y = distance * s * dest.y / Math.sqrt(v0 * v0 + v1 * v1);
		}
		
		public PanoSphereTP(double distance) {
			this.distance = distance;
		}
	}
	
	public static class SphereCPPano implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double phi = -dest.x / (distance * Math.PI / 2.0) ;
			double theta = Math.PI /2.0 + Math.atan(dest.y / (distance * Math.PI / 2.0));
			src.x = distance * theta * Math.cos(phi);
			src.y = distance * theta * Math.sin(phi);
		}
		
		public SphereCPPano(double distance) {
			this.distance = distance;
		}
	}
	
	public static class ErectRect implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			src.x = distance * Math.atan2(dest.x, distance);
			src.y = distance * Math.atan2(dest.y, Math.sqrt(distance * distance + dest.x * dest.x));
		}
		
		public ErectRect(double distance) {
			this.distance = distance;
		}
	}
	
	public static class ErectSphereTP implements TransformationFunction {
		double distance;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double r = Math.sqrt(dest.x * dest.x + dest.y * dest.y);
			double theta = r / distance;
			double s;
			if(theta == 0.0)
				s = 1.0 / distance;
			else
				s = Math.sin(theta) / r;
			double v1 = s * dest.x;   
			double v0 = Math.cos(theta);				
			src.x = distance * Math.atan2(v1, v0);
			src.y = distance * Math.atan(s * dest.y / Math.sqrt(v0 * v0 + v1 * v1)); 
		}
		
		public ErectSphereTP(double distance) {
			this.distance = distance;
		}
	}
	
	public static class MirrorSphereCP implements TransformationFunction {
		double distance;
		double b;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double theta = Math.sqrt(dest.x * dest.x + dest.y * dest.y ) / distance;
			double phi = Math.atan2(dest.y , dest.x);
			double rho = b * Math.sin( theta / 2.0 );
			src.x = -rho * Math.cos(phi);
			src.y = rho * Math.sin(phi);
		}
		
		public MirrorSphereCP(double distance, double b) {
			this.distance = distance;
			this.b = b;
		}
	}

	public static class MirrorErect implements TransformationFunction {
		double distance;
		double b;
		double b2;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double phi = dest.x / (distance * Math.PI / 2.0);
			double theta = -(dest.y + b2) / (distance * Math.PI / 2.0);
			double rho = b * Math.sin(theta / 2.0);
			src.x = -rho * Math.cos(phi);
			src.y = rho * Math.sin(phi);
		}
		
		public MirrorErect(double distance, double b, double b2) {
			this.distance = distance;
			this.b = b;
			this.b2 = b2;
		}
	}
	
	public static class MirrorPano implements TransformationFunction {
		double distance;
		double b;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double phi = -dest.x / (distance * Math.PI / 2.0) ;
			double theta = Math.PI / 2.0 + Math.atan(dest.y / (distance * Math.PI / 2.0));
			double rho = b * Math.sin(theta / 2.0);
			src.x = rho * Math.cos(phi);
			src.y = rho * Math.sin(phi);
		}
		
		public MirrorPano(double distance, double b) {
			this.distance = distance;
			this.b = b;
		}
	}

	public static class SphereCPMirror implements TransformationFunction {
		double distance;
		double b;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double rho = Math.sqrt(dest.x * dest.x + dest.y * dest.y);
			double theta = 2.0 * Math.asin(rho / b);
			double phi = Math.atan2(dest.y, dest.x);
			src.x = distance * theta * Math.cos(phi);
			src.y = distance * theta * Math.sin(phi);
		}
		
		public SphereCPMirror(double distance, double b) {
			this.distance = distance;
			this.b = b;
		}
	}

	public static class ShiftScaleRotate implements TransformationFunction {
		double shiftX;
		double shiftY;
		double scale;
		double cosPhi;
		double sinPhi;
		
		public void transform(Point2D.Double src, Point2D.Double dest) {
			double x = dest.x - shiftX;
			double y = dest.y - shiftY;
			src.x = (x * cosPhi - y * sinPhi) * scale;
			src.y = (x * sinPhi + y * cosPhi) * scale;
		}
		
		public ShiftScaleRotate(double shiftX, double shiftY, double scale, double cosPhi, double sinPhi) {
			this.shiftX = shiftX;
			this.shiftY = shiftY;
			this.scale = scale;
			this.cosPhi = cosPhi;
			this.sinPhi = sinPhi;
		}
	}
}
