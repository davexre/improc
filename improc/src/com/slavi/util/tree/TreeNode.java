package com.slavi.util.tree;

public class TreeNode<E> {
	
	protected E data;
	
	protected TreeNode<E> left, right;
	
	public TreeNode(E data) {
		this.data = data;
		left = null;
		right = null;
	}
	
	public E getData() {
		return data;
	}
	
	public TreeNode<E> getLeft() {
		return left;
	}
	
	public TreeNode<E> getRight() {
		return right;
	}
}
