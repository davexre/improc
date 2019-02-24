package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ajRecorderPanel.java

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;

import a.VoronoiApplet;

public class ajRecorderPanel extends Panel {

	public ajRecorderPanel(VoronoiApplet voronoiapplet, ajButtonImages ajbuttonimages) {
		va = voronoiapplet;
		setLayout(new GridBagLayout());
		rec_led = new ajLed();
		rec_led.basecolor = Color.green;
		rec_led.duration = 400L;
		rec_led.switch_off();
		record = new ajButton(1, ajbuttonimages.recordOffImg, ajbuttonimages.recordOnImg);
		stop = new ajButton(0, ajbuttonimages.stopOffImg, ajbuttonimages.stopOnImg);
		play = new ajButton(1, ajbuttonimages.playOffImg, ajbuttonimages.playOnImg);
		pause = new ajButton(1, ajbuttonimages.pauseOffImg, ajbuttonimages.pauseOnImg);
		load = new ajButton(0, ajbuttonimages.loadOffImg, ajbuttonimages.loadOnImg);
		save = new ajButton(0, ajbuttonimages.saveOffImg, ajbuttonimages.saveOnImg);
		firstrec = new Button("Erste Aufnahme");
		firstrec.setFont(new Font("Helvetica", 1, 14));
		nextrec = new Button("N\344chste Aufnahme");
		nextrec.setFont(new Font("Helvetica", 1, 14));
		stoprec = new Button("Aufnahme beenden");
		stoprec.setFont(new Font("Helvetica", 1, 14));
		recinfo = new Label("Aufnahme - von 6 (nicht aktiv)", 0);
		recinfo.setFont(new Font("Helvetica", 1, 14));
		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(1, 15, 15));
		CheckboxGroup checkboxgroup = new CheckboxGroup();
		slot0 = new Checkbox("Slot #0", checkboxgroup, true);
		slot1 = new Checkbox("Slot #1", checkboxgroup, false);
		slot2 = new Checkbox("Slot #2", checkboxgroup, false);
		slot3 = new Checkbox("Slot #3", checkboxgroup, false);
		slot4 = new Checkbox("Slot #4", checkboxgroup, false);
		slot5 = new Checkbox("Slot #5", checkboxgroup, false);
		panel.add(slot0);
		panel.add(slot1);
		panel.add(slot2);
		panel.add(slot3);
		panel.add(slot4);
		panel.add(slot5);
		String s = null; //va.getParameter("AOFRecord");
		if (s != null && s.compareTo("true") == 0) {
			LayoutTools.gridBagConstrain(this, firstrec, 0, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 15, 5, 5);
			LayoutTools.gridBagConstrain(this, nextrec, 1, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 15, 5, 5);
			LayoutTools.gridBagConstrain(this, stoprec, 2, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 15, 5, 5);
			LayoutTools.gridBagConstrain(this, recinfo, 0, 2, 1, 1, 0, 17, 1.0D, 0.0D, 15, 15, 5, 5);
			return;
		}
		LayoutTools.gridBagConstrain(this, rec_led, 0, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 15, 5, 5);
		LayoutTools.gridBagConstrain(this, record, 1, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 15, 5, 5);
		LayoutTools.gridBagConstrain(this, play, 2, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 5, 5, 5);
		LayoutTools.gridBagConstrain(this, pause, 3, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 5, 5, 5);
		LayoutTools.gridBagConstrain(this, stop, 4, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 5, 5, 5);
		LayoutTools.gridBagConstrain(this, load, 5, 1, 1, 1, 0, 10, 0.0D, 0.0D, 15, 15, 5, 5);
		if (!va.evRec.SecurityAlert)
			LayoutTools.gridBagConstrain(this, save, 6, 1, 1, 1, 2, 17, 1.0D, 0.0D, 15, 5, 5, 5);
		LayoutTools.gridBagConstrain(this, panel, 0, 2, 0, 1, 0, 10, 0.0D, 0.0D, 15, 15, 5, 5);
	}

	public boolean action(Event event, Object obj) {
		if (event.target == record) {
			va.do_record();
			return true;
		}
		if (event.target == stop || event.target == stoprec) {
			va.do_stop();
			return true;
		}
		if (event.target == play) {
			va.do_replay();
			return true;
		}
		if (event.target == pause) {
			va.do_pause();
			return true;
		}
		if (event.target == firstrec) {
			va.do_stop();
			va.clear_canvas();
			slot_off(va.evRec.selected_record);
			va.evRec.selected_record = 0;
			slot_on(0);
			nextrec.enable();
			va.do_record();
			return true;
		}
		if (event.target == nextrec) {
			va.do_stop();
			va.clear_canvas();
			slot_off(va.evRec.selected_record);
			va.evRec.inc_selected();
			slot_on(va.evRec.selected_record);
			if (va.evRec.selected_record == 5)
				nextrec.disable();
			va.do_record();
			return true;
		}
		if (event.target == load) {
			postEvent(new Event(this, 0L, 1001, 0, 0, 1, 0));
			load.switch_off();
			return true;
		}
		if (event.target == save) {
			postEvent(new Event(this, 0L, 1001, 0, 0, 2, 0));
			save.switch_off();
			return true;
		}
		if (event.target == this && event.key == 1) {
			va.evRec.do_load();
			return true;
		}
		if (event.target == this && event.key == 2) {
			va.evRec.do_save();
			return true;
		}
		if (event.target == slot0) {
			va.evRec.set_selected(0);
			if (va.evRec.recorded[va.evRec.selected_record] != null)
				rec_loaded();
			else
				no_rec_loaded();
			return true;
		}
		if (event.target == slot1) {
			va.evRec.set_selected(1);
			if (va.evRec.recorded[va.evRec.selected_record] != null)
				rec_loaded();
			else
				no_rec_loaded();
			return true;
		}
		if (event.target == slot2) {
			va.evRec.set_selected(2);
			if (va.evRec.recorded[va.evRec.selected_record] != null)
				rec_loaded();
			else
				no_rec_loaded();
			return true;
		}
		if (event.target == slot3) {
			va.evRec.set_selected(3);
			if (va.evRec.recorded[va.evRec.selected_record] != null)
				rec_loaded();
			else
				no_rec_loaded();
			return true;
		}
		if (event.target == slot4) {
			va.evRec.set_selected(4);
			if (va.evRec.recorded[va.evRec.selected_record] != null)
				rec_loaded();
			else
				no_rec_loaded();
			return true;
		}
		if (event.target == slot5) {
			va.evRec.set_selected(5);
			if (va.evRec.recorded[va.evRec.selected_record] != null)
				rec_loaded();
			else
				no_rec_loaded();
			return true;
		} else {
			return super.action(event, obj);
		}
	}

	public void end_record() {
		record.switch_off();
		pause.switch_off();
		play.switch_off();
	}

	public void stop_rec() {
		end_record();
		recinfo.setText("Aufnahme - von 6 (nicht aktiv)");
		va.frame.demo.setState(false);
		load.enable();
		save.enable();
	}

	public void do_replay() {
		record.switch_off();
		pause.switch_off();
		play.switch_on();
		load.disable();
		save.disable();
	}

	public void do_record() {
		record.switch_on();
		pause.switch_off();
		play.switch_off();
		recinfo.setText("Aufnahme " + (va.evRec.selected_record + 1) + " von 6");
		load.disable();
		save.disable();
		repaint();
	}

	public void rec_loaded() {
		play.enable();
		save.enable();
		rec_led.switch_on();
		repaint();
	}

	public void no_rec_loaded() {
		save.disable();
		rec_led.switch_off();
		repaint();
	}

	public void clear_record() {
		save.disable();
		rec_led.switch_off();
		repaint();
	}

	public void slot_on(int i) {
		switch (i) {
		case 0: // '\0'
			slot0.setState(true);
			break;

		case 1: // '\001'
			slot1.setState(true);
			break;

		case 2: // '\002'
			slot2.setState(true);
			break;

		case 3: // '\003'
			slot3.setState(true);
			break;

		case 4: // '\004'
			slot4.setState(true);
			break;

		case 5: // '\005'
			slot5.setState(true);
			break;
		}
		if (va.evRec.recorded[va.evRec.selected_record] != null) {
			rec_loaded();
			return;
		} else {
			no_rec_loaded();
			return;
		}
	}

	public void slot_off(int i) {
		switch (i) {
		case 0: // '\0'
			slot0.setState(false);
			return;

		case 1: // '\001'
			slot1.setState(false);
			return;

		case 2: // '\002'
			slot2.setState(false);
			return;

		case 3: // '\003'
			slot3.setState(false);
			return;

		case 4: // '\004'
			slot4.setState(false);
			return;

		case 5: // '\005'
			slot5.setState(false);
			return;
		}
	}

	VoronoiApplet va;
	public ajLed rec_led;
	ajButton record;
	ajButton play;
	ajButton stop;
	ajButton pause;
	public ajButton load;
	public ajButton save;
	Button firstrec;
	Button nextrec;
	Button stoprec;
	Checkbox slot0;
	Checkbox slot1;
	Checkbox slot2;
	Checkbox slot3;
	Checkbox slot4;
	Checkbox slot5;
	Label recinfo;
}
