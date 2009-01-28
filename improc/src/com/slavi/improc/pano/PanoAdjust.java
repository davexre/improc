package com.slavi.improc.pano;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.slavi.improc.pano.AlignInfo.cPrefsCorrectionMode;
import com.slavi.improc.pano.AlignInfo.optVars;
import com.slavi.improc.pano.ControlPoint.OptimizeType;
import com.slavi.improc.pano.ImageData.ImageFormat;
import com.slavi.improc.pano.LMDif.LMDifFcn;
import com.slavi.math.MathUtil;
import com.slavi.math.matrix.JLapack;
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
	static void makeParams(Point2D.Double p, ImageData srcImg, ImageData destImg, int colorIndex) {
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
		double shearX = srcImg.shear_x / srcImg.height;
		double shearY = srcImg.shear_y / srcImg.width;
		double horizontal = srcImg.horizontal_params[colorIndex];
		double vertical = srcImg.vertical_params[colorIndex];
		double rotX	= distance * Math.PI;						// 180Ў in screenpoints
		double rotY = -srcImg.yaw * MathUtil.deg2rad * distance; 	//    rotation angle in screenpoints
		double rad0 = srcImg.radial_params[colorIndex][0];
		double rad1 = srcImg.radial_params[colorIndex][1];
		double rad2 = srcImg.radial_params[colorIndex][2];
		double rad3 = srcImg.radial_params[colorIndex][3];
		double rad4 = srcImg.radial_params[colorIndex][4];
		
		if ((srcImg.correction_mode == cPrefsCorrectionMode.Radial) ||
			(srcImg.correction_mode == cPrefsCorrectionMode.Morph))
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
		if (srcImg.horizontal)
			TransformationFunctions.horiz(p, horizontal);
		if (srcImg.vertical)
			TransformationFunctions.vert(p, vertical);
		if (srcImg.radial) {
			switch (srcImg.correction_mode) {
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
		
		if (srcImg.shear)
			TransformationFunctions.shear(p, shearX, shearY);
		if (srcImg.horizontal)
			TransformationFunctions.horiz(p, horizontal);
		if (srcImg.vertical)
			TransformationFunctions.vert(p, vertical);
		if (srcImg.radial) {
			switch (srcImg.correction_mode) {
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
	static void makeInvParams(Point2D.Double p, ImageData srcImg, ImageData destImg, int colorIndex) {
		double a = srcImg.hfov * MathUtil.deg2rad;	// field of view in rad
		double b = destImg.hfov * MathUtil.deg2rad;
		Matrix mt = MathUtil.makeAngles(srcImg.pitch * MathUtil.deg2rad, 0.0, srcImg.roll * MathUtil.deg2rad, true);
		
		double scaleY;
		double distance;
		if (destImg.format == ImageFormat.Rectilinear) {
			// rectilinear panorama
			distance = (double) destImg.width / (2.0 * Math.tan(b / 2.0));
		} else {
			// equirectangular or panoramic or fisheye
			distance = ((double) destImg.width) / b;
		}

		if (srcImg.format == ImageFormat.Rectilinear) {
			// rectilinear image
			scaleY = (double) srcImg.width / (2.0 * Math.tan(a/2.0)) / distance; 
		} else {
			// pamoramic or fisheye image
			scaleY = ((double) srcImg.width) / a / distance;
		}
				
		double scaleX = 1.0 / scaleY;
		scaleY = scaleX;
		double shearX = - srcImg.shear_x / srcImg.height;
		double shearY = - srcImg.shear_y / srcImg.width;
		double horizontal = -srcImg.horizontal_params[colorIndex];
		double vertical = -srcImg.vertical_params[colorIndex];
		double rotX	= distance * Math.PI;						// 180Ў in screenpoints
		double rotY = srcImg.yaw * distance * MathUtil.deg2rad; 	//    rotation angle in screenpoints
		double rad0 = srcImg.radial_params[colorIndex][0];
		double rad1 = srcImg.radial_params[colorIndex][1];
		double rad2 = srcImg.radial_params[colorIndex][2];
		double rad3 = srcImg.radial_params[colorIndex][3];
		double rad4 = srcImg.radial_params[colorIndex][4];
		double rad5 = rad4;

		if ((srcImg.correction_mode == cPrefsCorrectionMode.Radial) ||
			(srcImg.correction_mode == cPrefsCorrectionMode.Morph))
			rad4 = ( (double)( srcImg.width < srcImg.height ? srcImg.width : srcImg.height) ) / 2.0;
		else
			rad4 = ((double) srcImg.height) / 2.0;
			
//		System.out.printf("hor=%f vert=%f rad0=%f, rad1=%f rad2=%f rad3=%f rad4=%f rad5=\n", horizontal, vertical, rad0, rad1, rad2, rad3, rad4);
//		System.out.printf("rot0=%f rot1=%f dist=%f\n", rotX, rotY, distance);
//		mt.printM("MT=");
		
		// Perform radial correction
		if (srcImg.shear)
			TransformationFunctions.shear(p, shearX, shearY);
		if (srcImg.horizontal)
			TransformationFunctions.horiz(p, horizontal);
		if (srcImg.vertical)
			TransformationFunctions.vert(p, vertical);
		if (srcImg.radial) {
			switch (srcImg.correction_mode) {
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

	int fcnPanoNperCP = 1; // number of functions per control point, 1 or 2
	
	double distControlPoint(Point2D.Double p0, Point2D.Double p1, ControlPoint cp, ImageData image, boolean isSphere) {
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

//			double scalarProduct = b0x0 * b1x0 + b0x1 * b1x1 + b0x2 * b1x2;
//			return Math.acos(scalarProduct) * alignInfo.pano.width / (2.0 * Math.PI);
			
			// new
			double radiansToPixelsFactor = alignInfo.pano.width / (alignInfo.pano.hfov * (Math.PI / 180.0));
			double dlon = p0.x - p1.x;
			if (dlon < -Math.PI) dlon += 2.0 * Math.PI;
			if (dlon > Math.PI) dlon -= 2.0 * Math.PI;

			cp.distanceComponent0 = (dlon * Math.sin(0.5 * (p0.y + p1.y))) * radiansToPixelsFactor; 
			cp.distanceComponent1 = (p0.y - p1.y) * radiansToPixelsFactor;
			
			double rx0 = b0x1 * b1x2 - b0x2 * b1x1;
			double rx1 = b0x2 * b1x0 - b0x0 * b1x2;
			double rx2 = b0x0 * b1x1 - b0x1 * b1x0;
			
			double scalarProduct = rx0 * rx0 + rx1 * rx1 + rx2 * rx2;
			double dangle = Math.asin(Math.sqrt(scalarProduct));
			
			scalarProduct = b0x0 * b1x0 + b0x1 * b1x1 + b0x2 * b1x2;
			if (scalarProduct < 0.0)
				dangle = Math.PI - dangle;
			double dist = dangle * radiansToPixelsFactor;

//			System.out.printf("CP[%d] x0=%10.8f y0=%10.8f x1=%10.8f y1=%10.8f\n", alignInfo.controlPoints.indexOf(cp), p0.x, p0.y, p1.x, p1.y);
//			System.out.printf("CP[%d] dangle=%10.8f scalarProduct=%10.8f\n", alignInfo.controlPoints.indexOf(cp), dangle, scalarProduct);
//			System.out.printf("CP[%d] dist=%10.8f radiansToPixelsFactor=%10.8f\n", alignInfo.controlPoints.indexOf(cp), dist, radiansToPixelsFactor);
			
			return dist;			
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
		double result = 0.0;
		switch (cp.type) {
		case x:
			// x difference
			result = (p0.x - p1.x) * (p0.x - p1.x);
			break;
		case y:
			// y-difference
			result = (p0.y - p1.y) * (p0.y - p1.y);
			break;
		case r:
		default: 
			// square of distance
//			cp.distanceComponent0 = p0.y - p1.y;
//			cp.distanceComponent1 = p0.x - p1.x;
			result = 
				(p0.y - p1.y) * (p0.y - p1.y) + 
				(p0.x - p1.x) * (p0.x - p1.x);
			break;
		}
		cp.distanceComponent0 = Math.sqrt(result);
		cp.distanceComponent1 = 0.0;
		return result;
	}
	
	boolean needInitialAvgFov = true;
	double initialAvgFov = 0.0;
	
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
				r += fvec.getItem(i, 0) * fvec.getItem(i, 0);
			}
			r = Math.sqrt(r / (double) m) * Math.sqrt((double) fcnPanoNperCP);
			System.out.printf("Average Difference between Controlpoints \nafter %d iteration(s): %10.8f pixels\n", numIt, r);
			numIt += 10;
			return;
		}
		
		int j = 0;
		int k;
		double sumhfov = 0.0;
		// Set global preferences structures using LM-params
		for (int i = 0; i < alignInfo.images.size(); i++) {
			ImageData im = alignInfo.images.get(i);
			optVars opt = alignInfo.options.get(i);
			
			if ((k = opt.yaw) > 0) {
				if (k == 1) {
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
			sumhfov += im.hfov;
			if ((k = opt.a) > 0) {
				if (k == 1) {
					im.radial_params[0][3] = x.getItem(j++, 0) / C_FACTOR;
				} else {
					im.radial_params[0][3] = alignInfo.images.get(k - 2).radial_params[0][3];
				}
			}
			if ((k = opt.b) > 0) {
				if (k == 1) {
					im.radial_params[0][2] = x.getItem(j++, 0) / C_FACTOR;
				} else {
					im.radial_params[0][2] = alignInfo.images.get(k - 2).radial_params[0][2];
				}
			}
			if ((k = opt.c) > 0) {
				if (k == 1) {
					im.radial_params[0][1] = x.getItem(j++, 0) / C_FACTOR;
				} else {
					im.radial_params[0][1] = alignInfo.images.get(k - 2).radial_params[0][1];
				}
			}
			if ((k = opt.d) > 0) {
				if (k == 1) {
					im.horizontal_params[0] = x.getItem(j++, 0);
				} else {
					im.horizontal_params[0] = alignInfo.images.get(k - 2).horizontal_params[0];
				}
			}
			if ((k = opt.e) > 0) {
				if (k == 1) {
					im.vertical_params[0] = x.getItem(j++, 0);
				} else {
					im.vertical_params[0] = alignInfo.images.get(k - 2).vertical_params[0];
				}
			}
			if ((k = opt.shear_x) > 0) {
				if (k == 1) {
					im.shear_x = x.getItem(j++, 0);
				} else {
					im.shear_x = alignInfo.images.get(k - 2).shear_x;
				}
			}
			if ((k = opt.shear_y) > 0) {
				if (k == 1) {
					im.shear_y = x.getItem(j++, 0);
				} else {
					im.shear_y = alignInfo.images.get(k - 2).shear_y;
				}
			}
			
			im.radial_params[0][0] = 1.0 - 
					(im.radial_params[0][3] + im.radial_params[0][2] + im.radial_params[0][1]);
		}
		
		double avgfovFromSAP = sumhfov / alignInfo.images.size();
		
		if (needInitialAvgFov) {
			initialAvgFov = avgfovFromSAP;
			needInitialAvgFov = false;
		}
		//System.out.printf("avgfovFromSAP=%10.8f\n", avgfovFromSAP);
		// Calculate distances
		Point2D.Double p0 = new Point2D.Double();
		Point2D.Double p1 = new Point2D.Double();
		ImageData sph = new ImageData();
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
			
			if ((initialAvgFov / avgfovFromSAP) > 1.0) {
				d *= initialAvgFov / avgfovFromSAP;
			}
			fvec.setItem(i, 0, d);
			avg += d * d;
		}
		
		avg = Math.sqrt(avg / alignInfo.controlPoints.size());
		for (int i = alignInfo.controlPoints.size(); i < m; i++)
			fvec.setItem(i, 0, avg);
		
//		System.out.printf("fvec norm=%12.8f xnorm=%12.8f\n", fvec.getForbeniusNorm(), x.getForbeniusNorm());
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
	}
	
	void RunLMOptimizer() throws Exception {
		// Initialize optimization params
		needInitialAvgFov = true;
		int n = alignInfo.numParam;
		int m = alignInfo.controlPoints.size();
		Matrix x = new Matrix(n, 1);
		Matrix fvec = new Matrix(m, 1);
		
		// Set LM params using global preferences structure
		// Change to cover range 0....1 (roughly)
		int j = 0; // Counter for optimization parameters
		for (int i = 0; i < alignInfo.images.size(); i++) {
			ImageData im = alignInfo.images.get(i);
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
				x.setItem(j++, 0, im.radial_params[0][3] * C_FACTOR);
			if(opt.b != 0)			// optimize b? 0-no 1-yes
				x.setItem(j++, 0, im.radial_params[0][2] * C_FACTOR);
			if(opt.c != 0)			// optimize c? 0-no 1-yes
				x.setItem(j++, 0, im.radial_params[0][1] * C_FACTOR);
			if(opt.d != 0)			// optimize d? 0-no 1-yes
				x.setItem(j++, 0, im.horizontal_params[0]);
			if(opt.e != 0)			// optimize e? 0-no 1-yes
				x.setItem(j++, 0, im.vertical_params[0]);
			if(opt.shear_x != 0)	// optimize shear_x? 0-no 1-yes
				x.setItem(j++, 0, im.shear_x);
			if(opt.shear_y != 0)	// optimize shear_y? 0-no 1-yes
				x.setItem(j++, 0, im.shear_y);
		}
		if (j != alignInfo.numParam)
			throw new RuntimeException("Invalid value for numParam");
		
		LMDif.lmdif(this, x, fvec, 0.05);
		LMDif.lmdif(this, x, fvec, 0.0001);
	}

	public static void main(String[] args) throws Exception {
		BufferedReader fin = new BufferedReader(new InputStreamReader(
				PanoAdjust.class.getResourceAsStream("optimizer2.txt")));
		PanoAdjust panoAdjust = new PanoAdjust();
		panoAdjust.alignInfo.readPanoScript(fin);
		
		panoAdjust.RunLMOptimizer();
		fin.close();
		panoAdjust.alignInfo.writePanoScript(System.out);

		Point2D.Double p0 = new Point2D.Double();
		Point2D.Double p1 = new Point2D.Double();
		Point2D.Double p2 = new Point2D.Double();
		for (int i = 0; i < panoAdjust.alignInfo.images.size(); i++) {
			ImageData src = panoAdjust.alignInfo.images.get(i);
			double roll = src.roll;
			double yaw = src.yaw;
			double pitch = src.pitch;
			src.roll = 0.0;
			src.yaw = 0.0;
			src.pitch = 0.0;
			
			p0.x = 1.0;
			p0.y = 1.0;
			
			p1.x = p0.x;
			p1.y = p0.y;
			PanoAdjust.makeInvParams(p1, src, panoAdjust.alignInfo.pano, 0);
			p2.x = p1.x;
			p2.y = p1.y;
			PanoAdjust.makeInvParams(p2, src, panoAdjust.alignInfo.pano, 0);
			
			double s = JLapack.hypot(p2.x - p0.x, p2.y - p0.y) / Math.sqrt(2.0);
			
			System.out.println("im=" + i + " d=" + s + " dx=" + (p2.x-p0.x) + " dy=" + (p2.x-p0.x));
			
			src.roll = roll;
			src.yaw = yaw;
			src.pitch = pitch;
		}
		
		Point2D.Double fov = panoAdjust.alignInfo.getFieldOfView();
		System.out.println("FOV=" + fov);
		panoAdjust.alignInfo.pano.hfov = fov.x;
		panoAdjust.alignInfo.calcOptimalPanoWidth();
		panoAdjust.alignInfo.calculateExtents(panoAdjust.alignInfo.pano);
		System.out.println("Pano extent is " + panoAdjust.alignInfo.pano.extentInPano);
		System.out.println("Width =" + panoAdjust.alignInfo.pano.width);
		System.out.println("Height=" + panoAdjust.alignInfo.pano.height);

		p0.x = 1.0;
		p0.y = 1.0;
		
		p1.x = p0.x;
		p1.y = p0.y;
		ImageData image = panoAdjust.alignInfo.images.get(0);
		PanoAdjust.makeInvParams(p1, image, panoAdjust.alignInfo.pano, 0);
		
		p2.x = p1.x;
		p2.y = p1.y;
		PanoAdjust.makeParams(p2, image, panoAdjust.alignInfo.pano, 0);
		
		System.out.println("P0=" + p0);
		System.out.println("P1=" + p1);
		System.out.println("P2=" + p2);
		
		PanoMake.makePano(panoAdjust.alignInfo);
		
		System.out.println("Done");
	}
}
