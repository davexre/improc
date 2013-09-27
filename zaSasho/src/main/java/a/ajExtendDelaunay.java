package a;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajExtendDelaunay.java

import java.awt.Color;
import java.awt.Graphics;

import a.obsolete.ajElement;
import a.obsolete.ajLine;
import a.obsolete.ajSegment;
import a.obsolete.ajStep;

public class ajExtendDelaunay extends ajDelaunay {

	public ajExtendDelaunay(ajCanvas ajcanvas) {
		super(ajcanvas);
		l = 1;
		oldmoveHullLayer = l++;
		oldmoveVorLayer = l++;
		oldmoveDelLayer = l++;
		oldrefreshLayer = l++;
		oldhideVorLayer = l++;
		oldhideDelLayer = l++;
		hideDelLayer = l++;
		hideVorLayer = l++;
		refreshLayer = l++;
		moveDelLayer = l++;
		moveVorLayer = l++;
		moveHullLayer = l++;
		oldrefreshPointLayer = l++;
		refreshPointLayer = l++;
		layers = l;
		moveDelColor = ajElement.darker(Color.gray);
		hideDelColor = ajElement.brighter(Color.lightGray);
		moveVorColor = ajElement.muchBrighter(super.VorColor);
		hideVorColor = ajElement.brighter(ajElement.veryMuchBrighter(moveVorColor));
		moveHullColor = Color.green;
		stepStarColor = ajElement.shiningColor;
		stepFlipColor = Color.magenta;
		stepCircleColor = ajElement.muchDarker(Color.green);
		actualHideDelColor = ajElement.backgroundColor;
		actualHideVorColor = ajElement.backgroundColor;
		moving = false;
		super.canvas.createLayers(layers);
	}

	public void showHidden(boolean flag) {
		if (flag) {
			actualHideDelColor = hideDelColor;
			actualHideVorColor = hideVorColor;
			return;
		} else {
			actualHideDelColor = ajElement.backgroundColor;
			actualHideVorColor = ajElement.backgroundColor;
			return;
		}
	}

	public void endMotion() {
		moving = false;
	}

	public void insertTemp(ajPoint ajpoint) {
		moving = true;
		super.canvas.moveCleanLayer(moveDelLayer, oldmoveDelLayer);
		super.canvas.moveCleanLayer(moveVorLayer, oldmoveVorLayer);
		super.canvas.moveCleanLayer(moveHullLayer, oldmoveHullLayer);
		super.canvas.moveCleanLayer(hideDelLayer, oldhideDelLayer);
		super.canvas.moveCleanLayer(hideVorLayer, oldhideVorLayer);
		super.canvas.changeColorInLayer(oldmoveDelLayer, ajElement.backgroundColor);
		super.canvas.changeColorInLayer(oldmoveVorLayer, ajElement.backgroundColor);
		super.canvas.changeColorInLayer(oldmoveHullLayer, ajElement.backgroundColor);
		super.canvas.changeColorInLayer(oldhideDelLayer, super.color);
		super.canvas.changeColorInLayer(oldhideVorLayer, super.VorColor);
		super.canvas.moveCleanLayer(refreshLayer, oldrefreshLayer);
		super.canvas.moveCleanLayer(refreshPointLayer, oldrefreshPointLayer);
		ajPoint ajpoint1 = new ajPoint(ajpoint, ajElement.shiningColor);
		super.canvas.add(ajpoint1, moveHullLayer);
		ajTriangle ajtriangle = insertTempSimple(ajpoint1);
		if (ajtriangle == null)
			return;
		if (super.showHull)
			if (ajtriangle.bc.halfplane) {
				ajTriangle ajtriangle2 = ajtriangle.bc;
				super.canvas.add(ajtriangle2.a, ajtriangle2.b, moveHullLayer, moveHullColor);
				ajtriangle2 = ajtriangle2.ca;
				super.canvas.add(ajtriangle2.a, ajtriangle2.b, moveHullLayer, moveHullColor);
				super.canvas.add(new ajPoint(ajtriangle2.a), refreshPointLayer);
				ajTriangle ajtriangle6 = ajtriangle.bc.bc;
				do {
					ajtriangle2 = ajtriangle2.ca;
					super.canvas.add(ajtriangle2.a, ajtriangle2.b, moveHullLayer, super.HullColor);
					super.canvas.add(new ajPoint(ajtriangle2.a), refreshPointLayer);
				} while (ajtriangle2 != ajtriangle6);
			} else {
				ajTriangle ajtriangle3 = super.startTriangleHull;
				do {
					super.canvas.add(ajtriangle3.a, ajtriangle3.b, moveHullLayer, super.HullColor);
					super.canvas.add(new ajPoint(ajtriangle3.a), refreshPointLayer);
					ajtriangle3 = ajtriangle3.ca;
				} while (ajtriangle3 != super.startTriangleHull);
			}
		ajTriangle ajtriangle4 = ajtriangle;
		do {
			flipTemp(ajtriangle4, ajtriangle4.ab.neighbor(ajtriangle4.a));
			ajtriangle4 = ajtriangle4.ca;
		} while (ajtriangle4 != ajtriangle && !ajtriangle4.halfplane);
		if (super.showVor) {
			ajTriangle ajtriangle1 = ajtriangle4;
			ajTriangle ajtriangle5;
			if (ajtriangle1.halfplane)
				ajtriangle5 = ajtriangle1.ab;
			else
				ajtriangle5 = ajtriangle1.bc;
			do {
				super.canvas.add(ajtriangle4.dualEdge(ajtriangle5), moveVorLayer, moveVorColor);
				if (!ajtriangle4.halfplane)
					super.canvas.add(ajtriangle4.dualEdge(ajtriangle4.ab), moveVorLayer, moveVorColor);
				ajtriangle4 = ajtriangle5;
				ajtriangle5 = ajtriangle4.bc;
			} while (ajtriangle4 != ajtriangle1 && !ajtriangle4.halfplane);
		}
	}

	public void insertTemp(ajPoint ajpoint, ajStep ajstep) {
		if (super.allCollinear)
			return;
		stepMode = true;
		stepList = ajstep;
		oldShowVor = super.showVor;
		oldShowDel = super.showDel;
		super.showVor = false;
		super.showDel = false;
		ajpoint.color = ajElement.shiningColor;
		ajTriangle ajtriangle = insertTempSimple(ajpoint);
		ajTriangle ajtriangle1 = ajtriangle;
		stepList.add(ajtriangle1.a, refreshPointLayer, ajElement.pointColor);
		do {
			stepList.add(ajtriangle1.a, ajtriangle1.b, moveDelLayer, stepStarColor);
			stepList.add(ajtriangle1.b, refreshPointLayer, ajElement.pointColor);
			ajtriangle1 = ajtriangle1.ca;
		} while (ajtriangle1 != ajtriangle && !ajtriangle1.halfplane);
		stepList.pause();
		ajtriangle1 = ajtriangle;
		stepList.add(ajtriangle1.b, ajpoint, moveDelLayer, moveDelColor);
		do {
			stepList.add(ajtriangle1.a, ajpoint, moveDelLayer, moveDelColor);
			ajtriangle1 = ajtriangle1.ca;
		} while (ajtriangle1 != ajtriangle && !ajtriangle1.halfplane);
		stepList.pause();
		ajtriangle1 = ajtriangle;
		do {
			flipTemp(ajtriangle1, ajtriangle1.ab.neighbor(ajtriangle1.a));
			ajtriangle1 = ajtriangle1.ca;
		} while (ajtriangle1 != ajtriangle && !ajtriangle1.halfplane);
		stepList.pause(2);
		stepList.add(ajpoint, refreshPointLayer, ajElement.pointColor);
		ajpoint.color = ajElement.pointColor;
		stepMode = false;
		super.showVor = oldShowVor;
		super.showDel = oldShowDel;
	}

	private ajTriangle insertTempSimple(ajPoint ajpoint) {
		if (!super.allCollinear) {
			root = ajTriangle.findTriangleNEW(root, ajpoint);
//			super.root = super.root.findTriangle(ajpoint);
			if (super.root.halfplane)
				return extendOutsideTemp(super.root, ajpoint);
			else
				return extendInsideTemp(super.root, ajpoint);
		}
		if (stepMode)
			return null;
		if (super.nPoints == 0)
			return null;
		if (super.nPoints == 1) {
			super.canvas.add(new ajPoint(super.firstP), refreshPointLayer);
			if (super.showDel)
				super.canvas.add(super.firstP, ajpoint, moveDelLayer, moveDelColor);
			if (super.showVor)
				super.canvas.add(ajpoint.Bisector(super.firstP), moveVorLayer, moveVorColor);
			return null;
		}
		if (super.showHull) {
			super.canvas.add(new ajPoint(super.firstP), refreshPointLayer);
			super.canvas.add(new ajPoint(super.lastP), refreshPointLayer);
			super.canvas.add(super.firstP, ajpoint, moveHullLayer, moveHullColor);
			super.canvas.add(ajpoint, super.lastP, moveHullLayer, moveHullColor);
			super.canvas.add(super.lastP, super.firstP, moveHullLayer, super.HullColor);
		}
		switch (ajpoint.pointToLine(super.firstP, super.lastP)) {
		case 1: // '\001'
			firstNonCollinearTemp(extendOutsideTemp(super.firstT.ab, ajpoint));
			break;

		case 2: // '\002'
			firstNonCollinearTemp(extendOutsideTemp(super.firstT, ajpoint));
			break;

		case 0: // '\0'
			insertCollinearTemp(ajpoint, 0);
			break;

		case 3: // '\003'
			insertCollinearTemp(ajpoint, 3);
			break;

		case 4: // '\004'
			insertCollinearTemp(ajpoint, 4);
			break;
		}
		return null;
	}

	private void firstNonCollinearTemp(ajTriangle ajtriangle) {
		if (!super.showVor)
			return;
		ajTriangle ajtriangle1 = ajtriangle;
		super.canvas.add(ajtriangle1.dualEdge(ajtriangle1.bc), moveVorLayer, moveVorColor);
		for (; !ajtriangle1.halfplane; ajtriangle1 = ajtriangle1.ca) {
			super.canvas.add(ajtriangle1.dualEdge(ajtriangle1.ab), moveVorLayer, moveVorColor);
			super.canvas.add(ajtriangle1.dualEdge(ajtriangle1.ca), moveVorLayer, moveVorColor);
		}

	}

	private void insertCollinearTemp(ajPoint ajpoint, int i) {
		ajTriangle ajtriangle = super.firstT;
		switch (i) {
		case 3: // '\003'
			if (super.showDel)
				super.canvas.add(super.firstP, ajpoint, moveDelLayer, moveDelColor);
			if (super.showVor)
				super.canvas.add(super.firstP.Bisector(ajpoint), moveVorLayer, moveVorColor);
			break;

		case 4: // '\004'
			if (super.showDel)
				super.canvas.add(super.lastP, ajpoint, moveDelLayer, moveDelColor);
			if (super.showVor)
				super.canvas.add(super.lastP.Bisector(ajpoint), moveVorLayer, moveVorColor);
			break;

		case 0: // '\0'
			for (; ajpoint.isGreater(ajtriangle.a); ajtriangle = ajtriangle.ca) {
				super.canvas.add(new ajPoint(ajtriangle.b), refreshPointLayer);
				if (super.showVor)
					super.canvas.add(ajtriangle.a.Bisector(ajtriangle.b), moveVorLayer, super.VorColor);
				if (super.showDel)
					super.canvas.add(ajtriangle.a, ajtriangle.b, moveDelLayer, super.color);
			}

			super.canvas.add(new ajPoint(ajtriangle.b), refreshPointLayer);
			if (super.showVor) {
				super.canvas.add(ajtriangle.a.Bisector(ajpoint), moveVorLayer, moveVorColor);
				super.canvas.add(ajtriangle.b.Bisector(ajpoint), moveVorLayer, moveVorColor);
			}
			if (super.showDel)
				super.canvas.add(ajtriangle.a, ajtriangle.b, moveDelLayer, moveDelColor);
			ajtriangle = ajtriangle.ca;
			break;
		}
		for (; ajtriangle.b != super.lastP; ajtriangle = ajtriangle.ca) {
			super.canvas.add(new ajPoint(ajtriangle.b), refreshPointLayer);
			if (super.showVor)
				super.canvas.add(ajtriangle.a.Bisector(ajtriangle.b), moveVorLayer, super.VorColor);
			if (super.showDel)
				super.canvas.add(ajtriangle.a, ajtriangle.b, moveDelLayer, super.color);
		}

		super.canvas.add(new ajPoint(super.lastP), refreshPointLayer);
	}

	private ajTriangle startTriangulationTempA(ajPoint ajpoint, ajPoint ajpoint1, ajPoint ajpoint2) {
		ajTriangle ajtriangle = null;
		int i = ajpoint2.pointToLine(ajpoint, ajpoint1);
		if (i == 1 || i == 4)
			ajtriangle = new ajTriangle(ajpoint, ajpoint1, ajpoint2);
		else if (i == 2 || i == 0)
			ajtriangle = new ajTriangle(ajpoint, ajpoint2, ajpoint1);
		else if (i == 3)
			ajtriangle = new ajTriangle(ajpoint2, ajpoint, ajpoint1);
		ajTriangle ajtriangle1 = new ajTriangle(ajtriangle.b, ajtriangle.a);
		ajTriangle ajtriangle2 = new ajTriangle(ajtriangle.c, ajtriangle.b);
		ajTriangle ajtriangle3 = new ajTriangle(ajtriangle.a, ajtriangle.c);
		ajtriangle.ab = ajtriangle1;
		ajtriangle1.ab = ajtriangle;
		ajtriangle.bc = ajtriangle2;
		ajtriangle2.ab = ajtriangle;
		ajtriangle.ca = ajtriangle3;
		ajtriangle3.ab = ajtriangle;
		ajtriangle1.ca = ajtriangle2;
		ajtriangle2.bc = ajtriangle1;
		ajtriangle2.ca = ajtriangle3;
		ajtriangle3.bc = ajtriangle2;
		ajtriangle3.ca = ajtriangle1;
		ajtriangle1.bc = ajtriangle3;
		return ajtriangle;
	}

	private ajTriangle extendInsideTemp(ajTriangle ajtriangle, ajPoint ajpoint) {
		ajTriangle ajtriangle1 = treatDegeneracyInsideTemp(ajtriangle, ajpoint);
		if (ajtriangle1 != null)
			return ajtriangle1;
		ajtriangle1 = new ajTriangle(ajtriangle.a, ajtriangle.b, ajpoint);
		ajTriangle ajtriangle2 = new ajTriangle(ajtriangle.b, ajtriangle.c, ajpoint);
		ajTriangle ajtriangle3 = new ajTriangle(ajtriangle.c, ajtriangle.a, ajpoint);
		ajtriangle1.ab = ajtriangle.ab;
		ajtriangle2.ab = ajtriangle.bc;
		ajtriangle3.ab = ajtriangle.ca;
		ajtriangle1.bc = ajtriangle2;
		ajtriangle2.ca = ajtriangle1;
		ajtriangle2.bc = ajtriangle3;
		ajtriangle3.ca = ajtriangle2;
		ajtriangle3.bc = ajtriangle1;
		ajtriangle1.ca = ajtriangle3;
		if (super.showDel) {
			super.canvas.add(ajtriangle.a, ajpoint, moveDelLayer, moveDelColor);
			super.canvas.add(ajtriangle.b, ajpoint, moveDelLayer, moveDelColor);
			super.canvas.add(ajtriangle.c, ajpoint, moveDelLayer, moveDelColor);
		}
		super.canvas.add(new ajPoint(ajtriangle.a), refreshPointLayer);
		super.canvas.add(new ajPoint(ajtriangle.b), refreshPointLayer);
		super.canvas.add(new ajPoint(ajtriangle.c), refreshPointLayer);
		return ajtriangle1;
	}

	private ajTriangle treatDegeneracyInsideTemp(ajTriangle ajtriangle, ajPoint ajpoint) {
		if (ajtriangle.ab.halfplane && ajpoint.pointToLine(ajtriangle.b, ajtriangle.a) == 0)
			return extendOutsideTemp(ajtriangle.ab, ajpoint);
		if (ajtriangle.bc.halfplane && ajpoint.pointToLine(ajtriangle.c, ajtriangle.b) == 0)
			return extendOutsideTemp(ajtriangle.bc, ajpoint);
		if (ajtriangle.ca.halfplane && ajpoint.pointToLine(ajtriangle.a, ajtriangle.c) == 0)
			return extendOutsideTemp(ajtriangle.ca, ajpoint);
		else
			return null;
	}

	private ajTriangle extendOutsideTemp(ajTriangle ajtriangle, ajPoint ajpoint) {
		if (ajpoint.pointToLine(ajtriangle.a, ajtriangle.b) == 0) {
			ajTriangle ajtriangle1 = new ajTriangle(ajtriangle.a, ajtriangle.b, ajpoint);
			ajTriangle ajtriangle3 = new ajTriangle(ajtriangle.a, ajpoint);
			ajTriangle ajtriangle6 = new ajTriangle(ajpoint, ajtriangle.b);
			ajtriangle1.ab = ajtriangle.ab;
			ajtriangle1.bc = ajtriangle6;
			ajtriangle6.ab = ajtriangle1;
			ajtriangle1.ca = ajtriangle3;
			ajtriangle3.ab = ajtriangle1;
			ajtriangle6.bc = ajtriangle.bc;
			ajtriangle6.ca = ajtriangle3;
			ajtriangle3.bc = ajtriangle6;
			ajtriangle3.ca = ajtriangle.ca;
			if (super.showDel) {
				super.canvas.add(ajtriangle.a, ajpoint, moveDelLayer, moveDelColor);
				super.canvas.add(ajtriangle.b, ajpoint, moveDelLayer, moveDelColor);
			}
			super.canvas.add(new ajPoint(ajtriangle.a), refreshPointLayer);
			super.canvas.add(new ajPoint(ajtriangle.b), refreshPointLayer);
			return ajtriangle1;
		}
		ajTriangle ajtriangle2 = new ajTriangle(ajtriangle.a, ajtriangle.b, ajpoint);
		ajtriangle2.ab = ajtriangle.ab;
		if (super.showDel) {
			super.canvas.add(ajtriangle.a, ajpoint, moveDelLayer, moveDelColor);
			super.canvas.add(ajtriangle.b, ajpoint, moveDelLayer, moveDelColor);
			if (super.allCollinear)
				super.canvas.add(ajtriangle.a, ajtriangle.b, refreshLayer, super.color);
		}
		super.canvas.add(new ajPoint(ajtriangle.a), refreshPointLayer);
		super.canvas.add(new ajPoint(ajtriangle.b), refreshPointLayer);
		ajTriangle ajtriangle7 = ajtriangle.ca;
		ajTriangle ajtriangle9 = ajtriangle2;
		for (; ajpoint.pointToLine(ajtriangle7.a, ajtriangle7.b) < 2; ajtriangle7 = ajtriangle7.ca) {
			if (super.showDel) {
				super.canvas.add(ajtriangle7.a, ajpoint, moveDelLayer, moveDelColor);
				if (super.allCollinear)
					super.canvas.add(ajtriangle7.a, ajtriangle7.b, refreshLayer, super.color);
			}
			super.canvas.add(new ajPoint(ajtriangle7.a), refreshPointLayer);
			ajTriangle ajtriangle4 = new ajTriangle(ajtriangle7.a, ajtriangle7.b, ajpoint);
			ajtriangle4.ab = ajtriangle7.ab;
			ajtriangle4.bc = ajtriangle9;
			ajtriangle9.ca = ajtriangle4;
			ajtriangle9 = ajtriangle4;
		}

		ajTriangle ajtriangle5 = new ajTriangle(ajtriangle9.a, ajpoint);
		ajtriangle5.ab = ajtriangle9;
		ajtriangle9.ca = ajtriangle5;
		ajtriangle5.ca = ajtriangle7;
		ajtriangle9 = ajtriangle5;
		ajTriangle ajtriangle8 = ajtriangle.bc;
		ajTriangle ajtriangle10 = ajtriangle2;
		for (; ajpoint.pointToLine(ajtriangle8.a, ajtriangle8.b) < 2; ajtriangle8 = ajtriangle8.bc) {
			if (super.showDel) {
				super.canvas.add(ajtriangle8.b, ajpoint, moveDelLayer, moveDelColor);
				if (super.allCollinear)
					super.canvas.add(ajtriangle8.a, ajtriangle8.b, refreshLayer, super.color);
			}
			super.canvas.add(new ajPoint(ajtriangle8.b), refreshPointLayer);
			ajtriangle5 = new ajTriangle(ajtriangle8.a, ajtriangle8.b, ajpoint);
			ajtriangle5.ab = ajtriangle8.ab;
			ajtriangle5.ca = ajtriangle10;
			ajtriangle10.bc = ajtriangle5;
			ajtriangle10 = ajtriangle5;
		}

		ajtriangle5 = new ajTriangle(ajpoint, ajtriangle10.b);
		ajtriangle5.ab = ajtriangle10;
		ajtriangle10.bc = ajtriangle5;
		ajtriangle5.bc = ajtriangle8;
		ajtriangle5.ca = ajtriangle9;
		ajtriangle9.bc = ajtriangle5;
		return ajtriangle10;
	}

	public void flipTemp(ajTriangle ajtriangle, ajTriangle ajtriangle1) {
		ajTriangle ajtriangle2 = ajtriangle.ab;
		if (super.showVor)
			super.canvas.add(ajtriangle1.dualEdge(ajtriangle2), hideVorLayer, actualHideVorColor);
		if (stepMode) {
			stepList.add(ajtriangle.a, ajtriangle.b, moveDelLayer, stepFlipColor);
			stepList.pause();
			if (!ajtriangle2.halfplane) {
				stepList.add(ajtriangle2.circum, oldmoveDelLayer, stepCircleColor);
				stepList.pause();
				stepList.remove(ajtriangle2.circum, oldmoveDelLayer);
			}
		}
		if (ajtriangle2.halfplane || !ajtriangle2.circumcircle_contains(ajtriangle.c)) {
			if (super.showDel)
				super.canvas.add(ajtriangle.a, ajtriangle.b, refreshLayer, super.color);
			if (stepMode)
				stepList.add(ajtriangle.a, ajtriangle.b, moveDelLayer, stepStarColor);
			return;
		}
		ajTriangle ajtriangle3;
		if (ajtriangle.a == ajtriangle2.a) {
			ajtriangle3 = new ajTriangle(ajtriangle.a, ajtriangle2.b, ajtriangle.c);
			ajtriangle3.ab = ajtriangle2.ab;
			ajtriangle.ab = ajtriangle2.bc;
		} else if (ajtriangle.a == ajtriangle2.b) {
			ajtriangle3 = new ajTriangle(ajtriangle.a, ajtriangle2.c, ajtriangle.c);
			ajtriangle3.ab = ajtriangle2.bc;
			ajtriangle.ab = ajtriangle2.ca;
		} else if (ajtriangle.a == ajtriangle2.c) {
			ajtriangle3 = new ajTriangle(ajtriangle.a, ajtriangle2.a, ajtriangle.c);
			ajtriangle3.ab = ajtriangle2.ca;
			ajtriangle.ab = ajtriangle2.ab;
		} else {
			System.out.println("Error in flipTemp.");
			return;
		}
		ajtriangle3.ca = ajtriangle.ca;
		ajtriangle3.ca.switchneighbors(ajtriangle, ajtriangle3);
		ajtriangle3.bc = ajtriangle;
		ajtriangle.ca = ajtriangle3;
		ajtriangle.a = ajtriangle3.b;
		ajtriangle.circumcircle();
		if (super.showDel) {
			super.canvas.add(ajtriangle3.a, ajtriangle.b, hideDelLayer, actualHideDelColor);
			super.canvas.add(ajtriangle.a, ajtriangle.c, moveDelLayer, moveDelColor);
		}
		super.canvas.add(new ajPoint(ajtriangle.a), refreshPointLayer);
		if (stepMode) {
			stepList.add(ajtriangle3.a, ajtriangle.b, moveDelLayer, actualHideDelColor);
			stepList.add(ajtriangle.a, ajtriangle.c, moveDelLayer, moveDelColor);
			stepList.add(ajtriangle.b, ajtriangle.c, moveDelLayer, moveDelColor);
			stepList.add(ajtriangle3.a, ajtriangle.c, moveDelLayer, moveDelColor);
			stepList.add(ajtriangle.a, refreshPointLayer, ajElement.pointColor);
			stepList.add(ajtriangle.a, ajtriangle.b, moveDelLayer, stepStarColor);
			stepList.add(ajtriangle3.a, ajtriangle3.b, moveDelLayer, stepStarColor);
			stepList.pause();
		}
		flipTemp(ajtriangle, ajtriangle2);
	}

	public void draw(Graphics g) {
		if (!super.allCollinear) {
			if (super.showDel) {
				g.setColor(super.color);
				super.root.visitAndDraw(g);
			}
			if (super.showVor) {
				g.setColor(super.VorColor);
				super.root.visitAndDrawDual(g);
			}
		} else if (super.nPoints >= 2) {
			for (ajTriangle ajtriangle = super.firstT; ajtriangle.b != super.lastP; ajtriangle = ajtriangle.ca) {
				if (super.showDel) {
					g.setColor(super.color);
					ajSegment.drawLineSegment(g, ajtriangle.b, ajtriangle.a);
				}
				if (super.showVor && (!moving || !super.allCollinear)) {
					g.setColor(super.VorColor);
					ajLine.drawBisector(g, ajtriangle.b, ajtriangle.a);
				}
			}

		}
		if (super.showHull && !moving && super.nPoints >= 2) {
			g.setColor(super.HullColor);
			super.startTriangleHull.visitAndDrawHull(g);
		}
	}

	private int l;
	final int oldmoveHullLayer;
	final int oldmoveVorLayer;
	final int oldmoveDelLayer;
	final int oldrefreshLayer;
	final int oldhideVorLayer;
	final int oldhideDelLayer;
	final int hideDelLayer;
	final int hideVorLayer;
	final int refreshLayer;
	final int moveDelLayer;
	final int moveVorLayer;
	final int moveHullLayer;
	final int oldrefreshPointLayer;
	final int refreshPointLayer;
	final int layers;
	Color moveDelColor;
	Color hideDelColor;
	Color moveVorColor;
	Color hideVorColor;
	Color moveHullColor;
	Color stepStarColor;
	Color stepFlipColor;
	Color stepCircleColor;
	Color actualHideDelColor;
	Color actualHideVorColor;
	private boolean stepMode;
	private ajStep stepList;
	private boolean oldShowVor;
	private boolean oldShowDel;
	private boolean moving;
}
