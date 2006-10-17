package com.slavi.testpackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.slavi.statistics.StatisticianImpl;
import com.slavi.statistics.Statistics;
import com.slavi.statistics.StatisticsItem;
import com.slavi.statistics.StatisticsLT;

public class StatisticsTest {

	public static void main(String[] args) throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(StatisticsTest.class
				.getResource("StatisticsTest.txt").getFile()));
		ArrayList items = new ArrayList();
		while (fin.ready()) {
			String str = fin.readLine();
			if (str.equals(""))
				break;
			StringTokenizer st = new StringTokenizer(str, "\t");
			double value = Double.parseDouble(st.nextToken());
			double weight = Double.parseDouble(st.nextToken());
			StatisticsItem item = new StatisticsItem(value, weight);
			items.add(item);
		}
		fin.close();

		StatisticsLT statLT = new StatisticsLT();
		statLT.setB(0.9);
		statLT.start();
		for (int i = 0; i < items.size(); i++)
			statLT.addValue(((StatisticsItem)items.get(i)).getValue());
		statLT.stop();
		System.out.println(statLT.toString());
		System.out.println("------------");
		
		Statistics stat = new Statistics();
		stat.setB(0.9);
		while (stat.calculateOne(items, StatisticianImpl.getInstance()) > 0) {
			System.out.println(stat.toString());
			System.out.println("------------");
		}
		System.out.println(stat.toString());
		System.out.println("------------");

		
		for (int i = 0; i < items.size(); i++)
			System.out.println(((StatisticsItem)items.get(i)).toString());
	}
}
