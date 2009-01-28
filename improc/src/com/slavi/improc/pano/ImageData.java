package com.slavi.improc.pano;

import java.awt.geom.Rectangle2D;

import com.slavi.improc.pano.AlignInfo.PTRect;
import com.slavi.improc.pano.AlignInfo.cPrefsCorrectionMode;

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
	public PTRect selection = null;
	
	public Rectangle2D.Double extentInPano = new Rectangle2D.Double();
	
	// How to correct the image
//	public cPrefs cP = new cPrefs();
	// Preferences structure for tool correct
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
	long newWidth = 0;				//  new width
	long newHeight = 0;			//  new height
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
