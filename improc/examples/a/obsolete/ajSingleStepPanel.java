package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajSingleStepPanel.java

import java.awt.Button;
import java.awt.Color;
import java.awt.Event;
import java.awt.GridBagLayout;
import java.awt.Panel;

import a.VoronoiApplet;

public class ajSingleStepPanel extends Panel {

	public ajSingleStepPanel(VoronoiApplet voronoiapplet, ajButtonImages ajbuttonimages) {
		va = voronoiapplet;
		setLayout(new GridBagLayout());
		stepBegin = new ajButton(0, ajbuttonimages.anfangOffImg, ajbuttonimages.anfangOnImg);
		stepEnd = new ajButton(0, ajbuttonimages.endeOffImg, ajbuttonimages.endeOnImg);
		stepOne = new ajButton(0, ajbuttonimages.stepOffImg, ajbuttonimages.stepOnImg);
		stepOneBack = new ajButton(0, ajbuttonimages.stepBackOffImg, ajbuttonimages.stepBackOnImg);
		stepPlay = new ajButton(1, ajbuttonimages.playOffImg, ajbuttonimages.playOnImg);
		stepStop = new ajButton(0, ajbuttonimages.stopOffImg, ajbuttonimages.stopOnImg);
		slider = new SpeedSlider(va, 250, 30, 100, 2000, 1000);
		ez_ende = new Button("Ende");
		ez_led = new ajLed();
		ez_led.basecolor = Color.orange;
		ez_led.duration = 400L;
		ez_led.switch_off();
		LayoutTools.gridBagConstrain(this, ez_led, 0, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 15, 5, 5);
		LayoutTools.gridBagConstrain(this, stepStop, 1, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 15, 5, 5);
		LayoutTools.gridBagConstrain(this, stepBegin, 2, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 5, 5, 5);
		LayoutTools.gridBagConstrain(this, stepOneBack, 3, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 5, 5, 5);
		LayoutTools.gridBagConstrain(this, stepPlay, 4, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 5, 5, 5);
		LayoutTools.gridBagConstrain(this, stepOne, 5, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 5, 5, 5);
		LayoutTools.gridBagConstrain(this, stepEnd, 6, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 5, 5, 5);
		LayoutTools.gridBagConstrain(this, slider, 7, 1, 1, 1, 0, 10, 0.0D, 0.0D, 5, 20, 5, 20);
		LayoutTools.gridBagConstrain(this, ez_ende, 8, 1, 1, 1, 0, 17, 1.0D, 0.0D, 15, 5, 5, 5);
	}

	public boolean action(Event event, Object obj) {
		if (event.target == va.evRec) {
			ajEvent ajevent = (ajEvent) event.arg;
			Object obj1 = null;
			Object obj2 = null;
			switch (ajevent.arg1) {
			case 0: // '\0'
				Event event1 = new Event(stepStop, event.when, 501, event.x, event.y, 0, event.modifiers);
				stepStop.postEvent(event1);
				mouseUpThread mouseupthread = new mouseUpThread(stepStop, event.when, event.x, event.y, event.modifiers);
				mouseupthread.start();
				return true;

			case 1: // '\001'
				Event event2 = new Event(stepBegin, event.when, 501, event.x, event.y, 0, event.modifiers);
				stepBegin.postEvent(event2);
				mouseUpThread mouseupthread1 = new mouseUpThread(stepBegin, event.when, event.x, event.y,
						event.modifiers);
				mouseupthread1.start();
				return true;

			case 2: // '\002'
				Event event3 = new Event(stepOneBack, event.when, 501, event.x, event.y, 0, event.modifiers);
				stepOneBack.postEvent(event3);
				mouseUpThread mouseupthread2 = new mouseUpThread(stepOneBack, event.when, event.x, event.y,
						event.modifiers);
				mouseupthread2.start();
				return true;

			case 3: // '\003'
				Event event4 = new Event(stepPlay, event.when, 501, event.x, event.y, 0, event.modifiers);
				stepPlay.postEvent(event4);
				return true;

			case 4: // '\004'
				Event event5 = new Event(stepOne, event.when, 501, event.x, event.y, 0, event.modifiers);
				stepOne.postEvent(event5);
				mouseUpThread mouseupthread3 = new mouseUpThread(stepOne, event.when, event.x, event.y, event.modifiers);
				mouseupthread3.start();
				return true;

			case 5: // '\005'
				Event event6 = new Event(stepEnd, event.when, 501, event.x, event.y, 0, event.modifiers);
				stepEnd.postEvent(event6);
				mouseUpThread mouseupthread4 = new mouseUpThread(stepEnd, event.when, event.x, event.y, event.modifiers);
				mouseupthread4.start();
				return true;

			case 6: // '\006'
				event.target = ez_ende;
				break;
			}
		} else {
			if (event.target == stepStop) {
				if (va.stepping) {
					va.evRec.recordEvent(12, 0);
					va.stepper.stop_replay();
				}
				return true;
			}
			if (event.target == stepBegin) {
				if (va.stepping) {
					va.evRec.recordEvent(12, 1);
					va.stepper.to_first();
				}
				return true;
			}
			if (event.target == stepOneBack) {
				if (va.stepping) {
					va.evRec.recordEvent(12, 2);
					va.stepper.step_one_back();
				}
				return true;
			}
			if (event.target == stepPlay) {
				if (va.stepping) {
					va.evRec.recordEvent(12, 3);
					va.stepper.start_replay();
				}
				return true;
			}
			if (event.target == stepOne) {
				if (va.stepping) {
					va.evRec.recordEvent(12, 4);
					va.stepper.single_step();
				}
				return true;
			}
			if (event.target == stepEnd) {
				if (va.stepping) {
					va.evRec.recordEvent(12, 5);
					va.stepper.to_last();
				}
				return true;
			}
		}
		return super.action(event, obj);
	}

	public static final int SM_STOP = 0;
	public static final int SM_BEGIN = 1;
	public static final int SM_ONE_BACK = 2;
	public static final int SM_AUTO = 3;
	public static final int SM_ONE = 4;
	public static final int SM_END = 5;
	public static final int SM_QUIT = 6;
	VoronoiApplet va;
	public ajLed ez_led;
	ajButton stepBegin;
	ajButton stepEnd;
	ajButton stepOne;
	ajButton stepOneBack;
	public ajButton stepPlay;
	ajButton stepStop;
	public SpeedSlider slider;
	public Button ez_ende;
}
