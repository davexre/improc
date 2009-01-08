package com.slavi.util.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class ConcurrentKDTree<E> extends KDTree<E> {
	protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public ConcurrentKDTree(int dimensions, boolean ignoreDuplicates) {
		super(dimensions, ignoreDuplicates);
	}

	public ConcurrentKDTree(int dimensions, List<E> items, boolean ignoreDuplicates) {
		super(dimensions, items, ignoreDuplicates);
	}
	
	public void clear() {
		lock.writeLock().lock();
		super.clear();
		lock.writeLock().unlock();
	}

	public ArrayList<E> toList() {
		lock.readLock().lock();
		try {
			return super.toList();
		} finally {
			lock.readLock().unlock();
		}
	}

	public void balance() {
		lock.writeLock().lock();
		try {
			super.balance();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public NearestNeighbours<E> getNearestNeighbours(E target, int maxNeighbours) {
		lock.readLock().lock();
		try {
			return super.getNearestNeighbours(target, maxNeighbours);
		} finally {
			lock.readLock().unlock();
		}
	}

	public NearestNeighbours<E> getNearestNeighboursBBF(E target, int maxNeighbours, int maxSearchSteps) {
		lock.readLock().lock();
		try {
			return super.getNearestNeighboursBBF(target, maxNeighbours, maxSearchSteps);
		} finally {
			lock.readLock().unlock();
		}
	}

	public E findMatching(E target) {
		lock.readLock().lock();
		try {
			return super.findMatching(target);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	private void add_recursive(E data, Node<E> curNode, int dimension, int depthLevel) {
		depthLevel++;
		double value = getValue(data, dimension);
		double curNodeValue = getValue(curNode.data, dimension);
		int nextDimension = (dimension + 1) % dimensions;
		if (value < curNodeValue) {
			if (curNode.left == null) {
				if (treeDepth < depthLevel)
					treeDepth = depthLevel;
				lock.readLock().unlock();
				lock.writeLock().lock();
				curNode.left = new Node<E>(data);
				lock.readLock().lock();
				lock.writeLock().unlock();
				mutations++;
				size++;
			} else
				add_recursive(data, curNode.left, nextDimension, depthLevel);
		} else if (ignoreDuplicates && (value == curNodeValue) && compareItems(data, curNode.data)) {
			return;
		} else {
			if (curNode.right == null) {
				if (treeDepth < depthLevel) 
					treeDepth = depthLevel;
				lock.readLock().unlock();
				lock.writeLock().lock();
				curNode.right = new Node<E>(data);
				lock.readLock().lock();
				lock.writeLock().unlock();
				mutations++;
				size++;
			} else
				add_recursive(data, curNode.right, nextDimension, depthLevel);
		}		
	}
	
	public void add(E item) {
		if (item == null)
			return;
		lock.readLock().lock();
		try {
			if (root == null) { 
				lock.readLock().unlock();
				lock.writeLock().lock();
				root = new Node<E>(item);
				lock.readLock().lock();
				lock.writeLock().unlock();
				mutations++;
				size++;
			} else {
				add_recursive(item, root, 0, 1);
			}
		} finally {
			lock.readLock().unlock();
		}
	}
}
