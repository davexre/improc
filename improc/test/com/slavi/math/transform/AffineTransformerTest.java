package com.slavi.math.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.junit.Test;

import com.slavi.TestUtils;
import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.TransformLearnerResult;

public class AffineTransformerTest {

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
		for (int xcounter = 0; xcounter < 2; xcounter++) {
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
		TestUtils.assertTrue("Learner adjusted", res.isAdjusted());
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
		TestUtils.assertTrue("Inverse", backward.inverse());
		
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
		TestUtils.assertMatrix0("Forward", p4);
		
		p2.mMul(backward, p3);
		p3.mSub(p1, p4);
		TestUtils.assertMatrix0("Inverse(Forward)", p4);
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
		TestUtils.assertMatrix0("", delta);
		transformBackward();
	}
}
