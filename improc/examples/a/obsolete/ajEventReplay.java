package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajRecorder.java

import java.util.Date;


public class ajEventReplay implements ajAnimation {

	public ajEventReplay(ajRecorder ajrecorder) {
		zero_t = (new Date()).getTime();
		rec = ajrecorder;
		rec.reclist = rec.recorded[rec.selected_record];
	}

	public void do_it(long l) {
		for (; rec.reclist != null && rec.reclist.timeStamp < l; rec.reclist = rec.reclist.nextEvent)
			rec.postAjEvent(rec.reclist);

		if (rec.reclist == null)
			rec.remove(this);
	}

	long zero_t;
	private ajRecorder rec;
}
