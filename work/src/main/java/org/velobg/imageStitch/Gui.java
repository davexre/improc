package org.velobg.imageStitch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class Gui {
	JTextArea uiLog;
	JTextField uiUrl;
	JTextField uiFolder;
	JProgressBar uiProgressBar;
	
	JComponent createLeftPanel() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(250, 0));
		panel.setLayout(new BorderLayout());
		ImageIcon icon = new ImageIcon(getClass().getResource("velobg_org_logo.png"));
		panel.add(new JLabel(icon), BorderLayout.NORTH);
		JLabel l = new JLabel("<html>Програмата за сваляне на чертежи-мозайка от сайта на sofia-agk.com и подреждането им в един цял чертеж.</html>");
		l.setBorder(BorderFactory.createLineBorder(Color.black));
		panel.add(l, BorderLayout.CENTER);
		return panel;
	}

	static final int GAP = 10;
	JComponent createFormFields() {
		JPanel panel = new JPanel();
		panel.setLayout(new SpringLayout());
		JLabel l = new JLabel("URL");
		panel.add(l);
		uiUrl = new JTextField();
		l.setLabelFor(uiUrl);
		l.setDisplayedMnemonic('U');
		panel.add(uiUrl);
		
		l = new JLabel("Folder");
		l.setDisplayedMnemonic('F');
		panel.add(l);
		uiFolder = new JTextField();
		l.setLabelFor(uiFolder);
		panel.add(uiFolder);

		SpringUtilities.makeCompactGrid(panel, 2, 2, GAP, GAP, GAP, GAP / 2);
		return panel;
	}
	
	JComponent createButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.TRAILING, GAP, GAP));
		JButton btn = new JButton("GO");
		panel.add(btn);
		return panel;
	}

	JComponent createProgressBarAndLog() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		uiLog = new JTextArea();
		uiLog.setEditable(false);
		panel.add(uiLog);
		uiProgressBar = new JProgressBar(0, 100);
		panel.add(uiProgressBar);
		
		return panel;
	}
	
	JComponent createTopPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(createFormFields());
		panel.add(createButtons());
		panel.add(createProgressBarAndLog());
		return panel;
	}
	
	void createAndShowGUI() {
		JFrame frame = new JFrame(
				"Сваляне на чертежи от сайта на sofia-agk.com");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		mainPanel.add(createLeftPanel(), BorderLayout.WEST);

		JPanel rightPanel = new JPanel();
		mainPanel.add(rightPanel, BorderLayout.CENTER);
		
		// Right
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(createTopPanel(), BorderLayout.NORTH);
		
		frame.add(mainPanel);
		frame.setPreferredSize(new Dimension(700, 500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new Gui().createAndShowGUI();
	}
}
