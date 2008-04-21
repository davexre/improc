package com.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;

import com.slavi.matrix.Matrix;
import com.slavi.statistics.AffineTransformLearner;
import com.slavi.statistics.PointsPair;

public class HuginTest {

	private static class CompareByDelta implements Comparator {
		public static final CompareByDelta instance = new CompareByDelta();

		public int compare(Object o1, Object o2) {
			PointsPair spp1 = (PointsPair)o1;
			PointsPair spp2 = (PointsPair)o2;
			return Double.compare(spp1.getValue(), spp2.getValue());
		} 
	}
	
	public static void sortByDelta(ArrayList items) {
		Collections.sort(items, CompareByDelta.instance);
	}		

	public static void main(String[] args) throws Exception {
		String fileName = "./../../images/hugin.txt";
		BufferedReader f = new BufferedReader(new FileReader(fileName));
		ArrayList items = new ArrayList();
		
		while (f.ready()) {
			String s = f.readLine();
			StringTokenizer st = new StringTokenizer(s);
			Matrix source = new Matrix(2, 1);
			Matrix target = new Matrix(2, 1);
			source.setItem(0, 0, Double.parseDouble(st.nextToken()));
			source.setItem(1, 0, Double.parseDouble(st.nextToken()));
			target.setItem(0, 0, Double.parseDouble(st.nextToken()));
			target.setItem(1, 0, Double.parseDouble(st.nextToken()));
			
			PointsPair pp = new PointsPair(source, target, 1);
			items.add(pp);
		}
		
		AffineTransformLearner atl = new AffineTransformLearner(2, 2, items);
		System.out.println("ADJUST = " + atl.calculateOne());
		sortByDelta(items);
		Matrix delta = atl.computeTransformedTargetDelta(false);
		System.out.println("==== max discrepancy ====");
		System.out.println(delta.toString());
		delta = atl.computeTransformedTargetDelta(true);
		System.out.println("==== max discrepancy 2 ====");
		System.out.println(delta.toString());
		for (int i = 0; i < items.size(); i++) {
			PointsPair pp = (PointsPair) items.get(i);
			System.out.println(Integer.toString(i) + "\t" + Double.toString(pp.getValue()));
		}
	}

}
