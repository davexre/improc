package com.slavi.tree;

public interface KDNode {

	public int getDimensions();
	
	public double getValue(int dimensionIndex);
	
	public boolean canFindDistanceToPoint(KDNode node);
	
	public KDNode getLeft();
	
	public void setLeft(KDNode node);
	
	public KDNode getRight();
	
	public void setRight(KDNode node);
}
