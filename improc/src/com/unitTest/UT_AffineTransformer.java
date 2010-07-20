package com.unitTest;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.AffineTransformLearner;
import com.slavi.math.transform.AffineTransformer;
import com.slavi.math.transform.TransformLearnerResult;

public class UT_AffineTransformer {

	public static class MyTestData implements Map.Entry<Point2D.Double, Point2D.Double>{
		Point2D.Double src = new Point2D.Double();
		Point2D.Double dest = new Point2D.Double();
		boolean isBad;
		double discrepancy;

		public MyTestData() {
		}
		
		public MyTestData(AffineTransform jTransform, Point2D.Double s) {
			src.setLocation(s);
			jTransform.transform(src, dest);
		}

		public Double getKey() {
			return src;
		}

		public Double getValue() {
			return dest;
		}

		public Double setValue(Double value) {
			throw new RuntimeException("Method not allowed");
		}
	}
	
	public static class MyTestAffineTransformer extends AffineTransformer<Point2D.Double, Point2D.Double> {
		public int getInputSize() {
			return 2;
		}

		public int getOutputSize() {
			return 2;
		}

		private double getCoord(Point2D.Double item, int coordIndex) {
			switch (coordIndex) {
				case 0: return item.x;
				case 1: return item.y;
				default: throw new RuntimeException("Invalid coordinate");
			}
		}

		private void setCoord(Point2D.Double item, int coordIndex, double value) {
			switch (coordIndex) {
				case 0: 
					item.x = value;
					break;				
				case 1: 
					item.y = value;
					break;
				default: throw new RuntimeException("Invalid coordinate");
			}
		}
		
		public double getSourceCoord(Point2D.Double item, int coordIndex) {
			return getCoord(item, coordIndex);
		}

		public double getTargetCoord(Point2D.Double item, int coordIndex) {
			return getCoord(item, coordIndex);
		}

		public void setSourceCoord(Point2D.Double item, int coordIndex, double value) {
			setCoord(item, coordIndex, value);
		}

		public void setTargetCoord(Point2D.Double item, int coordIndex, double value) {
			setCoord(item, coordIndex, value);
		}
	}
	
	public static class MyTestAffineTransformLearner extends AffineTransformLearner<Point2D.Double, Point2D.Double> {

		public MyTestAffineTransformLearner(Iterable<? extends Map.Entry<Point2D.Double, Point2D.Double>> pointsPairList) {
			super(new MyTestAffineTransformer(), pointsPairList);
		}

		public Point2D.Double createTemporaryTargetObject() {
			return new Point2D.Double();
		}

		public double getDiscrepancy(Entry<Point2D.Double, Point2D.Double> item) {
			return ((MyTestData) item).discrepancy;
		}

		public double getWeight(Entry<Point2D.Double, Point2D.Double> item) {
			return 1.0;
		}

		public boolean isBad(Entry<Point2D.Double, Point2D.Double> item) {
			return ((MyTestData) item).isBad;
		}

		public void setBad(Entry<Point2D.Double, Point2D.Double> item, boolean bad) {
			((MyTestData) item).isBad = bad;
		}

		public void setDiscrepancy(Entry<Point2D.Double, Point2D.Double> item, double discrepancy) {
			((MyTestData) item).discrepancy = discrepancy;
		}
	}
	
	private ArrayList<MyTestData> points;
	
	AffineTransform jTransform;
	
	private MyTestAffineTransformLearner learner;

	void dumpAffineTransform(AffineTransform af) {
		double[] d = new double[6]; 
		af.getMatrix(d);
		for (int i = 0; i < d.length; i++)
			System.out.println(d[i]);
	}
	
	private void addp(int x, int y) {
		Point2D.Double sd = new Point2D.Double(x, y);
		MyTestData pair = new MyTestData(jTransform, sd);
		points.add(pair);
	}
	
	private void generatePoints() {
		jTransform = new AffineTransform();
		jTransform.setToIdentity();
		jTransform.rotate(30 * MathUtil.deg2rad);
		jTransform.scale(123.456, 789.123);
		jTransform.shear(1.234, 2.345);
		jTransform.translate(100.567, 200.123);

//		System.out.println("== The java.awt.geom.AffineTransform is:");
//		dumpAffineTransform(jTransform);
		
		points = new ArrayList<MyTestData>();
		for (int xcounter = 0; xcounter < 2; xcounter++) {
			for (int ycounter = 0; ycounter < 2; ycounter++) {
				addp(xcounter, ycounter);
			}
		}
	}
	
	public void learn() {
		learner = new MyTestAffineTransformLearner(points);
		TransformLearnerResult res = learner.calculateOne();
		TestUtils.assertTrue("Learner adjusted", res.isAdjusted());
	}
	
	public static void main(String[] args) {
		UT_AffineTransformer test = new UT_AffineTransformer();
		test.generatePoints();
		test.learn();

		Matrix delta = test.learner.computeTransformedTargetDelta(true);
		TestUtils.assertMatrix0("", delta);
		System.out.println("Done");
	}
}
