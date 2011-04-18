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
public class RotationZYX implements Rotation3D {
	public static final RotationZYX instance = new RotationZYX();

	/**
	 * Return a rotation matrix R=R3*R2*R1
	 */
	public Matrix makeAngles(double r1, double r2, double r3) {
		/*
		 *  R1            R2            R3
		 *  1   0   0     c2  0  s2     c3 -s3  0
		 *  0  c1 -s1      0  1   0     s3  c3  0
		 *  0  s1  c1    -s2  0  c2      0   0  1
		 */
		double s1 = Math.sin(r1);
		double c1 = Math.cos(r1);
		
		double s2 = Math.sin(r2);
		double c2 = Math.cos(r2);

		double s3 = Math.sin(r3);
		double c3 = Math.cos(r3);

		/*
		 * R3*R2*R1
		 * c1*c2			-s1*c2			s2
		 * s1*c3+c1*s2*s3	c1*c3-s1*s2*s3	-c2*s3
		 * s1*s3-c1*s2*c3	c1*s3+s1*s2*c3	c2*c3
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, c1*c2);
		r.setItem(1, 0, -s1*c2);
		r.setItem(2, 0, s2);
		
		r.setItem(0, 1, s1*c3+c1*s2*s3);
		r.setItem(1, 1, c1*c3-s1*s2*s3);
		r.setItem(2, 1, -c2*s3);
		
		r.setItem(0, 2, s1*s3-c1*s2*c3);
		r.setItem(1, 2, c1*s3+s1*s2*c3);
		r.setItem(2, 2, c2*c3);
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
	 * angles[0] = r1,
	 * angles[1] = r2,
	 * angles[2] = r3
	 */
	public void getRotationAngles(Matrix m, double[] angles) {
		/*
		 * R3*R2*R1
		 * c1*c2			-s1*c2			s2
		 * s1*c3+c1*s2*s3	c1*c3-s1*s2*s3	-c2*s3
		 * s1*s3-c1*s2*c3	c1*s3+s1*s2*c3	c2*c3
		 */
		angles[0] = Math.atan2(-m.getItem(1, 0), m.getItem(0, 0));
		angles[1] = Math.asin(m.getItem(2, 0));
		angles[2] = Math.atan2(-m.getItem(2, 1), m.getItem(2, 2));
	}	
	
	/**
	 * Extracts the rotation angles that constructed the REVERSE rotation matrix M.
	 * The angles are returned in the angles array as 
	 * angles[0] = r1,
	 * angles[1] = r2,
	 * angles[2] = r3
	 */
	public void getRotationAnglesBackword(Matrix m, double[] angles) {
		/*
		 * M_foreward = R3*R2*R1
		 * c1*c2			-s1*c2			s2
		 * s1*c3+c1*s2*s3	c1*c3-s1*s2*s3	-c2*s3
		 * s1*s3-c1*s2*c3	c1*s3+s1*s2*c3	c2*c3
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
	 * angles[0] = r1,
	 * angles[1] = r2,
	 * angles[2] = r3
	 */
	public void getRotationAnglesBackword(double r1, double r2, double r3, double[] angles) {
		/*
		 * M_foreward = R3*R2*R1
		 * c1*c2			-s1*c2			s2
		 * s1*c3+c1*s2*s3	c1*c3-s1*s2*s3	-c2*s3
		 * s1*s3-c1*s2*c3	c1*s3+s1*s2*c3	c2*c3
		 * 
		 * M_backword = Transpose(M_foreward)
		 */
		double s1 = Math.sin(r1);
		double c1 = Math.cos(r1);
		
		double s2 = Math.sin(r2);
		double c2 = Math.cos(r2);

		double s3 = Math.sin(r3);
		double c3 = Math.cos(r3);

		angles[0] = Math.atan2(-(s1*c3+c1*s2*s3), c1*c2);
		angles[1] = Math.asin(s1*s3-c1*s2*c3);
		angles[2] = Math.atan2(-(c1*s3+s1*s2*c3), c2*c3);
	}	

	/*
	 * (sinX)' = cosX
	 * (cosX)' = -sinX
	 */

	public Matrix make_dF_dR3(double r1, double r2, double r3) {
		double s1 = Math.sin(r1);
		double c1 = Math.cos(r1);
		
		double s2 = Math.sin(r2);
		double c2 = Math.cos(r2);

		double s3 = Math.sin(r3);
		double c3 = Math.cos(r3);

		/*
		 * R3*R2*R1
		 * c1*c2			-s1*c2			s2
		 * s1*c3+c1*s2*s3	c1*c3-s1*s2*s3	-c2*s3
		 * s1*s3-c1*s2*c3	c1*s3+s1*s2*c3	c2*c3
		 *
		 * dF/d(r3)
		 * 0				0				0
		 * -s1*s3+c1*s2*c3	-c1*s3-s1*s2*c3	-c2*c3
		 * s1*c3+c1*s2*s3	c1*c3-s1*s2*s3	-c2*s3
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, 0);
		r.setItem(1, 0, 0);
		r.setItem(2, 0, 0);
		r.setItem(0, 1, -s1*s3+c1*s2*c3);
		r.setItem(1, 1, -c1*s3-s1*s2*c3);
		r.setItem(2, 1, -c2*c3);
		r.setItem(0, 2, s1*c3+c1*s2*s3);
		r.setItem(1, 2, c1*c3-s1*s2*s3);
		r.setItem(2, 2, -c2*s3);
		return r;
	}
	
	public Matrix make_dF_dR2(double r1, double r2, double r3) {
		double s1 = Math.sin(r1);
		double c1 = Math.cos(r1);
		
		double s2 = Math.sin(r2);
		double c2 = Math.cos(r2);

		double s3 = Math.sin(r3);
		double c3 = Math.cos(r3);

		/*
		 * R3*R2*R1
		 * c1*c2			-s1*c2			s2
		 * s1*c3+c1*s2*s3	c1*c3-s1*s2*s3	-c2*s3
		 * s1*s3-c1*s2*c3	c1*s3+s1*s2*c3	c2*c3
		 *
		 * dF/d(r2)
		 * -c1*s2			s1*s2			c2
		 * c1*c2*s3			-s1*c2*s3		s2*s3
		 * -c1*c2*c3		s1*c2*c3		-s2*c3
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, -c1*s2);
		r.setItem(1, 0, s1*s2);
		r.setItem(2, 0, c2);
		r.setItem(0, 1, c1*c2*s3);
		r.setItem(1, 1, -s1*c2*s3);
		r.setItem(2, 1, s2*s3);
		r.setItem(0, 2, -c1*c2*c3);
		r.setItem(1, 2, s1*c2*c3);
		r.setItem(2, 2, -s2*c3);
		return r;
	}
	
	public Matrix make_dF_dR1(double r1, double r2, double r3) {
		double s1 = Math.sin(r1);
		double c1 = Math.cos(r1);
		
		double s2 = Math.sin(r2);
		double c2 = Math.cos(r2);

		double s3 = Math.sin(r3);
		double c3 = Math.cos(r3);

		/*
		 * R3*R2*R1
		 * c1*c2			-s1*c2			s2
		 * s1*c3+c1*s2*s3	c1*c3-s1*s2*s3	-c2*s3
		 * s1*s3-c1*s2*c3	c1*s3+s1*s2*c3	c2*c3
		 *
		 * dF/d(r1)
		 * -s1*c2			-c1*c2			0
		 * c1*c3-s1*s2*s3	-s1*c3-c1*s2*s3	0
		 * c1*s3+s1*s2*c3	-s1*s3+c1*s2*c3	0
		 */
		Matrix r = new Matrix(3, 3);
		r.setItem(0, 0, -s1*c2);
		r.setItem(1, 0, -c1*c2);
		r.setItem(2, 0, 0);
		r.setItem(0, 1, c1*c3-s1*s2*s3);
		r.setItem(1, 1, -s1*c3-c1*s2*s3);
		r.setItem(2, 1, 0);
		r.setItem(0, 2, c1*s3+s1*s2*c3);
		r.setItem(1, 2, -s1*s3+c1*s2*c3);
		r.setItem(2, 2, 0);
		return r;
	}
}
