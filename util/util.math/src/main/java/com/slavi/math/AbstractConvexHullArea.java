package com.slavi.math;

public abstract class AbstractConvexHullArea {
	public abstract void resetPointIterator();

	/**
	 * Moves the iterator onto the next point (first if after reset).
	 * Returns false if no more points are available.  
	 */
	public abstract boolean nextPoint();
	
	public abstract double getX();
	public abstract double getY();
	
	/**
	 * Ideas borrowed from
	 * http://www.dr-mikes-maths.com/DotPlacer.html 
	 */
	public double getConvexHullArea() {
		double result = 0.0;
		
		// Find the left-most point, i.e. min X
		double startPointX = Double.MAX_VALUE;
		double startPointY = Double.MAX_VALUE;
		resetPointIterator();
		while (nextPoint()) {
			double curX = getX();
			if (curX < startPointX) {
				startPointX = curX;
				startPointY = getY();
			}
		}

		double currentPointX = startPointX;
		double currentPointY = startPointY;
		double localX = 1.0;
		double localY = 0.0;
		while (true) {
			double nextPointX = 0.0;
			double nextPointY = 0.0;
			double angle = -2.0;
			double length = -1;
			resetPointIterator();
			while (nextPoint()) {
				double tmpX = getX();
				double tmpY = getY();
				double dX = tmpX - currentPointX;
				double dY = tmpY - currentPointY;
				double hypot = MathUtil.hypot(dX, dY);
				if (hypot != 0) {
					double cur = (localX * dX + localY * dY) / hypot;
					if ((cur > angle) || ((cur == angle) && (hypot > length))) {
						nextPointX = tmpX;
						nextPointY = tmpY;
						length = hypot;
						angle = cur;
					}
				}
			}
			if (length < 0.0)
				return 0.0;
			result += currentPointX * nextPointY - nextPointX * currentPointY;
			double dX = nextPointX - currentPointX;
			double dY = nextPointY - currentPointY;
			double hypot = MathUtil.hypot(dX, dY);
			if (hypot != 0) {
				localX = dX / hypot;
				localY = dY / hypot;
			}
			currentPointX = nextPointX;
			currentPointY = nextPointY;

			dX = nextPointX - startPointX;
			dY = nextPointY - startPointY;
			if ((dX == 0.0) && (dY == 0.0))
				break;
		}
		return result * 0.5;
	}
}
