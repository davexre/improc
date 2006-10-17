package com.slavi.img.working;

public abstract class KDNodeBase implements KDNode {

	protected KDNode left = null;
	
	protected KDNode right = null;
	
	public boolean canFindDistanceToPoint(KDNode node) {
		return true;
	}

	public KDNode getLeft() {
		return left;
	}

	public KDNode getRight() {
		return right;
	}

	public void setLeft(KDNode node) {
		left = node;
	}

	public void setRight(KDNode node) {
		right = node;
	}
}
