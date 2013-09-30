package com.slavi.util.tree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.slavi.util.tree.KDTree;
import com.slavi.util.tree.TreeNode;

public abstract class TXTKDTree<E> {

	public abstract String nodeToString(E node);
	
	public abstract E nodeFromString(String source);

	private void toTextStream_recursive(TreeNode<E> node, PrintWriter fou) {
		if (node == null)
			return;
		fou.println(nodeToString(node.getData()));
		toTextStream_recursive(node.getLeft(), fou);
		toTextStream_recursive(node.getRight(), fou);
	}
	
	/**
	 * The tree is stored to a text stream one node at a line.
	 * <p>
	 * Note: This method is thread UNSAFE
	 */
	public void toTextStream(KDTree<E> tree, PrintWriter fou) {
		toTextStream_recursive(tree.getRoot(), fou);
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
