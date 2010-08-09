package com.slavi.arduino;

import gnu.io.SerialPort;

import java.util.StringTokenizer;

import com.slavi.util.Const;


public class ComPort {
	
	public int frequency;
	
	class LineProcess implements LineProcessor {
		public void processLine(String line) {
			StringTokenizer st = new StringTokenizer(line, ":");
			String stat = st.nextToken();
			int freq = Integer.parseInt(st.nextToken());
			
			boolean isPlaying = st.nextToken().equals("1");
			boolean wasButtonPressed = st.nextToken().equals("1");
			boolean isButtonDown = st.nextToken().equals("1");
		}
	}
	
	LineProcess lineProcessor = new LineProcess();
	
	public void doIt() throws Exception {
		frequency = Integer.parseInt(Const.properties.getProperty("ComPort.startFrequency", "10"));
		ComPortLineReader comReader = new ComPortLineReader();
		comReader.setParams("/dev/ttyUSB0", 9600, SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
		comReader.open(lineProcessor);
		
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
