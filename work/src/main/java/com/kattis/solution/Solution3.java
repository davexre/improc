package com.kattis.solution;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Solution3 {

	public static class Point {
		int x, y;
		
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	public void doIt(Scanner scanner) throws Exception {
		while (scanner.hasNext()) {
			int numberOfPoints = scanner.nextInt();
			if (numberOfPoints == 0)
				return;
			
			ArrayList<Point> points = new ArrayList<>();
			for (int i = 0; i < numberOfPoints; i++) {
				points.add(new Point(scanner.nextInt(), scanner.nextInt()));
			}
			Point min = points.get(0);
			for (int i = 1; i < numberOfPoints; i++) {
				Point p = points.get(i);
				if ((p.x < min.x) ||
					((p.x == min.x) && (p.y < min.y))) {
					min = p;
				}
			}
			ArrayList<Point> convex = new ArrayList<>();
			convex.add(min);
			double curAngle = 0;
			Point cur = min;
			while (true) {
				double curDiff = Double.MAX_VALUE;
				double curDist = 0;
				Point next = null;
				double nextAngle = 0;
				for (int i = 0; i < numberOfPoints; i++) {
					Point p = points.get(i);
					double dx = p.x - cur.x;
					double dy = p.y - cur.y;
					double distP = Math.sqrt(dx*dx + dy*dy);
					if (distP <= 0.0)
						continue;
					double a = Math.atan2(dy, dx);
					double diffP = curAngle - a;
					if ((diffP > curDiff) ||
						(diffP == curDiff && curDist >= distP)) {
						continue;
					}
					curDist = distP;
					curDiff = diffP;
					next = p;
					nextAngle = a;
				}
				if (next != null) {
					convex.add(next);
					double dx = next.x - min.x;
					double dy = next.y - min.y;
					double distFirst = Math.sqrt(dx*dx + dy*dy);
					if (distFirst <= 0.0)
						break;
					cur = next;
					curAngle = nextAngle;
				} else {
					break;
				}
			}
			
			double area = 0;
			cur = convex.get(convex.size() - 1);
			for (int i = 0; i < numberOfPoints; i++) {
				Point next = points.get(i);
				area += (next.y + cur.y) * (next.x - cur.x);
				cur = next;
			}
			area /= 2.0;
			System.out.println(String.format("%.1f", area));
		}
	}

	public static void main(String[] args) throws Exception {
		new Solution3().doIt(new Scanner(new BufferedInputStream(Solution1.class.getResourceAsStream("Solution3-data.txt"))));
		//new Solution3().doIt(new Scanner(new BufferedInputStream(System.in)));
	}
}
