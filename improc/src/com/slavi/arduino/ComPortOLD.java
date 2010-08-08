package com.slavi.arduino;

import java.io.*;
import java.util.*;
import gnu.io.*;

public class ComPortOLD implements SerialPortEventListener {
	static CommPortIdentifier portId;
	static Enumeration portList;
	InputStream inputStream;
	SerialPort serialPort;

	static OutputStream outputStream;
	static boolean outputBufferEmptyFlag = false;

	public void doIt() throws Exception {
		boolean portFound = false;
		String defaultPort = "/dev/ttyUSB0";
		portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (portId.getName().equals(defaultPort)) {
					System.out.println("Found port: " + defaultPort);
					portFound = true;
					break;
				}
			}
		}
		if (!portFound) {
			System.out.println("port " + defaultPort + " not found.");
			return;
		}
		
		serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);
		serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		
		inputStream = serialPort.getInputStream();
		outputStream = serialPort.getOutputStream();
		serialPort.addEventListener(this);
		serialPort.notifyOnDataAvailable(true);
		while (true) {
			int i = System.in.read();
			char c = (char) i;
			System.out.print(c);
			outputStream.write(c);
		}
		
	}
	
	StringBuilder sb = new StringBuilder();
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.BI:	// Break Interrupt
		case SerialPortEvent.OE:	// Overrun Error
		case SerialPortEvent.FE:	// Framing Error
		case SerialPortEvent.PE:	// Parity Error
		case SerialPortEvent.CD:	// Carrier Detect
		case SerialPortEvent.CTS:	// Clear To Send
		case SerialPortEvent.DSR:	// Data Set Ready
		case SerialPortEvent.RI:	// Ring Indicator
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			try {
				while (inputStream.available() > 0) {
					int i = inputStream.read();
					char c = (char) i;
					if (c == '\n') {
						System.err.println("IGOT: " + sb.toString());
						sb = new StringBuilder();
					} else {
						sb.append(c);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	public static void main(String[] args) throws Exception {
//		System.setProperty("java.library.path", "/home/slavian/.bin/");
		ComPortOLD test = new ComPortOLD();
		test.doIt();
	}
}
