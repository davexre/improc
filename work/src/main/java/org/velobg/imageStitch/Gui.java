package org.velobg.imageStitch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class Gui {
	JTextArea log;
	
	void createAndShowGUI() {
		JFrame frame = new JFrame(
				"Сваляне на чертежи от сайта на sofia-agk.com");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));

		{
			JPanel leftPanel = new JPanel();
			leftPanel.setPreferredSize(new Dimension(250, 0));
			leftPanel.setLayout(new GridBagLayout());
			mainPanel.add(leftPanel);
			
			ImageIcon icon = new ImageIcon(getClass().getResource("velobg_org_logo.png"));
			GridBagConstraints gbc = new GridBagConstraints();
			leftPanel.add(new JLabel(icon), gbc);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy = 1;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			JLabel l;
			l = new JLabel("<html>Програмата за сваляне на чертежи-мозайка от сайта на sofia-agk.com и подреждането им в един цял чертеж.</html>");
			l.setBorder(BorderFactory.createLineBorder(Color.black));
			leftPanel.add(l, gbc);
		}
		
		{
			JPanel rightPanel = new JPanel();
			rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
			mainPanel.add(rightPanel);
			
			JPanel panel = new JPanel();
			rightPanel.add(panel);
			
			
			log = new JTextArea();
			rightPanel.add(log);
		}
//				,
//				icon, SwingConstants.TRAILING);

		frame.add(mainPanel);
		frame.setPreferredSize(new Dimension(700, 500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new Gui().createAndShowGUI();
	}
}
