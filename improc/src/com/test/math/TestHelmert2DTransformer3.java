package com.test.math;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.slavi.math.MathUtil;
import com.slavi.math.transform.Helmert2DTransformLearner;
import com.slavi.math.transform.Helmert2DTransformer;
import com.slavi.math.transform.TransformLearnerResult;

public class TestHelmert2DTransformer3 {

	public static class MyTestData implements Map.Entry<Point2D.Double, Point2D.Double>{
		Point2D.Double src = new Point2D.Double();
		Point2D.Double dest = new Point2D.Double();
		boolean isBad;
		double discrepancy;

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
		double discrepancyThreshold = 15;
		
		public MyTestHelmert2DTransformLearner(MyTestHelmert2DTransformer transformer,
				Iterable<? extends Map.Entry<Point2D.Double, Point2D.Double>> pointsPairList) {
			super(transformer, pointsPairList);
		}

		public double getMaxAllowedDiscrepancy(TransformLearnerResult result) {
			double r = result.discrepancyStatistics.getMaxX();
			if (r >= discrepancyThreshold) {
				r = (result.discrepancyStatistics.getAvgValue() +
					result.discrepancyStatistics.getAvgValue() + 
					result.discrepancyStatistics.getMaxX()) / 3.0;
			}
			return r;
/*			double r = result.discrepancyStatistics.getAvgValue();
			double je = result.discrepancyStatistics.getJ_End();
			if (je < result.discrepancyStatistics.getMaxX())
				r = je;
//			double r = (result.discrepancyStatistics.getAvgValue() + result.discrepancyStatistics.getMaxX()) / 2;
			return r < 1.0 ? 1.0 : r;*/
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
	
	ArrayList<MyTestData> data = new ArrayList<MyTestData>();
	public static int minRequredGoodPointPairs = 10;

	public void loadData() throws Exception {
		String fileName = "d:/temp/asdf.txt";
		BufferedReader fin = new BufferedReader(new FileReader(fileName));
		
		while (true) {
			String line = fin.readLine();
			if (line == null) break;
			MyTestData d = new MyTestData();
			StringTokenizer st = new StringTokenizer(line, "\t");
			d.src.x = Double.parseDouble(st.nextToken());
			d.src.y = Double.parseDouble(st.nextToken());
			d.dest.x = Double.parseDouble(st.nextToken());
			d.dest.y = Double.parseDouble(st.nextToken());
			data.add(d);
		}
		
		fin.close();
	}
	
	public void learn() {
		MyTestHelmert2DTransformLearner learner = new MyTestHelmert2DTransformLearner(new MyTestHelmert2DTransformer(), data);
		for (int iter = 0; iter < 100; iter++) {
			System.out.println();
			TransformLearnerResult res = learner.calculateOne();
			Helmert2DTransformer<Point2D.Double, Point2D.Double> tr = (Helmert2DTransformer<Point2D.Double, Point2D.Double>) learner.transformer;
			System.out.println(res);
			
			double params[] = new double[4];
			tr.getParams(params);
			System.out.println();
			System.out.println("Scale = " + MathUtil.d4(params[0]));
			System.out.println("Rot   = " + MathUtil.rad2degStr(params[1]));
			System.out.println("X     = " + MathUtil.d4(params[2]));
			System.out.println("Y     = " + MathUtil.d4(params[3]));
			System.out.println();
						
			int goodCount = res.newGoodCount;
			if (res.isAdjusted() || (goodCount < minRequredGoodPointPairs)) {
//			if (res.isAdjusted() || res.isAdjustFailed()) {
				System.out.println("ADJUSTED");
//				break;
			}
		}

	}
	
	public static void main(String[] args) throws Exception {
		TestHelmert2DTransformer3 test = new TestHelmert2DTransformer3();
		test.loadData();
		test.learn();
	}
}
