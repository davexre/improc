package com.slavi.improc.pano;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.slavi.improc.pano.AlignInfo.cPrefsCorrectionMode;
import com.slavi.improc.pano.AlignInfo.optVars;
import com.slavi.improc.pano.ControlPoint.OptimizeType;
import com.slavi.improc.pano.Image.ImageFormat;
import com.slavi.improc.pano.LMDif.LMDifFcn;
import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;

public class PanoAdjust implements LMDifFcn {
	static double NORM_ANGLE(double x) {
		while (x > 180.0) {
			x -= 360.0;
		}
		while (x < -180.0) {
			x += 360.0;
		}
		return x;
	}

	private static double C_FACTOR = 100.0;

	AlignInfo alignInfo = new AlignInfo();
	
	// Set Makeparameters depending on adjustprefs, color and source image
	static void makeParams(Point2D.Double p, Image srcImg, Image destImg, int colorIndex) {
		double a = srcImg.hfov * MathUtil.deg2rad;	// field of view in rad
		double b = destImg.hfov * MathUtil.deg2rad;
		Matrix mt = MathUtil.makeAngles(- srcImg.pitch * MathUtil.deg2rad, 0.0, - srcImg.roll * MathUtil.deg2rad, false);
		
		double scale;
		double distance;
		if (destImg.format == ImageFormat.Rectilinear) {
			// rectilinear panorama
			distance = (double) destImg.width / (2.0 * Math.tan(b / 2.0));
			if (srcImg.format == ImageFormat.Rectilinear) {
				// rectilinear image
				scale = ((double) destImg.hfov / srcImg.hfov) * 
						(a / (2.0 * Math.tan(a / 2.0))) * ((double)srcImg.width / destImg.width) * 2.0 * Math.tan(b/2.0) / b;
			} else {
				// pamoramic or fisheye image
				scale = ((double)destImg.hfov / srcImg.hfov) * ((double)srcImg.width/ (double) destImg.width)
				   * 2.0 * Math.tan(b/2.0) / b; 
			}
		} else {
			// equirectangular or panoramic or fisheye
			distance = ((double) destImg.width) / b;
			if (srcImg.format == ImageFormat.Rectilinear) {
				// rectilinear image
				scale = ((double)destImg.hfov / srcImg.hfov) * (a /(2.0 * Math.tan(a/2.0))) * ((double)srcImg.width)/ ((double) destImg.width);
			} else {
				// pamoramic or fisheye image
				scale = ((double)destImg.hfov / srcImg.hfov) * ((double)srcImg.width)/ ((double) destImg.width);
			}
		}
		double shearX = srcImg.cP.shear_x / srcImg.height;
		double shearY = srcImg.cP.shear_y / srcImg.width;
		double horizontal = srcImg.cP.horizontal_params[colorIndex];
		double vertical = srcImg.cP.vertical_params[colorIndex];
		double rotX	= distance * Math.PI;						// 180Ў in screenpoints
		double rotY = -srcImg.yaw * distance * MathUtil.deg2rad; 	//    rotation angle in screenpoints
		double rad0 = srcImg.cP.radial_params[colorIndex][0];
		double rad1 = srcImg.cP.radial_params[colorIndex][1];
		double rad2 = srcImg.cP.radial_params[colorIndex][2];
		double rad3 = srcImg.cP.radial_params[colorIndex][3];
		double rad4 = srcImg.cP.radial_params[colorIndex][4];
		
		if ((srcImg.cP.correction_mode == cPrefsCorrectionMode.Radial) ||
			(srcImg.cP.correction_mode == cPrefsCorrectionMode.Morph))
			rad4 = ( (double)( srcImg.width < srcImg.height ? srcImg.width : srcImg.height) ) / 2.0;
		else
			rad4 = ((double) srcImg.height) / 2.0;
		
		switch (destImg.format) {
		case Rectilinear:
			// Convert rectilinear to equirect
			TransformationFunctions.erectRect(p, distance);
			break;
		case Panorama:
			// Convert panoramic to equirect
			TransformationFunctions.erectPano(p, distance);
			break;
		case FisheyeCirc:
		case FisheyeFF:
			// Convert panoramic to sphere
			TransformationFunctions.erectSphereTP(p, distance);
			break;
		default:
			break;
		}
		
		// Rotate equirect. image horizontally
		TransformationFunctions.rotateErect(p, rotX, rotY);
		// Convert spherical image to equirect.
		TransformationFunctions.sphereTPErect(p, distance);
		// Perspective Control spherical Image
		TransformationFunctions.perspSphere(p, mt, distance);
		
		// Perform radial correction
		if (srcImg.cP.horizontal)
			TransformationFunctions.horiz(p, horizontal);
		if (srcImg.cP.vertical)
			TransformationFunctions.vert(p, vertical);
		if (srcImg.cP.radial) {
			switch (srcImg.cP.correction_mode) {
			case Radial:
			case Morph:
				TransformationFunctions.invRadial(p, rad0, rad1, rad2, rad3, rad4);
				break;
			case Vertical:
				TransformationFunctions.invVertical(p, rad0, rad1, rad2, rad3, rad4);
				break;
			case Deregister:
				break;
			}
		}
		
		switch (srcImg.format) {
		case Rectilinear:
			// Convert rectilinear to spherical
			TransformationFunctions.rectSphereTP(p, distance);
			break;
		case Panorama:
			// Convert panoramic to spherical
			TransformationFunctions.panoSphereTP(p, distance);
			break;
		case Equirectangular:
			// Convert PSphere to spherical
			TransformationFunctions.erectSphereTP(p, distance);
			break;
		default:
			break;
		}
		
		// Scale image
		TransformationFunctions.resize(p, scale, scale);
		
		if (srcImg.cP.shear)
			TransformationFunctions.shear(p, shearX, shearY);
		if (srcImg.cP.horizontal)
			TransformationFunctions.horiz(p, horizontal);
		if (srcImg.cP.vertical)
			TransformationFunctions.vert(p, vertical);
		if (srcImg.cP.radial) {
			switch (srcImg.cP.correction_mode) {
			case Radial:
			case Morph:
				TransformationFunctions.radial(p, rad0, rad1, rad2, rad3, rad4, rad4);
				break;
			case Vertical:
				TransformationFunctions.vertical(p, rad0, rad1, rad2, rad3, rad4);
				break;
			case Deregister:
				TransformationFunctions.deregister(p, /*rad0,*/ rad1, rad2, rad3, rad4);
				break;
			}
		}
	}
	
	// Set inverse Makeparameters depending on adjustprefs, color and source image
	static void makeInvParams(Point2D.Double p, Image srcImg, Image destImg, int colorIndex) {
		double a = srcImg.hfov * MathUtil.deg2rad;	// field of view in rad
		double b = destImg.hfov * MathUtil.deg2rad;
		Matrix mt = MathUtil.makeAngles(srcImg.pitch * MathUtil.deg2rad, 0.0, srcImg.roll * MathUtil.deg2rad, true);
		
		double scaleY;
		double distance;
		if (destImg.format == ImageFormat.Rectilinear) {
			// rectilinear panorama
			distance = (double) destImg.width / (2.0 * Math.tan(b / 2.0));
			if (srcImg.format == ImageFormat.Rectilinear) {
				// rectilinear image
				scaleY = ((double) destImg.hfov / srcImg.hfov) * 
						(a / (2.0 * Math.tan(a / 2.0))) * ((double)srcImg.width / destImg.width) * 2.0 * Math.tan(b/2.0) / b;
			} else {
				// pamoramic or fisheye image
				scaleY = ((double)destImg.hfov / srcImg.hfov) * ((double)srcImg.width/ (double) destImg.width)
				   * 2.0 * Math.tan(b/2.0) / b; 
			}
		} else {
			// equirectangular or panoramic or fisheye
			distance = ((double) destImg.width) / b;
			if (srcImg.format == ImageFormat.Rectilinear) {
				// rectilinear image
				scaleY = ((double)destImg.hfov / srcImg.hfov) * (a /(2.0 * Math.tan(a/2.0))) * ((double)srcImg.width)/ ((double) destImg.width);
			} else {
				// pamoramic or fisheye image
				scaleY = ((double)destImg.hfov / srcImg.hfov) * ((double)srcImg.width)/ ((double) destImg.width);
			}
		}
		double scaleX = 1.0 / scaleY;
		scaleY = scaleX;
//		double shearX = im.cP.shear_x / im.height;
//		double shearY = im.cP.shear_y / im.width;
		double horizontal = -srcImg.cP.horizontal_params[colorIndex];
		double vertical = -srcImg.cP.vertical_params[colorIndex];
		double rotX	= distance * Math.PI;						// 180Ў in screenpoints
		double rotY = srcImg.yaw * distance * MathUtil.deg2rad; 	//    rotation angle in screenpoints
		double rad0 = srcImg.cP.radial_params[colorIndex][0];
		double rad1 = srcImg.cP.radial_params[colorIndex][1];
		double rad2 = srcImg.cP.radial_params[colorIndex][2];
		double rad3 = srcImg.cP.radial_params[colorIndex][3];
		double rad4 = srcImg.cP.radial_params[colorIndex][4];

		if ((srcImg.cP.correction_mode == cPrefsCorrectionMode.Radial) ||
			(srcImg.cP.correction_mode == cPrefsCorrectionMode.Morph))
			rad4 = ( (double)( srcImg.width < srcImg.height ? srcImg.width : srcImg.height) ) / 2.0;
		else
			rad4 = ((double) srcImg.height) / 2.0;
			
//		System.out.printf("hor=%f vert=%f rad0=%f, rad1=%f rad2=%f rad3=%f rad4=%f rad5=\n", horizontal, vertical, rad0, rad1, rad2, rad3, rad4);
//		System.out.printf("rot0=%f rot1=%f dist=%f\n", rotX, rotY, distance);
//		mt.printM("MT=");
		
		// Perform radial correction
		if (srcImg.cP.horizontal)
			TransformationFunctions.horiz(p, horizontal);
		if (srcImg.cP.vertical)
			TransformationFunctions.vert(p, vertical);
		if (srcImg.cP.radial) {
			switch (srcImg.cP.correction_mode) {
			case Radial:
			case Morph:
				TransformationFunctions.invRadial(p, rad0, rad1, rad2, rad3, rad4);
				break;
			case Vertical:
				TransformationFunctions.invVertical(p, rad0, rad1, rad2, rad3, rad4);
				break;
			case Deregister:
				break;
			}
		}

		// Scale image
		TransformationFunctions.resize(p, scaleX, scaleY);

		switch (srcImg.format) {
		case Rectilinear:
			// Convert rectilinear to spherical
			TransformationFunctions.sphereTPRect(p, distance);
			break;
		case Panorama:
			// Convert panoramic to spherical
			TransformationFunctions.sphereTPPano(p, distance);
			break;
		case Equirectangular:
			// Convert PSphere to spherical
			TransformationFunctions.sphereTPErect(p, distance);
			break;
		default:
			break;
		}

		// Perspective Control spherical Image
		TransformationFunctions.perspSphere(p, mt, distance);
		// Convert spherical image to equirect.
		TransformationFunctions.erectSphereTP(p, distance);
		// Rotate equirect. image horizontally
		TransformationFunctions.rotateErect(p, rotX, rotY);

		switch (destImg.format) {
		case Rectilinear:
			// Convert rectilinear to equirect
			TransformationFunctions.rectErect(p, distance);
			break;
		case Panorama:
			// Convert panoramic to equirect
			TransformationFunctions.panoErect(p, distance);
			break;
		case FisheyeCirc:
		case FisheyeFF:
			// Convert panoramic to sphere
			TransformationFunctions.sphereTPErect(p, distance);
			break;
		default:
			break;
		}
	}

	double distControlPoint(Point2D.Double p0, Point2D.Double p1, ControlPoint cp, Image image, boolean isSphere) {
		p0.x = cp.x0 - (double) cp.image0.width / 2.0 + 0.5;
		p0.y = cp.y0 - (double) cp.image0.height / 2.0 + 0.5;
		makeInvParams(p0, cp.image0, image, 0);

		p1.x = cp.x1 - (double) cp.image1.width / 2.0 + 0.5;
		p1.y = cp.y1 - (double) cp.image1.height / 2.0 + 0.5;
		makeInvParams(p1, cp.image1, image, 0);

		if (isSphere) {
			p0.x = p0.x * MathUtil.deg2rad; 
			p0.y = p0.y * MathUtil.deg2rad + Math.PI / 2.0;
			double b0x0 =   Math.sin(p0.x) * Math.sin(p0.y);
			double b0x1 =   Math.cos(p0.y);
			double b0x2 = - Math.cos(p0.x) * Math.sin(p0.y);
	
			p1.x = p1.x * MathUtil.deg2rad; 
			p1.y = p1.y * MathUtil.deg2rad + Math.PI / 2.0;
			double b1x0 =   Math.sin(p1.x) * Math.sin(p1.y);
			double b1x1 =   Math.cos(p1.y);
			double b1x2 = - Math.cos(p1.x) * Math.sin(p1.y);
			
			double scalarProduct = b0x0 * b1x0 + b0x1 * b1x1 + b0x2 * b1x2;
			return Math.acos(scalarProduct) * alignInfo.pano.width / (2.0 * Math.PI);
		}
		// take care of wrapping and points at edge of panorama
		if (alignInfo.pano.hfov == 360.0) {
			double delta = Math.abs(p0.x - p1.x);
			if (delta > alignInfo.pano.width / 2.0) {
				if (p0.x < p1.x)
					p0.x += alignInfo.pano.width;
				else
					p1.x += alignInfo.pano.width;
			}
		}
		// What do we want to optimize?
		switch (cp.type) {
		case x:
			// x difference
			return (p0.x - p1.x) * (p0.x - p1.x);
		case y:
			// y-difference
			return (p0.y - p1.y) * (p0.y - p1.y);
		case r:
		default:
			// square of distance
			return
				(p0.y - p1.y) * (p0.y - p1.y) + 
				(p0.x - p1.x) * (p0.x - p1.x); 
		}
	}
	
	/** 
	 * Levenberg-Marquardt function measuring the quality of the fit in fvec[]
	 * 
	 * @param x		is an array of length n. on input x must contain
	 *				an initial estimate of the solution vector. on output x
	 *				contains the final estimate of the solution vector.
	 * @param fvec	is an output array of length m which contains
	 * 				the functions evaluated at the output x.
	 */
	private int numIt = 0;
	public void fcn(Matrix x, Matrix fvec, int iflag) {
		int m = fvec.getSizeX();	// the number of functions.
//		int n = x.getSizeX();		// the number of variables. n must not exceed m.
		
		if (iflag == 0) {
			double r = 0.0;
			for (int i = 0; i < m; i++) {
				r += fvec.getItem(i, 0);
			}
			r = Math.sqrt(r / (double) m);
			System.out.printf("Average Difference between Controlpoints \nafter %d iteration(s): %g pixels\n", numIt, r);
			numIt += 10;
			return;
		}
		
		int j = 0;
		int k;
		// Set global preferences structures using LM-params
		for (int i = 0; i < alignInfo.images.size(); i++) {
			Image im = alignInfo.images.get(i);
			optVars opt = alignInfo.options.get(i);
			
			if ((k = opt.yaw) > 0) {
				if (k == 1) {
//					System.out.printf("YAW=%f\n", im.yaw);
//					System.out.printf("YAWNEW=%f\n", x.getItem(j, 0));
					im.yaw = x.getItem(j++, 0);
					im.yaw = NORM_ANGLE(im.yaw);
				} else {
					im.yaw = alignInfo.images.get(k - 2).yaw;
				}
			}
			if ((k = opt.pitch) > 0) {
				if (k == 1) {
					im.pitch = x.getItem(j++, 0);
					im.pitch = NORM_ANGLE(im.pitch);
				} else {
					im.pitch = alignInfo.images.get(k - 2).pitch;
				}
			}
			if ((k = opt.roll) > 0) {
				if (k == 1) {
					im.roll = x.getItem(j++, 0);
					NORM_ANGLE(im.roll);
				} else {
					im.roll = alignInfo.images.get(k - 2).roll;
				}
			}
			if ((k = opt.hfov) > 0) {
				if (k == 1) {
					im.hfov = x.getItem(j++, 0);
					if (im.hfov < 0.0)
						im.hfov = -im.hfov;
				} else {
					im.hfov = alignInfo.images.get(k - 2).hfov;
				}
			}
			if ((k = opt.a) > 0) {
				if (k == 1) {
					im.cP.radial_params[0][3] = x.getItem(j++, 0) / C_FACTOR;
				} else {
					im.cP.radial_params[0][3] = alignInfo.images.get(k - 2).cP.radial_params[0][3];
				}
			}
			if ((k = opt.b) > 0) {
				if (k == 1) {
					im.cP.radial_params[0][2] = x.getItem(j++, 0) / C_FACTOR;
				} else {
					im.cP.radial_params[0][2] = alignInfo.images.get(k - 2).cP.radial_params[0][2];
				}
			}
			if ((k = opt.c) > 0) {
				if (k == 1) {
					im.cP.radial_params[0][1] = x.getItem(j++, 0) / C_FACTOR;
				} else {
					im.cP.radial_params[0][1] = alignInfo.images.get(k - 2).cP.radial_params[0][1];
				}
			}
			if ((k = opt.d) > 0) {
				if (k == 1) {
					im.cP.horizontal_params[0] = x.getItem(j++, 0);
				} else {
					im.cP.horizontal_params[0] = alignInfo.images.get(k - 2).cP.horizontal_params[0];
				}
			}
			if ((k = opt.e) > 0) {
				if (k == 1) {
					im.cP.vertical_params[0] = x.getItem(j++, 0);
				} else {
					im.cP.vertical_params[0] = alignInfo.images.get(k - 2).cP.vertical_params[0];
				}
			}
		}
		
		// Calculate distances
		Point2D.Double p0 = new Point2D.Double();
		Point2D.Double p1 = new Point2D.Double();
		Image sph = new Image();
		sph.width = 360;
		sph.height = 180;
		sph.format = ImageFormat.Equirectangular;
		sph.hfov = 360.0;
		double avg = 0.0;
		
		for (int i = 0; i < alignInfo.controlPoints.size(); i++) {
			ControlPoint cp = alignInfo.controlPoints.get(i);
			double d = distControlPoint(p0, p1, cp, 
					cp.type == OptimizeType.r ? sph : alignInfo.pano, 
					cp.type == OptimizeType.r);
			fvec.setItem(i, 0, d);
			avg += d;
		}
		
		avg /= alignInfo.controlPoints.size();
		for (int i = alignInfo.controlPoints.size(); i < m; i++)
			fvec.setItem(i, 0, avg);
		
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
	}
	
	void RunLMOptimizer() throws Exception {
		// Initialize optimization params
		int n = alignInfo.numParam;
		int m = alignInfo.controlPoints.size();
		Matrix x = new Matrix(n, 1);
		Matrix fvec = new Matrix(m, 1);
		
		// Set LM params using global preferences structure
		// Change to cover range 0....1 (roughly)
		int j = 0; // Counter for optimization parameters
		for (int i = 0; i < alignInfo.images.size(); i++) {
			Image im = alignInfo.images.get(i);
			optVars opt = alignInfo.options.get(i);
			
			if(opt.yaw != 0)		// optimize alpha? 0-no 1-yes
				x.setItem(j++, 0, im.yaw);
			if(opt.pitch != 0)		// optimize pitch? 0-no 1-yes
				x.setItem(j++, 0, im.pitch);
			if(opt.roll != 0)		// optimize gamma? 0-no 1-yes
				x.setItem(j++, 0, im.roll);
			if(opt.hfov != 0)		// optimize hfov? 0-no 1-yes
				x.setItem(j++, 0, im.hfov);
			if(opt.a != 0)			// optimize a? 0-no 1-yes
				x.setItem(j++, 0, im.cP.radial_params[0][3] * C_FACTOR);
			if(opt.b != 0)			// optimize b? 0-no 1-yes
				x.setItem(j++, 0, im.cP.radial_params[0][2] * C_FACTOR);
			if(opt.c != 0)			// optimize c? 0-no 1-yes
				x.setItem(j++, 0, im.cP.radial_params[0][1] * C_FACTOR);
			if(opt.d != 0)			// optimize d? 0-no 1-yes
				x.setItem(j++, 0, im.cP.horizontal_params[0]);
			if(opt.e != 0)			// optimize e? 0-no 1-yes
				x.setItem(j++, 0, im.cP.vertical_params[0]);
		}
		if (j != alignInfo.numParam)
			throw new RuntimeException("Invalid value for numParam");
		
		LMDif.lmdif(this, x, fvec);
	}

	public static void main(String[] args) throws Exception {
		BufferedReader fin = new BufferedReader(new InputStreamReader(
				PanoAdjust.class.getResourceAsStream("optimizer2.txt")));
		PanoAdjust panoAdjust = new PanoAdjust();
		panoAdjust.alignInfo.readPanoScript(fin);
		
		panoAdjust.RunLMOptimizer();
		fin.close();
		panoAdjust.alignInfo.writePanoScript(System.out);
		System.out.println("Done");
	}
}
