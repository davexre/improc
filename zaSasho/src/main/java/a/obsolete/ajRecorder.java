package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajRecorder.java

import java.awt.Event;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import a.VoronoiApplet;
import a.ajPoint;

public class ajRecorder extends ajAnimator {

	public ajRecorder(VoronoiApplet voronoiapplet) {
		this(voronoiapplet, 30);
	}

	public ajRecorder(VoronoiApplet voronoiapplet, int i) {
		super(voronoiapplet, i);
		recfilename = "Voronoi.ani";
		SecurityAlert = false;
		save_op = false;
		selected = new StringBuffer();
		record = false;
		replay = false;
		recorded = new ajEvent[6];
		start_time = new long[6];
		va = voronoiapplet;
		zero_t = 0L;
		session_stamp = 0L;
		selected_record = 0;
		URL url = va.getCodeBase();
		String s = url.getProtocol();
		if (s.compareTo("http") == 0 || s.compareTo("HTTP") == 0) {
			System.out.println("Netbased applet. File storage disabled.");
			SecurityAlert = true;
			return;
		}
		try {
			File file = new File(url.getFile());
			String s1 = file.getParent();
			file = new File(s1);
			if (file.isDirectory()) {
				SecurityAlert = false;
				return;
			}
		} catch (SecurityException _ex) {
			System.err.println("Security Manager restricts file operations. \n");
			SecurityAlert = true;
			return;
		} catch (NullPointerException _ex) {
			return;
		}
	}

	public boolean hasRecord() {
		return recorded[selected_record] != null;
	}

	public void save_all(String s) {
		int i = selected_record;
		for (selected_record = 0; selected_record < 6; selected_record++)
			if (hasRecord())
				save_record(s);

		selected_record = i;
	}

	public void save_record(String s) {
		String s1 = s.concat(".").concat(String.valueOf(selected_record)).concat(".")
				.concat(String.valueOf(start_time[selected_record])).concat(".ani");
		save_op = true;
		selected.setLength(0);
		selected.append((new File(va.getCodeBase().getFile())).getParent());
		selected.append(File.separator);
		selected.append(s1);
		load_save();
	}

	public void save(String s) {
		DataOutputStream dataoutputstream = null;
		if (replay)
			stop_replay();
		if (record)
			stop_recording();
		try {
			dataoutputstream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(s)));
		} catch (IOException _ex) {
			System.out.println("FileOutputStream creation for " + s + " failed.");
			return;
		} catch (NullPointerException _ex) {
			System.out.println("DataOutputStream creation for " + s + " failed.");
			return;
		}
		try {
			dataoutputstream.writeUTF("VANI");
		} catch (IOException _ex) {
		}
		for (ajEvent ajevent = recorded[selected_record]; ajevent != null; ajevent = ajevent.nextEvent)
			ajevent.write(dataoutputstream);

		try {
			dataoutputstream.flush();
			dataoutputstream.close();
		} catch (IOException _ex) {
		}
		dataoutputstream = null;
	}

	public void load(URLConnection urlconnection) {
		DataInputStream datainputstream = null;
		if (record)
			stop_recording();
		if (replay)
			stop_replay();
		java.io.InputStream inputstream = null;
		try {
			inputstream = urlconnection.getInputStream();
		} catch (IOException _ex) {
			System.out.println("Could not open URLConnection InputStream for " + urlconnection + ".");
			return;
		}
		BufferedInputStream bufferedinputstream = null;
		try {
			bufferedinputstream = new BufferedInputStream(inputstream);
		} catch (Exception _ex) {
			System.out.println("BufferedInputStream creation failed on " + inputstream + ".");
			return;
		}
		try {
			datainputstream = new DataInputStream(bufferedinputstream);
		} catch (Exception _ex) {
			System.out.println("DataInputStream creation failed on " + bufferedinputstream + ".");
			return;
		}
		String s = null;
		try {
			s = datainputstream.readUTF();
		} catch (IOException _ex) {
			System.out.println("Read failed on input stream");
		}
		if (s == null || s.compareTo("VANI") != 0) {
			System.out.println("File Format Ident string missing: " + s + ".");
			try {
				datainputstream.close();
			} catch (IOException _ex) {
			}
			datainputstream = null;
			return;
		}
		recorded[selected_record] = null;
		ajEvent ajevent = recorded[selected_record];
		for (ajEvent ajevent1 = null; (ajevent1 = ajEvent.read(datainputstream)) != null;) {
			if (ajevent == null)
				recorded[selected_record] = ajevent1;
			else
				ajevent.nextEvent = ajevent1;
			ajevent = ajevent1;
		}

		try {
			datainputstream.close();
		} catch (IOException _ex) {
		}
		datainputstream = null;
	}

	public void start_recording() {
		if (replay)
			stop_replay();
		Date date = new Date();
		zero_t = date.getTime();
		record = true;
		recorded[selected_record] = null;
		start_time[selected_record] = zero_t - session_stamp;
	}

	public void stop_recording() {
		if (!record) {
			return;
		} else {
			record = false;
			return;
		}
	}

	public void start_replay() {
		if (record)
			stop_recording();
		record = false;
		replay = true;
		rep = new ajEventReplay(this);
		add(rep);
		start();
	}

	public void stop_replay() {
		if (!replay)
			return;
		replay = false;
		if (rep != null)
			remove(rep);
		rep = null;
		stop();
	}

	private void _recordEvent(ajEvent ajevent) {
		Date date = new Date();
		ajevent.timeStamp = date.getTime() - zero_t;
		if (recorded[selected_record] == null) {
			rl = ajevent;
			recorded[selected_record] = ajevent;
		} else {
			rl.nextEvent = ajevent;
		}
		rl = ajevent;
	}

	public void postAjEvent(ajEvent ajevent) {
		Event event = new Event(this, ajevent.timeStamp + zero_t, 1001, ajevent.arg1, ajevent.arg2, ajevent.arg3,
				ajevent.barg ? 1 : 0, ajevent);
		super.host.deliverEvent(event);
	}

	public void recordEvent(int i, ajPoint ajpoint) {
		if (!record) {
			return;
		} else {
			ajEvent ajevent = new ajEvent(i);
			ajevent.arg1 = (int) ajpoint.x;
			ajevent.arg2 = (int) ajpoint.y;
			ajevent.arg3 = ((ajElement) (ajpoint)).ident;
			_recordEvent(ajevent);
			return;
		}
	}

	public void recordEvent(int i, int j, int k) {
		if (!record) {
			return;
		} else {
			ajEvent ajevent = new ajEvent(i);
			ajevent.arg1 = j;
			ajevent.arg2 = k;
			_recordEvent(ajevent);
			return;
		}
	}

	public void recordEvent(int i, int j) {
		if (!record) {
			return;
		} else {
			ajEvent ajevent = new ajEvent(i);
			ajevent.arg1 = j;
			_recordEvent(ajevent);
			return;
		}
	}

	public void recordEvent(int i, boolean flag) {
		if (!record) {
			return;
		} else {
			ajEvent ajevent = new ajEvent(i);
			ajevent.barg = flag;
			_recordEvent(ajevent);
			return;
		}
	}

	public void recordEvent(int i) {
		if (!record) {
			return;
		} else {
			_recordEvent(new ajEvent(i));
			return;
		}
	}

	void do_load() {
		save_op = false;
		if (!SecurityAlert) {
			directory = System.getProperty("user.dir");
			extensionFilter extensionfilter = new extensionFilter("ani");
			listerWaitThread listerwaitthread = new listerWaitThread(va);
			try {
				lister = new ajFileLister(".ani", listerwaitthread, selected, directory, extensionfilter);
			} catch (IOException _ex) {
			}
			va.disable();
			lister.start();
			listerwaitthread.start();
			return;
		} else {
			selected.setLength(0);
			selected.append((new File(va.getCodeBase().getFile())).getParent());
			selected.append(File.separator);
			selected.append(recfilename);
			load_save();
			return;
		}
	}

	void do_save() {
		save_op = true;
		if (!SecurityAlert) {
			directory = System.getProperty("user.dir");
			extensionFilter extensionfilter = new extensionFilter("ani");
			listerWaitThread listerwaitthread = new listerWaitThread(va);
			try {
				lister = new ajFileLister(".ani", listerwaitthread, selected, directory, extensionfilter, true);
			} catch (IOException _ex) {
			}
			va.disable();
			lister.start();
			listerwaitthread.start();
			return;
		} else {
			selected.setLength(0);
			selected.append((new File(va.getCodeBase().getFile())).getParent());
			selected.append(File.separator);
			selected.append(recfilename);
			load_save();
			return;
		}
	}

	public synchronized void load_save() {
		if (selected.length() == 0)
			return;
		stop_replay();
		stop_recording();
		if (save_op) {
			save(selected.toString());
		} else {
			URL url = va.getCodeBase();
			URL url1 = null;
			try {
				url1 = new URL(url, selected.toString());
			} catch (MalformedURLException _ex) {
				System.err.println("Malformed URL: " + selected.toString());
				selected.setLength(0);
			}
			URLConnection urlconnection = null;
			try {
				urlconnection = url1.openConnection();
			} catch (IOException _ex) {
				System.out.println("no URLConnection to file " + url.getProtocol() + url.getHost() + selected);
				selected.setLength(0);
			}
			urlconnection.setDoInput(true);
			load(urlconnection);
			if (hasRecord())
				va.recPanel.rec_loaded();
			else
				va.recPanel.no_rec_loaded();
		}
		selected.setLength(0);
	}

	public void set_selected(int i) {
		if (i < recorded.length && i >= 0)
			selected_record = i;
	}

	public void inc_selected() {
		if (selected_record < recorded.length - 1)
			selected_record++;
	}

	public void set_session_stamp() {
		Date date = new Date();
		session_stamp = date.getTime();
	}

	VoronoiApplet va;
	ajFileLister lister;
	String recfilename;
	boolean SecurityAlert;
	boolean fileAccess;
	public boolean save_op;
	public StringBuffer selected;
	String directory;
	public long session_stamp;
	public long zero_t;
	public boolean record;
	public boolean replay;
	private ajEventReplay rep;
	public ajEvent recorded[];
	public long start_time[];
	public ajEvent reclist;
	public int selected_record;
	ajEvent rl;
}
