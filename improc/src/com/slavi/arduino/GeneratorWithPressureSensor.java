package com.slavi.arduino;

import gnu.io.SerialPort;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.slavi.math.MathUtil;
import com.slavi.util.Const;

public class GeneratorWithPressureSensor {
	
	final double toCelsius = 500.0 / 1024.0; 
	
	final int maxFrequency = 3080; //50000;
	final int pressureThresholdMax = 140;
	final int pressureThresholdLow = 120;
	final int temperature1ThresholdMax = (int) (80 / toCelsius);
	final int temperature1ThresholdLow = (int) (60 / toCelsius);
	final int temperature2ThresholdMax = (int) (80 / toCelsius);
	final int temperature2ThresholdLow = (int) (60 / toCelsius);

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
		frequency = 2900; //4700;
		String fouName = System.getProperty("user.home") + "/comport_4.log";
//		FileOutputStream fou = new FileOutputStream(fouName, true);
//		out = new PrintStream(fou, true); // autoflush
		out = System.out;
		out.println();
		out.println("*******************");
		out.println("* Started on " + df.format(System.currentTimeMillis()));
		out.println("* Using start frequency " + frequency);
		out.println("*******************");
		out.println();
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
		comReader.out.println("a" + temperature1ThresholdMax);
		comReader.out.println("b" + temperature2ThresholdMax);
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
			int curPressure = Integer.parseInt(st.nextToken());
			boolean isPlaying = st.nextToken().equals("1");
			boolean aborting = st.nextToken().equals("1");
			int maxPressure = Integer.parseInt(st.nextToken());
			int curTemperature1 = Integer.parseInt(st.nextToken());
			int curTemperature2 = Integer.parseInt(st.nextToken());

			boolean playNextFrequency = false;
			String comment = "";
			if (aborting) {
				if (maxPressure > pressureThresholdMax)
					comment = "MAX PRESSURE THRESHOLD EXCEEDED";
				else if (curTemperature1 > temperature1ThresholdMax)
					comment = "CELL TEMPERATURE THRESHOLD EXCEEDED";
				else if (curTemperature2 > temperature2ThresholdMax)
					comment = "MOSFET TEMPERATURE THRESHOLD EXCEEDED";
			} else if (isPlaying) {
				if (System.currentTimeMillis() - startedOn > 10000) {
					playNextFrequency = true;
				}
			} else if (curPressure > pressureThresholdLow) {
				comment = "Pressure still above low threshold";
			} else if (curTemperature1 > temperature1ThresholdLow) {
				comment = "Cell temperature still above threshold";
			} else if (curTemperature1 > temperature1ThresholdLow) {
				comment = "MOSFET temperature still above threshold";
			} else {
				playNextFrequency = true;
			}
			
			String str = df.format(System.currentTimeMillis()) + "\t" +
				Integer.toString(frequency) + "\t" +
				Integer.toString(curPressure) + "\t" +
				Integer.toString(maxPressure) + "\t" +
				MathUtil.d2(curTemperature1 * toCelsius) + "\t" +
				MathUtil.d2(curTemperature2 * toCelsius) + "\t" +
				(isPlaying ? "1" : "0") + "\t" +
				(aborting ? "1" : "0") + "\t" +
				comment;
			out.println(str);
//			System.out.println(str);
			
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
