package com.test.ui;

import javax.swing.JFrame;

import com.slavi.math.matrix.Matrix;
import com.slavi.util.ui.BitMatrix;

public class TestBitMatrix {
	public static void main(String[] args) {
		JFrame f = new JFrame("BitMatrix test");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		BitMatrix theComponent = new BitMatrix();

		Matrix m = new Matrix(20, 20);
		for (int i = m.getSizeX() - 1; i >= 0; i--)
			for (int j = m.getSizeY() - 1; j >= 0; j--)
				m.setItem(i, j, (double) (i * j) / (double) (m.getVectorSize()));
		theComponent.setMatrix(m);

		System.out.println(m.min());
		f.add(theComponent);
		f.setSize(400, 300);
		f.setVisible(true);
	}
}
