package manualTest;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SpringLayout.Constraints;

import com.slavi.util.swing.SwingUtil;


public class SwingUtilMakeSpringGrid {

	public static <C extends JComponent> C addBorder(C c) {
		c.setBorder(BorderFactory.createLineBorder(Color.black));
		return c;
	}
	
	JLabel makeLabel(String str) {
		return addBorder(new JLabel(str));
	}
	
	static int GAP = 5;

	JPanel makePanelOneRow(String suffix) {
		final JPanel panel = SwingUtil.addBorder(new JPanel());
		panel.setLayout(new SpringLayout());
	
		panel.add(makeLabel("aaaaa" + suffix));
		panel.add(makeLabel("b" + suffix));
		panel.add(makeLabel("c" + suffix));
		panel.add(makeLabel("d" + suffix));
		panel.add(makeLabel("e" + suffix));
		panel.add(makeLabel("f" + suffix));
		JButton btn = new JButton("btn");
		panel.add(btn);
		panel.add(makeLabel("h" + suffix));
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SpringLayout layout = (SpringLayout) panel.getLayout();
				Constraints cons = layout.getConstraints(panel);
				System.out.println(cons.getConstraint(SpringLayout.WEST).getValue());
				System.out.println(cons.getConstraint(SpringLayout.EAST).getValue());
				System.out.println(panel.getInsets().left);
				System.out.println(panel.getInsets().right);

			}
		});
		
		SwingUtil.makeSpringGrid(panel, 5, 1, 1, GAP, GAP, GAP, GAP / 2);
//		SwingUtil.makeCompactGrid(panel, 2, 3, GAP, GAP, GAP, GAP / 2);
		return panel;
	}

	JPanel makePanelComposite() {
		JPanel panel = SwingUtil.addBorder(new JPanel());
		panel.setLayout(new SpringLayout());

		panel.add(makePanelOneRow("1"));
		panel.add(makePanelOneRow("2"));
		panel.add(makePanelOneRow("3"));
		panel.add(makePanelOneRow("4"));
		panel.add(makePanelOneRow("5"));

		SwingUtil.makeSpringGrid(panel, 1, -200, 0, GAP, GAP, GAP, GAP / 2);
//		SwingUtil.makeCompactGrid(panel, 5, 1, GAP, GAP, GAP, GAP / 2);
		return panel;
	}
	
	JPanel makePane1() {
		JPanel panel = new JPanel();
		panel.setLayout(new SpringLayout());
	
		panel.add(makeLabel("a1"));
		panel.add(makeLabel("b1"));
//		Component scrollMe = makeLabel("c1");
		Component scrollMe = makePane2();
		panel.add(new JScrollPane(scrollMe, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		panel.add(makeLabel("d1"));
		panel.add(makeLabel("e1"));
		
		SwingUtil.makeSpringGrid(panel, 1, 2, 0, GAP, GAP, GAP, GAP / 2);
		return panel;
	}
	
	JPanel makePane2() {
		JPanel panel = new JPanel();
		panel.setLayout(new SpringLayout());
	
		panel.add(makeLabel("a1"));
		panel.add(makeLabel("b1"));
		panel.add(makeLabel("c1"));
		panel.add(makeLabel("d1"));
		panel.add(makeLabel("e1"));
		
		panel.add(makeLabel("a2"));
		panel.add(makeLabel("b22222222222222"));
		panel.add(makeLabel("c2"));
		panel.add(makeLabel("d2"));
		panel.add(makeLabel("e2"));
		
		panel.add(makeLabel("a3"));
		panel.add(makeLabel("b3"));
		panel.add(makeLabel("c3"));
		panel.add(addBorder(new JTextArea("d3")));
		panel.add(new JButton("e3"));
		
		panel.add(makeLabel("a4"));
		panel.add(makeLabel("b4"));
		panel.add(makeLabel("c4"));
		panel.add(new JProgressBar(0, 100));
		panel.add(makeLabel("e4"));
		
		panel.add(makeLabel("a5"));
		panel.add(makeLabel("b5"));
		panel.add(makeLabel("c5"));
//		panel.add(makeLabel("d5"));
//		panel.add(makeLabel("e5"));

		SwingUtil.makeSpringGrid(panel, 5, 2, 3, GAP, GAP, GAP, GAP / 2);
		return panel;
	}
	
	void test1() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.add(makePanelComposite());
//		frame.add(makePanelOneRow("1"));
		frame.setPreferredSize(new Dimension(700, 500));
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		new SwingUtilMakeSpringGrid().test1();
	}
}
