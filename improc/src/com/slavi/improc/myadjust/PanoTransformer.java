package com.slavi.improc.myadjust;

import java.util.ArrayList;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPairList;
import com.slavi.math.transform.TransformLearnerResult;

public abstract class PanoTransformer {

	public ArrayList<KeyPointPairList> chain;
	public ArrayList<KeyPointList> images;
	public ArrayList<KeyPointPairList> ignoredPairLists;
	public KeyPointList origin;
	
	public abstract void initialize(ArrayList<KeyPointPairList> chain);
	
	public abstract TransformLearnerResult calculateOne();
	
	public abstract double getDiscrepancyThreshold();
	
	/**
	 * Transforms from source image coordinate system into world coord.system.
	 * @param sx, sy	Coordinates in pixels of the source image with origin pixel(0,0)
	 * @param dest[3]	OUTPUT: The transformed coordinates in radians.  
	 * 					dest[0] = Longitude
	 * 					dest[1] = Zenith (pi/2-Latitude)
	 * 					dest[2] = r - radius, i.e. distance from the image pixel
	 * 						to the focal point or origin of the 3D image coordinate system. 
	 */
	public abstract void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]);
	
	/**
	 * Transforms from world coordinate system into source image coord.system. 
	 * @param rx		Longitude
	 * @param ry		Zenith (pi/2-Latitude)
	 * @param dest[3]	OUTPUT: The transformed coordinates. 
	 * 					dest[0] = x in image coordinates
	 * 					dest[1] = y in image coordinates
	 * 					dest[2] = r - radius, i.e. distance from the image pixel
	 * 						to the focal point or origin of the 3D image coordinate system. 
	 * 						If r > 0 coordinates are ok.
	 * 						If r <=0 the specified rx,ry are outside of the source image (on 
	 * 						the opposite side of the sphere) 
	 */
	public abstract void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]);
}
