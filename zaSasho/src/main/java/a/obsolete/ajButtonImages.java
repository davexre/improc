package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajButtonImages.java

import java.applet.Applet;
import java.awt.Image;

public class ajButtonImages {

	public ajButtonImages(Applet applet) {
		java.net.URL url = applet.getCodeBase();
		stopOffImg = applet.getImage(url, "Stop_Off.gif");
		stopOnImg = applet.getImage(url, "Stop_On.gif");
		pauseOffImg = applet.getImage(url, "Pause_Off.gif");
		pauseOnImg = applet.getImage(url, "Pause_On.gif");
		anfangOffImg = applet.getImage(url, "Anfang_Off.gif");
		anfangOnImg = applet.getImage(url, "Anfang_On.gif");
		stepBackOffImg = applet.getImage(url, "StepBack_Off.gif");
		stepBackOnImg = applet.getImage(url, "StepBack_On.gif");
		playOffImg = applet.getImage(url, "Play_Off.gif");
		playOnImg = applet.getImage(url, "Play_On.gif");
		recordOffImg = applet.getImage(url, "Record_Off.gif");
		recordOnImg = applet.getImage(url, "Record_On.gif");
		stepOffImg = applet.getImage(url, "Step_Off.gif");
		stepOnImg = applet.getImage(url, "Step_On.gif");
		endeOffImg = applet.getImage(url, "Ende_Off.gif");
		endeOnImg = applet.getImage(url, "Ende_On.gif");
		loadOffImg = applet.getImage(url, "Load_Off.gif");
		loadOnImg = applet.getImage(url, "Load_On.gif");
		saveOffImg = applet.getImage(url, "Save_Off.gif");
		saveOnImg = applet.getImage(url, "Save_On.gif");
	}

	public Image stopOffImg;
	public Image stopOnImg;
	public Image pauseOffImg;
	public Image pauseOnImg;
	public Image anfangOffImg;
	public Image anfangOnImg;
	public Image stepBackOffImg;
	public Image stepBackOnImg;
	public Image playOffImg;
	public Image playOnImg;
	public Image recordOffImg;
	public Image recordOnImg;
	public Image stepOffImg;
	public Image stepOnImg;
	public Image endeOffImg;
	public Image endeOnImg;
	public Image loadOffImg;
	public Image loadOnImg;
	public Image saveOffImg;
	public Image saveOnImg;
}
