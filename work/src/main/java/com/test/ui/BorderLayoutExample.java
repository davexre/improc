package com.test.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BorderLayoutExample {
	
	class BoxedPanel extends JPanel {
		public JPanel north;
		public JPanel east;
		public JPanel south;
		public JPanel west;
		public JPanel center;
		
		JPanel makePanel() {
			JPanel p = new JPanel();
			p.setBorder(BorderFactory.createLineBorder(Color.black));
			return p;
		}
		
		public BoxedPanel() {
			setLayout(new BorderLayout(5, 5));
			add(north = makePanel(), BorderLayout.NORTH);
			add(east = makePanel(), BorderLayout.EAST);
			add(south = makePanel(), BorderLayout.SOUTH);
			add(west = makePanel(), BorderLayout.WEST);
			add(center = makePanel(), BorderLayout.CENTER);
		}
	}
	
	void doIt() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BoxedPanel panel = new BoxedPanel();

		panel.north.add(new BoxedPanel());
		panel.north.add(new BoxedPanel());
		panel.north.add(new BoxedPanel());
		panel.east.add(new BoxedPanel());
		panel.south.add(new BoxedPanel());
		panel.west.add(new BoxedPanel());
		panel.center.add(new BoxedPanel());
		
		frame.add(panel);
		frame.setPreferredSize(new Dimension(200, 100));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new BorderLayoutExample().doIt();
	}
}
