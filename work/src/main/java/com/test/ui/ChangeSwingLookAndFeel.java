package com.test.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class ChangeSwingLookAndFeel {
	public static void main(String[] args) throws Exception {
		UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
		for (UIManager.LookAndFeelInfo look : looks) {
			final CountDownLatch latch = new CountDownLatch(1);
			System.out.println("Setting look and feel to " + look.getClassName());
			UIManager.setLookAndFeel(look.getClassName());
			JFrame aWindow = new JFrame("This is the Window Title");
			aWindow.setBounds(50, 100, 300, 300);
			aWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			aWindow.setVisible(true);
			JButton button = new JButton("Push me!");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (latch.getCount() > 0)
						latch.countDown();
				}
			});
			aWindow.add(button);
			button.setSize(200, 100);
			latch.await();
			aWindow.setVisible(false);
			aWindow.dispose();
		}
		System.out.println("DONE");
	}
}
