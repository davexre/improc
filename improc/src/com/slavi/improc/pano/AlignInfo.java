package com.slavi.improc.pano;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import com.slavi.improc.pano.ControlPoint.OptimizeType;
import com.slavi.improc.pano.Image.ImageFormat;
import com.slavi.math.matrix.JLapack;

public class AlignInfo {

	public static class Triangle {
		int[] vert = new int[3];	// Three vertices from list
		Image image;		// number of image for texture mapping
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
		int shear_x;
		int shear_y;
	}

	public static class StitchBuffer {	// Used describe how images should be merged
		String				srcName;		// Buffer should be merged to image; 0 if not.
		String				destName;		// Converted image (ie pano) should be saved to buffer; 0 if not
		int				feather;		// Width of feather
		int				colcorrect;		// Should the images be color corrected?
		int				seam;			// Where to put the seam (see above)
	}
	
	public static class SizePref { // sPrefs	// Preferences structure for 'pref' dialog
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

	
	ArrayList<Image>images = new ArrayList<Image>();	// Array of Pointers to Image Structs
	ArrayList<optVars>options = new ArrayList<optVars>();	// Mark variables to optimize
	//int				numIm;	 -> im.size()			// Number of images 
	ArrayList<ControlPoint>controlPoints = new ArrayList<ControlPoint>();		// List of Control points
	ArrayList<Triangle>triangles = new ArrayList<Triangle>();				// List of triangular faces
	int				nt;				// Number of triangular faces
	//int     			numPts;  -> cpt.size()				// Number of Control Points
	int				numParam;			// Number of parameters to optimize
	Image				pano;				// Panoramic Image decription
	StitchBuffer stitchBuffer = new StitchBuffer();				// Info on how to stitch the panorama
//	void				data;		// ????
	SizePref sizePref = new SizePref();	
	ArrayList<CoordInfo>coordInfos = new ArrayList<CoordInfo>();			// Real World coordinates
	
	public int getOptimalPanoWidth() {
		double scale = 0.0;
		Point2D.Double p0 = new Point2D.Double();
		Point2D.Double p1 = new Point2D.Double();
		for (int i = 0; i < images.size(); i++) {
			Image src = images.get(i);
			double roll = src.roll;
			double yaw = src.yaw;
			double pitch = src.pitch;
			src.roll = 0.0;
			src.yaw = 0.0;
			src.pitch = 0.0;
			
			p0.x = 0.0;
			p0.y = 0.0;
			PanoAdjust.makeInvParams(p0, src, pano, 0);
			p1.x = 1.0;
			p1.y = 1.0;
			PanoAdjust.makeInvParams(p1, src, pano, 0);
			
			double s = JLapack.hypot(p1.x - p0.x, p1.y - p0.y) / Math.sqrt(2.0);
			if (scale < s)
				scale = s;
			src.roll = roll;
			src.yaw = yaw;
			src.pitch = pitch;
		}
		return (int) (scale * (double) pano.width); // same scale for height
	}
	
	

	
	/////////////////////////
	
	static public double cubeRoot(double x) {
		if (x == 0.0)
			return 0.0;
		else if( x > 0.0 )
			return Math.pow(x, 1.0/3.0);
		else
			return -Math.pow(-x, 1.0/3.0);
	}
	
	static int squareZero(double a[], double root[]){
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
	
	static int cubeZero(double a[], double root[]){
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
	
	static double smallestRoot(double p[]) {
		double root[] = new double[3];
		double sroot = 1000.0;
		
		int n = cubeZero(p, root);
		for (int i = 0; i < n; i++) {
			if (root[i] > 0.0 && root[i] < sroot)
				sroot = root[i];
		}
		return sroot;
	}
	
	private void readImageLine(PTOTokenizer tokenizer, Image image, optVars opt, CoordInfo ci) {
		String t;
		while ((t = tokenizer.nextToken()) != null) {
			if (t.length() <= 0)
				continue;
			if ("w".equals(t)) {
				image.width = Integer.parseInt(tokenizer.nextToken());
			} else if ("h".equals(t)) {
				image.height = Integer.parseInt(tokenizer.nextToken());
			} else if ("v=".equals(t)) {
				opt.hfov = Integer.parseInt(tokenizer.nextToken());
			} else if ("v".equals(t)) {
				image.hfov = Double.parseDouble(tokenizer.nextToken());
			} else if ("a=".equals(t)) {
				opt.a = Integer.parseInt(tokenizer.nextToken()) + 2;
				image.cP.radial = true;
			} else if ("a".equals(t)) {
				image.cP.radial_params[0][3] = Double.parseDouble(tokenizer.nextToken());
				image.cP.radial = true;
			} else if ("b=".equals(t)) {
				opt.b = Integer.parseInt(tokenizer.nextToken()) + 2;
				image.cP.radial = true;
			} else if ("b".equals(t)) {
				image.cP.radial_params[0][2] = Double.parseDouble(tokenizer.nextToken());
				image.cP.radial = true;
			} else if ("c=".equals(t)) {
				opt.c = Integer.parseInt(tokenizer.nextToken()) + 2;
				image.cP.radial = true;
			} else if ("c".equals(t)) {
				image.cP.radial_params[0][1] = Double.parseDouble(tokenizer.nextToken());
				image.cP.radial = true;
			} else if ("f".equals(t)) {
				int k = Integer.parseInt(tokenizer.nextToken());
				switch (k) {
				case 0: image.format = ImageFormat.Rectilinear; break;
				case 1: 
					image.format = ImageFormat.Panorama;
					image.cP.correction_mode = cPrefsCorrectionMode.Vertical; // im->cP.correction_mode |= correction_mode_vertical;
					break;
				case 2: image.format = ImageFormat.FisheyeCirc; break;
				case 3: image.format = ImageFormat.FisheyeFF; break;
				case 4: 
					image.format = ImageFormat.Equirectangular; 
					image.cP.correction_mode = cPrefsCorrectionMode.Vertical; // im->cP.correction_mode |= correction_mode_vertical;
					break;
				case 8: image.format = ImageFormat.Orthographic; break;
				default:
					throw new RuntimeException("invalid imgae format");
				}
			} else if ("o".equals(t)) {
				image.cP.correction_mode = cPrefsCorrectionMode.Morph; // im->cP.correction_mode |=  correction_mode_morph;
			} else if ("y=".equals(t)) {
				opt.yaw = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("y".equals(t)) {
				image.yaw = Double.parseDouble(tokenizer.nextToken());
			} else if ("p=".equals(t)) {
				opt.pitch = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("p".equals(t)) {
				image.pitch = Double.parseDouble(tokenizer.nextToken());
			} else if ("r=".equals(t)) {
				opt.roll = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("r".equals(t)) {
				image.roll = Double.parseDouble(tokenizer.nextToken());
			} else if ("d=".equals(t)) {
				opt.d = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("d".equals(t)) {
				image.cP.horizontal_params[0] = Double.parseDouble(tokenizer.nextToken());
				image.cP.horizontal = true;
			} else if ("e=".equals(t)) {
				opt.e = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("e".equals(t)) {
				image.cP.vertical_params[0] = Double.parseDouble(tokenizer.nextToken());
				image.cP.vertical = true;
			} else if ("g=".equals(t)) {
				opt.shear_x = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("g".equals(t)) {
				image.cP.shear_x = Double.parseDouble(tokenizer.nextToken());
				image.cP.shear = true;
			} else if ("t=".equals(t)) {
				opt.shear_y = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("t".equals(t)) {
				image.cP.shear_y = Double.parseDouble(tokenizer.nextToken());
				image.cP.shear = true;
			} else if ("n".equals(t)) {
				image.name = tokenizer.nextToken();
			} else if ("m".equals(t)) {
				image.cP.frame = Integer.parseInt(tokenizer.nextToken());
				image.cP.cutFrame = true;
			} else if ("mx".equals(t)) {
				image.cP.fwidth = Integer.parseInt(tokenizer.nextToken());
				image.cP.cutFrame = true;
			} else if ("my".equals(t)) {
				image.cP.fheight = Integer.parseInt(tokenizer.nextToken());
				image.cP.cutFrame = true;
			} else if ("X".equals(t)) {
				ci.x[0] = Double.parseDouble(tokenizer.nextToken());
			} else if ("Y".equals(t)) {
				ci.x[1] = Double.parseDouble(tokenizer.nextToken());
			} else if ("Z".equals(t)) {
				ci.x[2] = Double.parseDouble(tokenizer.nextToken());
			} else if ("S".equals(t)) {
				image.selection.left = Integer.parseInt(tokenizer.nextToken());
				image.selection.right = Integer.parseInt(tokenizer.nextToken());
				image.selection.top = Integer.parseInt(tokenizer.nextToken());
				image.selection.bottom = Integer.parseInt(tokenizer.nextToken());
			} else if ("C".equals(t)) {
				image.selection.left = Integer.parseInt(tokenizer.nextToken());
				image.selection.right = Integer.parseInt(tokenizer.nextToken());
				image.selection.top = Integer.parseInt(tokenizer.nextToken());
				image.selection.bottom = Integer.parseInt(tokenizer.nextToken());
				image.cP.cutFrame = true;
			} else if ("+".equals(t)) {
				stitchBuffer.srcName = tokenizer.nextToken();
			} else if ("-".equals(t)) {
				stitchBuffer.destName = tokenizer.nextToken();
			} else if ("u".equals(t)) {
				stitchBuffer.feather = Integer.parseInt(tokenizer.nextToken());
			} else if ("s".equals(t)) {
				stitchBuffer.seam = Integer.parseInt(tokenizer.nextToken());
				// if(sBuf.seam != _dest)
				//	sBuf.seam = _middle;
			} else if ("k".equals(t)) {
				int i = Integer.parseInt(tokenizer.nextToken());
				stitchBuffer.colcorrect |= i & 3; 
				stitchBuffer.colcorrect += (i + 1) * 4; 
			}
		}
		
		// Set 4th polynomial parameter
		image.cP.radial_params[0][0] = 1.0 - (image.cP.radial_params[0][3] + image.cP.radial_params[0][2] + image.cP.radial_params[0][1]);
		for (int col = 1; col < 3; col++) {
			for (int i = 0; i < 4; i++)
				image.cP.radial_params[col][i]	= image.cP.radial_params[0][i];
			image.cP.vertical_params[col] = image.cP.vertical_params[0];
			image.cP.horizontal_params[col] = image.cP.horizontal_params[0];
		}
		// Restrict radial correction to monotonous interval
		double a[] = new double[4];
		for (int i = 0; i < 3; i++) {
			for (int k = 0; k < 4; k++) {
				a[k] = 0.0;//1.0e-10;
				if (image.cP.radial_params[i][k] != 0.0 ) {
					a[k] = (k + 1) * image.cP.radial_params[i][k];
				}
			}
			image.cP.radial_params[i][4] = smallestRoot(a);
		}
	}
	
	public void readPanoScript(BufferedReader fin) throws IOException {
		int variablesToOptimize = 0;
		while (fin.ready()) {
			String s = fin.readLine();
			if (s == null || s.length() <= 0)
				continue;
			PTOTokenizer tokenizer = new PTOTokenizer(s);
			tokenizer.nextToken();
			
			char c = s.charAt(0);
			switch (c) {
			case 'i': {
				// Image description
				Image im = new Image();
				optVars opt = new optVars();
				CoordInfo ci = new CoordInfo();
				readImageLine(tokenizer, im, opt, ci);
				images.add(im);
				options.add(opt);
				coordInfos.add(ci);
				break;
			}
			case 't': {
				// Triangle
				Triangle tr = new Triangle();
				String t;
				int i = 0;
				while ((t = tokenizer.nextToken()) != null) {
					if (t.length() <= 0)
						continue;
					if ("i".equals(t)) {
						tr.image = images.get(Integer.parseInt(tokenizer.nextToken()));
					} else {
						if (i < 3) {
							tr.vert[i++] = Integer.parseInt(tokenizer.nextToken());
						} else {
							tokenizer.nextToken();
						}
					}
				}
				triangles.add(tr);
				break;
			}
			case 'c': {
				// Control Point
				ControlPoint cp = new ControlPoint();
				String t;
				while ((t = tokenizer.nextToken()) != null) {
					if (t.length() <= 0)
						continue;
					if ("t".equals(t)) {
						int type = Integer.parseInt(tokenizer.nextToken());
						switch (type) {
						case 1: cp.type = OptimizeType.x; break;
						case 2: cp.type = OptimizeType.y; break;
						case 0: 
						default: cp.type = OptimizeType.r; break;
						}
					} else if ("n".equals(t)) {
						int n = Integer.parseInt(tokenizer.nextToken());
						cp.image0 = images.get(n);
					} else if ("N".equals(t)) {
						int n = Integer.parseInt(tokenizer.nextToken());
						cp.image1 = images.get(n);
					} else if ("x".equals(t)) {
						cp.x0 = Double.parseDouble(tokenizer.nextToken());
					} else if ("X".equals(t)) {
						cp.x1 = Double.parseDouble(tokenizer.nextToken());
					} else if ("y".equals(t)) {
						cp.y0 = Double.parseDouble(tokenizer.nextToken());
					} else if ("Y".equals(t)) {
						cp.y1 = Double.parseDouble(tokenizer.nextToken());
					} else if ("i".equals(t)) {
						int n = Integer.parseInt(tokenizer.nextToken());
						cp.image1 = cp.image0 = images.get(n);
					}
				}
				controlPoints.add(cp);
				break;
			}
			case 'm': {
				// Mode description
				String t;
				while ((t = tokenizer.nextToken()) != null) {
					if (t.length() <= 0)
						continue;
					if ("g".equals(t)) {
						sizePref.gamma = Double.parseDouble(tokenizer.nextToken());
					} else if ("i".equals(t)) {
						sizePref.interpolator = Integer.parseInt(tokenizer.nextToken());
					} else if ("p".equals(t)) {
						sizePref.optCreatePano = Integer.parseInt(tokenizer.nextToken());
					}
				}
				break;
			}
			case 'v': {
				// Variables to optimize
				String t;
				while ((t = tokenizer.nextToken()) != null) {
					if (t.length() <= 0)
						continue;
					int n = Integer.parseInt(tokenizer.nextToken());
					if ("y".equals(t)) {
						options.get(n).yaw = 1;
						variablesToOptimize++;
					} else if ("p".equals(t)) {
						options.get(n).pitch = 1;
						variablesToOptimize++;
					} else if ("r".equals(t)) {
						options.get(n).roll = 1;
						variablesToOptimize++;
					} else if ("v".equals(t)) {
						options.get(n).hfov = 1;
						variablesToOptimize++;
					} else if ("a".equals(t)) {
						options.get(n).a = 1;
						variablesToOptimize++;
					} else if ("b".equals(t)) {
						options.get(n).b = 1;
						variablesToOptimize++;
					} else if ("c".equals(t)) {
						options.get(n).c = 1;
						variablesToOptimize++;
					} else if ("d".equals(t)) {
						options.get(n).d = 1;
						variablesToOptimize++;
					} else if ("e".equals(t)) {
						options.get(n).e = 1;
						variablesToOptimize++;
					} else if ("X".equals(t)) {
						coordInfos.get(n).set[0] = 0;
						variablesToOptimize++;
					} else if ("Y".equals(t)) {
						coordInfos.get(n).set[1] = 0;
						variablesToOptimize++;
					} else if ("Z".equals(t)) {
						coordInfos.get(n).set[2] = 0;
						variablesToOptimize++;
					}
				}
				break;
			}
			case 'p':
				// panorama
				pano = new Image();
				optVars opt = new optVars();
				CoordInfo ci = new CoordInfo();
				readImageLine(tokenizer, pano, opt, ci);
//				if (pano.format == ImageFormat.FisheyeCirc)
//					pano.format = ImageFormat.Equirectangular;
				break;
			case '*':
				// End of script-data
			default:
				break;
			}
		}
				
		// Set up Panorama description
		
		if ((pano.width == 0) && (images.get(0).hfov != 0.0)) {
			// Set default for panorama width based on first image
			pano.width = (int)(((double)pano.hfov / images.get(0).hfov ) * images.get(0).width);
			pano.width /= 10; 
			pano.width *= 10; // Round to multiple of 10
		}
		
		if (pano.height == 0)
			pano.height = pano.width / 2;

		// Set up global information structure
		numParam 	= variablesToOptimize;
		// Set initial values for linked variables
		for (int i = 0; i < images.size(); i++){
			Image im = images.get(i);
			int k = options.get(i).yaw - 2;
			if( k >= 0 ) 
				im.yaw = images.get(k).yaw;

			k = options.get(i).pitch - 2;
			if( k >= 0 ) im.pitch = images.get(k).pitch;

			k = options.get(i).roll - 2;
			if( k >= 0 ) im.roll = images.get(k).roll;

			k = options.get(i).hfov - 2;
			if( k >= 0 ) im.hfov = images.get(k).hfov;

			k = options.get(i).a - 2;
			if( k >= 0 ) im.cP.radial_params[0][3] = images.get(k).cP.radial_params[0][3];

			k = options.get(i).b - 2;
			if( k >= 0 ) im.cP.radial_params[0][2] = images.get(k).cP.radial_params[0][2];

			k = options.get(i).c - 2;
			if( k >= 0 ) im.cP.radial_params[0][1] = images.get(k).cP.radial_params[0][1];

			k = options.get(i).d - 2;
			if( k >= 0 ) im.cP.horizontal_params[0] = images.get(k).cP.horizontal_params[0];

			k = options.get(i).e - 2;
			if( k >= 0 ) im.cP.vertical_params[0] = images.get(k).cP.vertical_params[0];

			im.cP.radial_params[0][0] = 1.0 - (im.cP.radial_params[0][3]
												+ im.cP.radial_params[0][2]
												+ im.cP.radial_params[0][1] ) ;
			SetEquColor(im.cP);
		}
	}
	
	static void SetEquColor( cPrefs cP )
	{
		for(int col = 1; col < 3; col++) {
			for(int i = 0; i < 4; i++)
				cP.radial_params[col][i] = cP.radial_params[0][i];
			cP.vertical_params[col] = cP.vertical_params[0];
			cP.horizontal_params[col] = cP.horizontal_params[0];
		}
	}
	
	public void writePanoScript(PrintStream fou) {
		int format = 0;
		switch (pano.format) {
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
		fou.println("p f" + Integer.toString(format) + " w" + Long.toString(pano.width) + " h" + Long.toString(pano.height) +
				" v" + Double.toString(pano.hfov) + " n\"" + pano.name + "\"");
		
		
		for (int i = 0; i < images.size(); i++) {
			Image im = images.get(i);
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
			
/*			int optHfov, opta, optb, optc;
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
				optc = 0;*/

			fou.printf("i w%d", im.width);
			fou.printf(" h%d", im.height);
			fou.printf(" f%d", format);
			fou.printf(" a%f", im.cP.radial_params[0][3]);
			fou.printf(" b%f", im.cP.radial_params[0][2]);
			fou.printf(" c%f", im.cP.radial_params[0][1]);
			fou.printf(" d%f", im.cP.horizontal_params[0]); 
			fou.printf(" e%f", im.cP.vertical_params[0]);
			fou.printf(" g0");
			fou.printf(" p%f", im.pitch);
			fou.printf(" r%f", im.roll);
			fou.printf(" t0");
			fou.printf(" v%f", im.hfov);
			fou.printf(" y%f", im.yaw);
			fou.printf(" u%d", stitchBuffer.feather);
			fou.printf(" n\"%s\"", im.name);
			fou.printf("\n");
			
/*			fou.println("i w" + Long.toString(im.width) + " h" + Long.toString(im.height) + " f" + Integer.toString(format) +
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
					" n\"" + im.name + "\"");*/
		}
	}
	
}
