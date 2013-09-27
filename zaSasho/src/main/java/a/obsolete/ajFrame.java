package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajFrame.java

import java.awt.CheckboxMenuItem;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.net.MalformedURLException;
import java.net.URL;

import a.VoronoiApplet;

public class ajFrame extends Frame {

	public ajFrame(VoronoiApplet voronoiapplet, String s, int i, int j) {
		super(s);
		va = voronoiapplet;
		resize(i, j);
		MenuBar menubar = new MenuBar();
		fm = new Menu("File");
		sm = new Menu("Show");
		em = new Menu("Edit");
		om = new Menu("Options");
		hm = new Menu("Help");
		close = new MenuItem("Close");
		fm.add(close);
		checkBoxVoronoi = new CheckboxMenuItem("Voronoi");
		checkBoxDelaunay = new CheckboxMenuItem("Delaunay");
		checkBoxConvexHull = new CheckboxMenuItem("Convex Hull");
		sm.add(checkBoxVoronoi);
		sm.add(checkBoxDelaunay);
		sm.add(checkBoxConvexHull);
		clr = new MenuItem("Clear");
		demo = new CheckboxMenuItem("Run Demo");
		em.add(clr);
		em.addSeparator();
		em.add(demo);
		unBuf = new CheckboxMenuItem("Unbuffered Display");
		thin = new CheckboxMenuItem("Thin Lines");
		Menu menu = new Menu("Delaunay");
		step = new CheckboxMenuItem("Step Mode");
		menu.add(step);
		om.add(unBuf);
		om.add(thin);
		om.addSeparator();
		om.add(menu);
		manual = new MenuItem("Manual");
		quick = new MenuItem("Quick Help");
		about = new MenuItem("About...");
		hm.add(about);
		hm.addSeparator();
		hm.add(quick);
		hm.add(manual);
		menubar.add(fm);
		menubar.add(sm);
		menubar.add(em);
		menubar.add(om);
		menubar.add(hm);
		menubar.setHelpMenu(hm);
		setMenuBar(menubar);
	}

	public boolean keyDown(Event event, int i) {
		return va.keyDown(event, i);
	}

	public boolean action(Event event, Object obj) {
		if (event.id == 1001) {
			if (event.target == close) {
				va.recFrame.hide();
				va.stepFrame.hide();
				hide();
				return true;
			}
			if (event.target == checkBoxVoronoi || event.target == checkBoxDelaunay || event.target == checkBoxConvexHull) {
				va.setViewtype(checkBoxVoronoi.getState(), checkBoxDelaunay.getState(), checkBoxConvexHull.getState());
				return true;
			}
			if (event.target == clr) {
				va.clear_canvas();
				return true;
			}
			if (event.target == demo) {
				if (demo.getState()) {
					if (va.recPanel.load.isEnabled()) {
						va.evRec.save_op = false;
						va.evRec.selected.setLength(0);
						va.evRec.selected.append("demo.ani");
						va.evRec.load_save();
						if (va.evRec.hasRecord()) {
							va.do_replay();
						} else {
							demo.setState(false);
							demo.disable();
						}
					}
				} else {
					va.do_stop();
				}
				return true;
			}
			if (event.target == unBuf) {
				va.unBufferedDisplay(unBuf.getState());
				return true;
			}
			if (event.target == thin) {
				va.thinLines(thin.getState());
				return true;
			}
			if (event.target == step) {
				va.stepMode = step.getState();
				va.evRec.recordEvent(10, va.stepMode);
				return true;
			}
			if (event.target == about) {
				ajMsgFrame ajmsgframe = new ajMsgFrame(
						"About VoronoiApplet",
						"VoroGlide\nVersion 2.2, May 2001\n \nWritten in Java at\nFernUniversit\344t Hagen, Praktische Informatik VI\n \nby\n \nChristian Icking, Rolf Klein, Peter K\366llner, Lihong Ma\n \nemail: javagroup@fernuni-hagen.de");
				ajmsgframe.show();
				return true;
			}
			if (event.target == quick) {
				ajMsgFrame ajmsgframe1 = new ajMsgFrame(
						"VoroGlide 2.2 Quick Help",
						"VoroGlide 2.2 Quick Help\n \nPush left mouse button to set point.\nPush right mouse button (or <Alt> mouse) to remove point.\nDrag mouse with left button pressed to move point.\n \nMenus\n \nFile: Close the applet frame.\nShow: Select a combination of diagrams to be shown.\nEdit: Clear canvas, Run demo animation.\nOptions:\n -Unbuffered (somewhat jagged, sometimes faster) vs. buffered display (smoother, default)\n -Thin (faster) vs. thick lines (nicer for presentations, default)\n -Delaunay: Step Mode: Display animation of Edge Flip Algorithm on point insertion.",
						0);
				ajmsgframe1.show();
				return true;
			}
			if (event.target == manual) {
				URL url;
				try {
					url = new URL(va.getCodeBase(), va.getHelpURL());
				} catch (MalformedURLException _ex) {
					url = null;
				}
				if (url != null)
					va.getAppletContext().showDocument(url, "VoroGlide User Manual");
				return true;
			}
		}
		return va.action(event, obj);
	}

	VoronoiApplet va;
	Menu fm;
	Menu sm;
	Menu em;
	Menu om;
	Menu hm;
	MenuItem close;
	public CheckboxMenuItem checkBoxVoronoi;
	public CheckboxMenuItem checkBoxDelaunay;
	public CheckboxMenuItem checkBoxConvexHull;
	public CheckboxMenuItem unBuf;
	public CheckboxMenuItem thin;
	public MenuItem clr;
	public CheckboxMenuItem demo;
	public CheckboxMenuItem step;
	MenuItem manual;
	MenuItem quick;
	MenuItem about;
}
