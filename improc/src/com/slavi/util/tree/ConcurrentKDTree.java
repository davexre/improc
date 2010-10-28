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
		try {
			super.clear();
		} finally {
			lock.writeLock().unlock();
		}
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

	public NearestNeighbours<E> getNearestNeighbours(E target, int maxNeighbours, double maxDistanceToTarget) {
		lock.readLock().lock();
		try {
			return super.getNearestNeighbours(target, maxNeighbours, maxDistanceToTarget);
		} finally {
			lock.readLock().unlock();
		}
	}

	public NearestNeighbours<E> getNearestNeighboursBBF(E target, int maxNeighbours, int maxSearchSteps, double maxDistanceToTarget) {
		lock.readLock().lock();
		try {
			return super.getNearestNeighboursBBF(target, maxNeighbours, maxSearchSteps, maxDistanceToTarget);
		} finally {
			lock.readLock().unlock();
		}
	}

	public NearestNeighbours<E> getNearestNeighboursMy(E target, int maxNeighbours, double maxDistancePerCoordinate, double maxDistanceToTarget) {
		lock.readLock().lock();
		try {
			return super.getNearestNeighboursMy(target, maxNeighbours, maxDistancePerCoordinate, maxDistanceToTarget);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public NearestNeighbours<E> getNearestNeighboursMyBBF(E target, int maxNeighbours, double maxDistancePerCoordinate, int maxSearchSteps, double maxDistanceToTarget) {
		lock.readLock().lock();
		try {
			return super.getNearestNeighboursMyBBF(target, maxNeighbours, maxDistancePerCoordinate, maxSearchSteps, maxDistanceToTarget);
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
	
	private void add_recursive(E data, TreeNode<E> curNode, int dimension, int depthLevel) {
		depthLevel++;
		double value = getValue(data, dimension);
		double curNodeValue = getValue(curNode.data, dimension);
		int nextDimension = (dimension + 1) % dimensions;
		if (value < curNodeValue) {
			if (curNode.left == null) {
				lock.readLock().unlock();
				lock.writeLock().lock();
				try {
					if (curNode.left == null) { // recheck needed
						if (treeDepth < depthLevel)
							treeDepth = depthLevel;
						curNode.left = new TreeNode<E>(data);
						mutations++;
						size++;
						return;
					}
				} finally {
					lock.readLock().lock();
					lock.writeLock().unlock();
				}
			} 
			add_recursive(data, curNode.left, nextDimension, depthLevel);
		} else if (ignoreDuplicates && (value == curNodeValue) && compareItems(data, curNode.data)) {
			return;
		} else {
			if (curNode.right == null) {
				lock.readLock().unlock();
				lock.writeLock().lock();
				try {
					if (curNode.right == null) { // recheck needed
						if (treeDepth < depthLevel)
							treeDepth = depthLevel;
						curNode.right = new TreeNode<E>(data);
						mutations++;
						size++;
						return;
					}
				} finally {
					lock.readLock().lock();
					lock.writeLock().unlock();
				}
			}
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
				try {
					if (root == null) { // recheck needed
						treeDepth = 1;
						root = new TreeNode<E>(item);
						mutations++;
						size++;
						return;
					}
				} finally {
					lock.readLock().lock();
					lock.writeLock().unlock();
				}
			}
			add_recursive(item, root, 0, 1);
		} finally {
			lock.readLock().unlock();
		}
	}
}
