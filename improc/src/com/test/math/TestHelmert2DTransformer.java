package com.test.math;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.Helmert2DTransformLearner;
import com.slavi.math.transform.Helmert2DTransformer;

public class TestHelmert2DTransformer {

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

		public Point2D.Double getKey() {
			return src;
		}

		public Point2D.Double getValue() {
			return dest;
		}

		public Point2D.Double setValue(Point2D.Double value) {
			throw new RuntimeException("Method not allowed");
		}
	}
	
	public static class MyTestHelmert2DTransformer extends Helmert2DTransformer<Point2D.Double, Point2D.Double> {
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
	
	public static class MyTestHelmert2DTransformLearner extends Helmert2DTransformLearner<Point2D.Double, Point2D.Double> {
		public MyTestHelmert2DTransformLearner(MyTestHelmert2DTransformer transformer,
				Iterable<? extends Map.Entry<Point2D.Double, Point2D.Double>> pointsPairList) {
			super(transformer, pointsPairList);
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
	
	private static double degreeToRad = Math.PI / 180;
	
	private ArrayList<MyTestData> points;
	
	AffineTransform jTransform;
	
	private MyTestHelmert2DTransformLearner learner;

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
	
	private void addp(int x, int y) {
		Point2D.Double sd = new Point2D.Double(x, y);
		MyTestData pair = new MyTestData(jTransform, sd);
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
		
		points = new ArrayList<MyTestData>();
		for (int xcounter = 0; xcounter < 5; xcounter++) {
			for (int ycounter = 0; ycounter < 3; ycounter++) {
				addp(xcounter, ycounter);
			}
		}
	}
	
	public void learn() {
		MyTestData pair;
		learner = new MyTestHelmert2DTransformLearner(new MyTestHelmert2DTransformer(), points);

		boolean res = learner.calculateOne();
		Helmert2DTransformer<Point2D.Double, Point2D.Double> tr = (Helmert2DTransformer<Point2D.Double, Point2D.Double>) learner.transformer;
		System.out.println("Learner adjusted: " + res);
		System.out.println(tr.toString());
		
		Point2D.Double dest = new Point2D.Double();
		pair = points.get(0);
		tr.transform(pair.src, dest);
		System.out.println("" + pair.dest.x + "\t" + pair.dest.y);
		System.out.println("" + dest.x + "\t" + dest.y);
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
