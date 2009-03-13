package com.test.math.RotationAdjust;

import com.slavi.math.matrix.Matrix;

public class MyCamera {
	public int cameraId;
	public MyPoint3D realOrigin;
	public Matrix real2camera;
	public double angles[];
	public double realFocalDistance;
	
	public Matrix camera2real;
	public double rx, ry, rz, scale;
	public double scaleZ = 1.0;
	public Matrix dMdX, dMdY, dMdZ;
	
	public int imageSizeX, imageSizeY;
	public double originX, originY;
}
