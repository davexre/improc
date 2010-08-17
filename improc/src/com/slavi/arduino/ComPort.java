package com.slavi.arduino;

import gnu.io.SerialPort;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.slavi.util.Const;

public class ComPort {
	
	final int maxFrequency = 50000;
	final int lowPresureThreshold = 110;
	final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH/mm/ss");

	final ArrayBlockingQueue<String>queue = new ArrayBlockingQueue<String>(5);
	
	public int frequency;
	
	public PrintStream out;
	
	ComPortLineReader comReader;
		
	long startedOn;
	
	long lastRefresh;

	LineProcessor lineProcessor = new LineProcessor() {
		public void processLine(String line) {
			queue.offer(line);
		}
	};

	void setup() throws Exception {
		frequency = Integer.parseInt(Const.properties.getProperty("ComPort.startFrequency", "100"));
		String fouName = System.getProperty("user.home") + "/comport.log";
		FileOutputStream fou = new FileOutputStream(fouName, true);
		out = new PrintStream(fou, true); // autoflush
//		out = System.out;
		out.println();
		out.println("*******************");
		out.println("* Started on " + df.format(System.currentTimeMillis()));
		out.println("* Using start frequency " + frequency);
		out.println("*******************");
		out.println();
		
		comReader = new ComPortLineReader();
		comReader.setParams("/dev/ttyUSB0", 9600, SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
		comReader.open(lineProcessor);
		
		startedOn = lastRefresh = 0;
	}
	
	void close() throws Exception {
		if (out != null) {
			out.println();
			out.println("*******************");
			out.println("* Stopped on " + df.format(System.currentTimeMillis()));
			out.println("* Frequency " + frequency);
			out.println("*******************");
			out.println();
			out.close();
		}
		if (comReader != null) {
			comReader.close();
		}
	}
	
	void doIt() throws Exception {
		comReader.out.println("t140");
		comReader.out.println("l");
		
		while (true) {
			// Check user abort
			if (System.in.available() > 0) {
				int i = System.in.read();
				char c = (char) i;
				if (c == 'q') {
					System.out.println("CLOSING...");
					comReader.out.println("s0");
					break;
				}
			}

			String line = queue.poll(100, TimeUnit.MILLISECONDS);
			if (line == null) {
				if (System.currentTimeMillis() - lastRefresh > 2000) {
					comReader.out.println("l");		// ping arduino
					lastRefresh = System.currentTimeMillis();
				}
				continue;
			}

			StringTokenizer st = new StringTokenizer(line, ":");
			int curPresure = Integer.parseInt(st.nextToken());
			boolean isPlaying = st.nextToken().equals("1");
			boolean aborting = st.nextToken().equals("1");
//			int freq = Integer.parseInt(st.nextToken());
//			int maxPresure = Integer.parseInt(st.nextToken());
//			int presureThreshold = Integer.parseInt(st.nextToken());
			boolean playNextFrequency = false;
			
			String comment = "";
			if (aborting) {
				comment = "MAX PRESURE THRESHOLD EXCEEDED";
			} else if (isPlaying) {
				if (System.currentTimeMillis() - startedOn > 10000) {
					playNextFrequency = true;
				}
			} else if (curPresure > lowPresureThreshold) {
				comment = "Presure still above low threshold";
			} else {
				playNextFrequency = true;
			}
			
			String str = df.format(System.currentTimeMillis()) + ":" + line + ":" + comment;
			out.println(str);
//			System.out.println(str);
			
			if (playNextFrequency) {
				if (frequency > maxFrequency) {
					out.println("\n\nAll frequencies tested. Test finished.\n\n");
					break;
				} else {
					String frequencyStr = Integer.toString(frequency);
					Const.properties.setProperty("ComPort.startFrequency", frequencyStr);
					startedOn = System.currentTimeMillis();
					comReader.out.println("s" + frequencyStr);
					isPlaying = true;
				}
				frequency++;
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
//		System.setProperty("java.library.path", "/home/slavian/.bin/");
//		-Djava.library.path=/home/slavian/.bin/
//		-Djava.library.path=D:\prg\rxtx
		ComPort test = new ComPort();
		try {
			test.setup();
			test.doIt();
		} finally {
			test.close();
		}
	}
}
