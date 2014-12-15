package com.test.math;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;

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
			super();
			this.size = size;
			initialize(polynomPower);
		}

		public int getInputSize() {
			return size;
		}

		public int getOutputSize() {
			return size;
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
	
	public void initialize(int numCoordinates, BufferedReader fin) throws IOException {
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

	void doIt() throws Exception {
		int testNo = 0;
		BufferedReader fin = new BufferedReader(new FileReader(
			TestPolynomialTransformer.class.getResource(
				(String)tests[testNo][2]).getFile()));
		int finCoordinates = (Integer)tests[testNo][1];
		int polynomPower = (Integer)tests[testNo][0];
		
		initialize(finCoordinates, fin);
		fin.close();
		
		//tester.readTransformer();
//		System.out.println("------ powers ---------");
//		System.out.println(tester.transformer.polynomPowers.toString());
//		System.out.println("-------------");
		learn(polynomPower);

		
		System.out.println(transformer.toString());
		Matrix delta = learner.computeTransformedTargetDelta(true);
		System.out.println("==== max discrepancy ====");
		System.out.println(delta.toString());
		
		/////////////////////////////
		// Now do the same using Apache Math
		
		Matrix matrixA = new Matrix(learner.outputSize * points.size(), learner.inputSize);
		for (int p = 0; p < points.size(); p++) {
			MyTestData data = points.get(p);
			for (int i = 0; i < learner.outputSize; i++) {
				for (int j = 0; j < learner.inputSize; j++) {
					matrixA.setItem(learner.outputSize * p + i, j, data.getKey().coords.getItem(j, 0));
				}
			}
		}
		final RealMatrix A = new BlockRealMatrix(matrixA.toArray());
		
		Matrix matrixL = new Matrix(1, learner.outputSize * points.size());
		double [] target = new double[learner.outputSize * points.size()];
		for (int p = 0; p < points.size(); p++) {
			MyTestData data = points.get(p);
			for (int i = 0; i < learner.outputSize; i++) {
				target[learner.outputSize * p + i] = data.getValue().coords.getItem(i, 0);
				matrixL.setItem(0, learner.outputSize * p + i, data.getValue().coords.getItem(i, 0));
			}
		}
		
		MultivariateVectorFunction modelFunction = new MultivariateVectorFunction() {
			public double[] value(double[] arg0) throws IllegalArgumentException {
				return A.operate(arg0);
			}
		};
		
		MultivariateMatrixFunction modelFunctionJacobian = new MultivariateMatrixFunction() {
			public double[][] value(double[] arg0) throws IllegalArgumentException {
				return A.getData();
			}
		};
		
		final double[] weights = new double[target.length];
		Arrays.fill(weights, 1.0);
		double x[] = new double[learner.inputSize];
		Arrays.fill(x, 0);
		
		LeastSquaresBuilder builder = new LeastSquaresBuilder();
		builder
			.model(modelFunction, modelFunctionJacobian)
			.target(target)
			.weight(new DiagonalMatrix(weights))
			.start(x)
			.checkerPair(new SimpleVectorValueChecker(1e-6, 1e-6))
			.maxEvaluations(100)
			.maxIterations(25);
		LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
		Optimum optimum = optimizer.optimize(builder.build());
		
		RealVector v = new ArrayRealVector(A.operate(target));
		RealVector diff = v.add(optimum.getResiduals().mapMultiply(-1));
		System.out.println(optimum.getResiduals().getDimension());
		System.out.println(Arrays.toString(optimum.getResiduals().toArray()));
		System.out.println(Arrays.toString(diff.toArray()));
	}

	public static void main(String[] args) throws Exception {
		new TestPolynomialTransformer().doIt();
		System.out.println("Done.");
	}
}
