package com.test.math;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.PolynomialTransformLearner;
import com.slavi.math.transform.PolynomialTransformer;
import com.slavi.math.transform.TransformLearnerResult;

public class TestPolynomialTransformer {

	public static class MyPoint {
		Matrix coords;
		
		MyPoint(int numCoordinates) {
			coords = new Matrix(numCoordinates, 1);
		}
	}
	
	public static class MyTestData implements Map.Entry<MyPoint, MyPoint>{
		MyPoint src;
		MyPoint dest;
		boolean isBad;
		double discrepancy;

		public MyTestData(int numCoordinates) {
			src = new MyPoint(numCoordinates);
			dest = new MyPoint(numCoordinates);
		}
		
		public MyPoint getKey() {
			return src;
		}

		public MyPoint getValue() {
			return dest;
		}

		public MyPoint setValue(MyPoint value) {
			throw new RuntimeException("Method not allowed");
		}
	}
	
	public static class MyTestPolynomialTransformer extends PolynomialTransformer<MyPoint, MyPoint> {
		int size;
		
		public MyTestPolynomialTransformer(int polynomPower, int size) {
			super(polynomPower);
			this.size = size;
		}

		public int getInputSize() {
			return 0;
		}

		public int getOutputSize() {
			return 0;
		}

		public double getSourceCoord(MyPoint item, int coordIndex) {
			return item.coords.getItem(coordIndex, 0);
		}

		public double getTargetCoord(MyPoint item, int coordIndex) {
			return item.coords.getItem(coordIndex, 0);
		}

		public void setSourceCoord(MyPoint item, int coordIndex, double value) {
			item.coords.setItem(coordIndex, 0, value);
		}

		public void setTargetCoord(MyPoint item, int coordIndex, double value) {
			item.coords.setItem(coordIndex, 0, value);
		}
	}
	
	public static class MyTestPolynomialTransformLearner extends PolynomialTransformLearner<MyPoint, MyPoint> {

		public MyTestPolynomialTransformLearner(MyTestPolynomialTransformer transformer, Iterable<? extends Map.Entry<MyPoint, MyPoint>> pointsPairList) {
			super(transformer, pointsPairList);
		}

		public MyPoint createTemporaryTargetObject() {
			return new MyPoint(transformer.getOutputSize());
		}

		public double getDiscrepancy(Entry<MyPoint, MyPoint> item) {
			return ((MyTestData) item).discrepancy;
		}

		public double getWeight(Entry<MyPoint, MyPoint> item) {
			return 1.0;
		}

		public boolean isBad(Entry<MyPoint, MyPoint> item) {
			return ((MyTestData) item).isBad;
		}

		public void setBad(Entry<MyPoint, MyPoint> item, boolean bad) {
			((MyTestData) item).isBad = bad;
		}

		public void setDiscrepancy(Entry<MyPoint, MyPoint> item, double discrepancy) {
			((MyTestData) item).discrepancy = discrepancy;
		}
	}

	
	
	public ArrayList<MyTestData> points;
	
	public MyTestPolynomialTransformLearner learner;
	
	public MyTestPolynomialTransformer transformer;
	
	public int numCoordinates;
	
	public void readTransformer() throws IOException {
		BufferedReader fin = new BufferedReader(new FileReader(
				TestPolynomialTransformer.class.getResource(
					"PolynomialTransformer.txt").getFile()));
		transformer = new MyTestPolynomialTransformer(4, 3);
//		transformer.sourceOrigin.load(fin);
//		transformer.originTarget.load(fin);
//		transformer.scaleSource.load(fin);
//		transformer.scaleTarget.load(fin);
		for (int j = 0; j < transformer.polynomCoefs.getSizeY(); j++) {
			String str = fin.readLine();
			StringTokenizer st = new StringTokenizer(str, "\t");
			st.nextToken();
			for (int i = 0; i < transformer.polynomCoefs.getSizeX(); i++)
				transformer.polynomCoefs.setItem(i, j, Double.parseDouble(st.nextToken()));				
		}
		fin.close();
	}
	
	public TestPolynomialTransformer(int numCoordinates, BufferedReader fin) throws IOException {
		this.numCoordinates = numCoordinates;
		points = new ArrayList<MyTestData>();
		// read points
		String str = fin.readLine();
		while (fin.ready()) {
			str = fin.readLine();
			if (str.equals(""))
				break;
			StringTokenizer st = new StringTokenizer(str, "\t");
			
			MyTestData item = new MyTestData(numCoordinates);
			for (int i = 0; i < numCoordinates; i++)
				item.src.coords.setItem(i, 0, Double.parseDouble(st.nextToken()));
			for (int i = 0; i < numCoordinates; i++)
				item.dest.coords.setItem(i, 0, Double.parseDouble(st.nextToken()));
			points.add(item);
		}
	}

	public void learn(int polynomPower) {
		transformer = new MyTestPolynomialTransformer(polynomPower, numCoordinates);
		learner = new MyTestPolynomialTransformLearner(transformer, points);
		int maxIterations = 100;
		int iteration = 0;
		TransformLearnerResult res = null;
		while (iteration++ < maxIterations) {
			res = learner.calculateOne();
			int goodCount = 0;
			for (MyTestData item : points)
				if (!learner.isBad(item))
					goodCount++;
			System.out.println("Iteration " + iteration + " has good " + goodCount + "/" + points.size());
			if (res.isAdjusted())
				break;
		}
		System.out.println("Learner adjusted: " + res.isAdjusted());
	}
		
	public void testTransformer() {
		MyPoint dest = new MyPoint(numCoordinates);
		Matrix minDif = new Matrix(numCoordinates, 1);
		Matrix maxDif = new Matrix(numCoordinates, 1);
		boolean isFirst = true;
		for (MyTestData item : points) {
			if (learner.isBad(item))
				continue;
			transformer.transform(item.src, dest);
			item.dest.coords.mSub(dest.coords, dest.coords);
			
			if (isFirst) {
				dest.coords.copyTo(minDif);
				dest.coords.copyTo(maxDif);
			} else {
				dest.coords.mMin(minDif, minDif);
				dest.coords.mMax(maxDif, maxDif);
			}
		}
		System.out.println("min/max diff");
		System.out.print(minDif.toString());			
		System.out.print(maxDif.toString());			
	}	
	
	public static Object[][] tests = {
		// Polynom powers, Test file coordinates, test file name
		{2, 3, "PolynomialTransformerTest-wgs-70.txt"},
		{3, 2, "PolynomialTransformerTest-us-cities.txt"}			
	};
	
	public static void main(String[] args) throws Exception {
		int testNo = 0;
		BufferedReader fin = new BufferedReader(new FileReader(
			TestPolynomialTransformer.class.getResource(
				(String)tests[testNo][2]).getFile()));
		int finCoordinates = (Integer)tests[testNo][1];
		int polynomPower = (Integer)tests[testNo][0];
		
		TestPolynomialTransformer tester = new TestPolynomialTransformer(finCoordinates, fin);
		fin.close();
		
		//tester.readTransformer();
//		System.out.println("------ powers ---------");
//		System.out.println(tester.transformer.polynomPowers.toString());
//		System.out.println("-------------");
		tester.learn(polynomPower);

		
		System.out.println(tester.transformer.toString());
		Matrix delta = tester.learner.computeTransformedTargetDelta(true);
		System.out.println("==== max discrepancy ====");
		System.out.println(delta.toString());
	}
}
