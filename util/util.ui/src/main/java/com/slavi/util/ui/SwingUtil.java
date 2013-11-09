package com.slavi.util.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Spring;
import javax.swing.SpringLayout;

/**
 * This class contains utility functions for creating user interface using the
 * Standard SWING library
 */
public class SwingUtil {
	/**
	 * Opens the standard SWING directory chooser dialog.
	 * @see #getDirectory(Component)
	 */
	public static String getDirectory() {
		return getDirectory(null);
	}
	
	/**
	 * Opens the standard SWING directory chooser dialog.
	 * <p>
	 * Returns the selected directory or if
	 * canceled returns an EMPTY string "" not a null.
	 */
	public static String getDirectory(Component parent) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select a folder");
		int retval = chooser.showOpenDialog(parent);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			return f == null ? "" : f.getAbsolutePath();
		}
		return "";
	}
	
	/**
	 * Opens the standard SWING file chooser dialog.
	 * @see #getFileName(Component)
	 */
	public static String getFileName() {
		return getFileName(null);
	}
	
	/**
	 * Opens the standard SWING file chooser dialog.
	 * <p>
	 * Returns the selected file or if
	 * canceled returns an EMPTY string "" not a null. The
	 * file MAY be a new one and MIGHT NOT exist.
	 */
	public static String getFileName(Component parent) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Select a file");
		int retval = chooser.showOpenDialog(parent);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			return f == null ? "" : f.getAbsolutePath();
		}
		return "";
	}

	/**
	 * Opens the standard SWING JOptionPane dialog.
	 * <p>
	 * The default selected options is the first object in the list.
	 * Returns the selected object or null is the dialog is canceled.
	 */
	public static Object getUIInput(Object ... values) {
		Object selected = JOptionPane.showInputDialog(null, "Choose one", "Input", JOptionPane.INFORMATION_MESSAGE,
				null, values, values[0]);
		return selected;
	}

	/**
	 * Aligns all the components of <code>parent</code> in a grid with
	 * <code>columns</code>. Each component in a cell is as wide/high as the
	 * maximum preferred width/height of the components in that cell, except for
	 * the cells in row <code>springRow</code> and column <code>springCol</code>
	 * wich are made "elastic" to in order the grid to fit the parent window.
	 * 
	 * @param parent
	 *            the parent container which elements need to be put in a grid
	 * @param columns
	 *            number of columns
	 * @param springRow
	 *            number of "elastic" row
	 * @param springCol
	 *            number of "elastic" column
	 * @param insetX
	 *            horizontal inset of the grid
	 * @param insetY
	 *            vertical inset of the grid
	 * @param xPad
	 *            horizontal padding between cells
	 * @param yPad
	 *            vertical padding between cells
	 */
	public static void makeSpringGrid(Container parent, int columns, int springRow, int springCol,
			int insetX, int insetY, int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
			System.err.println("The first argument to makeGrid must use SpringLayout.");
			return;
		}
		Component components[] = parent.getComponents();
		int maxRow = (components.length - 1) / columns;

		Spring pad;
		Component c2;
		String c2name;
		Spring c2spring;
		ArrayList<Spring> sizes;
		
		// WIDTH
		pad = Spring.constant(xPad);
		c2 = parent;
		c2name = SpringLayout.WEST;
		c2spring = Spring.constant(insetX);
		Spring totalSizeX = c2spring;
		sizes = new ArrayList<Spring>();
		
		for (int col = 0; col < columns; col++) {
			Spring width = Spring.constant(0);
			for (int i = col; i < components.length; i += columns) {
				Component c = components[i];
				SpringLayout.Constraints cons = layout.getConstraints(c);
				width = Spring.max(width, cons.getWidth());
			}
			sizes.add(width);
			totalSizeX = Spring.sum(totalSizeX, c2spring);
			totalSizeX = Spring.sum(totalSizeX, width);
			c2spring = pad;
		}

		c2spring = Spring.constant(insetX);
		for (int col = 0; col < columns; col++) {
			Spring width = sizes.get(col);

			Component c = null;
			for (int i = col; i < components.length; i += columns) {
				c = components[i];
				SpringLayout.Constraints cons = layout.getConstraints(c);
				if (col != springCol) {
					cons.setWidth(width);
				}
				if (col <= springCol) {
					layout.putConstraint(SpringLayout.WEST, c, c2spring, c2name, c2);
				}
			}
			c2 = c;
			c2name = SpringLayout.EAST;
			c2spring = pad;
		}

		pad = Spring.constant(-xPad);
		c2 = parent;
		c2name = SpringLayout.EAST;
		c2spring = Spring.constant(-insetX);
		for (int col = columns - 1; col >= Math.max(0, springCol); col--) {
			Component c = null;
			for (int i = col; i < components.length; i += columns) {
				c = components[i];
				layout.putConstraint(SpringLayout.EAST, c, c2spring, c2name, c2);
			}
			c2 = c;
			c2name = SpringLayout.WEST;
			c2spring = pad;
		}
		
		// HEIGHT
		pad = Spring.constant(yPad);
		c2 = parent;
		c2name = SpringLayout.NORTH;
		c2spring = Spring.constant(insetY);
		Spring totalSizeY = c2spring;
		
		sizes = new ArrayList<Spring>();
		for (int row = 0; row <= maxRow; row++) {
			int startI = row * columns;
			int endI = Math.min(startI + columns, components.length) - 1;
			Spring height = Spring.constant(0);
			for (int i = startI; i <= endI; i++) {
				Component c = components[i];
				SpringLayout.Constraints cons = layout.getConstraints(c);
				height = Spring.max(height, cons.getHeight());
			}
			sizes.add(height);
			totalSizeY = Spring.sum(totalSizeY, c2spring);
			totalSizeY = Spring.sum(totalSizeY, height);
			c2spring = pad;
		}
		
		c2spring = Spring.constant(insetY);
		for (int row = 0; row <= maxRow; row++) {
			int startI = row * columns;
			int endI = Math.min(startI + columns, components.length) - 1;
			Spring height = sizes.get(row);

			Component c = null;
			for (int i = startI; i <= endI; i++) {
				c = components[i];
				SpringLayout.Constraints cons = layout.getConstraints(c);
				if (row != springRow) {
					cons.setHeight(height);
				}
				if (row <= springRow) {
					layout.putConstraint(SpringLayout.NORTH, c, c2spring, c2name, c2);
				}
			}
			c2 = c;
			c2name = SpringLayout.SOUTH;
			c2spring = pad;
		}

		pad = Spring.constant(-yPad);
		c2 = parent;
		c2name = SpringLayout.SOUTH;
		c2spring = Spring.constant(-insetY);
		for (int row = maxRow; row >= Math.max(0, springRow); row--) {
			int startI = row * columns;
			int endI = Math.min(startI + columns, components.length) - 1;
			Component c = null;
			for (int i = startI; i <= endI; i++) {
				c = components[i];
				layout.putConstraint(SpringLayout.SOUTH, c, c2spring, c2name, c2);
			}
			c2 = c;
			c2name = SpringLayout.NORTH;
			c2spring = pad;
		}

		parent.setPreferredSize(new Dimension(totalSizeX.getValue(), totalSizeY.getValue()));
	}

	/**
	 * Aligns the first <code>rows</code> * <code>cols</code> components of
	 * <code>parent</code> in a grid. Each component is as big as the maximum
	 * preferred width and height of the components. The parent is made just big
	 * enough to fit them all.
	 * 
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @param initialX
	 *            x location to start the grid at
	 * @param initialY
	 *            y location to start the grid at
	 * @param xPad
	 *            x padding between cells
	 * @param yPad
	 *            y padding between cells
	 */
	public static void makeGrid(Container parent, int rows, int cols,
			int initialX, int initialY, int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
			System.err
					.println("The first argument to makeGrid must use SpringLayout.");
			return;
		}

		Spring xPadSpring = Spring.constant(xPad);
		Spring yPadSpring = Spring.constant(yPad);
		Spring initialXSpring = Spring.constant(initialX);
		Spring initialYSpring = Spring.constant(initialY);
		int max = rows * cols;

		// Calculate Springs that are the max of the width/height so that all
		// cells have the same size.
		Spring maxWidthSpring = layout.getConstraints(parent.getComponent(0))
				.getWidth();
		Spring maxHeightSpring = layout.getConstraints(parent.getComponent(0))
				.getHeight();
		for (int i = 1; i < max; i++) {
			SpringLayout.Constraints cons = layout.getConstraints(parent
					.getComponent(i));

			maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
			maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
		}

		// Apply the new width/height Spring. This forces all the
		// components to have the same size.
		for (int i = 0; i < max; i++) {
			SpringLayout.Constraints cons = layout.getConstraints(parent
					.getComponent(i));

			cons.setWidth(maxWidthSpring);
			cons.setHeight(maxHeightSpring);
		}

		// Then adjust the x/y constraints of all the cells so that they
		// are aligned in a grid.
		SpringLayout.Constraints lastCons = null;
		SpringLayout.Constraints lastRowCons = null;
		for (int i = 0; i < max; i++) {
			SpringLayout.Constraints cons = layout.getConstraints(parent
					.getComponent(i));
			if (i % cols == 0) { // start of new row
				lastRowCons = lastCons;
				cons.setX(initialXSpring);
			} else { // x position depends on previous component
				cons.setX(Spring.sum(lastCons.getConstraint(SpringLayout.EAST),
						xPadSpring));
			}

			if (i / cols == 0) { // first row
				cons.setY(initialYSpring);
			} else { // y position depends on previous row
				cons.setY(Spring.sum(
						lastRowCons.getConstraint(SpringLayout.SOUTH),
						yPadSpring));
			}
			lastCons = cons;
		}

		// Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(
				SpringLayout.SOUTH,
				Spring.sum(Spring.constant(yPad),
						lastCons.getConstraint(SpringLayout.SOUTH)));
		pCons.setConstraint(
				SpringLayout.EAST,
				Spring.sum(Spring.constant(xPad),
						lastCons.getConstraint(SpringLayout.EAST)));
	}

	/**
	 * Aligns the first <code>rows</code> * <code>cols</code> components of
	 * <code>parent</code> in a grid. Each component in a column is as wide as
	 * the maximum preferred width of the components in that column; height is
	 * similarly determined for each row. The parent is made just big enough to
	 * fit them all.
	 * 
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @param initialX
	 *            x location to start the grid at
	 * @param initialY
	 *            y location to start the grid at
	 * @param xPad
	 *            x padding between cells
	 * @param yPad
	 *            y padding between cells
	 */
	public static void makeCompactGrid(Container parent, int rows, int cols,
			int initialX, int initialY, int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
			System.err
					.println("The first argument to makeCompactGrid must use SpringLayout.");
			return;
		}

		// Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width, layout.getConstraints(parent.getComponent(r * cols + c)).getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints = layout.getConstraints(parent.getComponent(r * cols + c));
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		// Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height, layout.getConstraints(parent.getComponent(r * cols + c)).getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints = layout.getConstraints(parent.getComponent(r * cols + c));
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		// Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}

	public static <C extends JComponent> C addBorder(C c) {
		c.setBorder(BorderFactory.createLineBorder(Color.black));
		return c;
	}
	
	public static <C extends Container> C setEnabled(C container, boolean isEnabled) {
		if (container != null) {
			container.setEnabled(isEnabled);
			for (Component i : container.getComponents()) {
				if (i instanceof Container) {
					setEnabled((Container) i, isEnabled);
				}
			}
		}
		return container;
	}
	
	public static <C extends Component> C center(C c) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension size = c.getSize();
		int x = Math.max(0, (dim.width - size.width) / 2);
		int y = Math.max(0, (dim.height - size.height) / 2);
		c.setLocation(x, y);
		return c;
	}
}
