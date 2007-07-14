package com.slavi.tree;

public abstract class KDNodeBase<E extends KDNodeBase<E>> implements KDNode<E> {

	protected E left = null;
	
	protected E right = null;
	
	public boolean canFindDistanceToPoint(E node) {
		return true;
	}

	public E getLeft() {
		return left;
	}

	public E getRight() {
		return right;
	}

	public void setLeft(E node) {
		left = node;
	}

	public void setRight(E node) {
		right = node;
	}
}
