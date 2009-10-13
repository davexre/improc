package com.slavi.math;

import com.slavi.math.matrix.Matrix;

/**
 * Methods to be used with a Tait-Bryan angles <b>right-handed</b> coordinate 
 * system (<a href="http://en.wikipedia.org/wiki/Yaw,_pitch,_and_roll">yaw-pitch-roll</a>).
 * <p>
 * Yaw, pitch and roll are used in aerospace to define a rotation between 
 * a reference axis system and a vehicle-fixed axis system.
 * <p>
 * Consider an aircraft-body coordinate system with axes XYZ 
 * (sometimes named roll, pitch and yaw axes) which is fixed to the vehicle, 
 * rotating and translating with it. This intrinsic frame of the vehicle, 
 * XYZ system, is oriented such that the X-axis points forward along some 
 * convenient reference line along the body, the Y-axis points to the 
 * right of the vehicle along the wing, and the Z-axis points downward to 
 * form an orthogonal <b>right-handed</b> system.
 * <p>
 * Consider a coordinate system xyz, aligned having x pointing in the 
 * direction of true north, y pointing to true east, and the z-axis 
 * pointing down, normal to the local horizontal direction.
 * <p>
 * Given this setting, the rotation sequence from xyz to XYZ is specified 
 * by and defines the angles yaw, pitch and roll as follows:
 * <ul>
 * <li>Right-handed rotation Yaw/Psi ψ (-180, 180] about the z-axis</li>
 * <li>Right-handed rotation Pitch/Theta θ [-90, 90] about the new (once-rotated) y-axis</li>
 * <li>Right-handed rotation Roll/Phi φ [-180, 180] about the new (twice-rotated) x-axis</li>
 * </ul>
 * <p>
 * In order to convert a point in xyz to XYZ coordinates, one applies the matrix M
 * to the point: p(xyz)=M * p(XYZ), the point is represented as one COLUMN matrix.
 * The matrix M is constructed by the method makeAngles. The above transformation
 * is carried out be the method transformForeward.
 * <p>
 * In order to do the backword convertion, i.e. convert a point in XYZ to 
 * xyz coordinates use the method transformBackword, using the same matirx M.
 * <p> 
 * The default coordinate system in OpenGL(TM) is right-handed: the 
 * positive x and y axes point right and up, and the negative z axis 
 * points forward. Positive rotation is counterclockwise about the axis 
 * of rotation.
 * <p>
 * The points are represented as one ROW matirces. The matrix returned
 * by the method makeAngles is to be used as <code>p'=p*M</code>.
 * <p>
 * <div style="border: 1px solid;width:200px;padding:3px">
 *   <img src="doc-files/RotationZYX-RightHanded.jpg" width="100%" />
 *   <p style="text-align: center">Definition of right-handed coordinate system</p>
 * </div><br>
 * <div style="border: 1px solid;width:200px;padding:3px">
 *   <img src="doc-files/RotationZYX-YawPitchRoll.jpg" width="100%" />
 *   <p style="text-align: center">The position of all three axes</p>
 * </div><br>
 * <div style="border: 1px solid;width:200px;padding:3px">
 *   <img src="doc-files/RotationZYX-Plane.png" width="100%" />
 *   <p style="text-align: center">Tait-Bryan angles for an aircraft</p>
 * </div><br>
 * <div style="border: 1px solid;width:200px;padding:3px">
 *   <img src="doc-files/RotationZYX-RightX.gif" width="100%" />
 *   <p style="text-align: center">Positive X rotation</p>
 * </div><br>
 * <div style="border: 1px solid;width:200px;padding:3px">
 *   <img src="doc-files/RotationZYX-RightY.gif" width="100%" />
 *   <p style="text-align: center">Positive Y rotation</p>
 * </div><br>
 * <div style="border: 1px solid;width:200px;padding:3px">
 *   <img src="doc-files/RotationZYX-RightZ.gif" width="100%" />
 *   <p style="text-align: center">Positive Z rotation</p>
 * </div><br>
 */
public class RotationZYX {
	public static final RotationZYX instance = new RotationZYX();

	/**
	 * Return a rotation matrix R=Rz*Ry*Rx
	 */
	public Matrix makeAngles(double rx, double ry, double rz) {
		/*
		 *  Rx            Ry            Rz
		 *  1   0   0     cy  0  sy     cz -sz  0
		 *  0  cx -sx      0  1   0     sz  cz  0
		 *  0  sx  cx    -sy  0  cy      0   0  1
		 */
		double sx = Math.sin(rx);
		double cx = Math.cos(rx);
		
		double sy = Math.sin(ry);
		double cy = Math.cos(ry);

		double sz = Math.sin(rz);
		double cz = Math.cos(rz);

		/*
		 * Rz*Ry*Rx
		 * cx*cy			-sx*cy			sy
		 * sx*cz+cx*sy*sz	cx*cz-sx*sy*sz	-cy*sz
		 * sx*sz-cx*sy*cz	cx*sz+sx*sy*cz	cy*cz
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, cx*cy);
		r.setItem(1, 0, -sx*cy);
		r.setItem(2, 0, sy);
		
		r.setItem(0, 1, sx*cz+cx*sy*sz);
		r.setItem(1, 1, cx*cz-sx*sy*sz);
		r.setItem(2, 1, -cy*sz);
		
		r.setItem(0, 2, sx*sz-cx*sy*cz);
		r.setItem(1, 2, cx*sz+sx*sy*cz);
		r.setItem(2, 2, cy*cz);
		return r;
	}

	public Matrix makeAngles(double angles[]) {
		return makeAngles(angles[0], angles[1], angles[2]);
	}
	
	/**
	 * Transforms coordinates in source coordinate system into target coord system.
	 * To obtain a rotation matrix use {@link #makeAngles(double, double, double)}.
	 * The transformation is done as:
	 * <code>
	 * P = [x; y; z]
	 * DEST = ROT * P
	 * </code>
	 */
	public void transformForward(Matrix rot,
			double x, double y, double z, double dest[]) {
		dest[0] = x * rot.getItem(0, 0) + y * rot.getItem(1, 0) + z * rot.getItem(2, 0);
		dest[1] = x * rot.getItem(0, 1) + y * rot.getItem(1, 1) + z * rot.getItem(2, 1);
		dest[2] = x * rot.getItem(0, 2) + y * rot.getItem(1, 2) + z * rot.getItem(2, 2);
	}
	
	public void transformForward(Matrix rot, double src[], double dest[]) {
		transformForward(rot, src[0], src[1], src[2], dest);
	}
	
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
	public void transformBackward(Matrix rot,
			double x, double y, double z, double dest[]) {
		dest[0] = x * rot.getItem(0, 0) + y * rot.getItem(0, 1) + z * rot.getItem(0, 2);
		dest[1] = x * rot.getItem(1, 0) + y * rot.getItem(1, 1) + z * rot.getItem(1, 2);
		dest[2] = x * rot.getItem(2, 0) + y * rot.getItem(2, 1) + z * rot.getItem(2, 2);
	}
	
	public void transformBackward(Matrix rot, double src[], double dest[]) {
		transformBackward(rot, src[0], src[1], src[2], dest);
	}
	
	/**
	 * Extracts the rotation angles that constructed the rotation matrix M.
	 * The angles are returned in the angles array as 
	 * angles[0] = rx,
	 * angles[1] = ry,
	 * angles[2] = rz
	 */
	public void getRotationAngles(Matrix m, double[] angles) {
		/*
		 * Rz*Ry*Rx
		 * cx*cy			-sx*cy			sy
		 * sx*cz+cx*sy*sz	cx*cz-sx*sy*sz	-cy*sz
		 * sx*sz-cx*sy*cz	cx*sz+sx*sy*cz	cy*cz
		 */
		angles[0] = Math.atan2(-m.getItem(1, 0), m.getItem(0, 0));
		angles[1] = Math.asin(m.getItem(2, 0));
		angles[2] = Math.atan2(-m.getItem(2, 1), m.getItem(2, 2));
	}	
	
	/**
	 * Extracts the rotation angles that constructed the REVERSE rotation matrix M.
	 * The angles are returned in the angles array as 
	 * angles[0] = rx,
	 * angles[1] = ry,
	 * angles[2] = rz
	 */
	public void getRotationAnglesBackword(Matrix m, double[] angles) {
		/*
		 * M_foreward = Rz*Ry*Rx
		 * cx*cy			-sx*cy			sy
		 * sx*cz+cx*sy*sz	cx*cz-sx*sy*sz	-cy*sz
		 * sx*sz-cx*sy*cz	cx*sz+sx*sy*cz	cy*cz
		 * 
		 * M_backword = Transpose(M_foreward)
		 */
		angles[0] = Math.atan2(-m.getItem(0, 1), m.getItem(0, 0));
		angles[1] = Math.asin(m.getItem(0, 2));
		angles[2] = Math.atan2(-m.getItem(1, 2), m.getItem(2, 2));
	}	
	
	/**
	 * Computes the REVERSE rotation angles that constructed the REVERSE rotation matrix M.
	 * The angles are returned in the angles array as 
	 * angles[0] = rx,
	 * angles[1] = ry,
	 * angles[2] = rz
	 */
	public void getRotationAnglesBackword(double rx, double ry, double rz, double[] angles) {
		/*
		 * M_foreward = Rz*Ry*Rx
		 * cx*cy			-sx*cy			sy
		 * sx*cz+cx*sy*sz	cx*cz-sx*sy*sz	-cy*sz
		 * sx*sz-cx*sy*cz	cx*sz+sx*sy*cz	cy*cz
		 * 
		 * M_backword = Transpose(M_foreward)
		 */
		double sx = Math.sin(rx);
		double cx = Math.cos(rx);
		
		double sy = Math.sin(ry);
		double cy = Math.cos(ry);

		double sz = Math.sin(rz);
		double cz = Math.cos(rz);

		angles[0] = Math.atan2(-(sx*cz+cx*sy*sz), cx*cy);
		angles[1] = Math.asin(sx*sz-cx*sy*cz);
		angles[2] = Math.atan2(-(cx*sz+sx*sy*cz), cy*cz);
	}	

	/*
	 * (sinX)' = cosX
	 * (cosX)' = -sinX
	 */

	public Matrix make_dF_dZ(double rx, double ry, double rz) {
		double sx = Math.sin(rx);
		double cx = Math.cos(rx);
		
		double sy = Math.sin(ry);
		double cy = Math.cos(ry);

		double sz = Math.sin(rz);
		double cz = Math.cos(rz);

		/*
		 * Rz*Ry*Rx
		 * cx*cy			-sx*cy			sy
		 * sx*cz+cx*sy*sz	cx*cz-sx*sy*sz	-cy*sz
		 * sx*sz-cx*sy*cz	cx*sz+sx*sy*cz	cy*cz
		 *
		 * dF/d(rz)
		 * 0				0				0
		 * -sx*sz+cx*sy*cz	-cx*sz-sx*sy*cz	-cy*cz
		 * sx*cz+cx*sy*sz	cx*cz-sx*sy*sz	-cy*sz
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, 0);
		r.setItem(1, 0, 0);
		r.setItem(2, 0, 0);
		r.setItem(0, 1, -sx*sz+cx*sy*cz);
		r.setItem(1, 1, -cx*sz-sx*sy*cz);
		r.setItem(2, 1, -cy*cz);
		r.setItem(0, 2, sx*cz+cx*sy*sz);
		r.setItem(1, 2, cx*cz-sx*sy*sz);
		r.setItem(2, 2, -cy*sz);
		return r;
	}
	
	public Matrix make_dF_dY(double rx, double ry, double rz) {
		double sx = Math.sin(rx);
		double cx = Math.cos(rx);
		
		double sy = Math.sin(ry);
		double cy = Math.cos(ry);

		double sz = Math.sin(rz);
		double cz = Math.cos(rz);

		/*
		 * Rz*Ry*Rx
		 * cx*cy			-sx*cy			sy
		 * sx*cz+cx*sy*sz	cx*cz-sx*sy*sz	-cy*sz
		 * sx*sz-cx*sy*cz	cx*sz+sx*sy*cz	cy*cz
		 *
		 * dF/d(ry)
		 * -cx*sy			sx*sy			cy
		 * cx*cy*sz			-sx*cy*sz		sy*sz
		 * -cx*cy*cz		sx*cy*cz		-sy*cz
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, -cx*sy);
		r.setItem(1, 0, sx*sy);
		r.setItem(2, 0, cy);
		r.setItem(0, 1, cx*cy*sz);
		r.setItem(1, 1, -sx*cy*sz);
		r.setItem(2, 1, sy*sz);
		r.setItem(0, 2, -cx*cy*cz);
		r.setItem(1, 2, sx*cy*cz);
		r.setItem(2, 2, -sy*cz);
		return r;
	}
	
	public Matrix make_dF_dX(double rx, double ry, double rz) {
		double sx = Math.sin(rx);
		double cx = Math.cos(rx);
		
		double sy = Math.sin(ry);
		double cy = Math.cos(ry);

		double sz = Math.sin(rz);
		double cz = Math.cos(rz);

		/*
		 * Rz*Ry*Rx
		 * cx*cy			-sx*cy			sy
		 * sx*cz+cx*sy*sz	cx*cz-sx*sy*sz	-cy*sz
		 * sx*sz-cx*sy*cz	cx*sz+sx*sy*cz	cy*cz
		 *
		 * dF/d(rx)
		 * -sx*cy			-cx*cy			0
		 * cx*cz-sx*sy*sz	-sx*cz-cx*sy*sz	0
		 * cx*sz+sx*sy*cz	-sx*sz+cx*sy*cz	0
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, -sx*cy);
		r.setItem(1, 0, -cx*cy);
		r.setItem(2, 0, 0);
		r.setItem(0, 1, cx*cz-sx*sy*sz);
		r.setItem(1, 1, -sx*cz-cx*sy*sz);
		r.setItem(2, 1, 0);
		r.setItem(0, 2, cx*sz+sx*sy*cz);
		r.setItem(1, 2, -sx*sz+cx*sy*cz);
		r.setItem(2, 2, 0);
		return r;
	}
}
