package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajUpdate.java

import java.awt.Event;
import java.awt.Graphics;

public interface ajUpdate {

	public abstract void ajUpdate(Graphics g);

	public abstract boolean handleMouseDown(Event event, int i, int j);

	public abstract boolean handleMouseUp(Event event, int i, int j);

	public abstract boolean handleMouseDrag(Event event, int i, int j);

	public abstract boolean handleMouseEnter(Event event, int i, int j);

	public abstract boolean handleMouseExit(Event event, int i, int j);

	public abstract boolean handleMouseMove(Event event, int i, int j);

	public abstract boolean handleKeyDown(Event event, int i);

	public abstract boolean handleKeyUp(Event event, int i);
}
