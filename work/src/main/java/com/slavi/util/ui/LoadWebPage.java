package com.slavi.util.ui;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class LoadWebPage {

	public static void main(String args[]) {
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(620, 440);
		final JFXPanel fxpanel = new JFXPanel();
		frame.add(fxpanel);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				WebEngine engine;
				WebView wv = new WebView();
				engine = wv.getEngine();
				fxpanel.setScene(new Scene(wv));
				engine.load("http://google.com");
			}
		});
		frame.setVisible(true);
	}
}
