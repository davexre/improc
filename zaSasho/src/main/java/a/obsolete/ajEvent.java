package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajEvent.java

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ajEvent {

	public ajEvent(int i) {
		barg = false;
		eventType = i;
	}

	public void write(DataOutputStream dataoutputstream) {
		try {
			dataoutputstream.writeInt(eventType);
			dataoutputstream.writeLong(timeStamp);
			dataoutputstream.writeInt(arg1);
			dataoutputstream.writeInt(arg2);
			dataoutputstream.writeInt(arg3);
			dataoutputstream.writeBoolean(barg);
			dataoutputstream.flush();
			return;
		} catch (IOException _ex) {
			System.err.println("Write failed.");
		}
	}

	public static ajEvent read(DataInputStream datainputstream) {
		ajEvent ajevent = null;
		try {
			int i = datainputstream.readInt();
			ajevent = new ajEvent(i);
			ajevent.timeStamp = datainputstream.readLong();
			ajevent.arg1 = datainputstream.readInt();
			ajevent.arg2 = datainputstream.readInt();
			ajevent.arg3 = datainputstream.readInt();
			ajevent.barg = datainputstream.readBoolean();
		} catch (IOException _ex) {
			ajevent = null;
		}
		return ajevent;
	}

	public static final int SET_POINT = 0;
	public static final int MOVE_POINT = 1;
	public static final int VIEWTYPE = 2;
	public static final int RESIZE = 3;
	public static final int DOUBLE_BUFFER = 4;
	public static final int SHOW_REST = 5;
	public static final int CLEAR = 6;
	public static final int RM_POINT = 7;
	public static final int END_REC = 8;
	public static final int FINISH_MOVE_POINT = 9;
	public static final int STEP_MODE = 10;
	public static final int STEP_SPEED = 11;
	public static final int STEP = 12;
	public static final int SET_CIRCLE = 13;
	public static final int RM_CIRCLE = 14;
	public static final int SET_MARK = 15;
	public static final int RM_MARK = 16;
	public int eventType;
	public long timeStamp;
	public int arg1;
	public int arg2;
	public int arg3;
	public boolean barg;
	public ajEvent nextEvent;
}
