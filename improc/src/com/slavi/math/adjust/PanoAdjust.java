package com.slavi.math.adjust;

import java.awt.geom.Point2D;
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
		Image 				im[];				// Array of Pointers to Image Structs
		optVars				opt[];				// Mark variables to optimize
		int				numIm;				// Number of images 
		ControlPoint 			cpt[];				// List of Control points
		triangle			t;				// List of triangular faces
		int				nt;				// Number of triangular faces
		int     			numPts;				// Number of Control Points
		int				numParam;			// Number of parameters to optimize
		Image				pano;				// Panoramic Image decription
		stitchBuffer				st;				// Info on how to stitch the panorama
//		void				data;		// ????
		lmfunc				fcn;
		size_Prefs				sP;	
		CoordInfo			cim;				// Real World coordinates
	}

	AlignInfo g;
	
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
		src.x = cp.x0 - (double) cp.im0.width / 2.0 - 0.5;
		src.y = cp.y0 - (double) cp.im0.height / 2.0 - 0.5;
		Point2D.Double dest0 = new Point2D.Double();
		TransformationFunctions.execute_stack(stack, src, dest0);

		stack = makeInvParams(cp.im1, sph, 0);
		src.x = cp.x1 - (double) cp.im1.width / 2.0 - 0.5;
		src.y = cp.y1 - (double) cp.im1.height / 2.0 - 0.5;
		Point2D.Double dest1 = new Point2D.Double();
		TransformationFunctions.execute_stack(stack, src, dest1);

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
		for (int i = 0; i < g.numIm; i++) {
			if ((k = g.opt[i].yaw) > 0) {
				if (k == 1) {
					g.im[i].yaw = x.getItem(j++, 0);
					g.im[i].yaw = NORM_ANGLE(g.im[i].yaw);
				} else {
					g.im[i].yaw = g.im[k - 2].yaw;
				}
			}
			if ((k = g.opt[i].pitch) > 0) {
				if (k == 1) {
					g.im[i].pitch = x.getItem(j++, 0);
					g.im[i].pitch = NORM_ANGLE(g.im[i].pitch);
				} else {
					g.im[i].pitch = g.im[k - 2].pitch;
				}
			}
			if ((k = g.opt[i].roll) > 0) {
				if (k == 1) {
					g.im[i].roll = x.getItem(j++, 0);
					NORM_ANGLE(g.im[i].roll);
				} else {
					g.im[i].roll = g.im[k - 2].roll;
				}
			}
			if ((k = g.opt[i].hfov) > 0) {
				if (k == 1) {
					g.im[i].hfov = x.getItem(j++, 0);
					if (g.im[i].hfov < 0.0)
						g.im[i].hfov = -g.im[i].hfov;
				} else {
					g.im[i].hfov = g.im[k - 2].hfov;
				}
			}
			if ((k = g.opt[i].a) > 0) {
				if (k == 1) {
					g.im[i].cP.radial_params[0][3] = x.getItem(j++, 0) / C_FACTOR;
				} else {
					g.im[i].cP.radial_params[0][3] = g.im[k - 2].cP.radial_params[0][3];
				}
			}
			if ((k = g.opt[i].b) > 0) {
				if (k == 1) {
					g.im[i].cP.radial_params[0][2] = x.getItem(j++, 0) / C_FACTOR;
				} else {
					g.im[i].cP.radial_params[0][2] = g.im[k - 2].cP.radial_params[0][2];
				}
			}
			if ((k = g.opt[i].c) > 0) {
				if (k == 1) {
					g.im[i].cP.radial_params[0][1] = x.getItem(j++, 0)/ C_FACTOR;
				} else {
					g.im[i].cP.radial_params[0][1] = g.im[k - 2].cP.radial_params[0][1];
				}
			}
			if ((k = g.opt[i].d) > 0) {
				if (k == 1) {
					g.im[i].cP.horizontal_params[0] = x.getItem(j++, 0);
				} else {
					g.im[i].cP.horizontal_params[0] = g.im[k - 2].cP.horizontal_params[0];
				}
			}
			if ((k = g.opt[i].e) > 0) {
				if (k == 1) {
					g.im[i].cP.vertical_params[0] = x.getItem(j++, 0);
				} else {
					g.im[i].cP.vertical_params[0] = g.im[k - 2].cP.vertical_params[0];
				}
			}
		}
		
		// Calculate distances
		double avg = 0.0;
		for (int i = 0; i < g.numPts; i++) {
			ControlPoint cp = g.cpt[i];
			double d = distControlPoint(cp, cp.type == OptimizeType.r);
			fvec.setItem(i, 0, d);
			avg += d;
		}
		avg /= g.numPts;
		for (int i = g.numPts; i < m; i++)
			fvec.setItem(i, 0, avg);
	}
	
	void RunLMOptimizer() {
		// Initialize optimization params
		
	}
}
