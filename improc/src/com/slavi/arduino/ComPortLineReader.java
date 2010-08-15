package com.slavi.arduino;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class ComPortLineReader implements SerialPortEventListener {

	String portName;
	int speed;
	int dataBits;
/*
 * @see gnu.io.SerialProt
 * 
 * 	public static final int  PARITY_NONE            =0;
 *	public static final int  PARITY_ODD             =1;
 *	public static final int  PARITY_EVEN            =2;
 *	public static final int  PARITY_MARK            =3;
 *	public static final int  PARITY_SPACE           =4;
 */
	int parity;
	int stopBits;
	
	CommPortIdentifier portId;
	SerialPort serialPort;
	InputStream inputStream;
	OutputStream outputStream;
	public PrintStream out;
	LineProcessor lineProcessor;
	
	public void setParams(String portName, int speed, int dataBits, int parity, int stopBits) {
		this.portName = portName;
		this.speed = speed;
		this.dataBits = dataBits;
		this.parity = parity;
		this.stopBits = stopBits;
		portId = null;
	}
	
	public void open(LineProcessor lineProcessor) throws NoSuchPortException, PortInUseException, 
				UnsupportedCommOperationException, IOException, 
				TooManyListenersException {
		this.lineProcessor = lineProcessor;
		portId = CommPortIdentifier.getPortIdentifier(portName);
		serialPort = (SerialPort) portId.open("Arduino", 1234);		
		serialPort.setSerialPortParams(speed, dataBits, stopBits, parity);
		inputStream = serialPort.getInputStream();
		outputStream = serialPort.getOutputStream();
		out = new PrintStream(outputStream);
		serialPort.addEventListener(this);
		serialPort.notifyOnDataAvailable(true);
	}
	
	public void close() throws IOException {
		lineProcessor = null;
		if (serialPort != null)
			serialPort.removeEventListener();
		if (out != null)
			out.close();
		out = null;
		if (outputStream != null)
			outputStream.close();
		outputStream = null;
		if (inputStream != null)
			inputStream.close();
		inputStream = null;
		if (serialPort != null)
			serialPort.close();
		serialPort = null;
	}

	private StringBuilder sb = new StringBuilder();
	
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
					if ((c == '\r') || (c == '\n')) {
						if (sb.length() > 0) {
							String line = sb.toString();
							sb = new StringBuilder();
							if (lineProcessor != null) {
								try {
									lineProcessor.processLine(line);
								} catch (Throwable t) {
									t.printStackTrace();
								}
							}
						}
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
}
