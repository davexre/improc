package org.velobg.imageStitch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

public class Gui {
	public static void makeGrid(Container parent, int cols, int springRow, int springCol,
			int insetX, int insetY, int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
			System.err
					.println("The first argument to makeGrid must use SpringLayout.");
			return;
		}
		
		Spring xPadSpring = Spring.constant(xPad);
		Spring yPadSpring = Spring.constant(yPad);
		Spring initialXSpring = Spring.constant(insetX);
		Spring initialYSpring = Spring.constant(insetY);

		Component components[] = parent.getComponents();
		
		for (int i = 0; i < components.length; i++) {
			int col = i % cols;
			int row = i / cols;
			Component c = components[i];
			Component c2;
			String c2name;
			Spring c2spring;
			
			// WEST
			if (col == 0) {
				c2 = parent;
				c2name = SpringLayout.WEST;
				c2spring = initialXSpring;
			} else {
				c2 = components[i - 1];
				c2name = SpringLayout.EAST;
				c2spring = xPadSpring;
			}
			layout.putConstraint(SpringLayout.WEST, c, c2spring, c2name, c2);
			
			// EAST
			if ((col == cols - 1) || (i == components.length - 1)) {
				c2 = parent;
				c2name = SpringLayout.EAST;
				c2spring = initialXSpring;
			} else {
				c2 = components[i + 1];
				c2name = SpringLayout.EAST;
				c2spring = xPadSpring;
			}
			layout.putConstraint(SpringLayout.EAST, c, c2spring, c2name, c2);
			
			// NORTH
			if (row == 0) {
				c2 = parent;
				c2name = SpringLayout.NORTH;
				c2spring = initialYSpring;
			} else {
				c2 = components[i - cols];
				c2name = SpringLayout.SOUTH;
				c2spring = yPadSpring;
			}
			layout.putConstraint(SpringLayout.NORTH, c, c2spring, c2name, c2);
			
			// SOUTH
			if (i + cols >= components.length) {
				c2 = parent;
				c2name = SpringLayout.SOUTH;
				c2spring = initialYSpring;
			} else {
				c2 = components[i + cols];
				c2name = SpringLayout.NORTH;
				c2spring = yPadSpring;
			}
			layout.putConstraint(SpringLayout.SOUTH, c, c2spring, c2name, c2);
		}

		// WIDTH
		for (int col = 0; col < cols; col++) {
			Spring width = Spring.constant(0);
			for (int i = col; i < components.length; i += cols) {
				Component c = components[i];
				SpringLayout.Constraints cons = layout.getConstraints(c);
				width = Spring.max(width, cons.getWidth());
			}
			
			for (int i = col; i < components.length; i += cols) {
				if (i != springCol) {
					Component c = components[i];
					layout.putConstraint(SpringLayout.EAST, c, width, SpringLayout.WEST, c);
				}
			}
		}
		
		// HEIGHT
		for (int startI = 0; startI < components.length; startI += cols) {
			Spring height = Spring.constant(0);
			for (int i = Math.min(startI + cols, components.length); i >= startI; i--) {
				Component c = components[i];
				SpringLayout.Constraints cons = layout.getConstraints(c);
				height = Spring.max(height, cons.getHeight());
			}
			
			for (int i = Math.min(startI + cols, components.length); i >= startI; i--) {
				if (i != springRow) {
					Component c = components[i];
					layout.putConstraint(SpringLayout.NORTH, c, height, SpringLayout.SOUTH, c);
				}
			}
		}
	}

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
		JPanel pnl = new JPanel();
		pnl.setLayout(new SpringLayout());
		pnl.add(uiFolder);
		JButton btnFolder = new JButton("...");
		btnFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser j = new JFileChooser();
				j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				j.setDialogTitle("Избор на изходна директория");
				Integer opt = j.showDialog(uiUrl, "Избери");
				if (opt == JFileChooser.APPROVE_OPTION) {
					File fileToSave = j.getSelectedFile();
					uiFolder.setText(fileToSave.getAbsolutePath());
				}
			}
		});
		pnl.add(btnFolder);
		SpringUtilities.makeCompactGrid(pnl, 1, 2, 0, 0, 1, 0);
		
		panel.add(pnl);
		SpringUtilities.makeCompactGrid(panel, 2, 2, GAP, GAP, GAP, GAP / 2);
		return panel;
	}
	
	void log(final String str) throws InvocationTargetException, InterruptedException {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				uiLog.append(str);
				uiLog.append("\n");
			}
		});
	}
	
	class Task implements Runnable {
		public void run() {
			for (int i = 0; i < 100; i++) {
				try {
					final int I = i;
					log("Processing " + I);
					Thread.sleep(100);
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							uiProgressBar.setValue(I + 1);
						}
					});
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	JComponent createButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.TRAILING, GAP, GAP));
		JButton btn = new JButton("GO");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Task()).start();
			}
		});
		panel.add(btn);
		return panel;
	}

	JComponent createProgressBarAndLog() {
		JPanel panel = new JPanel();
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setLayout(new SpringLayout());

		uiLog = new JTextArea();
//		uiLog.setEditable(false);
		JScrollPane scroll = new JScrollPane (uiLog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(scroll);
//		panel.add(uiLog);
		uiProgressBar = new JProgressBar(0, 100);
		panel.add(uiProgressBar);
		SpringUtilities.makeCompactGrid(panel, 2, 1, 0, 0, 0, 0);
		
		return panel;
	}
	
	JComponent createTopPanel() {
		JPanel panel = new JPanel();
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setLayout(new SpringLayout());
		
		panel.add(createFormFields());
		panel.add(createButtons());
		
//		panel.add(createProgressBarAndLog());
		panel.add(new JLabel("asd"));
		SpringUtilities.makeCompactGrid(panel, 3, 1, 0, 0, 0, 0);

		return panel;
	}
	
	void createAndShowGUI() {
		JFrame frame = new JFrame(
				"Сваляне на чертежи от сайта на sofia-agk.com");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		panel.add(createLeftPanel(), BorderLayout.WEST);
		panel.add(createTopPanel(), BorderLayout.CENTER);
		
		frame.add(panel);
		frame.setPreferredSize(new Dimension(700, 500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new Gui().createAndShowGUI();
	}
}
