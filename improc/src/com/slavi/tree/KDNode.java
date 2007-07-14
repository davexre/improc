package com.slavi.tree;

public interface KDNode<E extends KDNode<E>> {

	public int getDimensions();
	
	public double getValue(int dimensionIndex);
	
	public boolean canFindDistanceToPoint(E node);
	
	public E getLeft();
	
	public void setLeft(E node);
	
	public E getRight();
	
	public void setRight(E node);
}
