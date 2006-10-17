package com.slavi.img;

public class NearestNeighbours {

	private KDNode target;
	
	private double[] values;
	
	private KDNode[] items;
	
	private int m_size;
	
	public int size() {
		return m_size;
	}
	
	public KDNode getItem(int atIndex) {
		return items[atIndex];
	}
	
	public double getValue(int atIndex) {
		return values[atIndex];
	}
	
	public int getCapacity() {
		return values.length;
	}
	
	public KDNode getTarget() {
		return target;
	}
	
	public NearestNeighbours(KDNode target, int maxCapacity) {
		this.target = target;
		this.values = new double[maxCapacity];
		this.items = new KDNode[maxCapacity];
		this.m_size = 0;
	}
	
	public int countAdds = 0;
	
	public void add(KDNode item, double value) {
		countAdds++;
		int insertAt = 0;
		// Find insert position
		for (; insertAt < m_size; insertAt++) {
			if (value < values[insertAt])
				break;
		}
		// If value is greater than all elements then ignore it.
		if (insertAt >= values.length)
			return;
		// Increase size if limit not reached
		if (m_size < values.length)
			m_size++;
		// Make room to insert
		int itemsToMove = m_size - insertAt - 1;
		if (itemsToMove > 0) {
			System.arraycopy(values, insertAt, values, insertAt + 1, itemsToMove);
			System.arraycopy(items, insertAt, items, insertAt + 1, itemsToMove);
		}
		// Put item in insertAt position
		values[insertAt] = value;
		items[insertAt] = item;
	}
}
