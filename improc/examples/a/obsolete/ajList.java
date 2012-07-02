package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajList.java

import java.io.PrintStream;


public class ajList {

	public void append(ajListElement ajlistelement) {
		if (last != null) {
			last.append(ajlistelement);
		} else {
			anchor = ajlistelement;
			ajlistelement.nextElement = null;
			ajlistelement.prevElement = null;
		}
		last = ajlistelement;
	}

	public void insert(ajListElement ajlistelement) {
		if (anchor != null) {
			anchor.insert(ajlistelement);
		} else {
			last = ajlistelement;
			ajlistelement.nextElement = null;
			ajlistelement.prevElement = null;
		}
		anchor = ajlistelement;
	}

	public void insert(ajListElement ajlistelement, ajListElement ajlistelement1) {
		if (ajlistelement1 == null) {
			append(ajlistelement);
			return;
		}
		if (ajlistelement1 == anchor) {
			insert(ajlistelement);
			return;
		} else {
			ajlistelement1.insert(ajlistelement);
			return;
		}
	}

	public void remove(ajListElement ajlistelement) {
		if (ajlistelement == anchor)
			anchor = ajlistelement.getNext();
		if (ajlistelement == last)
			last = ajlistelement.getPrev();
		ajlistelement.removeFromList();
	}

	public boolean isEmpty() {
		return anchor == null;
	}

	public String toString() {
		ajListElement ajlistelement = anchor;
		StringBuffer stringbuffer = new StringBuffer("");
		for (; ajlistelement != null; ajlistelement = ajlistelement.getNext()) {
			System.out.println("toString" + ajlistelement);
			stringbuffer.append(" " + ajlistelement.toString() + "\n");
		}

		return stringbuffer.toString();
	}

	public ajList() {
	}

	public ajListElement anchor;
	public ajListElement last;
}
