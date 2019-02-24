package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   socketListenerThread.java

import java.awt.Event;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import a.VoronoiApplet;

public class socketListenerThread extends Thread {

	public socketListenerThread(VoronoiApplet voronoiapplet) {
		rcvPort = 12987;
		vapp = voronoiapplet;
		buffer = new byte[1024];
		try {
			//dsock = new Socket(vapp.getCodeBase().getHost(), rcvPort);
			inStream = new DataInputStream(dsock.getInputStream());
			outStream = new DataOutputStream(dsock.getOutputStream());
			return;
		} catch (IOException _ex) {
			return;
		} catch (NullPointerException _ex) {
		}
		if (dsock == null)
			System.err.println("Could not connect to remote host");
	}

	public synchronized void run() {
		do {
			String s = null;
			try {
				try {
					s = inStream.readUTF();
				} catch (IOException _ex) {
				}
			} catch (NullPointerException _ex) {
				fail_counter++;
				if (fail_counter < 3) {
					(new socketListenerThread(vapp)).start();
					return;
				} else {
					System.err.println("Socket connection failed. Seems like VA_Server is not running on remote host.");
					return;
				}
			}
			if (!VA_Message.check_length(s)) {
				System.err.println("Message corrupted: " + s);
			} else {
				String s1 = VA_Message.get_message(s);
				String s2 = VA_Message.get_cmd(s1);
				if (s2.compareTo("REGISTER") == 0)
					System.out.println("VA_Server Registration #" + VA_Message.get_arg(s1));
				else if (s2.compareTo("START") == 0) {
					String s3 = VA_Message.get_arg(s1);
					synchronized (vapp) {
						if (vapp.recPanel.load.isEnabled()) {
							vapp.evRec.save_op = false;
							vapp.evRec.selected.setLength(0);
							//vapp.evRec.selected.append((new File(vapp.getCodeBase().getFile())).getParent());
							vapp.evRec.selected.append(File.separator);
							vapp.evRec.selected.append(s3);
							vapp.evRec.load_save();
							Event event5 = new Event(vapp.recPanel.play, 1001, null);
							vapp.recPanel.postEvent(event5);
						}
					}
				} else if (s2.compareTo("LOAD") == 0) {
					String s4 = VA_Message.get_arg(s1);
					synchronized (vapp) {
						if (vapp.recPanel.load.isEnabled()) {
							vapp.evRec.save_op = false;
							vapp.evRec.selected.setLength(0);
//							vapp.evRec.selected.append((new File(vapp.getCodeBase().getFile())).getParent());
							vapp.evRec.selected.append(File.separator);
							vapp.evRec.selected.append(s4);
							vapp.evRec.load_save();
						}
					}
				} else if (s2.compareTo("LOADSLOT") == 0) {
					String s5 = VA_Message.get_arg(s1);
					String s9 = VA_Message.get_cmd(s5);
					s5 = VA_Message.get_arg(s5);
					int j = Integer.parseInt(s9);
					synchronized (vapp) {
						int k = vapp.evRec.selected_record;
						vapp.recPanel.slot_off(vapp.evRec.selected_record);
						vapp.evRec.selected_record = j;
						vapp.recPanel.slot_on(vapp.evRec.selected_record);
						if (vapp.recPanel.load.isEnabled()) {
							vapp.evRec.save_op = false;
							vapp.evRec.selected.setLength(0);
//							vapp.evRec.selected.append((new File(vapp.getCodeBase().getFile())).getParent());
							vapp.evRec.selected.append(File.separator);
							vapp.evRec.selected.append(s5);
							vapp.evRec.load_save();
						}
						vapp.evRec.selected_record = k;
					}
				} else if (s2.compareTo("SAVE") == 0) {
					String s6 = VA_Message.get_arg(s1);
					synchronized (vapp) {
						if (vapp.recPanel.load.isEnabled()) {
							vapp.evRec.save_op = true;
							vapp.evRec.selected.setLength(0);
//							vapp.evRec.selected.append((new File(vapp.getCodeBase().getFile())).getParent());
							vapp.evRec.selected.append(File.separator);
							vapp.evRec.selected.append(s6);
							vapp.evRec.load_save();
						}
					}
				} else if (s2.compareTo("SAVEALL") == 0) {
					String s7 = VA_Message.get_arg(s1);
					synchronized (vapp) {
						if (vapp.recPanel.load.isEnabled())
							vapp.evRec.save_all(s7);
					}
				} else if (s2.compareTo("STOP") == 0) {
					Event event = new Event(vapp.recPanel.stop, 1001, null);
					vapp.recPanel.postEvent(event);
				} else if (s2.compareTo("STAMP") == 0)
					vapp.evRec.set_session_stamp();
				else if (s2.compareTo("PLAY") == 0) {
					Event event1 = new Event(vapp.recPanel.stop, 1001, null);
					vapp.recPanel.postEvent(event1);
					event1 = new Event(vapp.recPanel.play, 1001, null);
					vapp.recPanel.deliverEvent(event1);
				} else if (s2.compareTo("PLAYSLOT") == 0) {
					String s8 = VA_Message.get_arg(s1);
					int i = Integer.parseInt(s8);
					vapp.recPanel.slot_off(vapp.evRec.selected_record);
					vapp.evRec.selected_record = i;
					vapp.recPanel.slot_on(vapp.evRec.selected_record);
					Event event4 = new Event(vapp.recPanel.stop, 1001, null);
					vapp.recPanel.postEvent(event4);
					event4 = new Event(vapp.recPanel.play, 1001, null);
					vapp.recPanel.deliverEvent(event4);
				} else if (s2.compareTo("CLEAR") == 0) {
					Event event2 = new Event(vapp.frame.clr, 1001, null);
					vapp.frame.postEvent(event2);
				} else if (s2.compareTo("REC") == 0) {
					Event event3 = new Event(vapp.recPanel.record, 1001, null);
					vapp.recPanel.deliverEvent(event3);
				}
			}
		} while (true);
	}

	public void finalize() {
		try {
			inStream.close();
			outStream.close();
			dsock.close();
			return;
		} catch (IOException _ex) {
			return;
		}
	}

	VoronoiApplet vapp;
	Socket dsock;
	byte buffer[];
	DataInputStream inStream;
	DataOutputStream outStream;
	static int fail_counter;
	public int rcvPort;
}
