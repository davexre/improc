package com.test.ui;

import javax.swing.JFrame;

import com.slavi.ui.BarChart;

public class TestBarChart {
	public static void main(String[] args) {
		JFrame f = new JFrame("Barchar test");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BarChart bc = new BarChart();
		double[] arr = new double[(int) (Math.random() * 10 + 5)];
		for (int i = arr.length - 1; i >= 0; i--)
			arr[i] = Math.random();
		bc.setData(arr);

		f.add(bc);
		f.setSize(400, 300);
		f.setVisible(true);
	}
}
