package com.test.math;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.Helmert2DTransformLearner;
import com.slavi.math.transform.Helmert2DTransformer;
import com.slavi.math.transform.PointsPair;

public class TestHelmert2DTransformer {

	private static double degreeToRad = Math.PI / 180;
	
	private ArrayList points;
	
	AffineTransform jTransform;
	
	private Helmert2DTransformLearner learner;

	private void dumpAffineTransform(AffineTransform af) {
		double[] d = new double[6]; 
		af.getMatrix(d);
		for (int i = 0; i < d.length; i++)
			System.out.println(d[i]);
	}
	
	protected Matrix point2DToMatrix(Point2D source) {
		Matrix r = new Matrix(2, 1);
		r.setItem(0, 0, source.getX());
		r.setItem(1, 0, source.getY());
		return r;
	}
	
	public class TestPointPair extends PointsPair {
		public Point2D.Double source2D;
		public Point2D.Double target2D;
		
		public TestPointPair(Point2D.Double s) {
			super();
			source2D = s;
			target2D = new Point2D.Double();
			jTransform.transform(source2D, target2D);
			source = point2DToMatrix(source2D);
			target = point2DToMatrix(target2D);
			sourceTransformed = new Matrix(2, 1);
		}
	}
		
	private void addp(int x, int y) {
		Point2D.Double sd = new Point2D.Double(x, y);
		PointsPair pair = new TestPointPair(sd);
		points.add(pair);
	}
	
	private void generatePoints() {
		jTransform = new AffineTransform();
		jTransform.setToIdentity();
		jTransform.rotate(30 * degreeToRad);
		jTransform.scale(123.456, 123.456);
		jTransform.translate(100.567, 200.123);

		System.out.println("== The java.awt.geom.AffineTransform is:");
		dumpAffineTransform(jTransform);
		
		points = new ArrayList();
		for (int xcounter = 0; xcounter < 5; xcounter++) {
			for (int ycounter = 0; ycounter < 3; ycounter++) {
				addp(xcounter, ycounter);
			}
		}
	}
	
	public void learn() {
		TestPointPair pair;
		learner = new Helmert2DTransformLearner(points);

		boolean res = learner.calculateOne();
		Helmert2DTransformer tr = (Helmert2DTransformer) learner.transformer;
		System.out.println("Learner adjusted: " + res);
		System.out.println(tr.toString());
		
		Matrix dest = new Matrix(2, 1);
		pair = (TestPointPair)points.get(0);
		tr.transform(pair.source, dest);
		System.out.println("" + pair.target2D.x + "\t" + pair.target2D.y);
		System.out.println("" + dest.getItem(0,0) + "\t" + dest.getItem(1,0));
	}
	
	public static void main(String[] args) {
		TestHelmert2DTransformer test = new TestHelmert2DTransformer();
		test.generatePoints();
		test.learn();

		Matrix delta = test.learner.computeTransformedTargetDelta(false);
		System.out.println("==== max discrepancy ====");
		System.out.println(delta.toString());
		delta = test.learner.computeTransformedTargetDelta(true);
		System.out.println("==== max discrepancy 2 ====");
		System.out.println(delta.toString());
	}
}
