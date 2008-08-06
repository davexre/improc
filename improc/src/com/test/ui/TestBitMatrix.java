package com.test.ui;

import javax.swing.JFrame;

import com.slavi.math.matrix.Matrix;
import com.slavi.ui.BitMatrix;

public class TestBitMatrix {
	public static void main(String[] args) {
		JFrame f = new JFrame("BitMatrix test");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		BitMatrix theComponent = new BitMatrix();

		Matrix m = new Matrix(20, 20);
		// (int)(Math.random() * 5 + 5),
		// (int)(Math.random() * 5 + 5));
		for (int i = m.getSizeX() - 1; i >= 0; i--)
			for (int j = m.getSizeY() - 1; j >= 0; j--)
				// m.setItem(i, j, Math.random());
				m.setItem(i, j, (int) ((double) (i * j) / (double) (m.getVectorSize())));
		theComponent.setMatrix(m);

		f.add(theComponent);
		f.setSize(400, 300);
		f.setVisible(true);
	}
}
