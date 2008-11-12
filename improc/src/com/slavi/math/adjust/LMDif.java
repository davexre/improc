package com.slavi.math.adjust;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;

public class LMDif {
	
	// Actual parameters used by Xform functions for pano-creation
	public static class MakeParams {
		double 	scale[] = new double[2];	// scaling factors for resize;
		double 	shear[]  = new double[2];	// shear values
		double  rot[] = new double[2];		// horizontal rotation params
		Object	perspect[] = null;			// Parameters for perspective control functions
		double	rad[] = new double[6];		// coefficients for polynomial correction (0,...3) and source width/2 (4) and correction radius (5)	
		Matrix	mt = null;					// Matrix
		double  distance = 0;
		double	horizontal = 0;
		double	vertical = 0;
	}
	
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
		long 	magic;					//  File validity check, must be 20
		int 			radial;					//  Radial correction requested?
		double[][]	radial_params = new double[3][5];	//  3 colors x (4 coeffic. for 3rd order polys + correction radius)
		int 			vertical;				//  Vertical shift requested ?
		double[]		vertical_params = new double[3];		//  3 colors x vertical shift value
		int			horizontal;				//  horizontal tilt ( in screenpoints)
		double[]			horizontal_params = new double[3];	//  3 colours x horizontal shift value
		int			shear;					//  shear correction requested?
		double			shear_x;				//  horizontal shear values
		double			shear_y;				//  vertical shear values
		int 			resize;					//  scaling requested ?
		long			width;					//  new width
		long			height;					//  new height
		int			luminance;				//  correct luminance variation?
		double[]			lum_params = new double[3];			//  parameters for luminance corrections
		cPrefsCorrectionMode correction_mode;		//  0 - radial correction;1 - vertical correction;2 - deregistration
		int			cutFrame;				//  remove frame? 0 - no; 1 - yes
		int			fwidth;
		int 			fheight;
		int			frame;
		int			fourier;				//  Fourier filtering requested?
		int			fourier_mode;			//  _faddBlurr vs _fremoveBlurr
		String		psf;					//  Point Spread Function, full path/fsspec to psd-file
		int			fourier_nf;				//  Noise filtering: _nf_internal vs _nf_custom
		String		nff;					//  noise filtered file: full path/fsspec to psd-file
		double			filterfactor;			//  Hunt factor
		double			fourier_frame;			//  To correct edge errors

	}
	
	public static enum ImageFormat {
		Rectilinear,
		Panorama,
		FisheyeCirc,
		FisheyeFF,
		Equirectangular,
		SphericalCP,
		ShpericalTP,
		Miirror,
		Orthographic,
		Cubic
	}
	
	public static class Image {
		long width;
		long height;
		long bytesPerLine;
		long bitsPerPixel;	// Must be 24 or 32
		long dataSize; 
		byte[][] data;
		long dataformat;		// rgb, Lab etc
		ImageFormat format;			// Projection: rectilinear etc
		double hfov;
		double yaw;
		double pitch;
		double roll;
		cPrefs cP;				// How to correct the image
		String name;
		PTRect selection;
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

	// Control Points to adjust images
	public static class controlPoint {
		int[]  num = new int[2];							// Indices of Images 
		double[] x = new double[2];								// x - Coordinates 
		double[] y = new double[2];								// y - Coordinates 
		int  type;								// What to optimize: 0-r, 1-x, 2-y
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
	
	public interface lmfunc {
		public void lmFunc();
	}
	
	// Global data structure used by alignment optimization
	public static class AlignInfo {
		Image 				im[];				// Array of Pointers to Image Structs
		optVars				opt[];				// Mark variables to optimize
		int				numIm;				// Number of images 
		controlPoint 			cpt[];				// List of Control points
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

	MakeParams makeParams(Image im, Image pn, int colorIndex) {
		MakeParams mp = new MakeParams();
		double a = im.hfov * MathUtil.deg2rad;	// field of view in rad
		double b = pn.hfov * MathUtil.deg2rad;
		mp.mt = MathUtil.makeAngles(- im.pitch * MathUtil.deg2rad, 0.0, - im.roll * MathUtil.deg2rad, false);
		
		double scale;
		double distance;
		if (pn.format == ImageFormat.Rectilinear) {
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
		mp.scale[0] = mp.scale[1] = scale;
		mp.distance = distance;
		mp.shear[0] = im.cP.shear_x / im.height;
		mp.shear[1] = im.cP.shear_y / im.width;

		mp.rot[0]		= mp.distance * Math.PI;						// 180Ў in screenpoints
		mp.rot[1]		= -im.yaw * mp.distance * MathUtil.deg2rad; 	//    rotation angle in screenpoints
		mp.perspect[0] = mp.mt;
		mp.perspect[1] = mp.distance;

		for(int i = 0; i < 4; i++) {
			mp.rad[i] = im.cP.radial_params[colorIndex][i];
		}
		mp.rad[5] = im.cP.radial_params[colorIndex][4];
		
		if (im.cP.correction_mode == cPrefsCorrectionMode.Radial)
			mp.rad[4] = ( (double)( im.width < im.height ? im.width : im.height) ) / 2.0;
		else
			mp.rad[4] = ((double) im.height) / 2.0;
		
		mp.horizontal 	= im.cP.horizontal_params[colorIndex];
		mp.vertical 	= im.cP.vertical_params[colorIndex];
		
		int i = 0;
		switch (pn.format) {
		case Rectilinear:
			// Convert rectilinear to equirect
			break;

		case Panorama:
			// Convert panoramic to equirect
			break;
		
		case FisheyeCirc:
		case FisheyeFF:
			// Convert panoramic to sphere
			
			break;
		}
		
		
		
		return mp;
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
	void fcn(Matrix x, Matrix fvec) {
		int m = fvec.getSizeX();	// the number of functions.
		int n = x.getSizeX();		// the number of variables. n must not exceed m.
		
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
		
		double result = 0.0;
		for (int i = 0; i <= m; i++) {
//			if (g.cpt[i].)
		}
	}
	
	/* resolution of arithmetic */
	static final double MACHEP = 1.2e-16;  	
	static final double DBL_EPSILON = 1.0e-14;
	static final double gtol = DBL_EPSILON;
	static final double ftol = DBL_EPSILON;
	static final double xtol = DBL_EPSILON;
	static final double epsfcn = DBL_EPSILON * 10.0;//1.0e-15;
	
	/**
	 * subroutine fdjac2
	 * 
	 * this subroutine computes a forward-difference approximation
	 * to the m by n jacobian matrix associated with a specified
	 * problem of m functions in n variables.
	 */
	Matrix fdjac2(Matrix x, Matrix fvec) {
		int m = fvec.getSizeX();	// the number of functions.
		int n = x.getSizeX();		// the number of variables. n must not exceed m.
		double eps = epsfcn * epsfcn;
		
		Matrix fjac = new Matrix(m, n);
		Matrix wa = new Matrix(m, 1);
		for (int j = 0; j < n; j++) {
			double temp = x.getItem(j, 0);
			double h = eps * Math.abs(temp);
			if (h == 0.0)
				h = eps;
			x.setItem(j, 0, temp + h);
			fcn(x, wa);
			x.setItem(j, 0, temp);
			for (int i = 0; i < m; i++) {
				fjac.setItem(i, j, (wa.getItem(i, 0) - fvec.getItem(i, 0)) / h);
			}
		}
		return fjac;
	}
	
	/**
	 * subroutine lmdif
	 * 
	 * the purpose of lmdif is to minimize the sum of the squares of
	 * m nonlinear functions in n variables by a modification of
	 * the levenberg-marquardt algorithm. the user must provide a
	 * subroutine which calculates the functions. the jacobian is
	 * then calculated by a forward-difference approximation.
	 * 
	 * @param x		is an array of length n. on input x must contain
	 * 				an initial estimate of the solution vector. on output x
	 * 				contains the final estimate of the solution vector.
	 * @param fvec	is an output array of length m which contains
	 * 				the functions evaluated at the output x.
	 */
	void lmdif(Matrix x, Matrix fvec) {
		int m = fvec.getSizeX();	// the number of functions.
		int n = x.getSizeX();		// the number of variables. n must not exceed m.

		// evaluate the function at the starting point and calculate its norm.
		fcn(x, fvec);
		double fnorm = x.getForbeniusNorm();

		double xnorm = 0.0;
		double delta = 0.0;
		int iter = 0;
		while (true) {
			// calculate the jacobian matrix.
			Matrix fdjac = fdjac2(x, fvec);
			// compute the initial column norms
			Matrix diag = new Matrix(n, 1);
			Matrix wa2 = new Matrix(n, 1);
			for (int j = 0; j < n; j++) {
				double scale = 0.0;
				double sum = 1.0;
				for (int i = 0; i < m; i++) {
					double d = Math.abs(fdjac.getItem(i, j));
					if (d != 0.0) {
						if (scale < d) {
							double d1 = scale / d;
							sum = 1.0 + sum * d1 * d1;
							scale = d;
						} else {
							double d1 = d / scale;
							sum += d1 * d1;
						}
					}
					d = scale * Math.sqrt(sum);
					wa2.setItem(n, 0, d);
					if (d == 0.0)
						d = 1.0;
					diag.setItem(n, 0, d);
				}
			}
			
			// compute the qr factorization of the jacobian.
			Matrix q = new Matrix();
			Matrix tau = new Matrix();
			fdjac.qr(q, tau);
			Matrix wa1 = new Matrix(n, 1);
			for (int i = 0; i < n; i++)
				wa1.setItem(i, 0, fdjac.getItem(i, i));
			// on the first iteration and if mode is 1, scale according
			// to the norms of the columns of the initial jacobian.
			if (iter == 1) {
				// on the first iteration, calculate the norm of the scaled x
				// and initialize the step bound delta.
				Matrix wa3 = new Matrix(n, 1);
				for (int j = 0; j < n; j++) {
					wa3.setItem(j, 0, diag.getItem(j, 0) * x.getItem(j, 0));
				}
				xnorm = wa3.getForbeniusNorm();
				delta = (xnorm == 0.0 ? 1.0 : xnorm);
			}
				
			// form (q transpose)*fvec and store the first n components in qtf.
			Matrix wa4 = fvec.makeCopy();
			Matrix qtf = new Matrix(n, 1);
			for (int j = 0; j < n; j++) {
				double temp3 = q.getItem(j, j);
				if (temp3 != 0.0) {
					double sum = 0.0;
					for (int i = j; i < m; i++) {
						sum += q.getItem(j, i) * wa4.getItem(i, 0);
					}
					double temp = -sum / temp3;
					for (int i = j; i < m; i++) {
						wa4.setItem(i, 0, wa4.getItem(i, 0) + q.getItem(j, i) * temp);
					}
				}
				q.setItem(j, j, fdjac.getItem(j, j));
				qtf.setItem(j, 0, wa4.getItem(j, 0));
			}
			
			// compute the norm of the scaled gradient.
			double gnorm = 0.0;
			if (fnorm != 0.0) {
				for (int j = 0; j < n; j++) {
					if (wa2.getItem(j, 0) != 0.0) {
						double sum = 0.0;
						for (int i = 0; i < j; i++) {
							sum += fdjac.getItem(i, j) * (qtf.getItem(i, 0) / fnorm);
						}
						gnorm = Math.max(gnorm, Math.abs(sum / wa2.getItem(j, 0))); 
					}
				}
			}
			
			// test for convergence of the gradient norm.
			if (gnorm <= gtol)
				return; 
			
			// rescale if necessary.
			for (int j = 0; j < n; j++) {
				diag.setItem(j, 0, Math.max(diag.getItem(j, 0), wa2.getItem(j, 0)));
			}
			
			while (true) {
				// determine the levenberg-marquardt parameter.
				double par = lmpar(fdjac, diag, wa1, qtf, delta, wa2);
				// store the direction p and x + p. calculate the norm of p.
				Matrix wa3 = new Matrix(n, 1);
				for (int j = 0; j < n; j++) {
					wa1.setItem(j, 0, -wa1.getItem(j, 0));
					wa2.setItem(j, 0, x.getItem(j, 0) + wa1.getItem(j, 0));
					wa3.setItem(j, 0, diag.getItem(j, 0) * wa1.getItem(j, 0));
				}
				double pnorm = wa3.getForbeniusNorm();
				// on the first iteration, adjust the initial step bound.
				if (iter == 1) 
					delta = Math.min(delta, pnorm);
				// evaluate the function at x + p and calculate its norm.
				fcn(wa2, wa4);
				double fnorm1 = wa4.getForbeniusNorm();
				// compute the qr factorization of the jacobian.
				double actred = -1.0;
				if (p1 * fnorm1 < fnorm) {
					double temp = fnorm1 / fnorm;
					actred = 1.0 - temp * temp;
				}
				// compute the scaled predicted reduction and
				// the scaled directional derivative.
				for (int j = 0; j < n; j++) {
					wa3.setItem(j, 0, 0.0);
					double temp = wa1.getItem(j, 0);
					for (int i = 0; i <= j; i++) {
						wa3.setItem(i, 0, wa3.getItem(i, 0) + fdjac.getItem(i, j) * temp);
					}
				}
				double temp1 = wa3.getForbeniusNorm() / fnorm;
				double temp2 = (Math.sqrt(par) * pnorm) / fnorm;
				double prered = temp1 * temp1 + (temp2 * temp2) / 0.5;
				double dirder = -(temp1 * temp1 + temp2 * temp2);
				// compute the ratio of the actual to the predicted reduction.
				double ratio = 0.0;
				if (prered != 0.0)
					ratio = actred / prered;
				// update the step bound.
				if (ratio <= 0.25) {
					double temp;
					if (actred >= 0.0) {
						temp = 0.5;
					} else {
						temp = 0.5 * dirder / (dirder + 0.5 * actred);
					}
					if ( ((p1*fnorm1) >= fnorm) || (temp < p1) ) {
						temp = p1;
						delta = temp * Math.min(delta, pnorm / p1);
						par = par / temp;
					} else {
						if ((par == 0.0) || (ratio >= 0.75)) {
							delta = pnorm / 0.5;
							par = 0.5 * par;
						}
					}
				}
				
				// test for successful iteration.
				if (ratio >= 0.0001) {
					// successful iteration. update x, fvec, and their norms.
					for (int j = 0; j < n; j++) {
						x.setItem(j, 0, wa2.getItem(j, 0));
						wa2.setItem(j, 0, diag.getItem(j, 0) * x.getItem(j, 0));
					}
					for (int i = 0; i < m; i++) {
						fvec.setItem(i, 0, wa4.getItem(i, 0));
					}
					xnorm = wa2.getForbeniusNorm();
					fnorm = fnorm1;
					iter++;
				}
				// tests for convergence.
				if ( (Math.abs(actred) <= ftol) && (prered <= ftol) && (0.5 * ratio <= 1.0)) {
					// both actual and predicted relative reduction in the sum of squares are at most ftol.
					return; 
				}
				if (delta <= xtol * xnorm) {
					if ( (Math.abs(actred) <= ftol) && (prered <= ftol) && (0.5 * ratio <= 1.0)) {
						// conditions for info = 1 and info = 2 both hold.
						return;
					}							
					// relative error between two consecutive iterates is at most xtol.
					return;
				}
				// tests for termination and stringent tolerances.
				if ( (Math.abs(actred) <= MACHEP) && (prered <= MACHEP) && (0.5 * ratio <= 1.0)) {
					// ftol is too small. no further reduction in the sum of squares is possible.
					return;
				}
				if (delta <= MACHEP * xnorm) {
					// xtol is too small. no further improvement in the approximate solution x is possible.
					return;
				}
				if (gnorm <= MACHEP) {
					// gtol is too small. fvec is orthogonal to the columns of the jacobian to machine precision.
					return;
				}
				
				if (ratio >= 0.0001)
					break;
			}
			
			iter++;
		}
		
	}	
	
	static final double p1 = 0.1;
	static final double DWARF = 1.0e-38;
	/**
	 * subroutine lmpar
	 * 
	 * given an m by n matrix a, an n by n nonsingular diagonal
	 * matrix d, an m-vector b, and a positive number delta,
	 * the problem is to determine a value for the parameter
	 * par such that if x solves the system
	 * 
	 * ...
	 */
	public double lmpar(Matrix r, Matrix diag, Matrix x, Matrix qtb, double delta, Matrix sdiag) {
		double par = 0.0;
		int m = r.getSizeY();
		int n = r.getSizeX();
		
		if ((m < n) ||
			(qtb.getSizeX() != n) || (qtb.getSizeY() != 1) ||
			(diag.getSizeX() != 1) || (diag.getSizeY() != n)) {
			throw new IllegalArgumentException();
		}
		
		// compute and store in x the gauss-newton direction. if the
		// jacobian is rank-deficient, obtain a least squares solution.

		int nsing = n;
		Matrix wa1 = new Matrix(n, 1);
		for (int j = 0; j < n; j++) {
			wa1.setItem(j, 0, qtb.getItem(j, 0));
			if ((r.getItem(j, j) == 0.0) && (nsing == n))
				nsing = j;
			if (nsing < n)
				wa1.setItem(j, 0, 0.0);
		}

		for (int k = 0; k < nsing; k++) {
			int j = nsing - k - 1;
			wa1.setItem(j, 0, wa1.getItem(j, 0) / r.getItem(j, j));
			double temp = wa1.getItem(j, 0);
			int jm1 = j - 1;
			for (int i = 0; i <= jm1; i++) {
				wa1.setItem(i, 0, wa1.getItem(i, 0) - temp * r.getItem(i, j));
			}
		}

		for (int j = 0; j < n; j++) {
			x.setItem(j, 0, wa1.getItem(j, 0));
		}
		
		// initialize the iteration counter.
		// evaluate the function at the origin, and test
		// for acceptance of the gauss-newton direction.
		Matrix wa2 = new Matrix(n, 1);
		for (int j = 0; j < n; j++) {
			wa2.setItem(j, 0, diag.getItem(j, 0) * x.getItem(j, 0));
		}
		
		double dxnorm = wa2.getForbeniusNorm();
		double fp = dxnorm - delta;
		if (fp <= delta * p1) {
			return par; // ???
		}
		
		// if the jacobian is not rank deficient, the newton
		// step provides a lower bound, parl, for the zero of
		// the function. otherwise set this bound to zero.
		double parl = 0.0;
		if (nsing >= n) {
			for (int j = 0; j < n; j++) {
				wa1.setItem(j, 0, diag.getItem(j, 0) * (wa2.getItem(j, 0) / dxnorm));
			}
			for (int j = 0; j < n; j++) {
				double sum = 0.0;
				int jm1 = j - 1;
				if (jm1 >= 0) {
					for (int i = 0; i <= jm1; i++) {
						sum += r.getItem(i, j) * wa1.getItem(i, 0);
					}
				}
				wa1.setItem(j, 0, (wa1.getItem(j, 0) - sum) / r.getItem(j, j));
			}
			double temp = wa1.getForbeniusNorm();
			parl = ((fp / delta) / temp) / temp;
		}
		
		// calculate an upper bound, paru, for the zero of the function.
		for (int j = 0; j < n; j++) {
			double sum = 0.0;
			for (int i = 0; i <= j; i++) {
				sum += r.getItem(i, j) * qtb.getItem(i, 0);
			}
			wa1.setItem(j, 0, sum / diag.getItem(j, 0));
		}
		double gnorm = wa1.getForbeniusNorm();
		double paru = gnorm / delta;
		if (paru == 0.0) {
			paru = DWARF / Math.min(delta, p1);
		}
		// if the input par lies outside of the interval (parl,paru),
		// set par to the closer endpoint.
		par = Math.max(par, parl);
		par = Math.min(par, paru);
		if (par == 0.0)
			par = gnorm / dxnorm;
		
		int iter = 0;
		while (true) {
			iter++;
			// evaluate the function at the current value of par.
			if (par == 0.0)
				par = Math.max(DWARF, paru * 0.001);
			double temp = Math.sqrt(par);
			for (int j = 0; j < n; j++) {
				wa1.setItem(j, 0, temp * diag.getItem(j, 0));
			}
			qrsolv(r, wa1, x, qtb);
			for (int j = 0; j < n; j++) {
				wa2.setItem(j, 0, diag.getItem(j, 0) * x.getItem(j, 0));
			}
			dxnorm = wa2.getForbeniusNorm();
			temp = fp;
			fp = dxnorm - delta;
			// if the function is small enough, accept the current value
			// of par. also test for the exceptional cases where parl
			// is zero or the number of iterations has reached 10.
			if ( (Math.abs(fp) <= p1 * delta) || ((parl == 0.0) && (fp <= temp) && (temp < 0.0)) || (iter == 10))
				break;
			// compute the newton correction.
			for (int j = 0; j < n; j++) {
				wa1.setItem(j, 0, diag.getItem(j, 0) * (wa2.getItem(j, 0) / dxnorm));
			}
			for (int j = 0; j < n; j++) {
				wa1.setItem(j, 0, wa1.getItem(j, 0) / sdiag.getItem(j, 0));
				temp = wa1.getItem(j, 0);
				int jp1 = j + 1;
				for (int i = jp1; i < n; i++) {
					wa1.setItem(i, 0, wa1.getItem(i, 0) - r.getItem(i, j) * temp);
				}
			}
			temp = wa1.getForbeniusNorm();
			double parc = ((fp / delta) / temp) / temp;
			// depending on the sign of the function, update parl or paru.
			if (fp > 0.0)
				parl = Math.max(parl, par);
			if (fp < 0.0)
				paru = Math.min(paru, par);
			// compute an improved estimate for par.
			par = Math.max(parl, par + parc);
		}
		return par;
	}	

	/**
	 * subroutine qrsolv
	 * 
	 * given an m by n matrix a, an n by n diagonal matrix d,
	 * and an m-vector b, the problem is to determine an x which
	 * solves the system
	 * <pre>
	 *   a*x = b ,	  d*x = 0 ,
	 * </pre>
	 * in the least squares sense.
	 * 
	 * this subroutine completes the solution of the problem
	 * if it is provided with the necessary information from the
	 * qr factorization, with column pivoting, of a. that is, if
	 * a*p = q*r, where p is a permutation matrix, q has orthogonal
	 * columns, and r is an upper triangular matrix with diagonal
	 * elements of nonincreasing magnitude, then qrsolv expects
	 * the full upper triangle of r, the permutation matrix p,
	 * and the first n components of (q transpose)*b. the system
	 * a*x = b, d*x = 0, is then equivalent to
	 * <pre>
	 *    t	   t
	 * r*z = q *b ,  p *d*p*z = 0 ,
	 * </pre>
	 * where x = p*z. if this system does not have full rank,
	 * then a least squares solution is obtained. on output qrsolv
	 * also provides an upper triangular matrix s such that
	 * <pre>
	 *    t	 t		 t
	 *   p *(a *a + d*d)*p = s *s .
	 * </pre>
	 * s is computed within qrsolv and may be of separate interest.
	 * 
	 * 
	 * 
	 * @param r		is an n by n array. on input the full upper triangle
	 * 				must contain the full upper triangle of the matrix r.
	 * 				on output the full upper triangle is unaltered, and the
	 * 				strict lower triangle contains the strict upper triangle
	 * 				(transposed) of the upper triangular matrix s.
	 * @param diag	is an input array of length n which must contain the
	 * 				diagonal elements of the matrix d.
	 * @param x		is an output array of length n which contains the least
	 * 				squares solution of the system a*x = b, d*x = 0.
	 * @param qtb	is an input array of length n which must contain the first
	 * 				n elements of the vector (q transpose)*b.
	 */
	public void qrsolv(Matrix r, Matrix diag, Matrix x, Matrix qtb) {
		int m = r.getSizeY();
		int n = r.getSizeX();
		
		if ((m < n) ||
			(qtb.getSizeX() != n) || (qtb.getSizeY() != 1) ||
			(diag.getSizeX() != 1) || (diag.getSizeY() != n)) {
			throw new IllegalArgumentException();
		}
		x.resize(1, n);
		Matrix wa = qtb; //???
		
		// copy r and (q transpose)*b to preserve input and initialize s.
		// in particular, save the diagonal elements of r in x.

		for (int i = r.getSizeX() - 1; i >= 0; i--)
			for (int j = i - 1; j >= 0; j--)
				r.setItem(j, i, r.getItem(i, j));
		
		Matrix sdiag = new Matrix(n, 1);
		// eliminate the diagonal matrix d using a givens rotation.
		for (int j = 0; j < n; j++) {
			if (diag.getItem(j, 1) != 0.0) {
				for (int k = j; k < n; k++)
					sdiag.setItem(k, 0, 0.0);
				sdiag.setItem(j, 0, diag.getItem(j, 0));
							
				// the transformations to eliminate the row of d  
				// modify only a single element of (q transpose)*b
				// beyond the first n, which is initially zero.
				double qtbpj = 0.0;
				for (int k = j; k < n; k++) {
					// determine a givens rotation which eliminates the
					// appropriate element in the current row of d.    
					if (sdiag.getItem(k, 0) == 0.0)
						continue;
					double sin, cos;
					if (r.getItem(k, k) < Math.abs(sdiag.getItem(k, 0))) {
						double cotan = r.getItem(k, k) / sdiag.getItem(k, 0);
						sin = 0.5 / Math.sqrt(0.25 + 0.25 * cotan * cotan);
						cos = sin * cotan;
					} else {
						double tan = sdiag.getItem(k, 0) / r.getItem(k, k);
						cos = 0.5 / Math.sqrt(0.25 + 0.25 * tan * tan);
						sin = cos * tan;
					}
					// compute the modified diagonal element of r and
					// the modified element of ((q transpose)*b,0).
					r.setItem(k, k, cos * r.getItem(k, k) + sin * sdiag.getItem(k, 0));
					double temp = cos * wa.getItem(k, 0) + sin * qtbpj;
					qtbpj = -sin * wa.getItem(k, 0) + cos * qtbpj;
					wa.setItem(k, 0, temp);
					
					// accumulate the tranformation in the row of s.
					for (int i = k + 1; i < n; i++) {
						temp = cos * r.getItem(k, i) + sin * sdiag.getItem(i, 0);
						sdiag.setItem(i, 0, -sin * r.getItem(k, i) + cos * sdiag.getItem(i, 0));
						r.setItem(k, i, temp);
					}
				}
			}
			// store the diagonal element of s and restore
			// the corresponding diagonal element of r.
			sdiag.setItem(j, 0, r.getItem(j, j));
			r.setItem(j, j, x.getItem(j, 0));
		}
		// solve the triangular system for z. if the system is
		// singular, then obtain a least squares solution.
		int nsing = n;
		for (int j = 0; j < n; j++) {
			if( (sdiag.getItem(j, 0) == 0.0) && (nsing == n) )
				nsing = j;
			if(nsing < n)
				wa.setItem(j, 00, 0.0);
		}
		if (nsing >= 1) {
			for (int k=0; k<nsing; k++) {
				int j = nsing - k - 1;
				double sum = 0.0;
				int jp1 = j + 1;
				if (nsing > jp1) {
					for (int i = jp1; i<nsing; i++ ) {
						sum += r.getItem(jp1, i) * wa.getItem(i, 0);
					}
				}
				wa.setItem(j, 0, (wa.getItem(j, 0) - sum) / sdiag.getItem(j, 0));
			}			
		}
		// permute the components of z back to components of x.
//		for (int j = 0; j < n; j++) {
//			l = ipvt[j];
//			x[l] = wa[j];
//		}
	}
}
