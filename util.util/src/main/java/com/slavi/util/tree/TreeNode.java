package com.slavi.util.tree;

public class TreeNode<E> {
	
	protected final E data;
	
	protected volatile TreeNode<E> left;
	
	protected volatile TreeNode<E> right;
	
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
