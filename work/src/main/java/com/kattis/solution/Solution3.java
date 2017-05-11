package com.kattis.solution;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

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

	Point origin;
	// -1 left turn or p2 is closer to origin; 0 equal; 1 right turn 
	Comparator comparePoints = new Comparator<Point>() {
		public int compare(Point p2, Point p3) {
			int dx2 = p2.x - origin.x;
			int dy2 = p2.y - origin.y;
			int dx3 = p3.x - origin.x;
			int dy3 = p3.y - origin.y;
			int result = Integer.compare(dy2 * dx3, dx2 * dy3);
			if (result == 0) {
				return Integer.compare(
						Math.abs(dx2) + Math.abs(dy2), 
						Math.abs(dx3) + Math.abs(dy3));
			}
			return result;
		}
	};
	
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
				if ((p.y < startPoint.y) ||
					((p.y == startPoint.y) && (p.x < startPoint.x))) {
					startPoint = p;
				}
			}
			origin = startPoint;
			//points.remove(origin);
			Collections.sort(points, comparePoints);

			ArrayList<Point> convex = new ArrayList<>();
			convex.add(origin);
			for (Point p : points) {
				while (true) {
					int size = convex.size();
					if (size < 2) {
						convex.add(p);
						break;
					}
					origin = convex.get(size - 2);
					Point p2 = convex.get(size - 1);
					int r = comparePoints.compare(p2, p);
					if (r < 0) {
						// left turn
						convex.add(p);
						break;
					}
					convex.remove(size - 1);
				}
			}
			
			Point currentPoint = convex.get(convex.size() - 1);
			int area = 0;
			for (Point nextPoint : convex) {
				area += currentPoint.x * nextPoint.y - nextPoint.x * currentPoint.y;
				currentPoint = nextPoint;
			}
			System.out.println(String.format("%.1f", area * 0.5));
		}
	}

	public static void main(String[] args) throws Exception {
		//new Solution3().doIt(new Scanner(new BufferedInputStream(Solution1.class.getResourceAsStream("Solution3-data.txt"))));
		new Solution3().doIt(new Scanner(new BufferedInputStream(System.in)));
	}
}
