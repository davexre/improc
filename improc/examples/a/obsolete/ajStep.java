package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajStep.java

import java.awt.*;

import a.ajCanvas;
import a.ajPoint;

public class ajStep extends ajAnimator {

	public ajStep(ajSingleStepPanel ajsinglesteppanel, ajCanvas ajcanvas) {
		super(ajcanvas, 30);
		canvas = ajcanvas;
		pan = ajsinglesteppanel;
		n = 0L;
		step_n = 0L;
	}

	public synchronized void set_time(int i) {
		super.sleepTime = i;
	}

	public void begin() {
		to_first();
		start_replay();
	}

	public void reset() {
		stop_replay();
		recorded = null;
		step_pointer = null;
		n = 0L;
		step_n = 0L;
	}

	public synchronized void single_step() {
		if (step_pointer == null) {
			Event event = new Event(this, 1001, null);
			synchronized (canvas) {
				canvas.postEvent(event);
			}
			stop_replay();
			return;
		}
		for (; step_pointer != null && step_pointer.timeStamp <= step_n; step_pointer = step_pointer.nextStepElement)
			synchronized (canvas) {
				switch (step_pointer.what) {
				case 0: // '\0'
					canvas.add(step_pointer.element, step_pointer.layer);
					break;

				case 1: // '\001'
					canvas.remove(step_pointer.element, step_pointer.layer);
					break;
				}
			}

		canvas.repaint();
		if (step_n < n)
			step_n++;
	}

	public void to_first() {
		stop_replay();
		canvas.cleanTempLayers();
		canvas.repaint();
		step_pointer = recorded;
		step_n = 0L;
	}

	public void to_last() {
		to_first();
		for (; step_pointer != null && step_pointer.timeStamp < n; step_pointer = step_pointer.nextStepElement)
			synchronized (canvas) {
				switch (step_pointer.what) {
				case 0: // '\0'
					canvas.add(step_pointer.element, step_pointer.layer);
					break;

				case 1: // '\001'
					canvas.remove(step_pointer.element, step_pointer.layer);
					break;
				}
			}

		canvas.paintTempLayers();
		step_n = n;
	}

	public void start_replay() {
		rep = new ajStepElementReplay(this);
		add(rep);
		pan.stepPlay.switch_on();
		start();
	}

	public void stop_replay() {
		stop();
		pan.stepPlay.switch_off();
		if (rep != null)
			remove(rep);
		rep = null;
	}

	public void step_one_back() {
		long l = step_n;
		if (l < 1L)
			return;
		to_first();
		for (; step_pointer != null && step_pointer.timeStamp < l - 1L; step_pointer = step_pointer.nextStepElement)
			synchronized (canvas) {
				switch (step_pointer.what) {
				case 0: // '\0'
					canvas.add(step_pointer.element, step_pointer.layer);
					break;

				case 1: // '\001'
					canvas.remove(step_pointer.element, step_pointer.layer);
					break;
				}
			}

		step_n = l - 1L;
		canvas.paintTempLayers();
	}

	public void add(ajElement ajelement, int i) {
		ajStepElement ajstepelement = new ajStepElement(n, ajelement, i, 0);
		if (recorded == null) {
			step_pointer = ajstepelement;
			recorded = ajstepelement;
		} else {
			step_pointer.nextStepElement = ajstepelement;
		}
		step_pointer = ajstepelement;
	}

	public void remove(ajElement ajelement, int i) {
		ajStepElement ajstepelement = new ajStepElement(n, ajelement, i, 1);
		if (recorded == null) {
			step_pointer = ajstepelement;
			recorded = ajstepelement;
		} else {
			step_pointer.nextStepElement = ajstepelement;
		}
		step_pointer = ajstepelement;
	}

	public void add(ajPoint ajpoint, ajPoint ajpoint1, int i, Color color) {
		add(((ajElement) (new ajSegment(ajpoint, ajpoint1, color))), i);
	}

	public void add(ajElement ajelement, int i, Color color) {
		ajelement.color = color;
		add(ajelement, i);
	}

	public void add(ajPoint ajpoint, int i, Color color) {
		add(((ajElement) (new ajPoint(ajpoint, color))), i);
	}

	public void pause() {
		n++;
	}

	public void pause(int i) {
		n += i;
	}

	public long n;
	public long step_n;
	public ajStepElementReplay rep;
	public ajSingleStepPanel pan;
	public ajStepElement recorded;
	public ajStepElement step_pointer;
	ajCanvas canvas;
}
