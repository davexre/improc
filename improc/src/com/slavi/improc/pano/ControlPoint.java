package com.slavi.improc.pano;

/**
 * Control Points to adjust images
 */
public class ControlPoint {
	public static enum OptimizeType {
		r, x, y;
	}

	Image image0, image1;	//int[]  num = new int[2];			// Indices of Images 
	double x0, x1;			//double[] x = new double[2];		// x - Coordinates 
	double y0, y1;			//double[] y = new double[2];		// y - Coordinates 
	OptimizeType type;		// What to optimize: 0-r, 1-x, 2-y
	double distanceComponent0, distanceComponent1;
	
	public String toString() {
		return "x0=" + x0 + " y0=" + y0 + " x1=" + x1 + " y1=" + y1;
	}
}
