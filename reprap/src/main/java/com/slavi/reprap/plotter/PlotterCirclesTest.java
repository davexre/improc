package com.slavi.reprap.plotter;

public class PlotterCirclesTest {
	public static void main(String[] args) throws Exception {
		RepRapPlotter reprap = new RepRapPlotter();
		try {
			reprap.start("/dev/ttyUSB0");

			double x = 150000;
			double y = 150000;
			double r = 10000;
			double stepR = 10000;
			double maxSegmentLength = 5000;

			for (int i = 0; i < 5; i++) {
				reprap.drawCircle(x, y, r, maxSegmentLength);
				r += stepR;
			}
		} finally {
			reprap.stop();
		}
		System.out.println("Done.");
	}
}
