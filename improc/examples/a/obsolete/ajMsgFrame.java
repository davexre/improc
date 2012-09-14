package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajMsgFrame.java

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;


public class ajMsgFrame extends Frame {

	public ajMsgFrame(String s, String s1) {
		this(s, s1, 1);
	}

	public ajMsgFrame(String s, String s1, int i) {
		super(s);
		setLayout(new BorderLayout(15, 15));
		label = new MultiLineLabel(s1, i);
		add("Center", label);
		label.setFont(new Font("Helvetica", 1, 14));
		button = new Button("OK");
		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(1, 15, 15));
		panel.add(button);
		add("South", panel);
		pack();
	}

	public boolean action(Event event, Object obj) {
		if (event.target == button) {
			hide();
			dispose();
			return true;
		} else {
			return false;
		}
	}

	public boolean gotFocus(Event event, Object obj) {
		button.requestFocus();
		return true;
	}

	Button button;
	MultiLineLabel label;
}
