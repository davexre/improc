package com.slavi.arduino;

import gnu.io.SerialPort;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimerTask;

import com.slavi.util.Const;
import com.slavi.util.ui.SwtUtil;


public class ComPort {
	
	public PrintStream out;
	
	ComPortLineReader comReader;
	
	public int frequency;
	
	public static final int maxFrequency = 50000;
	
	long startedOn;
	
	class LineProcess implements LineProcessor {
		public void processLine(String line) {
			out.println(new Date() + ":" + line);
			StringTokenizer st = new StringTokenizer(line, ":");
			int freq = Integer.parseInt(st.nextToken());
			boolean isPlaying = st.nextToken().equals("1");
			boolean wasButtonPressed = st.nextToken().equals("1");
			boolean isButtonDown = st.nextToken().equals("1");

			if (wasButtonPressed) {
				out.println("BUTTON WAS PRESSED at frequency " + Integer.toString(freq));
			}
			
			if (isPlaying) {
				if (System.currentTimeMillis() - startedOn > 10000) {
					out.println("Stopping frequency " + freq);
					comReader.out.println("s0");
					comReader.out.println("l");
				}
			} else {
				// not playing
				if (isButtonDown) {
					out.println("Button is down. Wait and refresh state.");
					refreshArduinoState();
				} else {
					if (freq == frequency) {
						frequency++;
						if (frequency > maxFrequency) {
							out.println("All frequencies tested. Test finished.");
							System.exit(0);
						} else {
							String frequencyStr = Integer.toString(frequency);
							out.println("Starting new test at frequency " + frequencyStr);
							Const.properties.setProperty("ComPort.startFrequency", frequencyStr);
							startedOn = System.currentTimeMillis();
							comReader.out.println("s" + frequencyStr);
						}
					}
				}
			}
		}
	}
	
	public void refreshArduinoState() {
		SwtUtil.timer.schedule(refreshTimerTask, 2000); 
	}
	
	LineProcess lineProcessor = new LineProcess();
	
	class RefreshTimerTask extends TimerTask {
		public void run() {
			comReader.out.println("l"); // ping arduino
		}
	}
	
	RefreshTimerTask refreshTimerTask = new RefreshTimerTask();
	
	public void doIt() throws Exception {
		frequency = Integer.parseInt(Const.properties.getProperty("ComPort.startFrequency", "10"));
		String fouName = System.getProperty("user.home") + "/comport.log";
		FileOutputStream fou = new FileOutputStream(fouName, true);
		out = new PrintStream(fou, true); // autoflush
		out.println();
		out.println("*******************");
		out.println("* Started on " + new Date());
		out.println("* Using start frequency " + frequency);
		out.println("*******************");
		out.println();
		
		comReader = new ComPortLineReader();
		comReader.setParams("/dev/ttyUSB0", 9600, SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
		comReader.open(lineProcessor);
		
		comReader.out.println("l");
		
		while (true) {
			int i = System.in.read();
			char c = (char) i;
			System.out.print(c);
			comReader.out.print(c);
		}
	}	
	
	public static void main(String[] args) throws Exception {
//		System.setProperty("java.library.path", "/home/slavian/.bin/");
//		-Djava.library.path=/home/slavian/.bin/
//		-Djava.library.path=D:\prg\rxtx
		ComPort test = new ComPort();
		test.doIt();
	}
}
