package com.slavi.reprap.plotter;

import gnu.io.SerialPort;

import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.slavi.arduino.ComPortLineReader;
import com.slavi.arduino.LineProcessor;


public class PlotterCirclesTest {
	final static double XDelayBetweenSteppsAtMaxSpeedMicros = 1500;
	final static double YDelayBetweenSteppsAtMaxSpeedMicros = 1500;
	final static double ZDelayBetweenSteppsAtMaxSpeedMicros = 2500;

	final static double XAxisLengthInMicroM = 359800;
	final static double YAxisLengthInMicroM = 305900;
	final static double ZAxisLengthInMicroM = 163800;

	final static double XAxisLengthInSteps = 57537;
	final static double YAxisLengthInSteps = 49033;
	final static double ZAxisLengthInSteps = 6338;

	final static double XAxisResolutionInStepsPerDecimeter = (XAxisLengthInSteps * 1000.0 / (XAxisLengthInMicroM / 100.0));
	final static double YAxisResolutionInStepsPerDecimeter = (YAxisLengthInSteps * 1000.0 / (YAxisLengthInMicroM / 100.0));
	final static double ZAxisResolutionInStepsPerDecimeter = (ZAxisLengthInSteps * 1000.0 / (ZAxisLengthInMicroM / 100.0));
	
	final static double xMaxSpeedMmPerMin1 = (XAxisLengthInMicroM / 10000.0) / (XDelayBetweenSteppsAtMaxSpeedMicros / 1000000.0 / 60.0);
	
	final static double xStepsPerMM = XAxisResolutionInStepsPerDecimeter / 100.0; 
	final static double xStepsPerMin = 1.0 / (XDelayBetweenSteppsAtMaxSpeedMicros / 1000000 / 60 ); 
	final static double xMaxSpeedMmPerMin = xStepsPerMin / xStepsPerMM;    //(XDelayBetweenSteppsAtMaxSpeedMicros / 1.0 / 60.0) / (XAxisResolutionInStepsPerDecimeter / 100.0);

	ComPortLineReader comReader;

	final ArrayBlockingQueue<String>queue = new ArrayBlockingQueue<String>(5);
	
	LineProcessor lineProcessor = new LineProcessor() {
		public void processLine(String line) {
			queue.offer(line);
		}
	};
	
	boolean waitForOk = false;
	void sendCommand(String cmd) throws Exception {
		if (waitForOk) {
			String line = null;
			while (!"ok".equals(line)) {
				line = queue.poll(100, TimeUnit.MILLISECONDS);
				if (line != null)
					System.out.println("RECV: " + line);
			}
		}
		System.out.println("SEND: " + cmd);
		comReader.out.println(cmd);
		waitForOk = true;
	}
	
	boolean penUp = true;
	void doMoveTo(double x, double y, boolean penUp) throws Exception {
		if (this.penUp != penUp) {
			this.penUp = penUp;
			sendCommand(penUp ? "U" : "D");
		}
		sendCommand(String.format(Locale.US, "M%d,%d", (int) x, (int) y));
	}
	
	void drawCircle(double x, double y, double r, double maxSegmentLength) throws Exception {
		double perimeter = 2 * Math.PI * r;
		int segments = (int) Math.ceil(perimeter / maxSegmentLength);
		if (segments < 3)
			return;
		
		double dAngle = 2 * Math.PI / segments;
		boolean isFirst = true;
		for (int segment = 0; segment <= segments; segment++) {
			double angle = segment * dAngle;
			double px = x + Math.sin(angle) * r;
			double py = y + Math.cos(angle) * r;
			doMoveTo(px, py, isFirst);
			isFirst = false;
		}
	}
	
	void doIt() throws Exception {
		comReader = new ComPortLineReader();
		comReader.setParams("/dev/ttyUSB0", 115200, SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
		comReader.open(lineProcessor);
		try {
			double x = 150000;
			double y = 150000;
			double r = 10000;
			double stepR = 10000;
			double maxSegmentLength = 5000;
			
			sendCommand("U"); // pen up
			sendCommand("S200"); // set speed mm/min
			sendCommand("I"); // init XY
			sendCommand("Z"); // init Z
			sendCommand("U"); // pen up

			for (int i = 0; i < 5; i++) {
				drawCircle(x, y, r, maxSegmentLength);
				r += stepR;
			}
			sendCommand("U"); // up
			sendCommand("I"); // init xy
			sendCommand("Z"); // init z 
			sendCommand("U"); // up
			sendCommand("");  // wait
		} finally {
			if (comReader != null) {
				comReader.close();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		new PlotterCirclesTest().doIt();
		System.out.println("Done.");
	}

}
