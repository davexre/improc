package com.kattis.solution;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Scanner;

import com.slavi.math.MathUtil;

public class Solution3 {

	public static class Point {
		int x, y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public String toString() {
			return "X: " + x + ", Y: " + y;
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
			Point startPoint = points.get(0);
			for (int i = 1; i < numberOfPoints; i++) {
				Point p = points.get(i);
				if ((p.x < startPoint.x) ||
					((p.x == startPoint.x) && (p.y > startPoint.y))) {
					startPoint = p;
				}
			}
			//ArrayList<Point> convex = new ArrayList<>();
			//convex.add(startPoint);
			Point currentPoint = startPoint;
			double localX = 1.0;
			double localY = 0.0;
			double area = 0;
			while (true) {
				Point point;
				Point nextPoint = null;
				double angle = -2;
				double length = -1;
				for (int i = 0; i < numberOfPoints; i++) {
					point = points.get(i);
					double dX = point.x - currentPoint.x;
					double dY = point.y - currentPoint.y;
					double hypot = Math.sqrt(dX*dX + dY*dY);
					if (hypot <= 0.0)
						continue;
					double cur = (localX * dX + localY * dY) / hypot;
					if ((cur > angle) || ((cur == angle) && (hypot > length))) {
						nextPoint = point;
						length = hypot;
						angle = cur;
					}
				}
				if (nextPoint == null)
					break;
				//convex.add(nextPoint);
				double dX = nextPoint.x - currentPoint.x;
				double dY = nextPoint.y - currentPoint.y;
				double hypot = MathUtil.hypot(dX, dY);
				if (hypot != 0) {
					localX = dX / hypot;
					localY = dY / hypot;
				}
				area += nextPoint.x * currentPoint.y - currentPoint.x * nextPoint.y;
				currentPoint = nextPoint;

				dX = nextPoint.x - startPoint.x;
				dY = nextPoint.y - startPoint.y;
				if ((dX == 0.0) && (dY == 0.0))
					break;
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
