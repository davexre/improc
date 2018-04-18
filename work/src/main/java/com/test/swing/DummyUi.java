package com.test.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.slavi.ann.test.v2.Utils;
import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.swing.SwingUtil;

public class DummyUi {

	
	public static class SwingMatrix extends JComponent {
		int baseColor = 0x123456;
		BufferedImage img;
		
		public SwingMatrix() {
			//setOpaque(true);
			//setDoubleBuffered(true);
		}
		
		public synchronized void setValue(Matrix value) {
			img = Utils.toImage(baseColor, 0, 1, value, img);
			repaint();
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
		
		JFrame frame = new JFrame() {
			public Color getBackground() {
				return super.getBackground();
			}
			public boolean isOpaque() {
				return true;
			}
		};
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
