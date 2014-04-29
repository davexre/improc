package a;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajDelaunay.java

import java.awt.Color;
import java.util.ArrayList;

import a.obsolete.ajElement;

public class ajDelaunay extends ajElement {

	ajPoint firstP;
	ajPoint lastP;
	boolean allCollinear;
	ajTriangle firstT;
	ajTriangle lastT;
	public ajTriangle root;
	ajTriangle startTriangleHull;
	int nPoints;
	ajCanvas canvas;
	Color color;
	Color VorColor;
	Color HullColor;
	protected boolean showDel;
	protected boolean showVor;
	protected boolean showHull;

	ArrayList<ajPoint> points = new ArrayList<ajPoint>();
	
	public ajDelaunay(ajCanvas ajcanvas) {
		color = Color.black;
		VorColor = ajElement.muchDarker(Color.blue);
		HullColor = ajElement.muchDarker(Color.green);
		canvas = ajcanvas;
		nPoints = 0;
		allCollinear = true;
	}

	public void showDelaunay(boolean flag) {
		showDel = flag;
	}

	public boolean showDelaunayState() {
		return showDel;
	}

	public void showVoronoi(boolean flag) {
		showVor = flag;
	}

	public void showConvex(boolean flag) {
		showHull = flag;
	}

	public boolean showConvexState() {
		return showHull;
	}

	void recursiveAppendTriangle(ajTriangle t, ArrayList<ajTriangle> triangles, boolean visitFlag) {
		if (t == null)
			return;
		if (t.visitflag == visitFlag)
			return;
		t.visitflag = visitFlag;
		triangles.add(t);
		recursiveAppendTriangle(t.ab, triangles, visitFlag);
		recursiveAppendTriangle(t.bc, triangles, visitFlag);
		recursiveAppendTriangle(t.ca, triangles, visitFlag);
	}
	
	public ArrayList<ajTriangle> getTrianglesList() {
		ArrayList<ajTriangle> triangles = new ArrayList<ajTriangle>();
		recursiveAppendTriangle(root, triangles, !root.visitflag);
		return triangles;
	}
	
	public void insertPoint(ajPoint ajpoint) {
		points.add(ajpoint);
		ajTriangle t = insertPointSimple(ajpoint);
		if (t == null)
			return;
		ajTriangle t1 = t;
		do {
			flip(t1);
			t1 = t1.ca;
		} while (t1 != t && !t1.halfplane);
	}

	private ajTriangle insertPointSimple(ajPoint p) {
		nPoints++;
		if (root != null)
			root.dump();
		if (!allCollinear) {
			ajTriangle t = ajTriangle.findTriangleNEW(root, p);
//			ajTriangle t = root.findTriangle(p);
			if (t.halfplane)
				root = extendOutside(t, p);
			else
				root = extendInside(t, p);
			return root;
		}
		if (nPoints == 1) {
			firstP = p;
			return null;
		}
		if (nPoints == 2) {
			startTriangulation(p);
			return null;
		}
		switch (p.pointToLine(firstP, lastP)) {
		case 1: // '\001' // NegativePlane
			root = extendOutside(firstT.ab, p);
			allCollinear = false;
			break;

		case 2: // '\002' // PositivePlane
			root = extendOutside(firstT, p);
			allCollinear = false;
			break;

		case 0: // '\0' // Inside
			insertCollinear(p, 0);
			break;

		case 3: // '\003' // BeforeTheStartPoint
			insertCollinear(p, 3);
			break;

		case 4: // '\004' // AfterTheEndPoint
			insertCollinear(p, 4);
			break;
		}
		return null;
	}

	private void insertCollinear(ajPoint p, int i) {
		switch (i) {
		case 3: { // '\003'
			ajTriangle t = new ajTriangle(firstP, p);
			ajTriangle t3 = new ajTriangle(p, firstP);
			t.ab = t3;
			t3.ab = t;
			t.bc = t3;
			t3.ca = t;
			t.ca = firstT;
			firstT.bc = t;
			t3.bc = firstT.ab;
			firstT.ab.ca = t3;
			firstT = t;
			firstP = p;
			return;
		}
		case 4: { // '\004'
			ajTriangle t1 = new ajTriangle(p, lastP);
			ajTriangle t4 = new ajTriangle(lastP, p);
			t1.ab = t4;
			t4.ab = t1;
			t1.bc = lastT;
			lastT.ca = t1;
			t1.ca = t4;
			t4.bc = t1;
			t4.ca = lastT.ab;
			lastT.ab.bc = t4;
			lastT = t1;
			lastP = p;
			return;
		}
		case 0: { // '\0' 
			ajTriangle t;
			for (t = firstT; p.isGreater(t.a); t = t.ca)
				;
			ajTriangle t2 = new ajTriangle(p, t.b);
			ajTriangle t3 = new ajTriangle(t.b, p);
			t.b = p;
			t.ab.a = p;
			t2.ab = t3;
			t3.ab = t2;
			t2.bc = t.bc;
			t.bc.ca = t2;
			t2.ca = t;
			t.bc = t2;
			t3.ca = t.ab.ca;
			t.ab.ca.bc = t3;
			t3.bc = t.ab;
			t.ab.ca = t3;
			if (firstT == t) {
				firstT = t2;
				return;
			}
			break;
		}
		}
	}

	private void startTriangulation(ajPoint p) {
		ajPoint p2;
		ajPoint p3;
		if (firstP.isLess(p)) {
			p2 = firstP;
			p3 = p;
		} else {
			p2 = p;
			p3 = firstP;
		}
		firstT = new ajTriangle(p3, p2);
		lastT = firstT;
		ajTriangle t = new ajTriangle(p2, p3);
		firstT.ab = t;
		t.ab = firstT;
		firstT.bc = t;
		t.ca = firstT;
		firstT.ca = t;
		t.bc = firstT;
		firstP = firstT.b;
		lastP = lastT.a;
		startTriangleHull = firstT;
	}

	public ajTriangle extendInside(ajTriangle t, ajPoint p) {
		ajTriangle t1 = treatDegeneracyInside(t, p);
		if (t1 != null) {
			return t1;
		} else {
			ajTriangle t2 = new ajTriangle(t.c, t.a, p);
			ajTriangle t3 = new ajTriangle(t.b, t.c, p);
			t.c = p;
			t.circumcircle();
			t2.ab = t.ca;
			t2.bc = t;
			t2.ca = t3;
			t3.ab = t.bc;
			t3.bc = t2;
			t3.ca = t;
			t2.ab.switchneighbors(t, t2);
			t3.ab.switchneighbors(t, t3);
			t.bc = t3;
			t.ca = t2;
			return t;
		}
	}

	public ajTriangle treatDegeneracyInside(ajTriangle ajtriangle, ajPoint ajpoint) {
		if (ajtriangle.ab.halfplane && ajpoint.pointToLine(ajtriangle.b, ajtriangle.a) == 0)
			return extendOutside(ajtriangle.ab, ajpoint);
		if (ajtriangle.bc.halfplane && ajpoint.pointToLine(ajtriangle.c, ajtriangle.b) == 0)
			return extendOutside(ajtriangle.bc, ajpoint);
		if (ajtriangle.ca.halfplane && ajpoint.pointToLine(ajtriangle.a, ajtriangle.c) == 0)
			return extendOutside(ajtriangle.ca, ajpoint);
		else
			return null;
	}

	private ajTriangle extendOutside(ajTriangle t, ajPoint p) {
		if (p.pointToLine(t.a, t.b) == 0) {
			ajTriangle t1 = new ajTriangle(t.a, t.b, p);
			ajTriangle t3 = new ajTriangle(p, t.b);
			t.b = p;
			t1.ab = t.ab;
			t1.ab.switchneighbors(t, t1);
			t1.bc = t3;
			t3.ab = t1;
			t1.ca = t;
			t.ab = t1;
			t3.bc = t.bc;
			t3.bc.ca = t3;
			t3.ca = t;
			t.bc = t3;
			return t1;
		} else {
			ajTriangle t2 = extendcounterclock(t, p);
			ajTriangle t4 = extendclock(t, p);
			t2.bc = t4;
			t4.ca = t2;
			startTriangleHull = t4;
			return t4.ab;
		}
	}

	private static ajTriangle extendcounterclock(ajTriangle t, ajPoint p) {
		t.halfplane = false;
		t.c = p;
		t.circumcircle();
		ajTriangle t1 = t.ca;
		if (p.pointToLine(t1.a, t1.b) >= 2) {
			ajTriangle t2 = new ajTriangle(t.a, p);
			t2.ab = t;
			t.ca = t2;
			t2.ca = t1;
			t1.bc = t2;
			return t2;
		} else {
			return extendcounterclock(t1, p);
		}
	}

	private ajTriangle extendclock(ajTriangle t, ajPoint p) {
		t.halfplane = false;
		t.c = p;
		t.circumcircle();
		ajTriangle t1 = t.bc;
		if (p.pointToLine(t1.a, t1.b) >= 2) {
			ajTriangle t2 = new ajTriangle(p, t.b);
			t2.ab = t;
			t.bc = t2;
			t2.bc = t1;
			t1.ca = t2;
			return t2;
		} else {
			return extendclock(t1, p);
		}
	}

	public void flip(ajTriangle t) {
		ajTriangle t1 = t.ab;
		if (t1.halfplane || !t1.circumcircle_contains(t.c))
			return;
		ajTriangle t2;
		if (t.a == t1.a) {
			t2 = new ajTriangle(t1.b, t.b, t.c);
			t2.ab = t1.bc;
			t.ab = t1.ab;
		} else if (t.a == t1.b) {
			t2 = new ajTriangle(t1.c, t.b, t.c);
			t2.ab = t1.ca;
			t.ab = t1.bc;
		} else if (t.a == t1.c) {
			t2 = new ajTriangle(t1.a, t.b, t.c);
			t2.ab = t1.ab;
			t.ab = t1.ca;
		} else {
			System.out.println("Error in flip.");
			return;
		}
		t2.bc = t.bc;
		t2.ab.switchneighbors(t1, t2);
		t2.bc.switchneighbors(t, t2);
		t.bc = t2;
		t2.ca = t;
		t.b = t2.a;
		t.ab.switchneighbors(t1, t);
		t.circumcircle();
		flip(t);
		flip(t2);
	}

	public void dumpTriangles() {
		ArrayList<ajTriangle> triangles = getTrianglesList();
		for (ajTriangle t : triangles) {
			String a = Integer.toString(points.indexOf(t.a));
			String b = Integer.toString(points.indexOf(t.b));
			String c = t.halfplane ? "null" : Integer.toString(points.indexOf(t.c));
			System.out.println(a + ":" + b + ":" + c);
		}
	}
	
	public void recompute() {
		nPoints = 0;
		points.clear();
		allCollinear = true;
		canvas.clean();
		canvas.add(this);
		for (ajPoint ajpoint = (ajPoint) canvas.drawPoints.anchor; ajpoint != null; 
				ajpoint = (ajPoint) ajpoint.getNext())
			insertPoint(ajpoint);
		dumpTriangles();
	}
}
