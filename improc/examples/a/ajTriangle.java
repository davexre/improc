package a;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajTriangle.java

import java.awt.Graphics;
import java.io.PrintStream;

import a.obsolete.ajCircle;
import a.obsolete.ajElement;
import a.obsolete.ajRay;
import a.obsolete.ajSegment;

public class ajTriangle extends ajElement {

	public ajTriangle() {
		halfplane = false;
		visitflag = visitValue;
	}

	public ajTriangle(ajPoint ajpoint, ajPoint ajpoint1, ajPoint ajpoint2) {
		halfplane = false;
		visitflag = visitValue;
		a = ajpoint;
		int i = ajpoint2.pointToLine(ajpoint, ajpoint1);
		if (i <= 1 || i == 3 || i == 4) {
			b = ajpoint1;
			c = ajpoint2;
		} else {
			System.out.println("Warning, ajTriangle(A,B,C) expects points in counterclockwise order.");
			System.out.println("" + ajpoint + ajpoint1 + ajpoint2);
			b = ajpoint2;
			c = ajpoint1;
		}
		circumcircle();
	}

	public ajTriangle(ajPoint ajpoint, ajPoint ajpoint1) {
		halfplane = false;
		visitflag = visitValue;
		a = ajpoint;
		b = ajpoint1;
		halfplane = true;
	}

	public static ajTriangle findTriangleNEW(ajTriangle t, ajPoint p) {
		while (t != null) {
			switch (p.pointToLine(t.a, t.b)) {
			case 4:
				if (t.halfplane)
					return t;
				t = t.ab;
				continue;
			case 2:
				t = t.ab;
				continue;
			case 3:
				if (t.halfplane)
					return t;
				t = t.ca;
				continue;
			case 0:
			case 1:
			case 5:
			default:
				break;
			}
			if (t.halfplane) {
				return t;
			}

			switch (p.pointToLine(t.b, t.c)) {
			case 4:
				if (t.halfplane)
					return t;
				t = t.bc;
				continue;
			case 2:
				t = t.bc;
				continue;
			case 3:
				if (t.halfplane)
					return t;
				t = t.ab;
				continue;
			case 0:
			case 1:
			case 5:
			default:
				break;
			}
			
			switch (p.pointToLine(t.c, t.a)) {
			case 4:
				if (t.halfplane)
					return t;
				t = t.ca;
				continue;
			case 2:
				t = t.ca;
				continue;
			case 3:
				if (t.halfplane)
					return t;
				t = t.bc;
				continue;
			case 0:
			case 1:
			case 5:
			default:
				break;
			}

			return t;
		}
		return null;
	}
	
	public ajTriangle findTriangle(ajPoint ajpoint) {
		if (ajpoint.pointToLine(a, b) >= 2)
			return ab.findnext(ajpoint, this);
		if (halfplane)
			return this;
		if (ajpoint.pointToLine(b, c) >= 2)
			return bc.findnext(ajpoint, this);
		if (ajpoint.pointToLine(c, a) >= 2)
			return ca.findnext(ajpoint, this);
		else
			return this;
	}

	private ajTriangle findnext(ajPoint p, ajTriangle t) {
		if (halfplane) {
			switch (p.pointToLine(a, b)) {
			case 0: // '\0'
			case 1: // '\001'
				return this;

			case 3: // '\003'
				return ca.findnext(p, this);

			case 4: // '\004'
				return bc.findnext(p, this);

			case 2: // '\002'
				System.out.println("Should not happen: point not in halfplane.");
				return ab.findnext(p, this);
			}
		}
		ajPoint tmpA;
		ajPoint tmpB;
		ajPoint tmpC;
		ajTriangle t1;
		ajTriangle t2;
		if (ab == t) {
			tmpA = b;
			tmpB = c;
			tmpC = a;
			t1 = bc;
			t2 = ca;
		} else if (bc == t) {
			tmpA = c;
			tmpB = a;
			tmpC = b;
			t1 = ca;
			t2 = ab;
		} else {
			tmpA = a;
			tmpB = b;
			tmpC = c;
			t1 = ab;
			t2 = bc;
		}
		if (p.pointToLine(tmpA, tmpB) >= 2)
			return t1.findnext(p, this);
		if (p.pointToLine(tmpB, tmpC) >= 2)
			return t2.findnext(p, this);
		else
			return this;
	}

	public void switchneighbors(ajTriangle oldTr, ajTriangle newTr) {
		if (ab == oldTr) {
			ab = newTr;
			return;
		}
		if (bc == oldTr) {
			bc = newTr;
			return;
		}
		if (ca == oldTr) {
			ca = newTr;
			return;
		} else {
			System.out.println("Error, switchneighbors can't find Old.");
			return;
		}
	}

	public ajTriangle neighbor(ajPoint ajpoint) {
		if (a == ajpoint)
			return ca;
		if (b == ajpoint)
			return ab;
		if (c == ajpoint) {
			return bc;
		} else {
			System.out.println("Error, neighbors can't find p: " + ajpoint);
			return null;
		}
	}

	public ajCircle circumcircle() {
		float f = ((a.x - b.x) * (a.x + b.x) + (a.y - b.y) * (a.y + b.y)) / 2.0F;
		float f1 = ((b.x - c.x) * (b.x + c.x) + (b.y - c.y) * (b.y + c.y)) / 2.0F;
		float f2 = (a.x - b.x) * (b.y - c.y) - (b.x - c.x) * (a.y - b.y);
		if (f2 == 0.0F) {
			circum = new ajCircle(a, (1.0F / 0.0F));
		} else {
			ajPoint ajpoint = new ajPoint((f * (b.y - c.y) - f1 * (a.y - b.y)) / f2, (f1 * (a.x - b.x) - f
					* (b.x - c.x))
					/ f2);
			circum = new ajCircle(ajpoint, ajpoint.distance2(a));
		}
		return circum;
	}

	public boolean circumcircle_contains(ajPoint ajpoint) {
		return circum.r > circum.c.distance2(ajpoint);
	}

	public void visitAndDraw(Graphics g) {
		visitValue = !visitValue;
		visitMore(g);
	}

	private void visitMore(Graphics g) {
		visitflag = visitValue;
		if (ab.visitflag != visitValue)
			ajSegment.drawLineSegment(g, a, b);
		if (!halfplane) {
			if (bc.visitflag != visitValue)
				ajSegment.drawLineSegment(g, b, c);
			if (ca.visitflag != visitValue)
				ajSegment.drawLineSegment(g, c, a);
		}
		if (ab.visitflag != visitValue)
			ab.visitMore(g);
		if (bc.visitflag != visitValue)
			bc.visitMore(g);
		if (ca.visitflag != visitValue)
			ca.visitMore(g);
	}

	public void visitAndPrint() {
		visitValue = !visitValue;
		visitMorePrint();
	}

	private void visitMorePrint() {
		visitflag = visitValue;
		if (!halfplane)
			System.out.println(circum);
		if (ab.visitflag != visitValue)
			ab.visitMorePrint();
		if (bc.visitflag != visitValue)
			bc.visitMorePrint();
		if (ca.visitflag != visitValue)
			ca.visitMorePrint();
	}

	public void visitAndDrawDual(Graphics g) {
		visitValue = !visitValue;
		visitMoreDual(g);
	}

	private void visitMoreDual(Graphics g) {
		visitflag = visitValue;
		if (ab.visitflag != visitValue)
			drawDualEdge(g, ab);
		if (!halfplane) {
			if (bc.visitflag != visitValue)
				drawDualEdge(g, bc);
			if (ca.visitflag != visitValue)
				drawDualEdge(g, ca);
		}
		if (ab.visitflag != visitValue)
			ab.visitMoreDual(g);
		if (bc.visitflag != visitValue)
			bc.visitMoreDual(g);
		if (ca.visitflag != visitValue)
			ca.visitMoreDual(g);
	}

	public void visitAndDrawHull(Graphics g) {
		ajTriangle ajtriangle = this;
		do {
			ajSegment.drawLineSegment(g, ajtriangle.a, ajtriangle.b);
			ajtriangle = ajtriangle.ca;
		} while (ajtriangle != this);
	}

	public ajElement dualEdge(ajTriangle ajtriangle) {
		if (ajtriangle.halfplane)
			if (halfplane) {
				System.out.println("Warning, no dual edge between two halfplanes.");
				System.out.println("" + this + ajtriangle);
				return null;
			} else {
				return new ajRay(circum.c, new ajPoint(circum.c.x - (ajtriangle.b.y - ajtriangle.a.y), circum.c.y
						+ (ajtriangle.b.x - ajtriangle.a.x)));
			}
		if (halfplane)
			return new ajRay(ajtriangle.circum.c, new ajPoint(ajtriangle.circum.c.x - (b.y - a.y),
					ajtriangle.circum.c.y + (b.x - a.x)));
		else
			return new ajSegment(circum.c, ajtriangle.circum.c);
	}

	private void drawDualEdge(Graphics g, ajTriangle ajtriangle) {
		if (ajtriangle.halfplane) {
			ajRay.drawRay(g, circum.c, circum.c.x - (ajtriangle.b.y - ajtriangle.a.y), circum.c.y
					+ (ajtriangle.b.x - ajtriangle.a.x));
			return;
		}
		if (halfplane) {
			ajRay.drawRay(g, ajtriangle.circum.c, ajtriangle.circum.c.x - (b.y - a.y), ajtriangle.circum.c.y
					+ (b.x - a.x));
			return;
		} else {
			ajSegment.drawLineSegment(g, circum.c, ajtriangle.circum.c);
			return;
		}
	}

	public String toString() {
		String s = name(this) + a.toString() + b.toString();
		if (!halfplane)
			s = s + c.toString();
		s = s + name(ab) + name(bc) + name(ca);
		return s;
	}

	public static String name(ajTriangle ajtriangle) {
		if (ajtriangle == null)
			return " Tnull";
		else
			return " T" + ((ajElement) (ajtriangle)).ident;
	}

	ajPoint a;
	ajPoint b;
	ajPoint c;
	ajTriangle ab;
	ajTriangle bc;
	ajTriangle ca;
	ajCircle circum;
	boolean halfplane;
	boolean visitflag;
	private static boolean visitValue;
}
