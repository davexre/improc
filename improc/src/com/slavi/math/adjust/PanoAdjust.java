package com.slavi.math.adjust;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.slavi.math.MathUtil;
import com.slavi.math.adjust.LMDif.LMDifFcn;
import com.slavi.math.matrix.Matrix;

public class PanoAdjust implements LMDifFcn {
	public interface lmfunc {
		public void lmFunc();
	}

	double NORM_ANGLE(double x) {
		while (x > 180.0) {
			x -= 360.0;
		}
		while (x < -180.0) {
			x += 360.0;
		}
		return x;
	}
	
	private static double C_FACTOR = 100.0;

	public static class PTRect {
		long	top;
		long	bottom;
		long	left;
		long	right;
	}

	public static enum cPrefsCorrectionMode {
		Radial,
		Vertical,
		Deregister,
		Morph
	}
	
	// Preferences structure for tool correct
	public static class cPrefs {
		long magic = 20L;			//  File validity check, must be 20
		boolean radial = false;		//  Radial correction requested?
		double[][]	radial_params = new double[3][5];	//  3 colors x (4 coeffic. for 3rd order polys + correction radius)
		boolean vertical = false;	//  Vertical shift requested ?
		double[] vertical_params = new double[3];		//  3 colors x vertical shift value
		boolean horizontal = false;	//  horizontal tilt ( in screenpoints)
		double[] horizontal_params = new double[3];	//  3 colours x horizontal shift value
		boolean shear = false;		//  shear correction requested?
		double shear_x = 0.0;		//  horizontal shear values
		double shear_y = 0.0;		//  vertical shear values
		boolean resize = false;		//  scaling requested ?
		long width = 0;				//  new width
		long height = 0;			//  new height
		boolean luminance = false;	//  correct luminance variation?
		double[] lum_params = new double[3];		//  parameters for luminance corrections
		cPrefsCorrectionMode correction_mode = cPrefsCorrectionMode.Radial;		//  0 - radial correction;1 - vertical correction;2 - deregistration
		boolean cutFrame = false;	//  remove frame? 0 - no; 1 - yes
		int	fwidth = 100;
		int fheight = 100;
		int	frame = 0;
		boolean fourier = false;	//  Fourier filtering requested?
		int	fourier_mode = 0;		// default is _fremoveBlurr // _faddBlurr vs _fremoveBlurr
		String psf = "";			//  Point Spread Function, full path/fsspec to psd-file
		int	fourier_nf;				// default is _fremoveBlurr // Noise filtering: _nf_internal vs _nf_custom
		String nff = "";			//  noise filtered file: full path/fsspec to psd-file
		double filterfactor = 1.0;	//  Hunt factor
		double fourier_frame = 0.0;	//  To correct edge errors

	}
	
	public static enum ImageFormat {
		Rectilinear,
		Panorama,
		FisheyeCirc,
		FisheyeFF,
		Equirectangular,
		SphericalCP,
		ShpericalTP,
		Mirror,
		Orthographic,
		Cubic
	}
	
	public static class Image {
		long width = 0;
		long height = 0;
		long bytesPerLine = 0;
		long bitsPerPixel = 0;	// Must be 24 or 32
		long dataSize = 0;
		byte[][] data = null;
		long dataformat = 0;		// rgb, Lab etc
		ImageFormat format = ImageFormat.Rectilinear;	// Projection: rectilinear etc
		double hfov = 0;
		double yaw = 0;
		double pitch = 0;
		double roll = 0;
		cPrefs cP = new cPrefs();		// How to correct the image
		String name = "";
		PTRect selection = null;
	}
	
	// Indicate to optimizer which variables to optimize
	public static class optVars {
		int hfov;								//  optimize hfov? 0-no 1-yes , etc
		int yaw;				
		int pitch;				
		int roll;				
		int a;
		int b;
		int c;					
		int d;
		int e;
	}

	public static enum OptimizeType {
		r, x, y;
	}
	
	// Control Points to adjust images
	public static class ControlPoint {
		Image im0, im1; //int[]  num = new int[2];			// Indices of Images 
		double x0, x1;  //double[] x = new double[2];		// x - Coordinates 
		double y0, y1;  //double[] y = new double[2];		// y - Coordinates 
		OptimizeType type;				// What to optimize: 0-r, 1-x, 2-y
		
		public String toString() {
			return "x0=" + x0 + " y0=" + y0 + " x1=" + x1 + " y1=" + y1;
		}
	}
	
	public static class triangle {
		int[] vert = new int[3];	// Three vertices from list
		int nIm;		// number of image for texture mapping
	}
	
	public static class stitchBuffer {	// Used describe how images should be merged
		String				srcName;		// Buffer should be merged to image; 0 if not.
		String				destName;		// Converted image (ie pano) should be saved to buffer; 0 if not
		int				feather;		// Width of feather
		int				colcorrect;		// Should the images be color corrected?
		int				seam;			// Where to put the seam (see above)
	}
	
	public static class size_Prefs { // sPrefs	// Preferences structure for 'pref' dialog
		long			magic;					//  File validity check; must be 70
		int				displayPart;			// Display cropped/framed image ?
		int				saveFile;				// Save to tempfile? 0-no, 1-yes
		String		sFile;					// Full path to file (short name)
		int				launchApp;				// Open sFile ?
		String		lApp;					// the Application to launch
		int				interpolator;			// Which interpolator to use 
		double			gamma;					// Gamma correction value
		int				noAlpha;				// If new file is created: Don't save mask (Photoshop LE)
		int				optCreatePano;			// Optimizer creates panos? 0  no/ 1 yes
	}
	
	public static class CoordInfo {	// Real World 3D coordinates
		int  num;								// auxilliary index
		double[] x = new double[3];
		int[]  set = new int[3];
	}

	// Global data structure used by alignment optimization
	public static class AlignInfo {
		ArrayList<Image>im = new ArrayList<Image>();					// Array of Pointers to Image Structs
		ArrayList<optVars>opt = new ArrayList<optVars>();				// Mark variables to optimize
		//int				numIm;	 -> im.size()			// Number of images 
		ArrayList<ControlPoint>cpt = new ArrayList<ControlPoint>();		// List of Control points
		ArrayList<triangle>t = new ArrayList<triangle>();				// List of triangular faces
		int				nt;				// Number of triangular faces
		//int     			numPts;  -> cpt.size()				// Number of Control Points
		int				numParam;			// Number of parameters to optimize
		Image				pano;				// Panoramic Image decription
		stitchBuffer st = new stitchBuffer();				// Info on how to stitch the panorama
//		void				data;		// ????
		lmfunc				fcn;
		size_Prefs sP = new size_Prefs();	
		ArrayList<CoordInfo>cim = new ArrayList<CoordInfo>();			// Real World coordinates
	}

	AlignInfo g = new AlignInfo();
	
	// Set Makeparameters depending on adjustprefs, color and source image
	@SuppressWarnings("incomplete-switch")
	List<TransformationFunctions.TransformationFunction> makeParams(Image im, Image pn, int colorIndex) {
		double a = im.hfov * MathUtil.deg2rad;	// field of view in rad
		double b = pn.hfov * MathUtil.deg2rad;
		Matrix mt = MathUtil.makeAngles(- im.pitch * MathUtil.deg2rad, 0.0, - im.roll * MathUtil.deg2rad, false);
		
		double scale;
		double distance;
		if (pn.format == ImageFormat.Rectilinear) {
			// rectilinear panorama
			distance = (double) pn.width / (2.0 * Math.tan(b / 2.0));
			if (im.format == ImageFormat.Rectilinear) {
				// rectilinear image
				scale = ((double) pn.hfov / im.hfov) * 
						(a / (2.0 * Math.tan(a / 2.0))) * ((double)im.width / pn.width) * 2.0 * Math.tan(b/2.0) / b;
			} else {
				// pamoramic or fisheye image
				scale = ((double)pn.hfov / im.hfov) * ((double)im.width/ (double) pn.width)
				   * 2.0 * Math.tan(b/2.0) / b; 
			}
		} else {
			// equirectangular or panoramic or fisheye
			distance = ((double) pn.width) / b;
			if (im.format == ImageFormat.Rectilinear) {
				// rectilinear image
				scale = ((double)pn.hfov / im.hfov) * (a /(2.0 * Math.tan(a/2.0))) * ((double)im.width)/ ((double) pn.width);
			} else {
				// pamoramic or fisheye image
				scale = ((double)pn.hfov / im.hfov) * ((double)im.width)/ ((double) pn.width);
			}
		}
		double shearX = im.cP.shear_x / im.height;
		double shearY = im.cP.shear_y / im.width;
		double horizontal = im.cP.horizontal_params[colorIndex];
		double vertical = im.cP.vertical_params[colorIndex];
		double rotX	= distance * Math.PI;						// 180Ў in screenpoints
		double rotY = -im.yaw * distance * MathUtil.deg2rad; 	//    rotation angle in screenpoints
		double rad0 = im.cP.radial_params[colorIndex][0];
		double rad1 = im.cP.radial_params[colorIndex][1];
		double rad2 = im.cP.radial_params[colorIndex][2];
		double rad3 = im.cP.radial_params[colorIndex][3];
		double rad4 = im.cP.radial_params[colorIndex][4];
		
		if ((im.cP.correction_mode == cPrefsCorrectionMode.Radial) ||
			(im.cP.correction_mode == cPrefsCorrectionMode.Morph))
			rad4 = ( (double)( im.width < im.height ? im.width : im.height) ) / 2.0;
		else
			rad4 = ((double) im.height) / 2.0;
		
		ArrayList<TransformationFunctions.TransformationFunction>stack = 
			new ArrayList<TransformationFunctions.TransformationFunction>();
		
		switch (pn.format) {
		case Rectilinear:
			// Convert rectilinear to equirect
			stack.add(new TransformationFunctions.ErectRect(distance));
			break;
		case Panorama:
			// Convert panoramic to equirect
			stack.add(new TransformationFunctions.ErectPano(distance));
			break;
		case FisheyeCirc:
		case FisheyeFF:
			// Convert panoramic to sphere
			stack.add(new TransformationFunctions.ErectSphereTP(distance));
			break;
		}
		
		// Rotate equirect. image horizontally
		stack.add(new TransformationFunctions.RotateErect(rotX, rotY));
		// Convert spherical image to equirect.
		stack.add(new TransformationFunctions.SphereTPErect(distance));
		// Perspective Control spherical Image
		stack.add(new TransformationFunctions.PerspSphere(mt, distance));
		
		// Perform radial correction
		if (im.cP.horizontal)
			stack.add(new TransformationFunctions.Horiz(horizontal));
		if (im.cP.vertical)
			stack.add(new TransformationFunctions.Vert(vertical));
		if (im.cP.radial) {
			switch (im.cP.correction_mode) {
			case Radial:
			case Morph:
				stack.add(new TransformationFunctions.InvRadial(rad0, rad1, rad2, rad3, rad4));
				break;
			case Vertical:
				stack.add(new TransformationFunctions.InvVertical(rad0, rad1, rad2, rad3, rad4));
				break;
			case Deregister:
				break;
			}
		}
		
		switch (im.format) {
		case Rectilinear:
			// Convert rectilinear to spherical
			stack.add(new TransformationFunctions.RectSphereTP(distance));
			break;
		case Panorama:
			// Convert panoramic to spherical
			stack.add(new TransformationFunctions.PanoSphereTP(distance));
			break;
		case Equirectangular:
			// Convert PSphere to spherical
			stack.add(new TransformationFunctions.ErectSphereTP(distance));
			break;
		}
		
		// Scale image
		stack.add(new TransformationFunctions.Resize(scale, scale));
		
		if (im.cP.shear)
			stack.add(new TransformationFunctions.Shear(shearX, shearY));
		if (im.cP.horizontal)
			stack.add(new TransformationFunctions.Horiz(horizontal));
		if (im.cP.vertical)
			stack.add(new TransformationFunctions.Vert(vertical));
		if (im.cP.radial) {
			switch (im.cP.correction_mode) {
			case Radial:
			case Morph:
				stack.add(new TransformationFunctions.Radial(rad0, rad1, rad2, rad3, rad4, rad4));
				break;
			case Vertical:
				stack.add(new TransformationFunctions.Vertical(rad0, rad1, rad2, rad3, rad4));
				break;
			case Deregister:
				stack.add(new TransformationFunctions.Deregister(rad0, rad1, rad2, rad3, rad4));
				break;
			}
		}
		return stack;
	}

	// Set inverse Makeparameters depending on adjustprefs, color and source image
	@SuppressWarnings("incomplete-switch")
	List<TransformationFunctions.TransformationFunction> makeInvParams(Image im, Image pn, int colorIndex) {
		double a = im.hfov * MathUtil.deg2rad;	// field of view in rad
		double b = pn.hfov * MathUtil.deg2rad;
		Matrix mt = MathUtil.makeAngles(im.pitch * MathUtil.deg2rad, 0.0, im.roll * MathUtil.deg2rad, true);
		
		double scaleY;
		double distance;
		if (pn.format == ImageFormat.Rectilinear) {
			// rectilinear panorama
			distance = (double) pn.width / (2.0 * Math.tan(b / 2.0));
			if (im.format == ImageFormat.Rectilinear) {
				// rectilinear image
				scaleY = ((double) pn.hfov / im.hfov) * 
						(a / (2.0 * Math.tan(a / 2.0))) * ((double)im.width / pn.width) * 2.0 * Math.tan(b/2.0) / b;
			} else {
				// pamoramic or fisheye image
				scaleY = ((double)pn.hfov / im.hfov) * ((double)im.width/ (double) pn.width)
				   * 2.0 * Math.tan(b/2.0) / b; 
			}
		} else {
			// equirectangular or panoramic or fisheye
			distance = ((double) pn.width) / b;
			if (im.format == ImageFormat.Rectilinear) {
				// rectilinear image
				scaleY = ((double)pn.hfov / im.hfov) * (a /(2.0 * Math.tan(a/2.0))) * ((double)im.width)/ ((double) pn.width);
			} else {
				// pamoramic or fisheye image
				scaleY = ((double)pn.hfov / im.hfov) * ((double)im.width)/ ((double) pn.width);
			}
		}
		double scaleX = 1.0 / scaleY;
		scaleY = scaleX;
//		double shearX = im.cP.shear_x / im.height;
//		double shearY = im.cP.shear_y / im.width;
		double horizontal = -im.cP.horizontal_params[colorIndex];
		double vertical = -im.cP.vertical_params[colorIndex];
		double rotX	= distance * Math.PI;						// 180Ў in screenpoints
		double rotY = im.yaw * distance * MathUtil.deg2rad; 	//    rotation angle in screenpoints
		double rad0 = im.cP.radial_params[colorIndex][0];
		double rad1 = im.cP.radial_params[colorIndex][1];
		double rad2 = im.cP.radial_params[colorIndex][2];
		double rad3 = im.cP.radial_params[colorIndex][3];
		double rad4 = im.cP.radial_params[colorIndex][4];

		if ((im.cP.correction_mode == cPrefsCorrectionMode.Radial) ||
			(im.cP.correction_mode == cPrefsCorrectionMode.Morph))
			rad4 = ( (double)( im.width < im.height ? im.width : im.height) ) / 2.0;
		else
			rad4 = ((double) im.height) / 2.0;
			
//		System.out.printf("hor=%f vert=%f rad0=%f, rad1=%f rad2=%f rad3=%f rad4=%f rad5=\n", horizontal, vertical, rad0, rad1, rad2, rad3, rad4);
//		System.out.printf("rot0=%f rot1=%f dist=%f\n", rotX, rotY, distance);
//		mt.printM("MT=");
		
		ArrayList<TransformationFunctions.TransformationFunction>stack = 
			new ArrayList<TransformationFunctions.TransformationFunction>();

		// Perform radial correction
		if (im.cP.horizontal)
			stack.add(new TransformationFunctions.Horiz(horizontal));
		if (im.cP.vertical)
			stack.add(new TransformationFunctions.Vert(vertical));
		if (im.cP.radial) {
			switch (im.cP.correction_mode) {
			case Radial:
			case Morph:
				stack.add(new TransformationFunctions.InvRadial(rad0, rad1, rad2, rad3, rad4));
				break;
			case Vertical:
				stack.add(new TransformationFunctions.InvVertical(rad0, rad1, rad2, rad3, rad4));
				break;
			case Deregister:
				break;
			}
		}

		// Scale image
		stack.add(new TransformationFunctions.Resize(scaleX, scaleY));

		switch (im.format) {
		case Rectilinear:
			// Convert rectilinear to spherical
			stack.add(new TransformationFunctions.SphereTPRect(distance));
			break;
		case Panorama:
			// Convert panoramic to spherical
			stack.add(new TransformationFunctions.SphereTPPano(distance));
			break;
		case Equirectangular:
			// Convert PSphere to spherical
			stack.add(new TransformationFunctions.SphereTPErect(distance));
			break;
		}
		
		// Perspective Control spherical Image
		stack.add(new TransformationFunctions.PerspSphere(mt, distance));
		// Convert spherical image to equirect.
		stack.add(new TransformationFunctions.ErectSphereTP(distance));
		// Rotate equirect. image horizontally
		stack.add(new TransformationFunctions.RotateErect(rotX, rotY));
		
		switch (pn.format) {
		case Rectilinear:
			// Convert rectilinear to equirect
			stack.add(new TransformationFunctions.RectErect(distance));
			break;
		case Panorama:
			// Convert panoramic to equirect
			stack.add(new TransformationFunctions.PanoErect(distance));
			break;
		case FisheyeCirc:
		case FisheyeFF:
			// Convert panoramic to sphere
			stack.add(new TransformationFunctions.SphereTPErect(distance));
			break;
		}

//		for (TransformationFunctions.TransformationFunction i : stack) {
//			System.out.println(i.getClass().getName());
//		}		
		return stack;
	}

	double distControlPoint(ControlPoint cp, boolean isSphere) {
		Image sph = new Image();
		sph.width = 360;
		sph.height = 180;
		sph.format = ImageFormat.Equirectangular;
		sph.hfov = 360.0;
		
		Point2D.Double src = new Point2D.Double();

		List<TransformationFunctions.TransformationFunction> stack = makeInvParams(cp.im0, sph, 0);
		src.x = cp.x0 - (double) cp.im0.width / 2.0 + 0.5;
		src.y = cp.y0 - (double) cp.im0.height / 2.0 + 0.5;
		Point2D.Double dest0 = new Point2D.Double();
		TransformationFunctions.execute_stack(stack, src, dest0);

		stack = makeInvParams(cp.im1, sph, 0);
		src.x = cp.x1 - (double) cp.im1.width / 2.0 + 0.5;
		src.y = cp.y1 - (double) cp.im1.height / 2.0 + 0.5;
		Point2D.Double dest1 = new Point2D.Double();
		TransformationFunctions.execute_stack(stack, src, dest1);

//		if (LMDif.showDetails) {
//			System.out.printf("IMYAW=%f\n", cp.im1.yaw);
//			System.out.println("src X1=" + src.x);
//			System.out.println("src Y1=" + src.y);
//			
//			System.out.println("destX0=" + dest0.x);
//			System.out.println("destY0=" + dest0.y);
//			System.out.println("destX1=" + dest1.x);
//			System.out.println("destY1=" + dest1.y);
//		}
		
		if (isSphere) {
			dest0.x = dest0.x * MathUtil.deg2rad; 
			dest0.y = dest0.y * MathUtil.deg2rad + Math.PI / 2.0;
			double b0x0 =   Math.sin(dest0.x) * Math.sin(dest0.y);
			double b0x1 =   Math.cos(dest0.y);
			double b0x2 = - Math.cos(dest0.x) * Math.sin(dest0.y);
	
			dest1.x = dest1.x * MathUtil.deg2rad; 
			dest1.y = dest1.y * MathUtil.deg2rad + Math.PI / 2.0;
			double b1x0 =   Math.sin(dest1.x) * Math.sin(dest1.y);
			double b1x1 =   Math.cos(dest1.y);
			double b1x2 = - Math.cos(dest1.x) * Math.sin(dest1.y);
			
			double scalarProduct = b0x0 * b1x0 + b0x1 * b1x1 + b0x2 * b1x2;
//			if (LMDif.showDetails) {
//				double r = Math.acos(scalarProduct) * 10000;
//				System.out.println("Panowidth=" + r);
//			}
			return Math.acos(scalarProduct) * g.pano.width / (2.0 * Math.PI);
		}
		// take care of wrapping and points at edge of panorama
		if (g.pano.hfov == 360.0) {
			double delta = Math.abs(dest0.x - dest1.x);
			if (delta > g.pano.width / 2.0) {
				if (dest0.x < dest1.x)
					dest0.x += g.pano.width;
				else
					dest1.x += g.pano.width;
			}
		}
		// What do we want to optimize?
		switch (cp.type) {
		case x:
			// x difference
			return (dest0.x - dest1.x) * (dest0.x - dest1.x);
		case y:
			// y-difference
			return (dest0.y - dest1.y) * (dest0.y - dest1.y);
		case r:
		default:
			// square of distance
			return
				(dest0.y - dest1.y) * (dest0.y - dest1.y) + 
				(dest0.x - dest1.x) * (dest0.x - dest1.x); 
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
	public void fcn(Matrix x, Matrix fvec) {
		int m = fvec.getSizeX();	// the number of functions.
//		int n = x.getSizeX();		// the number of variables. n must not exceed m.
		
		int j = 0;
		int k;
		// Set global preferences structures using LM-params
		for (int i = 0; i < g.im.size(); i++) {
			Image im = g.im.get(i);
			optVars opt = g.opt.get(i);
			
			if ((k = g.opt.get(i).yaw) > 0) {
				if (k == 1) {
//					System.out.printf("YAW=%f\n", im.yaw);
//					System.out.printf("YAWNEW=%f\n", x.getItem(j, 0));
					im.yaw = x.getItem(j++, 0);
					im.yaw = NORM_ANGLE(im.yaw);
				} else {
					im.yaw = g.im.get(k - 2).yaw;
				}
			}
			if ((k = opt.pitch) > 0) {
				if (k == 1) {
					im.pitch = x.getItem(j++, 0);
					im.pitch = NORM_ANGLE(im.pitch);
				} else {
					im.pitch = g.im.get(k - 2).pitch;
				}
			}
			if ((k = opt.roll) > 0) {
				if (k == 1) {
					im.roll = x.getItem(j++, 0);
					NORM_ANGLE(im.roll);
				} else {
					im.roll = g.im.get(k - 2).roll;
				}
			}
			if ((k = opt.hfov) > 0) {
				if (k == 1) {
					im.hfov = x.getItem(j++, 0);
					if (im.hfov < 0.0)
						im.hfov = -im.hfov;
				} else {
					im.hfov = g.im.get(k - 2).hfov;
				}
			}
			if ((k = opt.a) > 0) {
				if (k == 1) {
					im.cP.radial_params[0][3] = x.getItem(j++, 0) / C_FACTOR;
				} else {
					im.cP.radial_params[0][3] = g.im.get(k - 2).cP.radial_params[0][3];
				}
			}
			if ((k = opt.b) > 0) {
				if (k == 1) {
					im.cP.radial_params[0][2] = x.getItem(j++, 0) / C_FACTOR;
				} else {
					im.cP.radial_params[0][2] = g.im.get(k - 2).cP.radial_params[0][2];
				}
			}
			if ((k = opt.c) > 0) {
				if (k == 1) {
					im.cP.radial_params[0][1] = x.getItem(j++, 0) / C_FACTOR;
				} else {
					im.cP.radial_params[0][1] = g.im.get(k - 2).cP.radial_params[0][1];
				}
			}
			if ((k = opt.d) > 0) {
				if (k == 1) {
					im.cP.horizontal_params[0] = x.getItem(j++, 0);
				} else {
					im.cP.horizontal_params[0] = g.im.get(k - 2).cP.horizontal_params[0];
				}
			}
			if ((k = opt.e) > 0) {
				if (k == 1) {
					im.cP.vertical_params[0] = x.getItem(j++, 0);
				} else {
					im.cP.vertical_params[0] = g.im.get(k - 2).cP.vertical_params[0];
				}
			}
		}
		
		// Calculate distances
		double avg = 0.0;
		for (int i = 0; i < g.cpt.size(); i++) {
			ControlPoint cp = g.cpt.get(i);
			double d = distControlPoint(cp, cp.type == OptimizeType.r);
//			if (LMDif.showDetails)
//				System.out.printf("!!%12.8f\n", d);
			fvec.setItem(i, 0, d);
			avg += d;

//			if (LMDif.showDetails) {
//				System.out.println("CP 355=" + cp);
//				System.out.println("D=" + d);
//			}
		}
		
		avg /= g.cpt.size();
		for (int i = g.cpt.size(); i < m; i++)
			fvec.setItem(i, 0, avg);
//		if (LMDif.showDetails)
//			fvec.printM("FVEC");
	}
	
	void RunLMOptimizer() throws Exception {
		// Initialize optimization params
		int n = g.numParam;
		int m = g.cpt.size();
		Matrix x = new Matrix(n, 1);
		Matrix fvec = new Matrix(m, 1);
		
		// Set LM params using global preferences structure
		// Change to cover range 0....1 (roughly)
		int j = 0; // Counter for optimization parameters
		for (int i = 0; i < g.im.size(); i++) {
			Image im = g.im.get(i);
			optVars opt = g.opt.get(i);
			
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
		if (j != g.numParam)
			throw new RuntimeException("Invalid value for numParam");
		
		LMDif.lmdif(this, x, fvec);
	}

	private void readImageLine(PTOTokenizer st, Image im, optVars opt, CoordInfo ci) {
		String t;
		while ((t = st.nextToken()) != null) {
			if (t.length() <= 0)
				continue;
			if ("w".equals(t)) {
				im.width = Long.parseLong(st.nextToken());
			} else if ("h".equals(t)) {
				im.height = Long.parseLong(st.nextToken());
			} else if ("v=".equals(t)) {
				opt.hfov = Integer.parseInt(st.nextToken());
			} else if ("v".equals(t)) {
				im.hfov = Double.parseDouble(st.nextToken());
			} else if ("a=".equals(t)) {
				opt.a = Integer.parseInt(st.nextToken()) + 2;
				im.cP.radial = true;
			} else if ("a".equals(t)) {
				im.cP.radial_params[0][3] = Double.parseDouble(st.nextToken());
				im.cP.radial = true;
			} else if ("b=".equals(t)) {
				opt.b = Integer.parseInt(st.nextToken()) + 2;
				im.cP.radial = true;
			} else if ("b".equals(t)) {
				im.cP.radial_params[0][2] = Double.parseDouble(st.nextToken());
				im.cP.radial = true;
			} else if ("c=".equals(t)) {
				opt.c = Integer.parseInt(st.nextToken()) + 2;
				im.cP.radial = true;
			} else if ("c".equals(t)) {
				im.cP.radial_params[0][1] = Double.parseDouble(st.nextToken());
				im.cP.radial = true;
			} else if ("f".equals(t)) {
				int k = Integer.parseInt(st.nextToken());
				switch (k) {
				case 0: im.format = ImageFormat.Rectilinear; break;
				case 1: 
					im.format = ImageFormat.Panorama;
					im.cP.correction_mode = cPrefsCorrectionMode.Vertical; // im->cP.correction_mode |= correction_mode_vertical;
					break;
				case 2: im.format = ImageFormat.FisheyeCirc; break;
				case 3: im.format = ImageFormat.FisheyeFF; break;
				case 4: 
					im.format = ImageFormat.Equirectangular; 
					im.cP.correction_mode = cPrefsCorrectionMode.Vertical; // im->cP.correction_mode |= correction_mode_vertical;
					break;
				case 8: im.format = ImageFormat.Orthographic; break;
				default:
					throw new RuntimeException("invalid imgae format");
				}
			} else if ("o".equals(t)) {
				im.cP.correction_mode = cPrefsCorrectionMode.Morph; // im->cP.correction_mode |=  correction_mode_morph;
			} else if ("y=".equals(t)) {
				opt.yaw = Integer.parseInt(st.nextToken()) + 2;
			} else if ("y".equals(t)) {
				im.yaw = Double.parseDouble(st.nextToken());
			} else if ("p=".equals(t)) {
				opt.pitch = Integer.parseInt(st.nextToken()) + 2;
			} else if ("p".equals(t)) {
				im.pitch = Double.parseDouble(st.nextToken());
			} else if ("r=".equals(t)) {
				opt.roll = Integer.parseInt(st.nextToken()) + 2;
			} else if ("r".equals(t)) {
				im.roll = Double.parseDouble(st.nextToken());
			} else if ("d=".equals(t)) {
				opt.d = Integer.parseInt(st.nextToken()) + 2;
			} else if ("d".equals(t)) {
				im.cP.horizontal_params[0] = Double.parseDouble(st.nextToken());
				im.cP.horizontal = true;
			} else if ("e=".equals(t)) {
				opt.e = Integer.parseInt(st.nextToken()) + 2;
			} else if ("e".equals(t)) {
				im.cP.vertical_params[0] = Double.parseDouble(st.nextToken());
				im.cP.vertical = true;
			} else if ("n".equals(t)) {
				im.name = st.nextToken();
			} else if ("m".equals(t)) {
				im.cP.frame = Integer.parseInt(st.nextToken());
				im.cP.cutFrame = true;
			} else if ("mx".equals(t)) {
				im.cP.fwidth = Integer.parseInt(st.nextToken());
				im.cP.cutFrame = true;
			} else if ("my".equals(t)) {
				im.cP.fheight = Integer.parseInt(st.nextToken());
				im.cP.cutFrame = true;
			} else if ("X".equals(t)) {
				ci.x[0] = Double.parseDouble(st.nextToken());
			} else if ("Y".equals(t)) {
				ci.x[1] = Double.parseDouble(st.nextToken());
			} else if ("Z".equals(t)) {
				ci.x[2] = Double.parseDouble(st.nextToken());
			} else if ("S".equals(t)) {
				im.selection.left = Integer.parseInt(st.nextToken());
				im.selection.right = Integer.parseInt(st.nextToken());
				im.selection.top = Integer.parseInt(st.nextToken());
				im.selection.bottom = Integer.parseInt(st.nextToken());
			} else if ("C".equals(t)) {
				im.selection.left = Integer.parseInt(st.nextToken());
				im.selection.right = Integer.parseInt(st.nextToken());
				im.selection.top = Integer.parseInt(st.nextToken());
				im.selection.bottom = Integer.parseInt(st.nextToken());
				im.cP.cutFrame = true;
			} else if ("+".equals(t)) {
				g.st.srcName = st.nextToken();
			} else if ("-".equals(t)) {
				g.st.destName = st.nextToken();
			} else if ("u".equals(t)) {
				g.st.feather = Integer.parseInt(st.nextToken());
			} else if ("s".equals(t)) {
				g.st.seam = Integer.parseInt(st.nextToken());
				// if(sBuf.seam != _dest)
				//	sBuf.seam = _middle;
			} else if ("k".equals(t)) {
				int i = Integer.parseInt(st.nextToken());
				g.st.colcorrect |= i & 3; 
				g.st.colcorrect += (i + 1) * 4; 
			}
		}
		
		// Set 4th polynomial parameter
		im.cP.radial_params[0][0] = 1.0 - (im.cP.radial_params[0][3] + im.cP.radial_params[0][2] + im.cP.radial_params[0][1]);
		for (int col = 1; col < 3; col++) {
			for (int i = 0; i < 4; i++)
				im.cP.radial_params[col][i]	= im.cP.radial_params[0][i];
			im.cP.vertical_params[col] = im.cP.vertical_params[0];
			im.cP.horizontal_params[col] = im.cP.horizontal_params[0];
		}
		// Restrict radial correction to monotonous interval
		double a[] = new double[4];
		for (int i = 0; i < 3; i++) {
			for (int k = 0; k < 4; k++) {
				a[k] = 0.0;//1.0e-10;
				if (im.cP.radial_params[i][k] != 0.0 ) {
					a[k] = (k + 1) * im.cP.radial_params[i][k];
				}
			}
			im.cP.radial_params[i][4] = smallestRoot(a);
		}
	}
	
	public void readPanoScript(BufferedReader fin) throws IOException {
		int variablesToOptimize = 0;
		while (fin.ready()) {
			String s = fin.readLine();
			if (s == null || s.length() <= 0)
				continue;
			PTOTokenizer st = new PTOTokenizer(s);
			st.nextToken();
			
			char c = s.charAt(0);
			switch (c) {
			case 'i': {
				// Image description
				Image im = new Image();
				optVars opt = new optVars();
				CoordInfo ci = new CoordInfo();
				readImageLine(st, im, opt, ci);
				g.im.add(im);
				g.opt.add(opt);
				g.cim.add(ci);
				break;
			}
			case 't': {
				// Triangle
				triangle tr = new triangle();
				String t;
				int i = 0;
				while ((t = st.nextToken()) != null) {
					if (t.length() <= 0)
						continue;
					if ("i".equals(t)) {
						tr.nIm = Integer.parseInt(st.nextToken());
					} else {
						if (i < 3) {
							tr.vert[i++] = Integer.parseInt(st.nextToken());
						} else {
							st.nextToken();
						}
					}
				}
				g.t.add(tr);
				break;
			}
			case 'c': {
				// Control Point
				ControlPoint cp = new ControlPoint();
				String t;
				while ((t = st.nextToken()) != null) {
					if (t.length() <= 0)
						continue;
					if ("t".equals(t)) {
						int type = Integer.parseInt(st.nextToken());
						switch (type) {
						case 1: cp.type = OptimizeType.x; break;
						case 2: cp.type = OptimizeType.y; break;
						case 0: 
						default: cp.type = OptimizeType.r; break;
						}
					} else if ("n".equals(t)) {
						int n = Integer.parseInt(st.nextToken());
						cp.im0 = g.im.get(n);
					} else if ("N".equals(t)) {
						int n = Integer.parseInt(st.nextToken());
						cp.im1 = g.im.get(n);
					} else if ("x".equals(t)) {
						cp.x0 = Double.parseDouble(st.nextToken());
					} else if ("X".equals(t)) {
						cp.x1 = Double.parseDouble(st.nextToken());
					} else if ("y".equals(t)) {
						cp.y0 = Double.parseDouble(st.nextToken());
					} else if ("Y".equals(t)) {
						cp.y1 = Double.parseDouble(st.nextToken());
					} else if ("i".equals(t)) {
						int n = Integer.parseInt(st.nextToken());
						cp.im1 = cp.im0 = g.im.get(n);
					}
				}
				g.cpt.add(cp);
				break;
			}
			case 'm': {
				// Mode description
				String t;
				while ((t = st.nextToken()) != null) {
					if (t.length() <= 0)
						continue;
					if ("g".equals(t)) {
						g.sP.gamma = Double.parseDouble(st.nextToken());
					} else if ("i".equals(t)) {
						g.sP.interpolator = Integer.parseInt(st.nextToken());
					} else if ("p".equals(t)) {
						g.sP.optCreatePano = Integer.parseInt(st.nextToken());
					}
				}
				break;
			}
			case 'v': {
				// Variables to optimize
				String t;
				while ((t = st.nextToken()) != null) {
					if (t.length() <= 0)
						continue;
					int n = Integer.parseInt(st.nextToken());
					if ("y".equals(t)) {
						g.opt.get(n).yaw = 1;
						variablesToOptimize++;
					} else if ("p".equals(t)) {
						g.opt.get(n).pitch = 1;
						variablesToOptimize++;
					} else if ("r".equals(t)) {
						g.opt.get(n).roll = 1;
						variablesToOptimize++;
					} else if ("v".equals(t)) {
						g.opt.get(n).hfov = 1;
						variablesToOptimize++;
					} else if ("a".equals(t)) {
						g.opt.get(n).a = 1;
						variablesToOptimize++;
					} else if ("b".equals(t)) {
						g.opt.get(n).b = 1;
						variablesToOptimize++;
					} else if ("c".equals(t)) {
						g.opt.get(n).c = 1;
						variablesToOptimize++;
					} else if ("d".equals(t)) {
						g.opt.get(n).d = 1;
						variablesToOptimize++;
					} else if ("e".equals(t)) {
						g.opt.get(n).e = 1;
						variablesToOptimize++;
					} else if ("X".equals(t)) {
						g.cim.get(n).set[0] = 0;
						variablesToOptimize++;
					} else if ("Y".equals(t)) {
						g.cim.get(n).set[1] = 0;
						variablesToOptimize++;
					} else if ("Z".equals(t)) {
						g.cim.get(n).set[2] = 0;
						variablesToOptimize++;
					}
				}
				break;
			}
			case 'p':
				// panorama
				g.pano = new Image();
				optVars opt = new optVars();
				CoordInfo ci = new CoordInfo();
				readImageLine(st, g.pano, opt, ci);
				if (g.pano.format == ImageFormat.FisheyeCirc)
					g.pano.format = ImageFormat.Equirectangular;
				break;
			case '*':
				// End of script-data
			default:
				break;
			}
		}
				
		// Set up Panorama description
		
		if ((g.pano.width == 0) && (g.im.get(0).hfov != 0.0)) {
			// Set default for panorama width based on first image
			g.pano.width = (int)(((double)g.pano.hfov / g.im.get(0).hfov ) * g.im.get(0).width);
			g.pano.width /= 10; 
			g.pano.width *= 10; // Round to multiple of 10
		}
		
		if (g.pano.height == 0)
			g.pano.height = g.pano.width / 2;

		// Set up global information structure
		g.numParam 	= variablesToOptimize;
		// Set initial values for linked variables
		for (int i = 0; i < g.im.size(); i++){
			Image im = g.im.get(i);
			int k = g.opt.get(i).yaw - 2;
			if( k >= 0 ) im.yaw = g.im.get(k).yaw;

			k = g.opt.get(i).pitch - 2;
			if( k >= 0 ) im.pitch = g.im.get(k).pitch;

			k = g.opt.get(i).roll - 2;
			if( k >= 0 ) im.roll = g.im.get(k).roll;

			k = g.opt.get(i).hfov - 2;
			if( k >= 0 ) im.hfov = g.im.get(k).hfov;

			k = g.opt.get(i).a - 2;
			if( k >= 0 ) im.cP.radial_params[0][3] = g.im.get(k).cP.radial_params[0][3];

			k = g.opt.get(i).b - 2;
			if( k >= 0 ) im.cP.radial_params[0][2] = g.im.get(k).cP.radial_params[0][2];

			k = g.opt.get(i).c - 2;
			if( k >= 0 ) im.cP.radial_params[0][1] = g.im.get(k).cP.radial_params[0][1];

			k = g.opt.get(i).d - 2;
			if( k >= 0 ) im.cP.horizontal_params[0] = g.im.get(k).cP.horizontal_params[0];

			k = g.opt.get(i).e - 2;
			if( k >= 0 ) im.cP.vertical_params[0] = g.im.get(k).cP.vertical_params[0];

			im.cP.radial_params[0][0] = 1.0 - (im.cP.radial_params[0][3]
												+ im.cP.radial_params[0][2]
												+ im.cP.radial_params[0][1] ) ;
			SetEquColor(im.cP);
		}
		
		System.out.println("!!!!!!!!!!!!!!!!!! n=" + g.numParam + " !!! m=" + g.cpt.size());
	}
	
	void SetEquColor( cPrefs cP )
	{
		for(int col = 1; col < 3; col++) {
			for(int i = 0; i < 4; i++)
				cP.radial_params[col][i] = cP.radial_params[0][i];
			cP.vertical_params[col] = cP.vertical_params[0];
			cP.horizontal_params[col] = cP.horizontal_params[0];
		}
	}
	
	public void writePanoScript(PrintWriter fou) {
		int format = 0;
		switch (g.pano.format) {
		case Rectilinear:		format = 0; break;
		case Panorama:			format = 1; break;
		case FisheyeCirc:		format = 2; break;
		case FisheyeFF:			format = 0; break;
		case Equirectangular:	format = 0; break;
		case SphericalCP:		format = 0; break;
		case ShpericalTP:		format = 0; break;
		case Mirror:			format = 0; break;
		case Orthographic:		format = 0; break;
		case Cubic:				format = 0; break;
		}
		fou.println("p " + Integer.toString(format) + " w" + Long.toString(g.pano.width) + " h" + Long.toString(g.pano.height) +
				" v" + Double.toString(g.pano.hfov) + " n\"" + g.pano.name + "\"");
		
		
		for (int i = 0; i < g.im.size(); i++) {
			Image im = g.im.get(i);
			switch (im.format) {
			case Rectilinear:		format = 0; break;
			case Panorama:			format = 1; break;
			case FisheyeCirc:		format = 2; break;
			case FisheyeFF:			format = 3; break;
			case Equirectangular:	format = 4; break;
			case SphericalCP:		format = 0; break;
			case ShpericalTP:		format = 0; break;
			case Mirror:			format = 0; break;
			case Orthographic:		format = 8; break;
			case Cubic:				format = 0; break;
			}
			
			int optHfov, opta, optb, optc;
			optVars opt = g.opt.get(i);
			if (opt.hfov == 1 || (opt.hfov > 1 &&  g.opt.get(opt.hfov-2).hfov == 1))
				optHfov = 1;
			else
				optHfov = 0;

			if (opt.a == 1 || (opt.a > 1 &&  g.opt.get(opt.a-2).a == 1 ))
				opta = 1;
			else
				opta = 0;
							
			if (opt.b == 1 || (opt.b > 1 &&  g.opt.get(opt.b-2).b == 1 ))
				optb = 1;
			else
				optb = 0;
							
			if (opt.c == 1 || (opt.c > 1 &&  g.opt.get(opt.c-2).c == 1 ))
				optc = 1;
			else
				optc = 0;

			fou.println("i w" + Long.toString(im.width) + " h" + Long.toString(im.height) + " f" + Integer.toString(format) +
					" a" + Double.toString(im.cP.radial_params[0][3]) + 
					" b" + Double.toString(im.cP.radial_params[0][2]) + 
					" c" + Double.toString(im.cP.radial_params[0][1]) + 
					" d" + Double.toString(im.cP.horizontal_params[0]) + 
					" e" + Double.toString(im.cP.vertical_params[0]) +
					" g0" +
					" p" + Double.toString(im.pitch) +
					" r" + Double.toString(im.roll) +
					" t0" +
					" v" + Double.toString(im.hfov) +
					" y" + Double.toString(im.yaw) +
					" u" + Double.toString(g.st.feather) +
					" n\"" + im.name + "\"");
		}
	}
	
	public double cubeRoot(double x) {
		if (x == 0.0)
			return 0.0;
		else if( x > 0.0 )
			return Math.pow(x, 1.0/3.0);
		else
			return -Math.pow(-x, 1.0/3.0);
	}
	
	int squareZero(double a[], double root[]){
		if (a[2] == 0.0) { 
			// linear equation
			if (a[1] == 0.0) { 
				// constant
				if (a[0] == 0.0) {
					root[0] = 0.0;
					return 1;
				}
				return 0;
			}
			root[0] = -a[0] / a[1];
			return 1; 
		}
		if (4.0 * a[2] * a[0] > a[1] * a[1]) {
			return 0; 
		}
		root[0] = (- a[1] + Math.sqrt( a[1] * a[1] - 4.0 * a[2] * a[0] )) / (2.0 * a[2]);
		root[1] = (- a[1] - Math.sqrt( a[1] * a[1] - 4.0 * a[2] * a[0] )) / (2.0 * a[2]);
		return 2;
	}
	
	int cubeZero(double a[], double root[]){
		if (a[3] == 0.0) { 
			// second order polynomial
			return squareZero(a, root);
		}
		double p = ((-1.0/3.0) * (a[2]/a[3]) * (a[2]/a[3]) + a[1]/a[3]) / 3.0;
		double q = ((2.0/27.0) * (a[2]/a[3]) * (a[2]/a[3]) * (a[2]/a[3]) - 
					(1.0/3.0) * (a[2]/a[3]) * (a[1]/a[3]) + a[0]/a[3]) / 2.0;
		
		if (q*q + p*p*p >= 0.0) {
			root[0] = cubeRoot(-q + Math.sqrt(q*q + p*p*p)) + cubeRoot(-q - Math.sqrt(q*q + p*p*p)) - a[2] / (3.0 * a[3]); 
			return 1;
		}
		double phi = Math.acos( -q / Math.sqrt(-p*p*p) );
		root[0] =  2.0 * Math.sqrt(-p) * Math.cos(phi/3.0) - a[2] / (3.0 * a[3]); 
		root[1] = -2.0 * Math.sqrt(-p) * Math.cos(phi/3.0 + Math.PI/3.0) - a[2] / (3.0 * a[3]); 
		root[2] = -2.0 * Math.sqrt(-p) * Math.cos(phi/3.0 - Math.PI/3.0) - a[2] / (3.0 * a[3]); 
		return 3;
	}
	
	double smallestRoot(double p[]) {
		double root[] = new double[3];
		double sroot = 1000.0;
		
		int n = cubeZero(p, root);
		for (int i = 0; i < n; i++) {
			if (root[i] > 0.0 && root[i] < sroot)
				sroot = root[i];
		}
		return sroot;
	}

	public static void main(String[] args) throws Exception {
		BufferedReader fin = new BufferedReader(new InputStreamReader(
				PanoAdjust.class.getResourceAsStream("optimizer.txt")));
		PanoAdjust panoAdjust = new PanoAdjust();
		panoAdjust.readPanoScript(fin);
		panoAdjust.RunLMOptimizer();
		fin.close();
		System.out.println("Done");
	}
}
