package com.slavi.tree;

public interface KDNodeSaver {

	public String nodeToString(KDNode node);
		
	public KDNode nodeFromString(String source);
}
