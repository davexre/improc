package com.slavi.util.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import com.slavi.math.matrix.Matrix;
import com.slavi.util.MatrixUtil;

public class BitMatrix extends JComponent {
	private static final long serialVersionUID = 3643680302268014549L;

	protected Matrix matrix = null;
	BufferedImage img;
	protected double checkedValue = 1;
	protected double unCheckedValue = 0;
	protected boolean usingGradientColors = true;

	public double getCheckedValue() {
		return checkedValue;
	}

	public void setCheckedValue(double checkedValue) {
		this.checkedValue = checkedValue;
		repaint();
	}

	public Matrix getMatrix() {
		return matrix;
	}

	public void setMatrix(Matrix matrix) {
		this.matrix = matrix;
		repaint();
	}

	public double getUnCheckedValue() {
		return unCheckedValue;
	}

	public void setUnCheckedValue(double unCheckedValue) {
		this.unCheckedValue = unCheckedValue;
		repaint();
	}

	public boolean isUsingGradientColors() {
		return usingGradientColors;
	}

	public void setUsingGradientColors(boolean useGradientColors) {
		this.usingGradientColors = useGradientColors;
		repaint();
	}

	protected void drawX(Graphics g) {
		int toX = this.getWidth() - 1;
		int toY = this.getHeight() - 1;
		g.fillRect(0, 0, toX, toY);
		g.drawLine(0, 0, toX, toY);
		g.drawLine(0, toY, toX, 0);
	}

	public void paint(Graphics g) {
		if (matrix == null) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight());
			return;
		}
		img = MatrixUtil.toImage(matrix, unCheckedValue, checkedValue, 0, img);
		g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
	}
}
