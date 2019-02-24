package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ajButtonImages.java

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ajButtonImages {

	public ajButtonImages() throws IOException {
		stopOffImg = ImageIO.read(getClass().getResourceAsStream("Stop_Off.gif"));
		stopOnImg = ImageIO.read(getClass().getResourceAsStream("Stop_On.gif"));
		pauseOffImg = ImageIO.read(getClass().getResourceAsStream("Pause_Off.gif"));
		pauseOnImg =  ImageIO.read(getClass().getResourceAsStream("Pause_On.gif"));
		anfangOffImg = ImageIO.read(getClass().getResourceAsStream("Anfang_Off.gif"));
		anfangOnImg = ImageIO.read(getClass().getResourceAsStream("Anfang_On.gif"));
		stepBackOffImg = ImageIO.read(getClass().getResourceAsStream("StepBack_Off.gif"));
		stepBackOnImg = ImageIO.read(getClass().getResourceAsStream("StepBack_On.gif"));
		playOffImg = ImageIO.read(getClass().getResourceAsStream("Play_Off.gif"));
		playOnImg = ImageIO.read(getClass().getResourceAsStream("Play_On.gif"));
		recordOffImg = ImageIO.read(getClass().getResourceAsStream("Record_Off.gif"));
		recordOnImg = ImageIO.read(getClass().getResourceAsStream("Record_On.gif"));
		stepOffImg = ImageIO.read(getClass().getResourceAsStream("Step_Off.gif"));
		stepOnImg = ImageIO.read(getClass().getResourceAsStream("Step_On.gif"));
		endeOffImg = ImageIO.read(getClass().getResourceAsStream("Ende_Off.gif"));
		endeOnImg = ImageIO.read(getClass().getResourceAsStream("Ende_On.gif"));
		loadOffImg = ImageIO.read(getClass().getResourceAsStream("Load_Off.gif"));
		loadOnImg = ImageIO.read(getClass().getResourceAsStream("Load_On.gif"));
		saveOffImg = ImageIO.read(getClass().getResourceAsStream("Save_Off.gif"));
		saveOnImg = ImageIO.read(getClass().getResourceAsStream("Save_On.gif"));
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
