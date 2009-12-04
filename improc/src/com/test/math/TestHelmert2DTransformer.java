package com.test.math;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.Helmert2DTransformLearner;
import com.slavi.math.transform.Helmert2DTransformer;
import com.slavi.math.transform.TransformLearnerResult;

public class TestHelmert2DTransformer {

	public static class MyTestData implements Map.Entry<Point2D.Double, Point2D.Double>{
		Point2D.Double src = new Point2D.Double();
		Point2D.Double dest = new Point2D.Double();
		boolean isBad;
		double discrepancy;
		boolean originalBad = false;

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

		public double getMaxAllowedDiscrepancy(TransformLearnerResult result) {
			double r = result.discrepancyStatistics.getAvgValue();
			double je = result.discrepancyStatistics.getJ_End();
			if (je < result.discrepancyStatistics.getMaxX())
				r = je;
//			double r = (result.discrepancyStatistics.getAvgValue() + result.discrepancyStatistics.getMaxX()) / 2;
			return r < 1.0 ? 1.0 : r;
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
	
	private MyTestData addp(int x, int y) {
		Point2D.Double sd = new Point2D.Double(x, y);
		MyTestData pair = new MyTestData(jTransform, sd);
		points.add(pair);
		return pair;
	}
	
	private void generatePoints() {
		jTransform = new AffineTransform();
		jTransform.setToIdentity();
		jTransform.rotate(90 * degreeToRad);
		jTransform.scale(123.456, 123.456);
		jTransform.translate(100.567, 200.123);

		System.out.println("== The java.awt.geom.AffineTransform is:");
		dumpAffineTransform(jTransform);

		int maxX = 50;
		int maxY = 50;
		
		points = new ArrayList<MyTestData>();
		for (int xcounter = 0; xcounter < maxX; xcounter++) {
			for (int ycounter = 0; ycounter < maxY; ycounter++) {
				addp(xcounter, ycounter);
			}
		}
		
		// add fake data
		int percentFakeData = 45;
		int goodPoints = points.size();
		int numberOfFakePoints = percentFakeData == 0 ? 0 : goodPoints * percentFakeData / (100 - percentFakeData);

		if (false) {
			int xcounter = 0;
			int ycounter = 0;
			for (int i = 0; i < numberOfFakePoints; i++) {
				MyTestData d = addp(xcounter, ycounter);
				d.originalBad = true;
				d.dest.x += 1000 + i * 10000 / numberOfFakePoints;
				d.dest.y += 1000 + i * 10000 / numberOfFakePoints;
				xcounter++;
				if (xcounter >= maxX) {
					ycounter++;
					xcounter = 0;
				}
			}
		} else {
			Random r = new Random();
			for (int i = 0; i < numberOfFakePoints; i++) {
				MyTestData d = addp(r.nextInt(maxX), r.nextInt(maxY));
				d.originalBad = true;
				d.dest.x += 1000 + r.nextInt(1000);
				d.dest.y += 1000 + r.nextInt(1000);
			}
		}

/*		MyTestData d = points.get(0);
		System.out.println(d.src);
		System.out.println(d.dest);
*/
		System.out.println("Good points " + goodPoints);
		System.out.println("Fake points " + numberOfFakePoints);
		System.out.println("All  points " + points.size());
	}
	
	public void learn() {
		MyTestData pair;
		learner = new MyTestHelmert2DTransformLearner(new MyTestHelmert2DTransformer(), points);
		
		for (int iter = 0; iter < 100; iter++) {
			System.out.println();
			System.out.println("**** Iteration " + iter);
			TransformLearnerResult res = learner.calculateOne();
			Helmert2DTransformer<Point2D.Double, Point2D.Double> tr = (Helmert2DTransformer<Point2D.Double, Point2D.Double>) learner.transformer;
			System.out.println(res);
			System.out.println(tr.toString());
			dumpBad();
			if (res.isAdjustFailed() || (res.discrepancyStatistics.getMaxX() < 1.0))
				break;
		}
		Helmert2DTransformer<Point2D.Double, Point2D.Double> tr = (Helmert2DTransformer<Point2D.Double, Point2D.Double>) learner.transformer;
		
		Point2D.Double dest = new Point2D.Double();
		pair = points.get(0);
		tr.transform(pair.src, dest);
		System.out.println("" + pair.dest.x + "\t" + pair.dest.y);
		System.out.println("" + dest.x + "\t" + dest.y);
	}
	
	public void dumpBad() {
		int badMarkedAsGood = 0;
		int goodMarkedAsBad = 0; 
		for (MyTestData d : points) {
			if (d.originalBad && (!d.isBad))
				badMarkedAsGood++;
			if (!d.originalBad && d.isBad)
				goodMarkedAsBad++;
		}
		System.out.println("Bad marked as good: " + badMarkedAsGood);		
		System.out.println("Good marked as bad: " + goodMarkedAsBad);		
	}
	
	void doit(MyTestData data, MyTestHelmert2DTransformer tr) {
		double SX = data.src.x;
		double SY = data.src.y;
		double TX = data.dest.x;
		double TY = data.dest.y;
		
		double B =  tr.a * SX - tr.b * SY + tr.c - TX;
		double C = +tr.b * SX + tr.a * SY + tr.d - TY;
		double F = B*B + C*C;
		double dF_da = 2.0 * (SX * B - SY * C); 
		double dF_db = 2.0 * (SY * B + SX * C); 
		double dF_dc = 2.0 * B; 
		double dF_dd = 2.0 * C; 
		
		double delta = 0.01;
		
		Point2D.Double dest0 = new Point2D.Double();
		Point2D.Double dest1 = new Point2D.Double();
		tr.transform(data.src, dest0);
		tr.a += delta;
		tr.b += delta;
		tr.c += delta;
		tr.d += delta;
		tr.transform(data.src, dest1);
		
		tr.a -= delta;
		tr.b -= delta;
		tr.c -= delta;
		tr.d -= delta;

		double dx = data.dest.x - dest0.x;
		double dy = data.dest.y - dest0.y;
		double F0 = dx*dx + dy*dy;
		
		dx = data.dest.x - dest1.x;
		dy = data.dest.y - dest1.y;
		double F1 = dx*dx + dy*dy;

		double FP = F1 + 
			dF_da * delta +
			dF_db * delta +
			dF_dc * delta +
			dF_dd * delta;
		
		System.out.println("FP " + FP);
		System.out.println("F1 " + F1);
		System.out.println("F0 " + F0);
		System.out.println("F  " + F);
	}

	public static void main(String[] args) {
		TestHelmert2DTransformer test = new TestHelmert2DTransformer();
		test.generatePoints();
		test.learn();

		test.doit(test.points.get(55), (MyTestHelmert2DTransformer) test.learner.transformer);
		
/*		Matrix delta = test.learner.computeTransformedTargetDelta(false);
		System.out.println("==== max discrepancy ====");
		System.out.println(delta.toString());
		delta = test.learner.computeTransformedTargetDelta(true);
		System.out.println("==== max discrepancy 2 ====");
		System.out.println(delta.toString());
		test.dumpBad();
		TestUtils.assertMatrix0("Delta", delta);*/
		System.out.println("Done");
	}
}
