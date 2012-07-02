package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajListElement.java

public abstract class ajListElement {

	public ajListElement getNext() {
		return nextElement;
	}

	public ajListElement getPrev() {
		return prevElement;
	}

	public void append(ajListElement ajlistelement) {
		ajlistelement.prevElement = this;
		ajlistelement.nextElement = nextElement;
		if (ajlistelement.nextElement != null)
			ajlistelement.nextElement.prevElement = ajlistelement;
		nextElement = ajlistelement;
	}

	public void insert(ajListElement ajlistelement) {
		ajlistelement.nextElement = this;
		ajlistelement.prevElement = prevElement;
		if (ajlistelement.prevElement != null)
			ajlistelement.prevElement.nextElement = ajlistelement;
		prevElement = ajlistelement;
	}

	public void removeFromList() {
		if (prevElement != null)
			prevElement.nextElement = nextElement;
		if (nextElement != null)
			nextElement.prevElement = prevElement;
		prevElement = null;
		nextElement = null;
	}

	public ajListElement() {
	}

	public ajListElement prevElement;
	public ajListElement nextElement;
	
	public void dump() {
		System.out.println("---------");
		System.out.println(getClass().getName());
		int i = 0;
		ajListElement el = this;
		while (el != null) {
			System.out.println(i + " " + this);
			el = el.nextElement;
		}
		System.out.println();
	}
}
