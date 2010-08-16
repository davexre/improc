package com.slavi.arduino;

import gnu.io.SerialPort;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.slavi.util.Const;


public class ComPort {
	
	public PrintStream out;

	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH/mm/ss");
	
	ArrayBlockingQueue<String>queue = new ArrayBlockingQueue<String>(5);
	
	ComPortLineReader comReader;
	
	public int frequency;
	
	public static final int maxFrequency = 50000;
	
	long startedOn;
	
	int lowPresureThreshold = 110;
	
	LineProcessor lineProcessor = new LineProcessor() {
		public void processLine(String line) {
			queue.offer(line);
		}
	};

	long lastTimeDataReceived;
	boolean playNextFrequency;
	boolean isPlaying;
	int curPresure;
	
	public void doIt() throws Exception {
		frequency = Integer.parseInt(Const.properties.getProperty("ComPort.startFrequency", "100"));
		String fouName = System.getProperty("user.home") + "/comport.log";
		FileOutputStream fou = new FileOutputStream(fouName, true);
		out = new PrintStream(fou, true); // autoflush
//		out = System.out;
		out.println();
		out.println("*******************");
		out.println("* Started on " + new Date());
		out.println("* Using start frequency " + frequency);
		out.println("*******************");
		out.println();
		
		comReader = new ComPortLineReader();
		comReader.setParams("/dev/ttyUSB0", 9600, SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
		comReader.open(lineProcessor);
		
		startedOn = lastTimeDataReceived = 0;
		playNextFrequency = true;
		isPlaying = false;
		
		comReader.out.println("t140");
		comReader.out.println("l");
		
		while (true) {
			String cmdToSend = null;
			String line = queue.poll(100, TimeUnit.MILLISECONDS);
			if (line != null) {
				String comment = "";
				StringTokenizer st = new StringTokenizer(line, ":");
				int freq = Integer.parseInt(st.nextToken());
				isPlaying = st.nextToken().equals("1");
				curPresure = Integer.parseInt(st.nextToken());
				int maxPresure = Integer.parseInt(st.nextToken());
				int presureThreshold = Integer.parseInt(st.nextToken());
		
				if (maxPresure > presureThreshold) {
					comment = "MAX PRESURE THRESHOLD EXCEEDED";
				} else if (!isPlaying) {
					if (curPresure > lowPresureThreshold) {
						comment = "Presure still above low threshold";
					} else {
						playNextFrequency = true;
					}
				}
				String str = df.format(System.currentTimeMillis()) + ":" + line + ":" + comment;
				out.println(str);
//				System.out.println(str);
				lastTimeDataReceived = System.currentTimeMillis();
			} else {
				if (System.currentTimeMillis() - lastTimeDataReceived > 2000) {
					cmdToSend = "l"; // ping arduino
				}
			}
			
			if (isPlaying) {
				if (System.currentTimeMillis() - startedOn > 10000) {
					playNextFrequency = true;
				}
			}
			
			if (playNextFrequency) {
				if (frequency > maxFrequency) {
					out.println("All frequencies tested. Test finished.");
					break;
				} else {
					String frequencyStr = Integer.toString(frequency);
					Const.properties.setProperty("ComPort.startFrequency", frequencyStr);
					startedOn = System.currentTimeMillis();
					cmdToSend = "s" + frequencyStr;
					isPlaying = true;
				}
				frequency++;
			}
			
			if (cmdToSend != null) {
				comReader.out.println(cmdToSend);
//				System.out.println(cmdToSend);
			}
					
			if (System.in.available() > 0) {
				int i = System.in.read();
				char c = (char) i;
				if (c == 'q') {
					System.out.println("CLOSING...");
					comReader.out.println("s0");
					break;
				}
			}
			playNextFrequency = false;
		}
		out.flush();
		out.close();
		comReader.close();
	}	
	
	public static void main(String[] args) throws Exception {
//		System.setProperty("java.library.path", "/home/slavian/.bin/");
//		-Djava.library.path=/home/slavian/.bin/
//		-Djava.library.path=D:\prg\rxtx
		ComPort test = new ComPort();
		test.doIt();
	}
}
