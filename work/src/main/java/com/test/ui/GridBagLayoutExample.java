package com.test.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GridBagLayoutExample {
	
	public static JPanel makePanel() {
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createLineBorder(Color.black));
		return p;
	}

	public static class BoxedPanel extends JPanel {
		public JPanel north;
		public JPanel east;
		public JPanel south;
		public JPanel west;
		public JPanel center;
		
		public BoxedPanel() {
			setLayout(new BorderLayout(5, 5));
			add(north = makePanel(), BorderLayout.NORTH);
			add(east = makePanel(), BorderLayout.EAST);
			add(south = makePanel(), BorderLayout.SOUTH);
			add(west = makePanel(), BorderLayout.WEST);
			add(center = makePanel(), BorderLayout.CENTER);
		}
	}

	public static class GridBagLayoutPanel extends JPanel {
		public JPanel north;
		public JPanel east;
		public JPanel south;
		public JPanel west;
		public JPanel center;
		
		public GridBagLayoutPanel() {
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 3;
			c.fill = GridBagConstraints.HORIZONTAL;
			add(north = makePanel(), c);
			
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 1;
			c.fill = GridBagConstraints.VERTICAL;
			add(west = makePanel(), c);

			c.gridx = 1;
			c.gridy = 1;
			c.gridwidth = 1;
			c.fill = GridBagConstraints.BOTH;
			add(center = makePanel(), c);

			c.gridx = 2;
			c.gridy = 1;
			c.gridwidth = 1;
			c.fill = GridBagConstraints.VERTICAL;
			add(east = makePanel(), c);

			c.gridx = 0;
			c.gridy = 2;
			c.gridwidth = 3;
			c.fill = GridBagConstraints.HORIZONTAL;
			add(south = makePanel(), c);
		}
	}
	
	void doIt() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		GridBagLayoutPanel panel = new GridBagLayoutPanel();
/*		panel.north.add(new BoxedPanel());
		panel.east.add(new BoxedPanel());
		panel.south.add(new BoxedPanel());
		panel.west.add(new BoxedPanel());
		panel.center.add(new BoxedPanel());
*/
/*
		BoxedPanel panel = new BoxedPanel();
		panel.north .add(new GridBagLayoutPanel());
		panel.east  .add(new GridBagLayoutPanel());
		panel.south .add(new GridBagLayoutPanel());
		panel.west  .add(new GridBagLayoutPanel());
		panel.center.add(new GridBagLayoutPanel());
*/
		panel.north .add(new JButton("north"));
		panel.east  .add(new JButton("east"));
		panel.south .add(new JButton("south"));
		panel.west  .add(new JButton("west"));
		panel.center.add(new JButton("center with long label"));
		
//		frame.add(panel, BorderLayout.NORTH);
		mainPanel.add(panel);
		frame.add(mainPanel);
		frame.setPreferredSize(new Dimension(600, 400));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new GridBagLayoutExample().doIt();
	}
}
