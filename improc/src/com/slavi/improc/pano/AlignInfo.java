package com.slavi.improc.pano;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Locale;

import com.slavi.improc.pano.ImageData.ImageFormat;
import com.slavi.math.MathUtil;

public class AlignInfo {

/*	public static class Triangle {
		int[] vert = new int[3];	// Three vertices from list
		ImageData image;		// number of image for texture mapping
	}*/
	
/*	public static class PTRect {
		long	top;
		long	bottom;
		long	left;
		long	right;
	}*/

/*	public static enum cPrefsCorrectionMode {
		Radial,
		Vertical,
		Deregister,
		Morph
	}*/
	
/*	public static class StitchBuffer {	// Used describe how images should be merged
		String				srcName;		// Buffer should be merged to image; 0 if not.
		String				destName;		// Converted image (ie pano) should be saved to buffer; 0 if not
		int				feather;		// Width of feather
		int				colcorrect;		// Should the images be color corrected?
		int				seam;			// Where to put the seam (see above)
	}*/
	
/*	public static class SizePref { // sPrefs	// Preferences structure for 'pref' dialog
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
	}*/
	
/*	public static class CoordInfo {	// Real World 3D coordinates
		int  num;								// auxilliary index
		double[] x = new double[3];
		int[]  set = new int[3];
	}*/

	
	ArrayList<ImageData>images = new ArrayList<ImageData>();	// Array of Pointers to Image Structs
	//int				numIm;	 -> im.size()			// Number of images 
	ArrayList<ControlPoint>controlPoints = new ArrayList<ControlPoint>();		// List of Control points
//	ArrayList<Triangle>triangles = new ArrayList<Triangle>();				// List of triangular faces
//	int				nt;				// Number of triangular faces
	//int     			numPts;  -> cpt.size()				// Number of Control Points
	int				numParam;			// Number of parameters to optimize
	ImageData				pano;				// Panoramic Image decription
//	StitchBuffer stitchBuffer = new StitchBuffer();				// Info on how to stitch the panorama
//	void				data;		// ????
//	SizePref sizePref = new SizePref();	
//	ArrayList<CoordInfo>coordInfos = new ArrayList<CoordInfo>();			// Real World coordinates
	
	public void calcOptimalPanoWidth() {
		calculateExtents(pano);
		pano.width = (int) pano.extentInPano.width;
		pano.height = (int) pano.extentInPano.height;
	}
	
	public int getOptimalPanoWidthORIGINAL() {
		double scale = 0.0;
		Point2D.Double p0 = new Point2D.Double();
		Point2D.Double p1 = new Point2D.Double();
		for (int i = 0; i < images.size(); i++) {
			ImageData src = images.get(i);
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
			src.extentInPano = new Rectangle2D.Double(p0.x, p0.y, p1.x, p1.y);
			
			double s = MathUtil.hypot(p1.x - p0.x, p1.y - p0.y);
			if (scale < s)
				scale = s;
			src.roll = roll;
			src.yaw = yaw;
			src.pitch = pitch;
		}
		return (int) (scale * (double) pano.width); // same scale for height
	}

	void calculateExtents(ImageData dest) {
		Point2D.Double p = new Point2D.Double();
		Point2D.Double min = new Point2D.Double();
		Point2D.Double max = new Point2D.Double();
		double destX = dest.width / 2.0 - 0.5;
		double destY = dest.height / 2.0 - 0.5;
		boolean isFirst = true;
		
		for (ImageData image : images) {
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
		System.out.println("Pano extent is " + dest.extentInPano);
		return new Point2D.Double(dest.extentInPano.width, dest.extentInPano.height);
	}

	public Point2D.Double getFieldOfViewOriginal() {
		double hfov = pano.hfov;
		int width = pano.width;
		int height = pano.height;
		ImageFormat format = pano.format;
		
		pano.hfov = 360.0;
		pano.width = 360;
		pano.height = 180;
		pano.format = ImageFormat.Equirectangular;

		double destX = pano.width / 2.0;
		double destY = pano.height / 2.0;
		
		Point2D.Double p = new Point2D.Double();
		Point2D.Double min = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		Point2D.Double max = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		for (ImageData image : images) {
			double srcX = image.width / 2.0;
			double srcY = image.height / 2.0;
			
			for (int i = 0; i < image.width; i++) {
				p.x = i - srcX;
				p.y = 0 - srcY;
				PanoAdjust.makeInvParams(p, image, pano, 0);
				p.x += destX;
				p.y += destY;
				if (min.x > p.x) min.x = p.x; 
				if (min.y > p.y) min.y = p.y; 
				if (max.x < p.x) max.x = p.x; 
				if (max.y < p.y) max.y = p.y; 

				p.x = i - srcX;
				p.y = image.height - 1 - srcY;
				PanoAdjust.makeInvParams(p, image, pano, 0);
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
				PanoAdjust.makeParams(p, image, pano, 0);
				p.x += destX;
				p.y += destY;
				if (min.x > p.x) min.x = p.x; 
				if (min.y > p.y) min.y = p.y; 
				if (max.x < p.x) max.x = p.x; 
				if (max.y < p.y) max.y = p.y; 

				p.x = image.width - 1 - srcX;
				p.y = j - srcY;
				PanoAdjust.makeParams(p, image, pano, 0);
				p.x += destX;
				p.y += destY;
				if (min.x > p.x) min.x = p.x; 
				if (min.y > p.y) min.y = p.y; 
				if (max.x < p.x) max.x = p.x; 
				if (max.y < p.y) max.y = p.y; 
			}
		}
		pano.hfov = hfov;
		pano.width = width;
		pano.height = height;
		pano.format = format;

		min.x -= 180.0;
		min.y -= 90.0;
		max.x -= 180.0;
		max.y -= 90.0;
		p.x = 2.0 * Math.max(Math.abs(min.x), Math.abs(max.x));
		p.y = 2.0 * Math.max(Math.abs(min.y), Math.abs(max.y));
		
		pano.width = (int) p.x;
		pano.height = (int) p.y;
		
		return p;
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
	
	private void readImageLine(PTOTokenizer tokenizer, ImageData image) {
		String t;
		while ((t = tokenizer.nextToken()) != null) {
			if (t.length() <= 0)
				continue;
			if ("w".equals(t)) {
				image.width = Integer.parseInt(tokenizer.nextToken());
			} else if ("h".equals(t)) {
				image.height = Integer.parseInt(tokenizer.nextToken());
			} else if ("v=".equals(t)) {
				image.optimizeHfov = Integer.parseInt(tokenizer.nextToken());
			} else if ("v".equals(t)) {
				image.hfov = Double.parseDouble(tokenizer.nextToken());
			} else if ("a=".equals(t)) {
				image.optimizeA = Integer.parseInt(tokenizer.nextToken()) + 2;
				image.radial = true;
			} else if ("a".equals(t)) {
				image.radial_params[0][3] = Double.parseDouble(tokenizer.nextToken());
				image.radial = true;
			} else if ("b=".equals(t)) {
				image.optimizeB = Integer.parseInt(tokenizer.nextToken()) + 2;
				image.radial = true;
			} else if ("b".equals(t)) {
				image.radial_params[0][2] = Double.parseDouble(tokenizer.nextToken());
				image.radial = true;
			} else if ("c=".equals(t)) {
				image.optimizeC = Integer.parseInt(tokenizer.nextToken()) + 2;
				image.radial = true;
			} else if ("c".equals(t)) {
				image.radial_params[0][1] = Double.parseDouble(tokenizer.nextToken());
				image.radial = true;
			} else if ("f".equals(t)) {
				int k = Integer.parseInt(tokenizer.nextToken());
				switch (k) {
				case 0: image.format = ImageFormat.Rectilinear; break;
				case 1: 
					image.format = ImageFormat.Panorama;
					// not supported. Correction mode is always radial
//					image.correction_mode = cPrefsCorrectionMode.Vertical; // im->cP.correction_mode |= correction_mode_vertical;
					break;
				case 2: image.format = ImageFormat.FisheyeCirc; break;
				case 3: image.format = ImageFormat.FisheyeFF; break;
				case 4: 
					image.format = ImageFormat.Equirectangular; 
					// not supported. Correction mode is always radial
//					image.correction_mode = cPrefsCorrectionMode.Vertical; // im->cP.correction_mode |= correction_mode_vertical;
					break;
				case 8: image.format = ImageFormat.Orthographic; break;
				default:
					throw new RuntimeException("invalid imgae format");
				}
			} else if ("o".equals(t)) {
				// not supported. Correction mode is always radial
//				image.correction_mode = cPrefsCorrectionMode.Morph; // im->cP.correction_mode |=  correction_mode_morph;
			} else if ("y=".equals(t)) {
				image.optimizeYaw = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("y".equals(t)) {
				image.yaw = Double.parseDouble(tokenizer.nextToken());
			} else if ("p=".equals(t)) {
				image.optimizePitch = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("p".equals(t)) {
				image.pitch = Double.parseDouble(tokenizer.nextToken());
			} else if ("r=".equals(t)) {
				image.optimizeRoll = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("r".equals(t)) {
				image.roll = Double.parseDouble(tokenizer.nextToken());
			} else if ("d=".equals(t)) {
				image.optimizeD = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("d".equals(t)) {
				image.horizontal_params[0] = Double.parseDouble(tokenizer.nextToken());
				image.horizontal = true;
			} else if ("e=".equals(t)) {
				image.optimizeE = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("e".equals(t)) {
				image.vertical_params[0] = Double.parseDouble(tokenizer.nextToken());
				image.vertical = true;
			} else if ("g=".equals(t)) {
				image.optimizeShearX = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("g".equals(t)) {
				image.shear_x = Double.parseDouble(tokenizer.nextToken());
				image.shear = true;
			} else if ("t=".equals(t)) {
				image.optimizeShearY = Integer.parseInt(tokenizer.nextToken()) + 2;
			} else if ("t".equals(t)) {
				image.shear_y = Double.parseDouble(tokenizer.nextToken());
				image.shear = true;
			} else if ("n".equals(t)) {
				image.name = tokenizer.nextToken();
			} else if ("m".equals(t)) {
				// not supported
/*				image.frame = Integer.parseInt(tokenizer.nextToken());
				image.cutFrame = true;*/
			} else if ("mx".equals(t)) {
				// not supported
/*				image.fwidth = Integer.parseInt(tokenizer.nextToken());
				image.cutFrame = true;*/
			} else if ("my".equals(t)) {
				// not supported
/*				image.fheight = Integer.parseInt(tokenizer.nextToken());
				image.cutFrame = true;*/
			} else if ("X".equals(t)) {
				// not supported
//				ci.x[0] = Double.parseDouble(tokenizer.nextToken());
			} else if ("Y".equals(t)) {
				// not supported
//				ci.x[1] = Double.parseDouble(tokenizer.nextToken());
			} else if ("Z".equals(t)) {
				// not supported
//				ci.x[2] = Double.parseDouble(tokenizer.nextToken());
			} else if ("S".equals(t)) {
				// not supported
/*				image.selection.left = Integer.parseInt(tokenizer.nextToken());
				image.selection.right = Integer.parseInt(tokenizer.nextToken());
				image.selection.top = Integer.parseInt(tokenizer.nextToken());
				image.selection.bottom = Integer.parseInt(tokenizer.nextToken());*/
			} else if ("C".equals(t)) {
				// not supported
/*				image.selection.left = Integer.parseInt(tokenizer.nextToken());
				image.selection.right = Integer.parseInt(tokenizer.nextToken());
				image.selection.top = Integer.parseInt(tokenizer.nextToken());
				image.selection.bottom = Integer.parseInt(tokenizer.nextToken());
				image.cutFrame = true;*/
			} else if ("+".equals(t)) {
				// not supported
//				stitchBuffer.srcName = tokenizer.nextToken();
			} else if ("-".equals(t)) {
				// not supported
//				stitchBuffer.destName = tokenizer.nextToken();
			} else if ("u".equals(t)) {
				// not supported
//				stitchBuffer.feather = Integer.parseInt(tokenizer.nextToken());
			} else if ("s".equals(t)) {
				// not supported
//				stitchBuffer.seam = Integer.parseInt(tokenizer.nextToken());
				// if(sBuf.seam != _dest)
				//	sBuf.seam = _middle;
			} else if ("k".equals(t)) {
				// not supported
/*				int i = Integer.parseInt(tokenizer.nextToken());
				stitchBuffer.colcorrect |= i & 3; 
				stitchBuffer.colcorrect += (i + 1) * 4;*/ 
			}
		}
		
		// Set 4th polynomial parameter
		image.radial_params[0][0] = 1.0 - (image.radial_params[0][3] + image.radial_params[0][2] + image.radial_params[0][1]);
		for (int col = 1; col < 3; col++) {
			for (int i = 0; i < 4; i++)
				image.radial_params[col][i]	= image.radial_params[0][i];
			image.vertical_params[col] = image.vertical_params[0];
			image.horizontal_params[col] = image.horizontal_params[0];
		}
		// Restrict radial correction to monotonous interval
		double a[] = new double[4];
		for (int i = 0; i < 3; i++) {
			for (int k = 0; k < 4; k++) {
				a[k] = 0.0;//1.0e-10;
				if (image.radial_params[i][k] != 0.0 ) {
					a[k] = (k + 1) * image.radial_params[i][k];
				}
			}
			image.radial_params[i][4] = smallestRoot(a);
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
				ImageData im = new ImageData();
				// not supported
//				CoordInfo ci = new CoordInfo();
				readImageLine(tokenizer, im /*, ci*/);
				images.add(im);
//				coordInfos.add(ci);
				break;
			}
			case 't': {
				// Triangle
				// not supported
/*				Triangle tr = new Triangle();
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
				triangles.add(tr);*/
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
						// ignore type. use always r -> optimize by distance
//						switch (type) {
//						case 1: cp.type = OptimizeType.x; break;
//						case 2: cp.type = OptimizeType.y; break;
//						case 0: 
//						default: cp.type = OptimizeType.r; break;
//						}
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
				// not supported
/*				String t;
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
				}*/
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
						images.get(n).optimizeYaw = 1;
						variablesToOptimize++;
					} else if ("p".equals(t)) {
						images.get(n).optimizePitch = 1;
						variablesToOptimize++;
					} else if ("r".equals(t)) {
						images.get(n).optimizeRoll = 1;
						variablesToOptimize++;
					} else if ("v".equals(t)) {
						images.get(n).optimizeHfov = 1;
						variablesToOptimize++;
					} else if ("a".equals(t)) {
						images.get(n).optimizeA = 1;
						variablesToOptimize++;
					} else if ("b".equals(t)) {
						images.get(n).optimizeB = 1;
						variablesToOptimize++;
					} else if ("c".equals(t)) {
						images.get(n).optimizeC = 1;
						variablesToOptimize++;
					} else if ("d".equals(t)) {
						images.get(n).optimizeD = 1;
						variablesToOptimize++;
					} else if ("e".equals(t)) {
						images.get(n).optimizeE = 1;
						variablesToOptimize++;
					} else if ("X".equals(t)) {
						// not supported
//						coordInfos.get(n).set[0] = 0;
//						variablesToOptimize++;
					} else if ("Y".equals(t)) {
						// not supported
//						coordInfos.get(n).set[1] = 0;
//						variablesToOptimize++;
					} else if ("Z".equals(t)) {
						// not supported
//						coordInfos.get(n).set[2] = 0;
//						variablesToOptimize++;
					}
				}
				break;
			}
			case 'p':
				// panorama
				pano = new ImageData();
//				CoordInfo ci = new CoordInfo();
				readImageLine(tokenizer, pano /*, ci*/);
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
			ImageData im = images.get(i);
			int k = images.get(i).optimizeYaw - 2;
			if( k >= 0 ) 
				im.yaw = images.get(k).yaw;

			k = images.get(i).optimizePitch - 2;
			if( k >= 0 ) im.pitch = images.get(k).pitch;

			k = images.get(i).optimizeRoll - 2;
			if( k >= 0 ) im.roll = images.get(k).roll;

			k = images.get(i).optimizeHfov - 2;
			if( k >= 0 ) im.hfov = images.get(k).hfov;

			k = images.get(i).optimizeA - 2;
			if( k >= 0 ) im.radial_params[0][3] = images.get(k).radial_params[0][3];

			k = images.get(i).optimizeB - 2;
			if( k >= 0 ) im.radial_params[0][2] = images.get(k).radial_params[0][2];

			k = images.get(i).optimizeC - 2;
			if( k >= 0 ) im.radial_params[0][1] = images.get(k).radial_params[0][1];

			k = images.get(i).optimizeD - 2;
			if( k >= 0 ) im.horizontal_params[0] = images.get(k).horizontal_params[0];

			k = images.get(i).optimizeE - 2;
			if( k >= 0 ) im.vertical_params[0] = images.get(k).vertical_params[0];

			im.radial_params[0][0] = 1.0 - (im.radial_params[0][3]
												+ im.radial_params[0][2]
												+ im.radial_params[0][1] ) ;
			SetEquColor(im);
		}
	}
	
	static void SetEquColor( ImageData im )
	{
		for(int col = 1; col < 3; col++) {
			for(int i = 0; i < 4; i++)
				im.radial_params[col][i] = im.radial_params[0][i];
			im.vertical_params[col] = im.vertical_params[0];
			im.horizontal_params[col] = im.horizontal_params[0];
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
			ImageData im = images.get(i);
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
			optVars opt = g.image.get(i);
			if (image.hfov == 1 || (image.hfov > 1 &&  g.image.get(image.hfov-2).hfov == 1))
				optHfov = 1;
			else
				optHfov = 0;

			if (image.a == 1 || (image.a > 1 &&  g.image.get(image.a-2).a == 1 ))
				opta = 1;
			else
				opta = 0;
							
			if (image.b == 1 || (image.b > 1 &&  g.image.get(image.b-2).b == 1 ))
				optb = 1;
			else
				optb = 0;
							
			if (image.c == 1 || (image.c > 1 &&  g.image.get(image.c-2).c == 1 ))
				optc = 1;
			else
				optc = 0;*/

			fou.printf(Locale.US, "i w%d", im.width);
			fou.printf(Locale.US, " h%d", im.height);
			fou.printf(Locale.US, " f%d", format);
			fou.printf(Locale.US, " a%f", im.radial_params[0][3]);
			fou.printf(Locale.US, " b%f", im.radial_params[0][2]);
			fou.printf(Locale.US, " c%f", im.radial_params[0][1]);
			fou.printf(Locale.US, " d%f", im.horizontal_params[0]); 
			fou.printf(Locale.US, " e%f", im.vertical_params[0]);
			fou.printf(Locale.US, " g0");
			fou.printf(Locale.US, " p%f", im.pitch);
			fou.printf(Locale.US, " r%f", im.roll);
			fou.printf(Locale.US, " t0");
			fou.printf(Locale.US, " v%f", im.hfov);
			fou.printf(Locale.US, " y%f", im.yaw);
//			fou.printf(Locale.US, " u%d", stitchBuffer.feather);
			fou.printf(Locale.US, " n\"%s\"", im.name);
			fou.printf(Locale.US, "\n");
			
/*			fou.println(Locale.US, "i w" + Long.toString(im.width) + " h" + Long.toString(im.height) + " f" + Integer.toString(format) +
					" a" + Double.toString(im.radial_params[0][3]) + 
					" b" + Double.toString(im.radial_params[0][2]) + 
					" c" + Double.toString(im.radial_params[0][1]) + 
					" d" + Double.toString(im.horizontal_params[0]) + 
					" e" + Double.toString(im.vertical_params[0]) +
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
