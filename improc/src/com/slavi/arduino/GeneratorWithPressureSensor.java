package com.slavi.arduino;

import gnu.io.SerialPort;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.slavi.math.MathUtil;
import com.slavi.util.Beep;
import com.slavi.util.Const;

public class GeneratorWithPressureSensor {

	final double analogReadingToVoltage = 5.0 / 1024.0;
	final double shuntResistance = 0.275;	// Shunt resistance in Ohms

	final double toCelsius = 100 * analogReadingToVoltage; 
	final double toAmpers = analogReadingToVoltage / shuntResistance; 
	
	final int maxFrequency = 50000;

	final int pressureThresholdMax = 140;
	final int pressureThresholdLow = 120;
	final int cellTemperatureThresholdMax = (int) (80 / toCelsius);
	final int cellTemperatureThresholdLow = (int) (60 / toCelsius);
	final int mosfetTemperatureThresholdMax = (int) (80 / toCelsius);
	final int mosfetTemperatureThresholdLow = (int) (60 / toCelsius);
	final int currentThresholdMax = (int) (10 / toAmpers);
	final int currentThresholdLow = (int) (6 / toAmpers);

	final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
//		frequency = 2900; //4700;
		String fouName = System.getProperty("user.home") + "/comport_6.log";
		FileOutputStream fou = new FileOutputStream(fouName, true);
		out = new PrintStream(fou, true); // autoflush
//		out = System.out;
		out.println();
		out.println("*******************");
		out.println("* Started on " + df.format(System.currentTimeMillis()));
		out.println("* Using start frequency " + frequency);
		out.println("*******************");
		out.println("* time frequency isPlaying aborting pressure cellTemperature mosfetTemperature ambientTemperature current maxPressure maxCurrent COMMENT");
		out.println("*******************");
		frequency--; // Will increase by 1 before sending it to arduino.
		
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
		comReader.out.println("a" + cellTemperatureThresholdMax);
		comReader.out.println("b" + mosfetTemperatureThresholdMax);
		comReader.out.println("d" + currentThresholdMax);
		comReader.out.println("t" + pressureThresholdMax);
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
				if (System.currentTimeMillis() - lastRefresh > 1000) {
					comReader.out.println("l");		// ping arduino
					lastRefresh = System.currentTimeMillis();
				}
				continue;
			}

			StringTokenizer st = new StringTokenizer(line, "\t");
			boolean isPlaying = st.nextToken().equals("1");
			boolean aborting = st.nextToken().equals("1");
			int pressure = Integer.parseInt(st.nextToken());
			int cellTemperature = Integer.parseInt(st.nextToken());
			int mosfetTemperature = Integer.parseInt(st.nextToken());
			int ambientTemperature = Integer.parseInt(st.nextToken());
			int current = Integer.parseInt(st.nextToken());
			int maxPressure = Integer.parseInt(st.nextToken());
			int maxCurrent = Integer.parseInt(st.nextToken());

			boolean playNextFrequency = false;
			String comment = "";
			if (aborting) {
				Beep.beep();
				Beep.beep();
				if (maxPressure >= pressureThresholdMax)
					comment = "MAX PRESSURE THRESHOLD EXCEEDED";
				else if (cellTemperature >= cellTemperatureThresholdMax)
					comment = "CELL TEMPERATURE THRESHOLD EXCEEDED";
				else if (mosfetTemperature >= mosfetTemperatureThresholdMax)
					comment = "MOSFET TEMPERATURE THRESHOLD EXCEEDED";
				else if (current >= currentThresholdMax)
					comment = "CURRENT THRESHOLD EXCEEDED";
			} else if (isPlaying) {
				if (System.currentTimeMillis() - startedOn > 10000) {
					playNextFrequency = true;
				}
			} else if (pressure >= pressureThresholdLow) {
				comment = "Pressure still above low threshold";
			} else if (cellTemperature >= cellTemperatureThresholdLow) {
				comment = "Cell temperature still above threshold";
			} else if (mosfetTemperature >= mosfetTemperatureThresholdLow) {
				comment = "MOSFET temperature still above threshold";
			} else if (current >= currentThresholdLow) {
				comment = "CURRENT still above threshold";
			} else {
				playNextFrequency = true;
			}
			
			String str = 
				df.format(System.currentTimeMillis()) + "\t" +
				Integer.toString(frequency) + "\t" +
				(isPlaying ? "1" : "0") + "\t" +
				(aborting ? "1" : "0") + "\t" +
				Integer.toString(pressure) + "\t" +
				MathUtil.d2(cellTemperature * toCelsius) + "\t" +
				MathUtil.d2(mosfetTemperature * toCelsius) + "\t" +
				MathUtil.d2(ambientTemperature * toCelsius) + "\t" +
				MathUtil.d2(current * toAmpers) + "\t" +
				Integer.toString(maxPressure) + "\t" +
				MathUtil.d2(maxCurrent * toAmpers) + "\t" +
				comment;
			out.println(str);
			
			if (playNextFrequency) {
				frequency++;
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
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
//		System.setProperty("java.library.path", "/home/slavian/.bin/");
//		-Djava.library.path=/home/slavian/.bin/
//		-Djava.library.path=D:\prg\rxtx
		GeneratorWithPressureSensor test = new GeneratorWithPressureSensor();
		try {
			test.setup();
			test.doIt();
		} finally {
			test.close();
		}
	}
}
