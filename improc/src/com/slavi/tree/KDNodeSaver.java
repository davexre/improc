package com.slavi.tree;

public interface KDNodeSaver<E extends KDNode<E>> {

	public String nodeToString(E node);
		
	public E nodeFromString(String source);
}
