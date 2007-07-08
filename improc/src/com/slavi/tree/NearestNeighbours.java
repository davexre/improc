package com.slavi.tree;


public class NearestNeighbours{

	private KDNode target;
	
	private double[] distancesToTarget;
	
	private KDNode[] items;
	
	private int m_size;
	
	public int size() {
		return m_size;
	}
	
	public KDNode getItem(int atIndex) {
		return (KDNode)items[atIndex];
	}
	
	public double getDistanceToTarget(int atIndex) {
		return distancesToTarget[atIndex];
	}
	
	public int getCapacity() {
		return distancesToTarget.length;
	}
	
	public KDNode getTarget() {
		return target;
	}
	
	public NearestNeighbours(KDNode target, int maxCapacity) {
		this.target = target;
		this.distancesToTarget = new double[maxCapacity];
		this.items = new KDNode[maxCapacity];
		this.m_size = 0;
	}
	
	public int countAdds = 0;
	
	public void add(KDNode item, double distanceToTarget) {
		countAdds++;
		int insertAt = 0;
		// Find insert position
		for (; insertAt < m_size; insertAt++) {
			if (distanceToTarget < distancesToTarget[insertAt])
				break;
		}
		// If value is greater than all elements then ignore it.
		if (insertAt >= distancesToTarget.length)
			return;
		// Increase size if limit not reached
		if (m_size < distancesToTarget.length)
			m_size++;
		// Make room to insert
		int itemsToMove = m_size - insertAt - 1;
		if (itemsToMove > 0) {
			System.arraycopy(distancesToTarget, insertAt, distancesToTarget, insertAt + 1, itemsToMove);
			System.arraycopy(items, insertAt, items, insertAt + 1, itemsToMove);
		}
		// Put item in insertAt position
		distancesToTarget[insertAt] = distanceToTarget;
		items[insertAt] = item;
	}
}
