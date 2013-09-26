package com.slavi.math.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.testUtil.TestUtil;

public class Helmert2DTransformerTest {

	static void dumpAffineTransform(AffineTransform af) {
		double[] d = new double[6]; 
		af.getMatrix(d);
		for (int i = 0; i < d.length; i++)
			System.out.println(d[i]);
		System.out.println();
		System.out.println("A0=" + MathUtil.d4(d[0]));
		System.out.println("B0=" + MathUtil.d4(d[2]));
		System.out.println("C0=" + MathUtil.d4(d[4]));
		System.out.println("D0=" + MathUtil.d4(d[5]));
	}
	
	static TransformerDataTestImpl addp(ArrayList<TransformerDataTestImpl> points, AffineTransform jTransform, int x, int y) {
		Point2D.Double sd = new Point2D.Double(x, y);
		TransformerDataTestImpl pair = new TransformerDataTestImpl(jTransform, sd);
		points.add(pair);
		return pair;
	}
	
	static ArrayList<TransformerDataTestImpl> generatePoints() {
		AffineTransform jTransform = new AffineTransform();
		jTransform.setToIdentity();
		jTransform.rotate(9 * MathUtil.deg2rad);
		jTransform.scale(1.23456, 1.23456);
		jTransform.translate(100.567, 200.123);

		double[] temp = new double[6]; 
		jTransform.getMatrix(temp);
//		double a0 = temp[0];
//		double b0 = temp[2];
//		double c0 = temp[4];
//		double d0 = temp[5];

		System.out.println("== The java.awt.geom.AffineTransform is:");
		dumpAffineTransform(jTransform);

		int maxX = 20;
		int maxY = 20;
		
		ArrayList<TransformerDataTestImpl> points = new ArrayList<TransformerDataTestImpl>();
		for (int xcounter = 0; xcounter < maxX; xcounter++) {
			for (int ycounter = 0; ycounter < maxY; ycounter++) {
				addp(points, jTransform, xcounter, ycounter);
			}
		}
		
		// add fake data
		int percentFakeData = 70;
		int goodPoints = points.size();
		int numberOfFakePoints = percentFakeData == 0 ? 0 : goodPoints * percentFakeData / (100 - percentFakeData);
//		numberOfFakePoints = 0;
		
		Random r = new Random();
		for (int i = 0; i < numberOfFakePoints; i++) {
			TransformerDataTestImpl d = addp(points, jTransform, r.nextInt(maxX), r.nextInt(maxY));
			d.originalBad = true;
			d.dest.x += 10 + r.nextInt(100);
			d.dest.y += 10 + r.nextInt(100);
		}

/*		TransformerTestDataImpl d = points.get(0);
		System.out.println(d.src);
		System.out.println(d.dest);
*/
		System.out.println();
		System.out.println("Good points " + goodPoints);
		System.out.println("Fake points " + numberOfFakePoints);
		System.out.println("All  points " + points.size());
		
		return points;
	}
	
	static void dumpBad(ArrayList<TransformerDataTestImpl> points) {
		int badMarkedAsGood = 0;
		int goodMarkedAsBad = 0; 
		for (TransformerDataTestImpl d : points) {
			if (d.originalBad && (!d.isBad))
				badMarkedAsGood++;
			if (!d.originalBad && d.isBad)
				goodMarkedAsBad++;
		}
		System.out.println("Bad marked as good: " + badMarkedAsGood);		
		System.out.println("Good marked as bad: " + goodMarkedAsBad);		
	}

	@Test
	public void testLearn() {
		ArrayList<TransformerDataTestImpl> points = generatePoints();
		Helmert2DTransformLearnerTestImpl learner = new Helmert2DTransformLearnerTestImpl(new Helmert2DTransformerTestImpl(), points);

		TransformerDataTestImpl pair;
//		System.out.println("*******************");
//		System.out.println(learner.calculateTwo());
//		dumpBad();
		
//		Helmert2DTransformer<Point2D.Double, Point2D.Double> tr = (Helmert2DTransformer<Point2D.Double, Point2D.Double>) learner.transformer;
		System.out.println(learner.transformer.toString());
//		learner.calculateTwo();
		for (int iter = 0; iter < 50; iter++) {
			System.out.println("******************* " + iter);
			TransformLearnerResult res = learner.calculateOne();
			System.out.println(res);
			System.out.println(learner.transformer.toString());
			dumpBad(points);
			if (res.isAdjustFailed() || (res.discrepancyStatistics.getMaxX() < TestUtil.precision))
				break;
//			learner.testDerivative(a0, b0, c0, d0);
		}
		
		Point2D.Double dest = new Point2D.Double();
		pair = points.get(0);
		learner.transformer.transform(pair.src, dest);
		System.out.println("" + pair.dest.x + "\t" + pair.dest.y);
		System.out.println("" + dest.x + "\t" + dest.y);

//		learner.testDerivative(a0, b0, c0, d0);

		Matrix delta = learner.computeTransformedTargetDelta(false);
		System.out.println("==== max discrepancy ====");
		System.out.println(delta.toString());
		delta = learner.computeTransformedTargetDelta(true);
		System.out.println("==== max discrepancy 2 ====");
		System.out.println(delta.toString());
		
		TestUtil.assertMatrix0("Delta", delta);
	}

	
	
	@Test
	public void testGetSetParams() {
		Helmert2DTransformerTestImpl tr = new Helmert2DTransformerTestImpl();
		double p1[] = new double[4];
		p1[0] = 2.2;
		p1[1] = -3.4;
		p1[2] = 5;
		p1[3] = 6;
		
		tr.a = p1[0];
		tr.b = p1[1];
		tr.c = p1[2];
		tr.d = p1[3];
		
		double params[] = new double[4];
		tr.getParams(params);
		tr.setParams(params[0], params[1], params[2], params[3]);
		
		double p2[] = new double[4];
		p2[0] = tr.a;
		p2[1] = tr.b;
		p2[2] = tr.c;
		p2[3] = tr.d;

		TestUtil.assertEqual("", p1, p2);
	}
}
