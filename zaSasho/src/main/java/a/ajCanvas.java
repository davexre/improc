package a;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajCanvas.java

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Image;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import a.obsolete.ajAnimator;
import a.obsolete.ajElement;
import a.obsolete.ajList;
import a.obsolete.ajSegment;
import a.obsolete.ajUpdateable;

public class ajCanvas extends Canvas {

	public ajCanvas() {
		realized = false;
		animate = new ajAnimator(this);
		useOffscreen = false;
		toFile = true;
		fromFile = false;
		setBackground(ajElement.backgroundColor);
	}

	public ajCanvas(int i) {
		realized = false;
		animate = new ajAnimator(this);
		useOffscreen = false;
		toFile = true;
		fromFile = false;
		drawElements = new ajList[i];
		setBackground(ajElement.backgroundColor);
		veryClean();
	}

	public ajCanvas(int i, int j) {
		this(i);
		animate.sleepTime = j;
	}

	public void createLayers(int i) {
		drawElements = new ajList[i];
		veryClean();
	}

	protected void fixOffscreen() {
		Dimension dimension = size();
		if (offscreen == null || imgWidth != dimension.width || imgHeight != dimension.height) {
			if (realized)
				offscreen = createImage(dimension.width, dimension.height);
			imgWidth = dimension.width;
			imgHeight = dimension.height;
		}
	}

	public void resize(int i, int j) {
		super.resize(i, j);
		width = i;
		height = j;
		if (paintGraphics != null)
			paintGraphics.clipRect(0, 0, width, height);
	}

	public void resize(Dimension dimension) {
		resize(dimension.width, dimension.height);
	}

	public void setOffscreen() {
		useOffscreen = true;
		try {
			fixOffscreen();
		} catch (IllegalArgumentException _ex) {
			unsetOffscreen();
			return;
		}
		if (offscreen != null)
			paintGraphics = offscreen.getGraphics();
		else
			paintGraphics = getGraphics();
		if (paintGraphics != null)
			paintGraphics.clipRect(0, 0, width, height);
	}

	public void unsetOffscreen() {
		useOffscreen = false;
		offscreen = null;
		paintGraphics = getGraphics();
		if (paintGraphics != null)
			paintGraphics.clipRect(0, 0, width, height);
	}

	public void add(ajElement ajelement, int i) {
		if (i >= 0 && i < drawElements.length) {
			drawElements[i].append(ajelement);
			ajelement.priority = i;
			return;
		} else {
			System.out.println("Wrong layer number: " + i);
			return;
		}
	}

	public void add(ajElement ajelement) {
		add(ajelement, 0);
	}

	public void add(ajElement ajelement, int i, Color color) {
		ajelement.color = color;
		add(ajelement, i);
	}

	public void add(ajPoint ajpoint, ajPoint ajpoint1, int i, Color color) {
		add(((ajElement) (new ajSegment(ajpoint, ajpoint1, color))), i);
	}

	public void add(ajPoint ajpoint) {
		drawPoints.append(ajpoint);
	}

	public void remove(ajElement ajelement) {
		remove(ajelement, ajelement.priority);
	}

	public void remove(ajElement ajelement, int i) {
		drawElements[i].remove(ajelement);
		animate.remove(ajelement);
	}

	public void remove(ajPoint ajpoint) {
		drawPoints.remove(ajpoint);
		animate.remove(ajpoint);
	}

	public void cleanLayer(int i) {
		drawElements[i] = new ajList();
	}

	public void cleanTempLayers() {
		for (int i = 1; i < drawElements.length; i++)
			cleanLayer(i);

	}

	public void clean() {
		for (int i = 0; i < drawElements.length; i++)
			cleanLayer(i);

	}

	public void veryClean() {
		clean();
		drawPoints = new ajList();
	}

	public ajElement pick(int i, int j) {
		for (int k = drawElements.length - 1; k >= 0; k--) {
			for (ajElement ajelement = (ajElement) drawElements[k].anchor; ajelement != null; ajelement = (ajElement) ajelement
					.getNext())
				if (ajelement.match(i, j))
					return ajelement;

		}

		return null;
	}

	public ajPoint pickPoint(ajPoint ajpoint) {
		return pickPoint(ajpoint.x, ajpoint.y);
	}

	public ajPoint pickPoint(float f, float f1) {
		ajPoint ajpoint = (ajPoint) drawPoints.anchor;
		ajPoint ajpoint1 = null;
		float f2 = ajPoint.distClose;
		for (; ajpoint != null; ajpoint = (ajPoint) ajpoint.getNext()) {
			float f3 = ajpoint.distance2(f, f1);
			if (f3 <= ajPoint.distClose && (ajpoint1 == null || f3 < f2)) {
				ajpoint1 = ajpoint;
				f2 = f3;
			}
		}

		return ajpoint1;
	}

	public void update(Graphics g) {
		if (updateInterface != null)
			updateInterface.ajUpdate(g);
	}

	public void paint(Graphics g) {
		if (!realized && useOffscreen) {
			realized = true;
			setOffscreen();
		}
		super.paint(paintGraphics);
		for (int i = 0; i < drawElements.length; i++)
			paintLayer(i);

		paintPoints();
		toScreen();
	}

	public void paintLayer(int i) {
		for (ajElement ajelement = (ajElement) drawElements[i].anchor; ajelement != null; ajelement = (ajElement) ajelement
				.getNext())
			if (!ajelement.hidden)
				ajelement.draw(paintGraphics);

	}

	public void paintTempLayers() {
		for (int i = 1; i < drawElements.length; i++)
			paintLayer(i);

		toScreen();
	}

	public void paintPoints() {
		for (ajPoint ajpoint = (ajPoint) drawPoints.anchor; ajpoint != null; ajpoint = (ajPoint) ajpoint.getNext())
			if (!((ajElement) (ajpoint)).hidden)
				ajpoint.draw(paintGraphics);

	}

	public void printPoints() {
		System.out.println("printPoints:");
		for (ajPoint ajpoint = (ajPoint) drawPoints.anchor; ajpoint != null; ajpoint = (ajPoint) ajpoint.getNext())
			System.out.println(" " + ajpoint);

	}

	public void toScreen() {
		if (useOffscreen)
			getGraphics().drawImage(offscreen, 0, 0, this);
	}

	public void changeColorInLayer(int i, Color color) {
		for (ajElement ajelement = (ajElement) drawElements[i].anchor; ajelement != null; ajelement = (ajElement) ajelement
				.getNext())
			ajelement.color = color;

	}

	public void moveCleanLayer(int i, int j) {
		drawElements[j] = drawElements[i];
		cleanLayer(i);
	}

	public boolean keyDown(Event event, int i) {
		if (updateInterface != null)
			return updateInterface.handleKeyDown(event, i);
		else
			return super.keyDown(event, i);
	}

	public boolean mouseDown(Event event, int i, int j) {
		if (updateInterface != null)
			return updateInterface.handleMouseDown(event, i, j);
		else
			return super.mouseDown(event, i, j);
	}

	public boolean mouseDrag(Event event, int i, int j) {
		if (updateInterface != null)
			return updateInterface.handleMouseDrag(event, i, j);
		else
			return super.mouseDrag(event, i, j);
	}

	public boolean mouseUp(Event event, int i, int j) {
		if (updateInterface != null)
			return updateInterface.handleMouseUp(event, i, j);
		else
			return super.mouseUp(event, i, j);
	}

	public void start_animation() {
		animate.start();
	}

	public void stop_animation() {
		animate.stop();
	}

	public synchronized void reshape(int i, int j, int k, int l) {
		super.reshape(i, j, k, l);
		width = k;
		height = l;
		if (useOffscreen) {
			offscreen = null;
			setOffscreen();
		} else {
			unsetOffscreen();
		}
		if (paintGraphics != null)
			paintGraphics.clipRect(0, 0, width, height);
	}

	public Dimension minimumSize() {
		return new Dimension(400, 200);
	}

	public Dimension preferredSize() {
		return new Dimension(width, height);
	}

	private boolean realized;
	public static final int PR_LOW = 0;
	public static final int PR_MED = 1;
	public static final int PR_HIGH = 2;
	public ajList drawElements[];
	
	public ajList drawPoints;	// List of points to form triangles!!!!!
	
	protected Image offscreen;
	protected int imgWidth;
	protected int imgHeight;
	public ajAnimator animate;
	public boolean useOffscreen;
	private Graphics paintGraphics;
	public int width;
	public int height;
	DataInputStream din;
	DataOutputStream dos;
	boolean toFile;
	boolean fromFile;
	public ajUpdateable updateInterface;
}
