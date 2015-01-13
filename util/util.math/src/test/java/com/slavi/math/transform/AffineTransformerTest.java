package com.slavi.math.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.junit.Assert;
import org.junit.Test;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;

public class AffineTransformerTest {
	public static double precision = 1.0 / 10000.0;

	private AffineTransformLearnerTestImpl learner;

	static ArrayList<TransformerDataTestImpl> generatePoints() {
		AffineTransform jTransform = new AffineTransform();
		jTransform.setToIdentity();
		jTransform.rotate(30 * MathUtil.deg2rad);
		jTransform.scale(2, 1);
//		jTransform.scale(123.456, 789.123);
//		jTransform.shear(1.234, 2.345);
		jTransform.translate(100.567, 200.123);
		
		ArrayList<TransformerDataTestImpl> points = new ArrayList<TransformerDataTestImpl>();
		for (int xcounter = 1; xcounter < 4; xcounter++) {
			for (int ycounter = 0; ycounter < 2; ycounter++) {
				Point2D.Double sd = new Point2D.Double(xcounter, ycounter);
				TransformerDataTestImpl pair = new TransformerDataTestImpl(jTransform, sd);
				points.add(pair);
			}
		}
		return points;
	}
	
	void learn() {
		ArrayList<TransformerDataTestImpl> points = generatePoints();
		learner = new AffineTransformLearnerTestImpl(points);
		TransformLearnerResult res = learner.calculateOne();
//		System.out.println(res);
		Assert.assertTrue("Learner adjusted", res.isAdjusted());
	}

	void transformBackward() {
		AffineTransformerTestImpl tr = (AffineTransformerTestImpl) learner.transformer;
		Matrix forward = new Matrix(3, 3);
		
		forward.setItem(0, 0, tr.affineCoefs.getItem(0, 0));
		forward.setItem(1, 0, tr.affineCoefs.getItem(1, 0));
		forward.setItem(2, 0, 0);

		forward.setItem(0, 1, tr.affineCoefs.getItem(0, 1));
		forward.setItem(1, 1, tr.affineCoefs.getItem(1, 1));
		forward.setItem(2, 1, 0);

		forward.setItem(0, 2, tr.origin.getItem(0, 0));
		forward.setItem(1, 2, tr.origin.getItem(1, 0));
		forward.setItem(2, 2, 1);
		
		Matrix backward = new Matrix(3, 3);
		forward.copyTo(backward);
		Assert.assertTrue("Inverse", backward.inverse());
		
		Point2D.Double srcP = new Point2D.Double(100, 200);
		Point2D.Double destP = new Point2D.Double();
		tr.transform(srcP, destP);
		
		Matrix p1 = new Matrix(3, 1);
		Matrix p2 = new Matrix(3, 1);
		Matrix p3 = new Matrix(3, 1);
		Matrix p4 = new Matrix(3, 1);

		p1.setItem(0, 0, srcP.x);
		p1.setItem(1, 0, srcP.y);
		p1.setItem(2, 0, 1);
		
		p3.setItem(0, 0, destP.x);
		p3.setItem(1, 0, destP.y);
		p3.setItem(2, 0, 1);		
		
		p1.mMul(forward, p2);
		p2.mSub(p3, p4);
		Assert.assertTrue("Forward", p4.is0(precision));
		
		p2.mMul(backward, p3);
		p3.mSub(p1, p4);
		Assert.assertTrue("Inverse(Forward)", p4.is0(precision));
/*		
//		tr.affineCoefs.printM("Coefs");
//		tr.origin.printM("Origin");

		Matrix tmp = new Matrix(1, 3);
		tmp.setItem(0, 0, p2.getItem(0, 0) - tr.origin.getItem(0, 0));
		tmp.setItem(0, 1, p2.getItem(1, 0) - tr.origin.getItem(1, 0));
		tmp.setItem(0, 2, 1);
		
		Matrix forward2 = new Matrix(3, 3);
		forward.copyTo(forward2);
		forward2.setItem(0, 2, 0);
		forward2.setItem(1, 2, 0);
		
		forward.printM("Forward");
		forward2.printM("Forward2");
		
		forward2.mMul(tmp, p2);
		p2.transpose(p3);
		p3.mSub(p1, p4);
		p3.printM("P3");
		p4.printM("P4");
		
		System.out.println("Square deviation from 0 " + p4.getSquaredDeviationFrom0());*/
	}
	
	@Test
	public void testAffineTransformer() throws Exception {
		learn();
		Matrix delta = learner.computeTransformedTargetDelta(true);
		
		Assert.assertTrue(delta.is0(precision));
		transformBackward();
		
		/////////////////////////////
		// Now do the same using Apache Math
		ArrayList<TransformerDataTestImpl> points = (ArrayList) learner.items;
		
		Matrix matrixA = new Matrix(learner.outputSize * points.size(), (learner.inputSize + 1) * learner.outputSize);
		matrixA.make0();
		Matrix matrixL = new Matrix(1, learner.outputSize * points.size());
		double [] target = new double[learner.outputSize * points.size()];
		
		for (int p = 0; p < points.size(); p++) {
			TransformerDataTestImpl data = points.get(p);
			for (int i = 0; i < learner.outputSize; i++) {
				for (int j = 0; j < learner.inputSize; j++) {
					double v = j == 0 ? data.getKey().getX() : data.getKey().getY();
					matrixA.setItem(learner.outputSize * p + i, (learner.inputSize + 1) * i + j, v);
				}
				matrixA.setItem(learner.outputSize * p + i, (learner.inputSize + 1) * i + learner.inputSize, 1);
				double v = i == 0 ? data.getValue().getX() : data.getValue().getY();
				target[learner.outputSize * p + i] = v;
				matrixL.setItem(0, learner.outputSize * p + i, v);
			}
		}
		//Matrix matrixB = matrixA.makeCopy();
		//matrixB.transpose(matrixA);
		final RealMatrix A = new BlockRealMatrix(matrixA.toArray());
		//final RealMatrix B = new BlockRealMatrix(matrixB.toArray());

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
		
		final double[] weights = new double[learner.outputSize * points.size()];
		Arrays.fill(weights, 1.0);
		double x[] = new double[(learner.inputSize + 1) * learner.outputSize];
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
//		LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
		GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer();
		Optimum optimum = optimizer.optimize(builder.build());

		RealVector v = new ArrayRealVector(A.operate(optimum.getPoint()));
		RealVector diff = v.mapMultiplyToSelf(-1).add( new ArrayRealVector(target));
		System.out.println(optimum.getResiduals().getDimension());
		System.out.println(Arrays.toString(optimum.getPoint().toArray()));
		System.out.println(Arrays.toString(diff.toArray()));
	}
	
	public static void main(String[] args) throws Exception {
		new AffineTransformerTest().testAffineTransformer();
	}
}
