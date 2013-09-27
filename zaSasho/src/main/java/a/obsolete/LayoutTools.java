package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LayoutTools.java

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LayoutTools {

	public static void gridBagConstrain(Container container, Component component, int i, int j, int k, int l, int i1,
			int j1, double d, double d1, int k1, int l1, int i2, int j2) {
		GridBagConstraints gridbagconstraints = new GridBagConstraints();
		gridbagconstraints.gridx = i;
		gridbagconstraints.gridy = j;
		gridbagconstraints.gridwidth = k;
		gridbagconstraints.gridheight = l;
		gridbagconstraints.fill = i1;
		gridbagconstraints.anchor = j1;
		gridbagconstraints.weightx = d;
		gridbagconstraints.weighty = d1;
		if (k1 + i2 + l1 + j2 > 0)
			gridbagconstraints.insets = new Insets(k1, l1, i2, j2);
		((GridBagLayout) container.getLayout()).setConstraints(component, gridbagconstraints);
		container.add(component);
	}

	public LayoutTools() {
	}
}
