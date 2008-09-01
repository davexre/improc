package com.slavi.util.tree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.slavi.util.tree.KDTree.Node;

public abstract class KDNodeSaver<E> {

	public abstract String nodeToString(E node);
	
	public abstract E nodeFromString(String source);

	private void toTextStream_recursive(Node<E> node, PrintWriter fou) {
		if (node == null)
			return;
		fou.println(nodeToString(node.data));
		toTextStream_recursive(node.left, fou);
		toTextStream_recursive(node.right, fou);
	}
	
	/**
	 * The tree is stored to a text stream one node at a line.
	 */
	public void toTextStream(KDTree<E> tree, PrintWriter fou) {
		if (tree instanceof ConcurrentKDTree) {
			((ConcurrentKDTree<E>)tree).lock.readLock().lock();
		}
		try {
			toTextStream_recursive(tree.root, fou);
		} finally {
			if (tree instanceof ConcurrentKDTree) {
				((ConcurrentKDTree<E>)tree).lock.readLock().unlock();
			}
		}
	}
	
	/**
	 * Reads and APPENDS nodes from a text stream one node at a line till EOF.
	 * If a line is empty or if the very first character is # the line is ignored. 
	 * @return The number of items read. 
	 */
	public int fromTextStream(KDTree<E> tree, BufferedReader fin) throws IOException {
		int result = 0;
		while (fin.ready()) {
			String str = fin.readLine().trim();
			if ((str.length() > 0) && (str.charAt(0) != '#')) {
				tree.add(nodeFromString(str));
				result++;
			}
		}
		return result;
	}
}