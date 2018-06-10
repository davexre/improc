package com.test.math;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.slavi.math.adjust.Statistics;

public class TestStatistics {

	public static class MyData {
		boolean bad;
		double value;
		double weight;
		
		public MyData(double value, double weight) {
			this.bad = false;
			this.value = value;
			this.weight = weight;
		}
		
		public String toString() {
			return 	(bad ? "BAD" : "   ") +
				" Value=" + Double.toString(value) + 
				"\tW=" + Double.toString(weight); 
		}
	}
	
	public static boolean calculateOne(ArrayList<MyData> items, Statistics stat) {
		stat.start();
		for (MyData item : items) {
			if (!item.bad)
				stat.addValue(item.value, item.weight);
		}
		stat.stop();

		boolean hasNewBad = false;
		for (MyData item : items) {
			boolean newBad = stat.isBad(item.value);
			if (newBad != item.bad)
				hasNewBad = true;
			item.bad = newBad;
		}
		return hasNewBad;
	}
	
	public static void main(String[] args) throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(TestStatistics.class
				.getResource("StatisticsTest.txt").getFile()));
		ArrayList<MyData> items = new ArrayList<MyData>();
		while (fin.ready()) {
			String str = fin.readLine();
			if ((str == null) || (str.equals("")))
				break;
			StringTokenizer st = new StringTokenizer(str, "\t");
			st.nextToken();
			double value = Double.parseDouble(st.nextToken());
			double weight = Double.parseDouble(st.nextToken());
			MyData item = new MyData(value, weight);
			items.add(item);
		}
		fin.close();
		MyData tmpitem = new MyData(Double.NaN, 1.0);
		items.add(tmpitem);

		Statistics stat = new Statistics();
		stat.setB(0.9);
//		for (int i = 0; i < 5; i++)
			if (!calculateOne(items, stat));
//				break;
		System.out.println(stat.toString());
		System.out.println("------------");
		
		for (MyData item : items) {
			System.out.println(item.toString());
		}
		System.out.println(stat.isBad(Double.NaN));
	}
}
