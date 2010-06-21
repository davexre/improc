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
	
	public abstract void transformForeward(double sx, double sy, KeyPointList srcImage, double dest[]);
	
	public abstract void transformBackward(double rx, double ry, KeyPointList srcImage, double dest[]);
}
