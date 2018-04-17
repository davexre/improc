package com.test.swing;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.ColorConversion;
import com.slavi.util.swing.SwingUtil;

public class DummyUi {

	
	public static class SwingMatrix extends JComponent {
		int baseColor = 0x123456;
		BufferedImage img;
		
		public SwingMatrix() {
			setOpaque(true);
			//setDoubleBuffered(true);
		}
		
		public synchronized void setValue(Matrix value) {
			img = genImg(img, value, baseColor);
			repaint();
		}
		
		public static BufferedImage assertSize(BufferedImage bi, int width, int height) {
			if (bi == null || bi.getWidth() != width || bi.getHeight() != height || bi.getType() != BufferedImage.TYPE_INT_RGB)
				bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			return bi;
		}
		
		public static  BufferedImage genImg(BufferedImage bi, Matrix m, int baseColor) {
			if (m == null || m.getSizeX() <= 0 || m.getSizeY() <= 0)
				return null;
			bi = assertSize(bi, m.getSizeX(), m.getSizeY());
			double col1[] = new double[3];
			double col2[] = new double[3];
			ColorConversion.RGB.fromRGB(baseColor, col1);
			ColorConversion.HSL.fromDRGB(col1, col1);

			for (int i = m.getSizeX() - 1; i >= 0; i--) {
				for (int j = m.getSizeY() - 1; j >= 0; j--) {
					col1[1] = m.getItem(i, j); //MathUtil.mapValue(m.getItem(i, j), 0d, 1d, 0d, 1d);
					ColorConversion.HSL.toDRGB(col1, col2);
					int rgb = ColorConversion.RGB.toRGB(col2);
					bi.setRGB(i, j, rgb);
				}
			}
			return bi;
		}
		
		public synchronized void paint(Graphics g) {
			BufferedImage img = this.img;
			if (img == null) {
				g.setColor(new Color(baseColor));
				g.fillRect(0, 0, getWidth(), getHeight());
			} else {
				g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
			}
		}
	}
	
	public void doIt(String[] args) throws Exception {
		UIManager.setLookAndFeel(new com.sun.java.swing.plaf.gtk.GTKLookAndFeel());
		JFrame frame = new JFrame();
		frame.setBackground(Color.RED); // maximze flicker. try to find a solution
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
