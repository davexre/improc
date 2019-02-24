package a;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   VoronoiApplet.java

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JPanel;

import a.obsolete.ajButtonImages;
import a.obsolete.ajElement;
import a.obsolete.ajEvent;
import a.obsolete.ajFrame;
import a.obsolete.ajRecorder;
import a.obsolete.ajRecorderPanel;
import a.obsolete.ajSegment;
import a.obsolete.ajSingleStepPanel;
import a.obsolete.ajStep;
import a.obsolete.ajUpdateable;
import a.obsolete.socketListenerThread;

public class VoronoiApplet extends JPanel implements ajUpdateable {

	void addPoint(int x, int y) {
		ajPoint p = new ajPoint(x, y);
		delau.insertPoint(p);
		canvas.add(p);
	}

	public VoronoiApplet() throws IOException {
		hasPrint = false;
		stepMode = false;
		stepping = false;
		stopped = false;

		String s = null;		//getParameter("FrameWidth");
		String s1 = null;		//getParameter("FrameHeight");
		String s2 = null;		//getParameter("Panels");
		String s3 = null;		//getParameter("RemoteControl");
		String s4 = null;		//getParameter("FrameShow");
		String s5 = null;		//getParameter("Color");
								//getParameter("unBuf");
		String s6 = null;		//getParameter("Info");
		helpURL = null;			//getParameter("HelpURL");
		if (helpURL == null)
			helpURL = "VA_UserManual.html";
		String s7 = System.getProperty("java.version");
		hasPrint = !s7.startsWith("1.0");
		if (s6 != null && s6.compareTo("true") == 0) {
			String s8 = System.getProperty("java.vendor");
			String s9 = System.getProperty("java.vendor.url");
			String s10 = System.getProperty("java.class.version");
			String s12 = System.getProperty("os.name");
			String s13 = System.getProperty("os.arch");
			String s14 = System.getProperty("os.version");
			System.err.println("Java Environment Info");
			System.err.println("Java");
			System.err.println("     Version:        " + s7);
					System.err.println("     Vendor:         " + s8);
			System.err.println("     Vendor URL:     " + s9);
			System.err.println("     API Version:    " + s10);
			System.err.println("OS");
			System.err.println("   Name:             " + s12);
			System.err.println("   Architecture:     " + s13);
			System.err.println("   Version:     " + s14);
		}
		try {
			f_w = (new Integer(s)).intValue();
		} catch (NumberFormatException _ex) {
			f_w = 800;
		}
		try {
			f_h = (new Integer(s1)).intValue();
		} catch (NumberFormatException _ex) {
			f_h = 800;
		}
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		if (f_w > dimension.width)
			f_w = (int) ((double) (float) dimension.width * 0.80000000000000004D);
		if (f_h > dimension.height)
			f_h = (int) ((double) (float) dimension.height * 0.80000000000000004D);
		frame = new ajFrame(this, "VoroGlide 2.2", f_w, f_h);
		evRec = new ajRecorder(this);
		canvas = new ajCanvas();
		canvas.animate.sleepTime = 30;
		canvas.resize(size());
		setLayout(new BorderLayout());
		frame.add("Center", canvas);
		canvas.unsetOffscreen();
		delau = new ajExtendDelaunay(canvas);
/*
		addPoint(100, 100);
		addPoint(200, 200);
		addPoint(200, 100);
		addPoint(500, 100);
		addPoint(250, 200);
//		addPoint(210, 180);
		*/
		recFrame = new Frame("Recorder");
		stepFrame = new Frame("Step Mode");
		ajButtonImages ajbuttonimages = new ajButtonImages();
		stepPanel = new ajSingleStepPanel(this, ajbuttonimages);
		recPanel = new ajRecorderPanel(this, ajbuttonimages);
		stepFrame.add("Center", stepPanel);
		stepFrame.pack();
		recFrame.add("Center", recPanel);
		recFrame.pack();
		recPanel.save.disable();
		stepper = new ajStep(stepPanel, canvas);
		stepper.set_time(1000);
		canvas.add(delau);
		canvas.updateInterface = this;
		if (s3 != null && s3.compareTo("true") == 0) {
			socketListenerThread socketlistenerthread = new socketListenerThread(this);
			socketlistenerthread.start();
		}
		frame.pack();
		frame.resize(f_w, f_h);
		if (s4 != null && s4.compareTo("true") == 0) {
			frame.show();
			if (s2 != null && s2.compareTo("all") == 0) {
				recFrame.show();
				stepFrame.show();
			} else if (s2 != null && s2.compareTo("simple") == 0) {
				recFrame.hide();
				stepFrame.hide();
			} else {
				recFrame.hide();
				stepFrame.hide();
			}
		}
		if (s5 != null) {
			Color color = new Color(Integer.parseInt(s5, 16));
			if (color != null)
				setBackground(color);
		}
		setLayout(new BorderLayout(5, 5));
		String s11 = null;		//getParameter("ButtonText");
		if (s11 != null)
			sf = new Button(s11);
		else
			sf = new Button("Show Voronoi Frame");
		sf.setFont(new Font("Helvetica", 1, 14));
		add("Center", sf);
		show();
		setFrameViewtype(1);
		frame.unBuf.setState(false);
		unBufferedDisplay(frame.unBuf.getState());
		frame.thin.setState(false);
		thinLines(frame.thin.getState());
	}

	public void destroy() {
		recFrame.hide();
		stepFrame.hide();
		frame.hide();
		recFrame.dispose();
		stepFrame.dispose();
		frame.dispose();
	}

	public String getHelpURL() {
		return helpURL;
	}

	public void ajUpdate(Graphics g) {
		if (dragEvent == null || pick == null) {
			canvas.paint(g);
			return;
		} else {
			pick.move(dragEvent.x, dragEvent.y);
			evRec.recordEvent(1, pick);
			delau.insertTemp(pick);
			canvas.paintTempLayers();
			dragEvent = null;
			return;
		}
	}

	public boolean handleMouseDown(Event event, int i, int j) {
		if (pick != null || stepping)
			return true;
		pick = canvas.pickPoint(i, j);
		System.out.println("event.modifiers " + event.modifiers);
		if (event.modifiers == 2)
			if (pick == null) {
				return true;
			} else {
				evRec.recordEvent(7, pick);
				canvas.remove(pick);
				delau.recompute();
				canvas.repaint();
				pick = null;
				return true;
			}
		if (pick != null) {
			evRec.recordEvent(0, pick);
			canvas.remove(pick);
			delau.recompute();
			delau.insertTemp(pick);
			canvas.repaint();
			return true;
		}
		ajPoint ajpoint = new ajPoint(i, j);
		evRec.recordEvent(0, ajpoint);
		canvas.add(ajpoint);
		if (stepMode && delau.showDelaunayState()) {
			stepPanel.ez_led.switch_on();
			stepper.reset();
			stepping = true;
			step_point = ajpoint;
			delau.insertTemp(ajpoint, stepper);
			stepPanel.stepPlay.switch_on();
			stepper.begin();
		} else {
			delau.insertPoint(ajpoint);
			canvas.repaint();
		}
		return true;
	}

	public boolean handleMouseDrag(Event event, int i, int j) {
		if (pick == null || stepping)
			return true;
		if (i < 0)
			event.x = -1;
		else if (i >= canvas.width)
			event.x = canvas.width;
		if (j < 0)
			event.y = -1;
		else if (j >= canvas.height)
			event.y = canvas.height;
		if (canvas.pickPoint(event.x, event.y) != null) {
			return true;
		} else {
			dragEvent = event;
			canvas.repaint();
			return true;
		}
	}

	public boolean handleMouseUp(Event event, int i, int j) {
		if (stepping)
			return true;
		evRec.recordEvent(9, event.x, event.y);
		if (pick == null) {
			return true;
		} else {
			canvas.cleanTempLayers();
			pick.color = ajElement.pointColor;
			canvas.add(pick);
			delau.insertPoint(pick);
			delau.endMotion();
			canvas.repaint();
			pick = null;
			dragEvent = null;
			return true;
		}
	}

	public boolean handleMouseEnter(Event event, int i, int j) {
		return stepping;
	}

	public boolean handleMouseExit(Event event, int i, int j) {
		return stepping;
	}

	public boolean handleMouseMove(Event event, int i, int j) {
		return stepping;
	}

	public boolean handleKeyDown(Event event, int i) {
		return stepping;
	}

	public boolean handleKeyUp(Event event, int i) {
		return stepping;
	}

	public boolean keyDown(Event event, int i) {
		if (stepping)
			return true;
		if (i == 115) {
			stepFrame.show();
			return true;
		}
		if (i == 114) {
			recFrame.show();
			return true;
		}
		if (i == 97) {
			recFrame.show();
			stepFrame.show();
			return true;
		}
		if (i == 83) {
			stepFrame.hide();
			return true;
		}
		if (i == 82) {
			recFrame.hide();
			return true;
		}
		if (i == 65) {
			recFrame.hide();
			stepFrame.hide();
			return true;
		} else {
			return super.keyDown(event, i);
		}
	}

	public boolean action(Event event, Object obj) {
		if (event.target == evRec) {
			ajEvent ajevent = (ajEvent) event.arg;
			switch (ajevent.eventType) {
			case 0: // '\0'
				Event event1 = new Event(canvas, event.when, 501, ajevent.arg1, ajevent.arg2, 0, 0);
				canvas.postEvent(event1);
				return true;

			case 1: // '\001'
				Event event2 = new Event(canvas, event.when, 506, ajevent.arg1, ajevent.arg2, 0, 0);
				canvas.postEvent(event2);
				return true;

			case 9: // '\t'
				Event event3 = new Event(canvas, event.when, 502, ajevent.arg1, ajevent.arg2, 0, 0);
				canvas.postEvent(event3);
				return true;

			case 2: // '\002'
				setFrameViewtype(ajevent.arg1);
				return true;

			case 5: // '\005'
				return true;

			case 4: // '\004'
				frame.unBuf.setState(ajevent.barg);
				Event event4 = new Event(frame.unBuf, event.when, 1001, ajevent.arg1, ajevent.arg2, 0, 0);
				frame.postEvent(event4);
				return true;

			case 10: // '\n'
				frame.step.setState(ajevent.barg);
				Event event5 = new Event(frame.step, event.when, 1001, ajevent.arg1, ajevent.arg2, 0, 0);
				frame.postEvent(event5);
				return true;

			case 11: // '\013'
				stepPanel.slider.SetValue(ajevent.arg1);
				event.target = stepPanel.slider;
				break;

			case 12: // '\f'
				return stepPanel.action(event, obj);

			case 6: // '\006'
				Event event6 = new Event(frame.clr, event.when, 1001, ajevent.arg1, ajevent.arg2, 0, 0);
				frame.postEvent(event6);
				return true;

			case 3: // '\003'
				return true;

			case 7: // '\007'
				Event event7 = new Event(canvas, event.when, 501, ajevent.arg1, ajevent.arg2, 0, 4);
				canvas.postEvent(event7);
				return true;

			case 8: // '\b'
				//showStatus("Replay finished.");
				recPanel.stop_rec();
				return true;

			default:
				return true;
			}
		}
		if (event.target == stepPanel.ez_ende || event.target == stepper) {
			end_stepping(event.target == stepPanel.ez_ende, event.target == stepPanel.ez_ende);
			return true;
		}
		String s = null;		//getParameter("Panels");
		if (event.target == sf) {
			frame.show();
			if (s != null && s.compareTo("all") == 0) {
				recFrame.show();
				stepFrame.show();
			} else if (s != null && s.compareTo("simple") == 0) {
				recFrame.hide();
				stepFrame.hide();
			} else {
				recFrame.hide();
				stepFrame.hide();
			}
			return true;
		} else {
			return super.action(event, obj);
		}
	}

	public void do_stop() {
		if (clip != null)
			clip.stop();
		if (stopped) {
			stopped = false;
			evRec.start();
		}
		evRec.recordEvent(8);
		evRec.stop_recording();
		//showStatus("Stopped.");
		end_stepping(false, true);
		if (pick != null) {
			canvas.cleanTempLayers();
			pick.color = ajElement.pointColor;
			canvas.add(pick);
			delau.insertPoint(pick);
			delau.endMotion();
			canvas.repaint();
			pick = null;
			dragEvent = null;
		}
		frame.demo.setState(false);
		evRec.stop_replay();
		recPanel.stop_rec();
	}

	public void do_replay() {
		if (stopped) {
			stopped = false;
			evRec.start();
		}
		evRec.stop_recording();
		evRec.stop_replay();
		if (evRec.hasRecord()) {
			clear_canvas();
			//showStatus("Replay");
			recPanel.do_replay();
			evRec.start_replay();
		}
	}

	public void do_pause() {
		if (!stopped) {
			evRec.stop();
			stopped = true;
			return;
		} else {
			evRec.start();
			stopped = false;
			return;
		}
	}

	public void do_record() {
		if (stopped) {
			stopped = false;
			evRec.start();
		}
		evRec.stop_replay();
		//showStatus("Recording...");
		String s = null;		//getParameter("AudioClip");
		if (s != null)
			try {
				URL url = new URL(s);
				if (url != null) {
					//clip = getAudioClip(url);
					//clip.play();
				}
			} catch (MalformedURLException _ex) {
			} catch (NullPointerException _ex) {
			}
		recPanel.do_record();
		evRec.start_recording();
		recordInitialState(viewtype, frame.unBuf.getState(), false, stepMode, stepPanel.slider.GetValue());
		for (ajPoint ajpoint = (ajPoint) canvas.drawPoints.anchor; ajpoint != null; ajpoint = (ajPoint) ajpoint
				.getNext())
			evRec.recordEvent(0, ajpoint);

		recPanel.rec_led.switch_on();
	}

	void setFrameViewtype(int i) {
		if ((i & 1) != 0)
			frame.checkBoxVoronoi.setState(true);
		else
			frame.checkBoxVoronoi.setState(false);
		if ((i & 2) != 0)
			frame.checkBoxDelaunay.setState(true);
		else
			frame.checkBoxDelaunay.setState(false);
		if ((i & 4) != 0)
			frame.checkBoxConvexHull.setState(true);
		else
			frame.checkBoxConvexHull.setState(false);
		setViewtype(frame.checkBoxVoronoi.getState(), frame.checkBoxDelaunay.getState(), frame.checkBoxConvexHull.getState());
	}

	public void setViewtype(boolean flag, boolean flag1, boolean flag2) {
		delau.showVoronoi(flag);
		delau.showDelaunay(flag1);
		delau.showConvex(flag2);
		viewtype = 0;
		if (flag)
			viewtype |= 1;
		if (flag1)
			viewtype |= 2;
		if (flag2)
			viewtype |= 4;
		canvas.repaint();
		evRec.recordEvent(2, viewtype);
	}

	void showRest(boolean flag) {
		delau.showHidden(flag);
		evRec.recordEvent(5, flag);
	}

	public void unBufferedDisplay(boolean flag) {
		if (flag) {
			canvas.unsetOffscreen();
		} else {
			String s = null;		//getParameter("unBuf");
			if (s != null && s.compareTo("true") == 0) {
				System.err.println("Buffered Display not possible");
				canvas.unsetOffscreen();
			} else {
				canvas.setOffscreen();
			}
		}
		evRec.recordEvent(4, flag);
	}

	public void thinLines(boolean flag) {
		ajSegment.SetThinLines(flag);
		ajPoint.SetSmallPoints(flag);
		canvas.repaint();
	}

	public void clear_canvas() {
		evRec.recordEvent(6);
		end_stepping(false, true);
		delau = new ajExtendDelaunay(canvas);
		canvas = ((ajDelaunay) (delau)).canvas;
		setFrameViewtype(viewtype);
		unBufferedDisplay(frame.unBuf.getState());
		thinLines(frame.thin.getState());
		showRest(false);
		canvas.add(delau);
		canvas.repaint();
	}

	public void recordInitialState(int i, boolean flag, boolean flag1, boolean flag2, int j) {
		evRec.recordEvent(6);
		evRec.recordEvent(2, i);
		evRec.recordEvent(4, flag);
		evRec.recordEvent(5, flag1);
		evRec.recordEvent(10, flag2);
		evRec.recordEvent(11, j);
	}

	public void end_stepping(boolean flag, boolean flag1) {
		if (stepping) {
			stepping = false;
			if (flag)
				evRec.recordEvent(12, 6);
			stepPanel.ez_led.switch_off();
			stepPanel.stepPlay.switch_off();
			if (flag1)
				stepper.stop_replay();
			canvas.cleanTempLayers();
			delau.insertPoint(step_point);
			step_point = null;
			canvas.repaint();
		}
	}

	static final int SHOW_VORO = 1;
	static final int SHOW_DEL = 2;
	static final int SHOW_CONV = 4;
	boolean hasPrint;
	ajCanvas canvas;
	ajExtendDelaunay delau;
	public ajRecorder evRec;
	public ajStep stepper;
	AudioClip clip;
	public ajFrame frame;
	int f_w;
	int f_h;
	public boolean stepMode;
	public boolean stepping;
	Button sf;
	ajSingleStepPanel stepPanel;
	public ajRecorderPanel recPanel;
	public Frame recFrame;
	public Frame stepFrame;
	int viewtype;
	Event dragEvent;
	ajPoint pick;
	ajPoint step_point;
	boolean stopped;
	String helpURL;

	public static void main(String[] args) throws IOException {
		JFrame f = new JFrame();
		f.add(new VoronoiApplet());
		f.setSize(400, 400);
		f.setVisible(true);
	}
}
