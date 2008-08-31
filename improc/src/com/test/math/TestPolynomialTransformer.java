package com.test.math;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.slavi.math.matrix.Matrix;
import com.slavi.math.transform.PointsPair;
import com.slavi.math.transform.PolynomialTransformLearner;
import com.slavi.math.transform.PolynomialTransformer;

public class TestPolynomialTransformer {

	public ArrayList<PointsPair> points;
	
	public PolynomialTransformLearner learner;
	
	public PolynomialTransformer transformer;
	
	public int numCoordinates;
	
	public void readTransformer() throws IOException {
		BufferedReader fin = new BufferedReader(new FileReader(
				TestPolynomialTransformer.class.getResource(
					"PolynomialTransformer.txt").getFile()));
		transformer = new PolynomialTransformer(4, 3, 3);
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
		points = new ArrayList<PointsPair>();
		// read points
		String str = fin.readLine();
		while (fin.ready()) {
			str = fin.readLine();
			if (str.equals(""))
				break;
			StringTokenizer st = new StringTokenizer(str, "\t");
			
			Matrix source = new Matrix(numCoordinates, 1);
			Matrix target = new Matrix(numCoordinates, 1);
			for (int i = 0; i < numCoordinates; i++)
				source.setItem(i, 0, Double.parseDouble(st.nextToken()));
			for (int i = 0; i < numCoordinates; i++)
				target.setItem(i, 0, Double.parseDouble(st.nextToken()));
			PointsPair pair = new PointsPair(source, target, 1);
			points.add(pair);
		}
	}

	public void learn(int polynomPower) {
		learner = new PolynomialTransformLearner(polynomPower, numCoordinates, numCoordinates, points);
		int maxIterations = 100;
		int iteration = 0;
		boolean adjusted = false;
		while ((iteration++ < maxIterations) && (!adjusted)) {
			adjusted = learner.calculateOne();
			int goodCount = 0;
			for (int i = points.size() - 1; i >= 0; i--)
				if (!points.get(i).isBad())
					goodCount++;
			System.out.println("Iteration " + iteration + " has good " + goodCount + "/" + points.size());
		}
		System.out.println("Learner adjusted: " + adjusted);
		transformer = (PolynomialTransformer) learner.transformer;
	}
		
	public void testTransformer() {
		Matrix dest = new Matrix(numCoordinates, 1);
		Matrix minDif = new Matrix(numCoordinates, 1);
		Matrix maxDif = new Matrix(numCoordinates, 1);
		boolean isFirst = true;
		for (int p = 0; p < points.size(); p++) {
			PointsPair item = points.get(0);
			if (item.isBad())
				continue;
			transformer.transform(item.source, dest);
			item.target.mSub(dest, dest);
			
			if (isFirst) {
				dest.copyTo(minDif);
				dest.copyTo(maxDif);
			} else {
				dest.mMin(minDif, minDif);
				dest.mMax(maxDif, maxDif);
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
