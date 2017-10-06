package com.slavi.math;

import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.slavi.math.adjust.Statistics;

public class TestApacheMath {

	void doIt() throws Exception {
		SummaryStatistics st = new SummaryStatistics();
		Statistics s = new Statistics();
		DescriptiveStatistics ds = new DescriptiveStatistics(20);
		//ds.setWindowSize(80);
		
		s.start();
		Random rnd = new Random();
		for (int i = 0; i < 100; i++) {
			double d = rnd.nextDouble();
			st.addValue(d);
			s.addValue(d);
			ds.addValue(d);
		}
		System.out.println(ds.getMean());
		s.stop();
		System.out.println(st);
		System.out.println(s.toString(Statistics.CStatAll));
	}

	public static void main(String[] args) throws Exception {
		new TestApacheMath().doIt();
		System.out.println("Done.");
	}
}
