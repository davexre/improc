package com.test.math.RotationAdjust;

import java.util.ArrayList;
import java.util.List;

import com.slavi.math.RotationXYZ;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.BaseTransformer;

public class Utils {

	public static List<MyPoint3D> generateRealPoints() {
		ArrayList<MyPoint3D> result = new ArrayList<MyPoint3D>();
		for (int x = 0; x < 3; x++)
			for (int y = 0; y < 2; y++)
				for (int z = 0; z < 2; z++) {
					MyPoint3D p = new MyPoint3D();
					p.p.setItem(0, 0, x);
					p.p.setItem(0, 1, y);
					p.p.setItem(0, 2, z);
					result.add(p);
				}					
		return result;
	}

	public static MyCamera[] generateCameras(MyPoint3D cameraOrigin, double cameraAngles[][]) {
		MyCamera[] result = new MyCamera[cameraAngles.length];
		int cameraId = 1;
		for (int i = 0; i < cameraAngles.length; i++) {
			double[] data = cameraAngles[i];
			MyCamera c = new MyCamera();
			c.cameraId = cameraId++;
			c.realOrigin = cameraOrigin;
			c.real2camera = RotationXYZ.makeAngles(data[0], data[1], data[2]);
			c.angles = data;
			c.realFocalDistance = data[3];
			c.camera2real = c.real2camera.makeCopy();
			c.camera2real.inverse();
			c.rx = data[0];
			c.ry = data[1];
			c.rz = data[2];
			result[i] = c;
		}		
		return result;
	}
	
	public static List<MyPointPair> generatePointPairs(MyCamera cameras[], List<MyPoint3D> realPoints) {
		ArrayList<MyPointPair> result = new ArrayList<MyPointPair>();
		Matrix tmp1 = new Matrix(1, 3);
		Matrix tmp2 = new Matrix(1, 3);
		
		for (int cameraIndex = 0; cameraIndex < cameras.length; cameraIndex++) {
			MyCamera src = cameras[cameraIndex];
			MyCamera dest = cameras[(cameraIndex + 1) % cameras.length];

			for (MyPoint3D p : realPoints) {
				MyPointPair pp = new MyPointPair();
				pp.realPoint = p;
		
				p.p.mSub(src.realOrigin.p, tmp1);
				src.real2camera.mMul(tmp1, tmp2);
				double z = tmp2.getItem(0, 2);
				if (z <= 0.0)
					continue;
				double scale = src.realFocalDistance / z;
				pp.srcPoint = new MyImagePoint();
				pp.srcPoint.camera = src;
				pp.srcPoint.x = tmp2.getItem(0, 0) * scale;
				pp.srcPoint.y = tmp2.getItem(0, 1) * scale;

				p.p.mSub(dest.realOrigin.p, tmp1);
				dest.real2camera.mMul(tmp1, tmp2);
				z = tmp2.getItem(0, 2);
				if (z <= 0.0)
					continue;
				scale = dest.realFocalDistance / z;
				pp.destPoint = new MyImagePoint();
				pp.destPoint.camera = dest;
				pp.destPoint.x = tmp2.getItem(0, 0) * scale;
				pp.destPoint.y = tmp2.getItem(0, 1) * scale;

				result.add(pp);
			}
		}
		return result;
	}
	
	public static void calculateDiscrepancy(MyCamera[] cameras, 
			List<MyPointPair> pointPairs, BaseTransformer<MyImagePoint, MyImagePoint> tr) {
		MyImagePoint p1 = new MyImagePoint();
		MyImagePoint p2 = new MyImagePoint();
		
		for (MyCamera camera : cameras) {
			camera.stat = new Statistics();
			camera.stat.start();
		}
		
		Statistics stat = new Statistics();
		stat.start();
		for (MyPointPair pair : pointPairs) {
/*			p1.x = pair.srcPoint.x;
			p1.y = pair.srcPoint.y;
			p1.z = pair.srcPoint.camera.realFocalDistance;
			p2.x = pair.destPoint.x;
			p2.y = pair.destPoint.y;
			p2.z = pair.destPoint.camera.realFocalDistance;
*/			
			tr.transform(pair.srcPoint, p1);
			tr.transform(pair.destPoint, p2);

			///////////////
			pair.myDiscrepancy = Math.sqrt(
				Math.pow(p1.y*p2.z - p2.y*p1.z, 2) +	
				Math.pow(p1.x*p2.z - p2.x*p1.z, 2) +	
				Math.pow(p1.x*p2.y - p2.x*p1.y, 2)	
				);
			stat.addValue(pair.myDiscrepancy);
			pair.srcPoint.camera.stat.addValue(pair.myDiscrepancy);
			pair.destPoint.camera.stat.addValue(pair.myDiscrepancy);
		}
		stat.stop();
		System.out.println("MyDiscrepancy statistics:");
		System.out.println(stat.toString(Statistics.CStatMinMax));

		for (MyCamera camera : cameras) {
			camera.stat.stop();
			System.out.println();
			System.out.println("Camera " + camera.cameraId);
			System.out.println(camera.stat.toString(Statistics.CStatMinMax));
		}
	}
	
}
