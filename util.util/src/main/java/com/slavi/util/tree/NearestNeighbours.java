package com.slavi.util.tree;

public class NearestNeighbours<E> {
	int searchSteps;
	
	int usedSearchSteps;

	double[][] hr;

	E target;
	
	double[] distancesToTarget;
	
	double maxDistanceToTarget;
	
	E[] items;
	
	int size;
	
	NearestNeighbours(E target, int maxCapacity, int dimensions, double maxDistanceToTarget) {
		this.target = target;
		distancesToTarget = new double[maxCapacity];
		items = (E[])new Object[maxCapacity];
		size = 0;
		searchSteps = 0;
		usedSearchSteps = 0;
		hr = new double[dimensions][2];
		this.maxDistanceToTarget = maxDistanceToTarget;

		for (int i = dimensions - 1; i >= 0; i--) {
			hr[i][0] = Double.MIN_VALUE;
			hr[i][1] = Double.MAX_VALUE;
		}
	}
	
	public int getSize() {
		return size;
	}
	
	public E getItem(int atIndex) {
		return items[atIndex];
	}
	
	public double getDistanceToTarget(int atIndex) {
		return distancesToTarget[atIndex];
	}

	public double getMaxDistanceToTarget() {
		return getSize() < getCapacity() ? maxDistanceToTarget : getDistanceToTarget(getSize() - 1); 
	}
	
	public int getCapacity() {
		return distancesToTarget.length;
	}
	
	public E getTarget() {
		return target;
	}
	
	public int countAdds = 0;

	public int getSearchSteps() {
		return searchSteps;
	}
	
	public int getUsedSearchSteps() {
		return usedSearchSteps;
	}
	
	void add(E item, double distanceToTarget) {
		countAdds++;
		if (distanceToTarget > maxDistanceToTarget)
			return;
		int insertAt = 0;
		// Find insert position
		for (; insertAt < size; insertAt++) {
			if (distanceToTarget < distancesToTarget[insertAt])
				break;
		}
		// If value is greater than all elements then ignore it.
		if (insertAt >= distancesToTarget.length)
			return;
		// Increase size if limit not reached
		if (size < distancesToTarget.length)
			size++;
		// Make room to insert
		int itemsToMove = size - insertAt - 1;
		if (itemsToMove > 0) {
			System.arraycopy(distancesToTarget, insertAt, distancesToTarget, insertAt + 1, itemsToMove);
			System.arraycopy(items, insertAt, items, insertAt + 1, itemsToMove);
		}
		// Put item in insertAt position
		distancesToTarget[insertAt] = distanceToTarget;
		items[insertAt] = item;
	}
}
