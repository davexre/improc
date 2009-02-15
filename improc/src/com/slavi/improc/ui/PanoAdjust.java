package com.slavi.improc.ui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.slavi.improc.PanoPair;
import com.slavi.improc.PanoPairList;
import com.slavi.improc.pano.ImageData;
import com.slavi.improc.pano.LMDif;
import com.slavi.improc.pano.TransformationFunctions;
import com.slavi.improc.pano.ImageData.ImageFormat;
import com.slavi.improc.pano.LMDif.LMDifFcn;
import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.Const;

public class PanoAdjust implements LMDifFcn {

	// variables to optimize
	boolean yaw = true;
	boolean pitch = true;
	boolean roll = true;
	boolean hfov = false;
	boolean a = false;
	boolean b = false;
	boolean c = false;
	boolean d = false;
	boolean e = false;
//	boolean X = false;
//	boolean Y = false;
//	boolean Z = false;
	boolean shearX = false;
	boolean shearY = false;

	int countVariablesToOptimize() {
		int result = 0;
		if (yaw) result++;
		if (pitch) result++;
		if (roll) result++;
		if (hfov) result++;
		if (a) result++;
		if (b) result++;
		if (c) result++;
		if (d) result++;
		if (e) result++;
//		if (X) result++;
//		if (Y) result++;
//		if (Z) result++;
		if (shearX) result++;
		if (shearY) result++;
		return result;
	}

	boolean needInitialAvgFov = true;
	double initialAvgFov = 0.0;
	int numParam; // Number of parameters to optimize
	int numControlPoints;
	Map<String, ImageData> images = new HashMap<String, ImageData>();
	int fcnPanoNperCP = 1; // number of functions per control point, 1 or 2
	private int numIt = 0;

	static double NORM_ANGLE(double x) {
		while (x > 180.0) {
			x -= 360.0;
		}
		while (x <= -180.0) {
			x += 360.0;
		}
		return x;
	}

	static double NORM_ANGLE_HALF(double x) {
		while (x > 90.0) {
			x -= 180.0;
		}
		while (x <= -90.0) {
			x += 180.0;
		}
		return x;
	}

	private static double C_FACTOR = 100.0;
	
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
		for (ImageData im : images.values()) {
			if (im.optimizeYaw != 0) {
				im.yaw = x.getItem(j++, 0);
				im.yaw = NORM_ANGLE_HALF(im.yaw);
			}
			if (im.optimizePitch != 0) {
				im.pitch = x.getItem(j++, 0);
				im.pitch = NORM_ANGLE(im.pitch);
			}
			if (im.optimizeRoll != 0) {
				im.roll = x.getItem(j++, 0);
				NORM_ANGLE(im.roll);
			}
			if (im.optimizeHfov != 0) {
				im.hfov = x.getItem(j++, 0);
				if (im.hfov < 0.0)
					im.hfov = -im.hfov;
			}
			sumhfov += im.hfov;
			if (im.optimizeA != 0) {
				im.radial_params[0][3] = x.getItem(j++, 0) / C_FACTOR;
			}
			if (im.optimizeB != 0) {
				im.radial_params[0][2] = x.getItem(j++, 0) / C_FACTOR;
			}
			if (im.optimizeC != 0) {
				im.radial_params[0][1] = x.getItem(j++, 0) / C_FACTOR;
			}
			if (im.optimizeD != 0) {
				im.horizontal_params[0] = x.getItem(j++, 0);
			}
			if (im.optimizeE != 0) {
				im.vertical_params[0] = x.getItem(j++, 0);
			}
			if (im.optimizeShearX != 0) {
				im.shear_x = x.getItem(j++, 0);
			}
			if (im.optimizeShearY != 0) {
				im.shear_y = x.getItem(j++, 0);
			}
			im.radial_params[0][0] = 1.0 - 
					(im.radial_params[0][3] + im.radial_params[0][2] + im.radial_params[0][1]);
		}
		
		double avgfovFromSAP = sumhfov / images.size();
		
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

		int fvecIndex = 0;
		for (PanoPairList panoList : panoChain){
			for (PanoPair pair : panoList.items) {
				double d = distControlPoint(p0, p1, panoList, pair, sph, true);
				
				if ((initialAvgFov / avgfovFromSAP) > 1.0) {
					d *= initialAvgFov / avgfovFromSAP;
				}
				fvec.setItem(fvecIndex, 0, d);
				avg += d * d;
				fvecIndex++;
			}
		}
		
		avg = Math.sqrt(avg / numControlPoints);
		for (int i = numControlPoints; i < m; i++)
			fvec.setItem(i, 0, avg);
		
//		System.out.printf("fvec norm=%12.8f xnorm=%12.8f\n", fvec.getForbeniusNorm(), x.getForbeniusNorm());
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
	}
	
	double distControlPoint(Point2D.Double p0, Point2D.Double p1, PanoPairList panoList, PanoPair cp, ImageData image, boolean isSphere) {
		p0.x = cp.sx - (double) panoList.sourceImageSizeX / 2.0 + 0.5;
		p0.y = cp.sy - (double) panoList.sourceImageSizeY / 2.0 + 0.5;
		makeInvParams(p0, panoList.source, image, 0);

		p1.x = cp.tx - (double) panoList.targetImageSizeX / 2.0 + 0.5;
		p1.y = cp.ty - (double) panoList.targetImageSizeY / 2.0 + 0.5;
		makeInvParams(p1, panoList.target, image, 0);

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
			double radiansToPixelsFactor = pano.width / (pano.hfov * (Math.PI / 180.0));
			double dlon = p0.x - p1.x;
			if (dlon < -Math.PI) dlon += 2.0 * Math.PI;
			if (dlon > Math.PI) dlon -= 2.0 * Math.PI;

//			cp.distanceComponent0 = (dlon * Math.sin(0.5 * (p0.y + p1.y))) * radiansToPixelsFactor; 
//			cp.distanceComponent1 = (p0.y - p1.y) * radiansToPixelsFactor;
			
			double rx0 = b0x1 * b1x2 - b0x2 * b1x1;
			double rx1 = b0x2 * b1x0 - b0x0 * b1x2;
			double rx2 = b0x0 * b1x1 - b0x1 * b1x0;
			
			double scalarProduct = rx0 * rx0 + rx1 * rx1 + rx2 * rx2;
			double dangle = Math.asin(Math.sqrt(scalarProduct));
			
			scalarProduct = b0x0 * b1x0 + b0x1 * b1x1 + b0x2 * b1x2;
			if (scalarProduct < 0.0)
				dangle = Math.PI - dangle;
			double dist = dangle * radiansToPixelsFactor;
			return dist;			
		}
		// take care of wrapping and points at edge of panorama
		if (pano.hfov == 360.0) {
			double delta = Math.abs(p0.x - p1.x);
			if (delta > pano.width / 2.0) {
				if (p0.x < p1.x)
					p0.x += pano.width;
				else
					p1.x += pano.width;
			}
		}
		// ignore type. use always r -> optimize by distance
		double result = 
			(p0.y - p1.y) * (p0.y - p1.y) + 
			(p0.x - p1.x) * (p0.x - p1.x);
//		cp.distanceComponent0 = Math.sqrt(result);
//		cp.distanceComponent1 = 0.0;
		return result;
	}
	
	/**
	 * Converts point p in destImg coordinate system to srcImg coordinate system. 
	 */
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
		
		// Correction mode is always radial
		rad4 = ( (double)( srcImg.width < srcImg.height ? srcImg.width : srcImg.height) ) / 2.0;
/*		if ((srcImg.correction_mode == cPrefsCorrectionMode.Radial) ||
			(srcImg.correction_mode == cPrefsCorrectionMode.Morph))
			rad4 = ( (double)( srcImg.width < srcImg.height ? srcImg.width : srcImg.height) ) / 2.0;
		else
			rad4 = ((double) srcImg.height) / 2.0;*/
		
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
			// Correction mode is always radial
			TransformationFunctions.invRadial(p, rad0, rad1, rad2, rad3, rad4);
/*			switch (srcImg.correction_mode) {
			case Radial:
			case Morph:
				TransformationFunctions.invRadial(p, rad0, rad1, rad2, rad3, rad4);
				break;
			case Vertical:
				TransformationFunctions.invVertical(p, rad0, rad1, rad2, rad3, rad4);
				break;
			case Deregister:
				break;
			}*/
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
			// Correction mode is always radial
			TransformationFunctions.radial(p, rad0, rad1, rad2, rad3, rad4, rad4);
/*			switch (srcImg.correction_mode) {
			case Radial:
			case Morph:
				TransformationFunctions.radial(p, rad0, rad1, rad2, rad3, rad4, rad4);
				break;
			case Vertical:
				TransformationFunctions.vertical(p, rad0, rad1, rad2, rad3, rad4);
				break;
			case Deregister:
				TransformationFunctions.deregister(p, rad1, rad2, rad3, rad4);
				break;
			}*/
		}
	}
	
	/**
	 * Converts point p in srcImg coordinate system to destImg coordinate system. 
	 */
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

		// Correction mode is always radial
		rad4 = ( (double)( srcImg.width < srcImg.height ? srcImg.width : srcImg.height) ) / 2.0;
/*		if ((srcImg.correction_mode == cPrefsCorrectionMode.Radial) ||
			(srcImg.correction_mode == cPrefsCorrectionMode.Morph))
			rad4 = ( (double)( srcImg.width < srcImg.height ? srcImg.width : srcImg.height) ) / 2.0;
		else
			rad4 = ((double) srcImg.height) / 2.0;*/
			
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
			// Correction mode is always radial
			TransformationFunctions.invRadial(p, rad0, rad1, rad2, rad3, rad4);
/*			switch (srcImg.correction_mode) {
			case Radial:
			case Morph:
				TransformationFunctions.invRadial(p, rad0, rad1, rad2, rad3, rad4);
				break;
			case Vertical:
				TransformationFunctions.invVertical(p, rad0, rad1, rad2, rad3, rad4);
				break;
			case Deregister:
				break;
			}*/
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
	
	private ImageData addImage(String image, int width, int height) {
		ImageData result = images.get(image);
		if (result == null) {
			result = new ImageData();
			result.name = image;
			result.width = width;
			result.height = height;
			result.hfov = 38;
			result.optimizeYaw = yaw ? 1 : 0;
			result.optimizePitch = pitch ? 1 : 0;
			result.optimizeRoll = roll ? 1 : 0;
			result.optimizeHfov = hfov ? 1 : 0;
			result.optimizeA = a ? 1 : 0;
			result.optimizeB = b ? 1 : 0;
			result.optimizeC = c ? 1 : 0;
			result.optimizeD = d ? 1 : 0;
			result.optimizeE = e ? 1 : 0;
			result.optimizeShearX = shearX ? 1 : 0;
			result.optimizeShearY = shearY ? 1 : 0;
//			result.optimizeX = X ? 1 : 0;
//			result.optimizeY = Y ? 1 : 0;
//			result.optimizeZ = Z ? 1 : 0;
			images.put(image, result);
		}
		return result;
	}
	
	void RunLMOptimizer() throws Exception {
		// Initialize optimization params
		needInitialAvgFov = true;
		int n = numParam;
		int m = numControlPoints;
		Matrix x = new Matrix(n, 1);
		Matrix fvec = new Matrix(m, 1);
		
		// Set LM params using global preferences structure
		// Change to cover range 0....1 (roughly)
		int j = 0; // Counter for optimization parameters
		for (ImageData im : images.values()) {
			if(im.optimizeYaw != 0)		// optimize alpha? 0-no 1-yes
				x.setItem(j++, 0, im.yaw);
			if(im.optimizePitch != 0)		// optimize pitch? 0-no 1-yes
				x.setItem(j++, 0, im.pitch);
			if(im.optimizeRoll != 0)		// optimize gamma? 0-no 1-yes
				x.setItem(j++, 0, im.roll);
			if(im.optimizeHfov != 0)		// optimize hfov? 0-no 1-yes
				x.setItem(j++, 0, im.hfov);
			if(im.optimizeA != 0)			// optimize a? 0-no 1-yes
				x.setItem(j++, 0, im.radial_params[0][3] * C_FACTOR);
			if(im.optimizeB != 0)			// optimize b? 0-no 1-yes
				x.setItem(j++, 0, im.radial_params[0][2] * C_FACTOR);
			if(im.optimizeC != 0)			// optimize c? 0-no 1-yes
				x.setItem(j++, 0, im.radial_params[0][1] * C_FACTOR);
			if(im.optimizeD != 0)			// optimize d? 0-no 1-yes
				x.setItem(j++, 0, im.horizontal_params[0]);
			if(im.optimizeE != 0)			// optimize e? 0-no 1-yes
				x.setItem(j++, 0, im.vertical_params[0]);
			if(im.optimizeShearX != 0)	// optimize shear_x? 0-no 1-yes
				x.setItem(j++, 0, im.shear_x);
			if(im.optimizeShearY != 0)	// optimize shear_y? 0-no 1-yes
				x.setItem(j++, 0, im.shear_y);
		}
		if (j != numParam)
			throw new RuntimeException("Invalid value for numParam");
		
		LMDif.lmdif(this, x, fvec, 0.05);
		LMDif.lmdif(this, x, fvec, 0.0001);
	}

	ArrayList<PanoPairList> panoChain;
	ImageData pano; // Panoramic Image decription

	void calculateExtents(ImageData dest) {
		Point2D.Double p = new Point2D.Double();
		Point2D.Double min = new Point2D.Double();
		Point2D.Double max = new Point2D.Double();
		double destX = dest.width / 2.0 - 0.5;
		double destY = dest.height / 2.0 - 0.5;
		boolean isFirst = true;
		
		for (ImageData image : images.values()) {
			double srcX = image.width / 2.0 - 0.5;
			double srcY = image.height / 2.0 - 0.5;
			
			min.x = min.y = Double.POSITIVE_INFINITY;
			max.x = max.y = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < image.width; i++) {
				p.x = i - srcX;
				p.y = 0 - srcY;
				PanoAdjust.makeInvParams(p, image, dest, 0);
				p.x += destX;
				p.y += destY;
				if (min.x > p.x) min.x = p.x; 
				if (min.y > p.y) min.y = p.y; 
				if (max.x < p.x) max.x = p.x; 
				if (max.y < p.y) max.y = p.y; 

				p.x = i - srcX;
				p.y = image.height - 1 - srcY;
				PanoAdjust.makeInvParams(p, image, dest, 0);
				p.x += destX;
				p.y += destY;
				if (min.x > p.x) min.x = p.x; 
				if (min.y > p.y) min.y = p.y; 
				if (max.x < p.x) max.x = p.x; 
				if (max.y < p.y) max.y = p.y; 
			}
				
			for (int j = 0; j < image.height; j++) {
				p.x = 0 - srcX;
				p.y = j - srcY;
				PanoAdjust.makeInvParams(p, image, dest, 0);
				p.x += destX;
				p.y += destY;
				if (min.x > p.x) min.x = p.x; 
				if (min.y > p.y) min.y = p.y; 
				if (max.x < p.x) max.x = p.x; 
				if (max.y < p.y) max.y = p.y; 

				p.x = image.width - 1 - srcX;
				p.y = j - srcY;
				PanoAdjust.makeInvParams(p, image, dest, 0);
				p.x += destX;
				p.y += destY;
				if (min.x > p.x) min.x = p.x; 
				if (min.y > p.y) min.y = p.y; 
				if (max.x < p.x) max.x = p.x; 
				if (max.y < p.y) max.y = p.y; 
			}
			
			image.extentInPano.x = min.x;
			image.extentInPano.y = min.y;
			image.extentInPano.width = max.x - min.x;
			image.extentInPano.height = max.y - min.y;
			
			if (isFirst) {
				dest.extentInPano.x = image.extentInPano.x;
				dest.extentInPano.y = image.extentInPano.y;
				dest.extentInPano.width = image.extentInPano.width;
				dest.extentInPano.height = image.extentInPano.height;
				isFirst = false;
			} else {
				Rectangle2D.union(image.extentInPano, dest.extentInPano, dest.extentInPano);
			}
		}
	}
	
	public Point2D.Double getFieldOfView() {
		ImageData dest = new ImageData();
		dest.hfov = 360.0;
		dest.width = 360;
		dest.height = 180;
		dest.format = ImageFormat.Equirectangular;
		calculateExtents(dest);
		return new Point2D.Double(dest.extentInPano.width, dest.extentInPano.height);
	}

	public void calcOptimalPanoWidth() {
		calculateExtents(pano);
		pano.width = (int) pano.extentInPano.width;
		pano.height = (int) pano.extentInPano.height;
	}

	static int panoCounter = 0;
	
	int safeGetPixel(BufferedImage bi, int atX, int atY) {
		if ((atX < 0) || (atX >= bi.getWidth()) ||
			(atY < 0) || (atY >= bi.getHeight()))
				return -1;
		return bi.getRGB(atX, atY) & 0x00ffffff;
	}
	
	void safeSetPixel(BufferedImage bi, int atX, int atY, int color) {
		if ((color < 0) ||
			(atX < 0) || (atX >= bi.getWidth()) ||
			(atY < 0) || (atY >= bi.getHeight()))
				return;
		bi.setRGB(atX, atY, color);
	}
	
	static final int colors[] = {
		0x00ff0000,
		0x0000ff00,
		0x000000ff,		
		0x0000ffff,		
		0x00ff00ff,		
		0x00ffff00,
		0x00ff9600
	};
	
	public void makePano() throws IOException {
		BufferedImage bi = new BufferedImage(pano.width, pano.height, BufferedImage.TYPE_INT_RGB);
		Point2D.Double p = new Point2D.Double();
		
		double panoX = pano.width / 2.0 - 0.5;
		double panoY = pano.height / 2.0 - 0.5;

		for (ImageData image : images.values()) {
			int x1 = (int) image.extentInPano.x;
			int x2 = (int) (image.extentInPano.width + image.extentInPano.x);
			int y1 = (int) image.extentInPano.y;
			int y2 = (int) (image.extentInPano.height + image.extentInPano.y);
			BufferedImage im = ImageIO.read(new File(image.name));

			double imageX = image.width / 2.0 - 0.5;
			double imageY = image.height / 2.0 - 0.5;
			
			for (int j = y1; j <= y2; j++)
				for (int i = x1; i <= x2; i++) {
					p.x = i - panoX; 
					p.y = j - panoY;
					makeParams(p, image, pano, 0);
					p.x += imageX;
					p.y += imageY;
					int color = safeGetPixel(im, (int) p.x, (int) p.y);
					safeSetPixel(bi, i, j, color);
				}
		}
		panoCounter++;
		String fouName = Const.tempDir + "/temp" + panoCounter + ".png"; 
		System.out.println("Output file is " + fouName);
		
		int pairCounter = 0;
		// Draw matching points
		for (PanoPairList l : panoChain) {
			ImageData sourceImage = images.get(l.sourceImage);
			ImageData targetImage = images.get(l.targetImage);
			double sourceX = sourceImage.width / 2.0 - 0.5;
			double sourceY = sourceImage.height / 2.0 - 0.5;
			double targetX = targetImage.width / 2.0 - 0.5;
			double targetY = targetImage.height / 2.0 - 0.5;
			for (PanoPair pp : l.items) {
				int color = colors[(pairCounter++) % colors.length];
				
				p.x = pp.sx - sourceX; 
				p.y = pp.sy - sourceY;
				makeInvParams(p, sourceImage, pano, 0);
				p.x += panoX;
				p.y += panoY;
				
				int atX = (int) p.x;
				int atY = (int) p.y;
				for (int i = 0; i < 5; i++) {
					safeSetPixel(bi, atX - 2 + i, atY, color);
					safeSetPixel(bi, atX, atY - 2 + i, color);
				}

				p.x = pp.tx - targetX; 
				p.y = pp.ty - targetY;
				makeInvParams(p, targetImage, pano, 0);
				p.x += panoX;
				p.y += panoY;
				
				atX = (int) p.x;
				atY = (int) p.y;
				for (int i = 0; i < 5; i++) {
					safeSetPixel(bi, atX - 2 + i, atY - 2 + i, color);
					safeSetPixel(bi, atX - 2 + i, atY + 2 - i, color);
				}
			}
		}		
		
		ImageIO.write(bi, "png", new File(fouName));
	}

	void checkBidirectionalTransform() {
		Point2D.Double p0 = new Point2D.Double();
		Point2D.Double p1 = new Point2D.Double();
		Point2D.Double p2 = new Point2D.Double();
		
		int imageCounter = 0;
		for (ImageData src : images.values()) {
			p0.x = 1.0;
			p0.y = 1.0;
			
			p1.x = p0.x;
			p1.y = p0.y;
			PanoAdjust.makeInvParams(p1, src, pano, 0);
			p2.x = p1.x;
			p2.y = p1.y;
			PanoAdjust.makeParams(p2, src, pano, 0);
			
			double s = MathUtil.hypot(p2.x - p0.x, p2.y - p0.y) / Math.sqrt(2.0);
			
			System.out.printf("im=%d d=%10.8f dx=%10.8f dy=%10.8f\n", imageCounter, s, p2.x - p0.x, p2.x - p0.x);
			imageCounter++;
		}
	}	
	
	static int dumpCount = 1;
	void dumpPanoPairsInFile() throws FileNotFoundException {
		String fname = Const.tempDir + "/dump" + (dumpCount++) + ".txt";
		
		// Dump the pano pairs in a file
		PrintStream out = new PrintStream(fname);
		for (String image : images.keySet()) {
			out.println(image);
		}
		for (PanoPairList item : panoChain) {
			for (PanoPair pp : item.items) {
				out.println(
						Double.toString(pp.discrepancy) + "\t" +
						Double.toString(pp.distance1) + "\t" +
						Double.toString(pp.distance2) + "\t" +
						item.sourceImage + "\t" +
						item.targetImage
						);
			}
		}
		System.out.println("Dump file is " + fname);
		out.close();
	}
		
	public void processOne(ArrayList<PanoPairList> panoChain) throws Exception {
		this.panoChain = panoChain;
		numControlPoints = 0;
		for (PanoPairList panoList : panoChain) {
			numControlPoints += panoList.items.size();
			panoList.source = addImage(panoList.sourceImage, panoList.sourceImageSizeX, panoList.sourceImageSizeY);
			panoList.target = addImage(panoList.targetImage, panoList.targetImageSizeX, panoList.targetImageSizeY);
		}
		numParam = countVariablesToOptimize() * images.size();
		pano = new ImageData();
		pano.hfov = 360.0;
		pano.width = 2000;
		pano.height = 1000;
		pano.format = ImageFormat.Equirectangular;
		
		RunLMOptimizer();
		
//		dumpPanoPairsInFile();
		checkBidirectionalTransform();
		
		Point2D.Double fov = getFieldOfView();
		System.out.println("Old FOV=" + pano.hfov + " FOV=" + fov);
		pano.hfov = fov.x;
		calcOptimalPanoWidth();
		calculateExtents(pano);
		System.out.println("Pano extent is " + pano.extentInPano);
		System.out.println("Width =" + pano.width);
		System.out.println("Height=" + pano.height);

		makePano();
	}
}
