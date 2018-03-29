package com.test.swing;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.ColorConversion;
import com.slavi.util.ColorConversion.RGB;
import com.slavi.util.swing.SwingUtil;

public class DummyUi {

	public static class SwingMatrix extends JComponent {
		Object lock = new Object();
		Matrix m = new Matrix();
		
		public SwingMatrix() {
			setDoubleBuffered(true);
		}
		
		public synchronized void setValue(Matrix value) {
			if (value == null) {
				m.resize(0, 0);
			} else {
				value.copyTo(m);
			}
			repaint();
		}
		
		int baseColor = 0x123456;
		public Color getColor(double value) {
			double col[] = new double[3];
			ColorConversion.RGB.fromRGB(baseColor, col);
			ColorConversion.HSL.fromDRGB(col, col);
			col[1] = value;
			//int c = (int) MathUtil.mapValue(value, 0, 1, 0, 255);
			//c = c | (c << 8) | (c << 16);
			ColorConversion.HSL.toDRGB(col, col);
			int c = ColorConversion.RGB.toRGB(col);
			return new Color(c);
		}
		
		public synchronized void paint(Graphics g) {
			if (m.getSizeX() == 0 || m.getSizeY() == 0) {
				g.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
				return;
			}
			double dX = ((double) this.getWidth()) / m.getSizeX(); 
			double dY = ((double) this.getHeight()) / m.getSizeY();

			for (int i = 0; i < m.getSizeX(); i++)
				for (int j = 0; j < m.getSizeY(); j++) {
					g.setColor(getColor(m.getItem(i, j)));
					g.fillRect(
							(int) (i * dX), 
							(int) (j * dY), 
							(int)((i + 1) * dX) - (int)(i * dX), 
							(int)((j + 1) * dY) - (int)(j * dY));
				}
		}
	}
	
	public void doIt(String[] args) throws Exception {
		UIManager.setLookAndFeel(new com.sun.java.swing.plaf.gtk.GTKLookAndFeel());
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		SwingMatrix sm = new SwingMatrix();
		Matrix m = new Matrix(10,10);
		//m.makeE();
		for (int i = m.getVectorSize() - 1; i >=0; i--)
			m.setVectorItem(i, MathUtil.mapValue(i, 0, m.getVectorSize() - 1, 0, 1));
		
		sm.setValue(m);
		frame.add(sm);
		
		frame.setSize(100, 100);
		SwingUtil.center(frame);
		frame.setVisible(true);
	}

	public static void main(String[] args) throws Exception {
		new DummyUi().doIt(args);
		System.out.println("Done.");
	}
}
