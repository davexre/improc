package com.slavi.util.tree;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class ConcurrentKDTree<E> extends KDTree<E> {
	protected final ReentrantReadWriteLock lock;

	public ConcurrentKDTree(int dimensions) {
		super(dimensions);
		lock = new ReentrantReadWriteLock();
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

	public NearestNeighbours<E> getNearestNeighboursBBFOriginal(E target, int maxNeighbours, int maxSearchSteps) {
		lock.readLock().lock();
		try {
			return super.getNearestNeighboursBBFOriginal(target, maxNeighbours, maxSearchSteps);
		} finally {
			lock.readLock().unlock();
		}
	}

	public void add(E node) {
		if (node == null)
			return;
		lock.writeLock().lock();
		try {
			super.add(node);
		} finally {
			lock.writeLock().unlock();
		}
	}
}
