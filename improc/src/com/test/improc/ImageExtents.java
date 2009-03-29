package com.test.improc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.slavi.math.MathUtil;

public class ImageExtents {

	public static class AngleSegment {
		double middle, fov, lower, upper;
		
		public boolean isInside(double angle) {
			if (lower < upper) {
				return (lower <= angle) && (angle <= upper);
			} else {
				return (lower <= angle) || (angle <= upper);
			}
		}
		
		public String toString() {
			return
				MathUtil.d4(middle * MathUtil.rad2deg) + "\t" +
				MathUtil.d4(lower * MathUtil.rad2deg) + "\t" +
				MathUtil.d4(upper * MathUtil.rad2deg) + "\t";
		}
	}
	
	ArrayList<AngleSegment> read() throws Exception {
		ArrayList<AngleSegment> result = new ArrayList<AngleSegment>();
		BufferedReader fin = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("ImageExtents.txt")));
		while (fin.ready()) {
			String str = fin.readLine();
			if (str.equals(""))
				break;
			StringTokenizer st = new StringTokenizer(str);
			double angle = Double.parseDouble(st.nextToken()) * MathUtil.deg2rad;
			double fov = Double.parseDouble(st.nextToken()) * MathUtil.deg2rad;
			AngleSegment a = new AngleSegment();
			a.middle = angle;
			a.fov = fov;
			double fov2 = fov / 2.0;
			a.lower = MathUtil.fixAngleMPI_PI(angle - fov2);
			a.upper = MathUtil.fixAngleMPI_PI(angle + fov2);
			result.add(a);
		}		
		fin.close();
		return result;
	}

	void calc(ArrayList<AngleSegment> data) {
		System.out.println("------ data --------");
		for (AngleSegment s : data) {
			System.out.println(s);
		}

		AngleSegment as = new AngleSegment();
		AngleSegment t = data.remove(0);
		as.lower = t.lower;
		as.upper = t.upper;
		
		System.out.println();
		System.out.println(t.toString() + as.toString());
		boolean loopMore = true;
		while (loopMore) {
			loopMore = false;
			for (int i = data.size() - 1; i >= 0; i--) {
				AngleSegment s = data.get(i);
				boolean currentOk = false;
				
				if (as.isInside(s.lower)) {
					currentOk = true;
					if (s.isInside(s.upper)) {
						as.upper = s.upper;
						System.out.println(s.toString() + as.toString());
					}
				} else if (as.isInside(s.upper)) {
					currentOk = true;
					if (s.isInside(s.lower)) {
						as.lower = s.lower;
						System.out.println(s.toString() + as.toString());
					}
				}
				
				if (currentOk) {
					loopMore = true;
					data.remove(i);
				}
			}
		}
		System.out.println();
		System.out.println(as);
		System.out.println("------ remaining --------");
		for (AngleSegment s : data) {
			System.out.println(s);
		}
	}
	
	public static void main(String[] args) throws Exception {
		ImageExtents imex = new ImageExtents();
		ArrayList<AngleSegment> as = imex.read();
		imex.calc(as);
//		for (AngleSegment s : as) {
//			System.out.println(s);
//		}
	}
}
