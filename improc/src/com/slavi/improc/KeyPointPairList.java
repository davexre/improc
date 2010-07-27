package com.slavi.improc;

import java.util.ArrayList;

import com.slavi.math.transform.TransformLearnerResult;

public class KeyPointPairList {
	/**
	 * Mapping between source points and KeyPointPairs
	 */
	public final ArrayList<KeyPointPair> items = new ArrayList<KeyPointPair>();
	
	public KeyPointList source = null;

	public KeyPointList target = null;

	// Helmert transformation parameters (Validate keypoint pairs)
	public double scale;
	public double angle;
	public double translateX;
	public double translateY;
	
	// Spherical pano adjust
	public double sphereRZ1;
	public double sphereRY;
	public double sphereRZ2;

	// ZYX and XYZ pano adjust
	public double rx, ry, rz; // source->target angles of rotation

	// Helmert pano adjust
	public double a;
	public double b;
	public double hTranslateX; // c
	public double hTranslateY; // d
	
	public double maxDiscrepancy;
	public double recoverDiscrepancy;
	public TransformLearnerResult transformResult;
}
