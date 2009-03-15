package com.slavi.improc.pano;

import java.awt.geom.Rectangle2D;

import com.slavi.math.matrix.Matrix;

public class ImageData {
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
	
	public int width = 0;
	public int height = 0;
	public ImageFormat format = ImageFormat.Rectilinear;	// Projection: rectilinear etc
	public double hfov = 0;
	public double yaw = 0;
	public double pitch = 0;
	public double roll = 0;
	
	public String name = "";
//	public PTRect selection = null;
	
	public Rectangle2D.Double extentInPano = new Rectangle2D.Double();
	
	// How to correct the image
//	public cPrefs cP = new cPrefs();
	// Preferences structure for tool correct
	public long magic = 20L;			//  File validity check, must be 20
	public boolean radial = false;		//  Radial correction requested?
	public double[][]	radial_params = new double[3][5];	//  3 colors x (4 coeffic. for 3rd order polys + correction radius)
	public boolean vertical = false;	//  Vertical shift requested ?
	public double[] vertical_params = new double[3];		//  3 colors x vertical shift value
	public boolean horizontal = false;	//  horizontal tilt ( in screenpoints)
	public double[] horizontal_params = new double[3];	//  3 colours x horizontal shift value
	public boolean shear = false;		//  shear correction requested?
	public double shear_x = 0.0;		//  horizontal shear values
	public double shear_y = 0.0;		//  vertical shear values
	public boolean resize = false;		//  scaling requested ?
	public long newWidth = 0;				//  new width
	public long newHeight = 0;			//  new height
	public boolean luminance = false;	//  correct luminance variation?
	public double[] lum_params = new double[3];		//  parameters for luminance corrections
	// not supported. Correction mode is always radial
//	cPrefsCorrectionMode correction_mode = cPrefsCorrectionMode.Radial;		//  0 - radial correction;1 - vertical correction;2 - deregistration
//	boolean cutFrame = false;	//  remove frame? 0 - no; 1 - yes
	public int	fwidth = 100;
	public int fheight = 100;
	public int	frame = 0;
	public boolean fourier = false;	//  Fourier filtering requested?
	public int	fourier_mode = 0;		// default is _fremoveBlurr // _faddBlurr vs _fremoveBlurr
	public String psf = "";			//  Point Spread Function, full path/fsspec to psd-file
	public int	fourier_nf;				// default is _fremoveBlurr // Noise filtering: _nf_internal vs _nf_custom
	public String nff = "";			//  noise filtered file: full path/fsspec to psd-file
	public double filterfactor = 1.0;	//  Hunt factor
	public double fourier_frame = 0.0;	//  To correct edge errors

	// Mark variables to optimize
	// Indicate to optimizer which variables to optimize
	public int optimizeHfov;								//  optimize hfov? 0-no 1-yes , etc
	public int optimizeYaw;				
	public int optimizePitch;				
	public int optimizeRoll;				
	public int optimizeA;
	public int optimizeB;
	public int optimizeC;					
	public int optimizeD;
	public int optimizeE;
	public int optimizeShearX;
	public int optimizeShearY;
}
