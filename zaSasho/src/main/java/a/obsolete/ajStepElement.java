package a.obsolete;


// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajStepElement.java

public class ajStepElement {

	public ajStepElement(long l, ajElement ajelement, int i, int j) {
		timeStamp = l;
		element = ajelement;
		layer = i;
		what = j;
	}

	public ajElement element;
	public int layer;
	public int what;
	public static final int ST_ADD = 0;
	public static final int ST_REMOVE = 1;
	public long timeStamp;
	public ajStepElement nextStepElement;
}
