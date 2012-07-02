package a.obsolete;


// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajStep.java

public class ajStepElementReplay implements ajAnimation {

	public ajStepElementReplay(ajStep ajstep) {
		step = ajstep;
	}

	public void do_it(long l) {
		step.single_step();
	}

	private ajStep step;
}
