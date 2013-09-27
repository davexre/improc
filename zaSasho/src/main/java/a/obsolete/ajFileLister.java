package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ajFileLister.java

import java.awt.Button;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;


public class ajFileLister extends Frame implements Runnable {

	public ajFileLister(String s, Thread thread, StringBuffer stringbuffer, String s1, FilenameFilter filenamefilter)
			throws IOException {
		this(s, thread, stringbuffer, s1, filenamefilter, false);
	}

	public ajFileLister(String s, Thread thread, StringBuffer stringbuffer, String s1, FilenameFilter filenamefilter,
			boolean flag) throws IOException {
		super("File Selector");
		finished = false;
		selected = stringbuffer;
		stringbuffer.setLength(0);
		suff = s;
		directory = s1;
		filter = filenamefilter;
		listener = thread;
		allow_new = flag;
	}

	public void start() {
		setLayout(new GridBagLayout());
		list = new List(12, false);
		infoarea = new TextField();
		dir_name = new TextField();
		infoarea.setEditable(false);
		buttons = new Panel();
		parent = new Button("chdir ..");
		cancel = new Button("Cancel");
		accept = new Button("Accept");
		buttons.add(parent);
		buttons.add(cancel);
		buttons.add(accept);
		LayoutTools.gridBagConstrain(this, dir_name, 1, 0, 1, 1, 2, 10, 1.0D, 0.0D, 0, 0, 0, 0);
		LayoutTools.gridBagConstrain(this, list, 1, 1, 1, 1, 2, 10, 1.0D, 0.0D, 0, 0, 0, 0);
		LayoutTools.gridBagConstrain(this, buttons, 1, 2, 1, 1, 2, 10, 1.0D, 0.0D, 0, 0, 0, 0);
		new_name = new TextField("", 80);
		new_name.setEditable(allow_new);
		LayoutTools.gridBagConstrain(this, new_name, 1, 3, 1, 1, 2, 10, 1.0D, 0.0D, 0, 0, 0, 0);
		LayoutTools.gridBagConstrain(this, infoarea, 1, 4, 1, 1, 2, 10, 1.0D, 0.0D, 0, 0, 0, 0);
		resize(550, 400);
		show();
		try {
			list_directory(directory);
		} catch (IOException _ex) {
			finished = true;
			stop();
		}
		if (thr == null) {
			thr = new Thread(this);
			thr.start();
		}
	}

	public void run() {
		while (!finished)
			try {
				Thread.sleep(200L);
			} catch (InterruptedException _ex) {
			}
	}

	public synchronized void stop() {
		hide();
		if (thr != null && thr.isAlive()) {
			synchronized (listener) {
				listener.notify();
			}
			thr.stop();
		}
		thr = null;
	}

	public void list_directory(String s) throws IOException {
		File file = new File(s);
		if (!file.isDirectory())
			throw new IllegalArgumentException("ajFileLister: no such directory");
		list.clear();
		cwd = file;
		dir_name.setText(s);
		entries = cwd.list(filter);
		for (int i = 0; i < entries.length; i++)
			list.addItem(entries[i]);

	}

	public void show_info(String s) throws IOException {
		File file = new File(cwd, s);
		if (!file.exists())
			throw new IllegalArgumentException("ajFileLister.showInfo(): no such file or directory");
		String s1;
		if (file.isDirectory())
			s1 = "Directory: ";
		else
			s1 = "File: ";
		s1 += s + " ";
		s1 += (file.canRead() ? "r" : " ") + " " + (file.canWrite() ? "w" : " ") + " " + file.length() + " "
				+ new java.util.Date(file.lastModified());
		infoarea.setText(s1);
	}

	public boolean handleEvent(Event event) {
		if (event.target == accept) {
			hide();
			finished = true;
			stop();
			return true;
		}
		if (event.target == cancel) {
			selected.setLength(0);
			hide();
			finished = true;
			stop();
			return true;
		}
		if (event.target == parent) {
			String s = cwd.getParent();
			if (s == null)
				s = "/";
			try {
				list_directory(s);
			} catch (IllegalArgumentException _ex) {
				infoarea.setText("Already at top");
			} catch (IOException _ex) {
				infoarea.setText("I/O Error");
			}
			return true;
		}
		if (event.target == list) {
			if (event.id == 701)
				try {
					String s1 = entries[((Integer) event.arg).intValue()];
					File file1 = new File(s1);
					show_info(s1);
					if (file1.isFile()) {
						new_name.setText(file1.getAbsolutePath());
						selected.setLength(0);
						selected.append(file1.getAbsolutePath());
					}
				} catch (IOException _ex) {
					infoarea.setText("I/O Error");
				}
			else if (event.id == 1001)
				try {
					String s2 = (new File(cwd, (String) event.arg)).getAbsolutePath();
					try {
						list_directory(s2);
					} catch (IllegalArgumentException _ex) {
						selected.setLength(0);
						selected.append(s2);
						finished = true;
						hide();
						stop();
					}
				} catch (IOException _ex) {
					infoarea.setText("I/O Error");
				}
			return true;
		}
		if (event.target == new_name && event.id == 1001)
			try {
				File file = null;
				try {
					file = new File((String) event.arg);
				} catch (NullPointerException _ex) {
					System.out.println(cwd + "[" + event.arg + "]");
					file = new File(cwd, (String) event.arg);
				}
				String s3 = file.getAbsolutePath();
				file = new File(s3);
				try {
					list_directory(file.getParent());
				} catch (IllegalArgumentException _ex) {
					infoarea.setText("no directory " + file.getParent());
				}
				if (!file.exists() || file.isFile() && file.canWrite()) {
					selected.setLength(0);
					selected.append(s3);
					if (!s3.endsWith(suff))
						selected.append(suff);
					new_name.setText(selected.toString());
				} else {
					new_name.setText("");
					if (file.isDirectory())
						list_directory(s3);
					else
						infoarea.setText("not a valid filename.");
				}
			} catch (IOException _ex) {
				infoarea.setText("I/O Error");
			}
		return super.handleEvent(event);
	}

	private Thread thr;
	private StringBuffer selected;
	private List list;
	private TextField infoarea;
	private TextField new_name;
	private TextField dir_name;
	private Panel buttons;
	private Button parent;
	private Button cancel;
	private Button accept;
	private FilenameFilter filter;
	private File cwd;
	private String entries[];
	private boolean finished;
	private String directory;
	private Thread listener;
	private boolean allow_new;
	private String suff;
}
