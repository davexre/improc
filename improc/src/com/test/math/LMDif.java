package com.test.math;

import com.slavi.math.matrix.Matrix;

public class LMDif {
	
	public static class PTRect {
		long	top;
		long	bottom;
		long	left;
		long	right;
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
		int			correction_mode;		//  0 - radial correction;1 - vertical correction;2 - deregistration
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
	
	public static class Image {
		long width;
		long height;
		long bytesPerLine;
		long bitsPerPixel;	// Must be 24 or 32
		long dataSize; 
		byte[][] data;
		long dataformat;		// rgb, Lab etc
		long format;			// Projection: rectilinear etc
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
		Image 				im;				// Array of Pointers to Image Structs
		optVars				opt;				// Mark variables to optimize
		int				numIm;				// Number of images 
		controlPoint 			cpt;				// List of Control points
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
	public void lmpar(Matrix r, Matrix diag, Matrix x, Matrix qtb) {
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
		// if the jacobian is not rank deficient, the newton

		
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
