package com.slavi.arduino;

import gnu.io.SerialPort;


public class ComPort {
	
	class LineProcess implements LineProcessor {
		public void processLine(String line) {
			System.out.println("IGOT: " + line);
		}
	}
	
	LineProcess lineProcessor = new LineProcess();
	
	public void doIt() throws Exception {
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
		ComPortOLD test = new ComPortOLD();
		test.doIt();
	}
}
