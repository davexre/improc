package com.test.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BoxLayoutExample {
	
	public static JPanel makePanel() {
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createLineBorder(Color.black));
		return p;
	}

	public static JButton addButton(Container pane, String text) {
		JButton btn = new JButton(text);
		btn.setAlignmentX(0.9f);
		pane.add(btn);
		return btn;
	}
	
	void doIt() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		addButton(panel, "north");
		addButton(panel, "east");
		addButton(panel, "south");
		addButton(panel, "west");
		addButton(panel, "center with long label");
		
		frame.add(panel);
		frame.setPreferredSize(new Dimension(600, 400));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new BoxLayoutExample().doIt();
	}
}
