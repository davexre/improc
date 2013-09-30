package com.slavi.math;

import com.slavi.math.matrix.Matrix;

public interface Rotation3D {

	/**
	 * Returns a rotation matrix R=F(r1, r2, r3) 
	 */
	public Matrix makeAngles(double r1, double r2, double r3);

	/**
	 * Returns a rotation matrix R=F(r1, r2, r3)
	 * <pre>
	 * angles[0] = r1 
	 * angles[1] = r2 
	 * angles[2] = r3
	 * </pre> 
	 */
	public Matrix makeAngles(double angles[]);

	/**
	 * Transforms coordinates in source coordinate system into target coord system.
	 * To obtain a rotation matrix use {@link #makeAngles(double, double, double)}.
	 * The transformation is done as:
	 * <code>
	 * P = [x; y; z]
	 * DEST = ROT * P
	 * </code>
	 */
	public void transformForward(Matrix rot, double x, double y, double z, double dest[]);

	public void transformForward(Matrix rot, double src[], double dest[]);
	
	/**
	 * Transforms coordinates in target coordinate system into source coord system.
	 * To obtain a rotation matrix use {@link #makeAngles(double, double, double)}.
	 * The transformation is done as:
	 * <code>
	 * Since:
	 * Inverse(ROT) = Transpose(ROT)
	 * P1 = [x; y; z]
	 * DEST = Transpose(ROT) * P1
	 * </code>
	 */
	public void transformBackward(Matrix rot, double x, double y, double z, double dest[]);

	public void transformBackward(Matrix rot, double src[], double dest[]);

	/**
	 * Extracts the rotation angles that constructed the rotation matrix M.
	 * The angles are returned in the angles array as 
	 * angles[0] = rx,
	 * angles[1] = ry,
	 * angles[2] = rz
	 */
	public void getRotationAngles(Matrix m, double[] angles);

	/**
	 * Extracts the rotation angles that constructed the REVERSE rotation matrix M.
	 * The angles are returned in the angles array as 
	 * angles[0] = r1,
	 * angles[1] = r2,
	 * angles[2] = r3
	 */
	public void getRotationAnglesBackword(Matrix m, double[] angles);
	
	/**
	 * Computes the REVERSE rotation angles that constructed the REVERSE rotation matrix M.
	 * The angles are returned in the angles array as 
	 * angles[0] = r1,
	 * angles[1] = r2,
	 * angles[2] = r3
	 */
	public void getRotationAnglesBackword(double r1, double r2, double r3, double[] angles);

	public Matrix make_dF_dR1(double r1, double r2, double r3);
	
	public Matrix make_dF_dR2(double r1, double r2, double r3);
	
	public Matrix make_dF_dR3(double r1, double r2, double r3);
}
