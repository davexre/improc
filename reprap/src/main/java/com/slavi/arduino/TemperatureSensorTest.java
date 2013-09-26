package com.slavi.arduino;

import gnu.io.SerialPort;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.slavi.math.MathUtil;

/**
 * Use with AnalogSensorTest 
 */
public class TemperatureSensorTest {
	
	final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	final ArrayBlockingQueue<String>queue = new ArrayBlockingQueue<String>(5);
	
	public PrintStream out;
	
	ComPortLineReader comReader;
		
//	long startedOn;
//	
//	long lastRefresh;

	LineProcessor lineProcessor = new LineProcessor() {
		public void processLine(String line) {
			queue.offer(line);
		}
	};

	void setup() throws Exception {
//		String fouName = System.getProperty("user.home") + "/comport.log";
//		FileOutputStream fou = new FileOutputStream(fouName, true);
//		out = new PrintStream(fou, true); // autoflush
		out = System.out;
		out.println();
		out.println("*******************");
		out.println("* Started on " + df.format(System.currentTimeMillis()));
		out.println("*******************");
		out.println();
		
		comReader = new ComPortLineReader();
		comReader.setParams("/dev/ttyUSB0", 9600, SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
		comReader.open(lineProcessor);
		
//		startedOn = lastRefresh = 0;
	}
	
	void close() throws Exception {
		if (out != null) {
			out.println();
			out.println("*******************");
			out.println("* Stopped on " + df.format(System.currentTimeMillis()));
			out.println("*******************");
			out.println();
			out.close();
		}
		if (comReader != null) {
			comReader.close();
		}
	}
	
	void doIt() throws Exception {
		while (true) {
			// Check user abort
			if (System.in.available() > 0) {
				int i = System.in.read();
				char c = (char) i;
				if (c == 'q') {
					System.out.println("CLOSING...");
					break;
				}
			}

			String line = queue.poll(100, TimeUnit.MILLISECONDS);
			if (line == null) {
//				if (System.currentTimeMillis() - lastRefresh > 2000) {
//					comReader.out.println("l");		// ping arduino
//					lastRefresh = System.currentTimeMillis();
//				}
				continue;
			}
			StringTokenizer st = new StringTokenizer(line, "\t");
			String str = df.format(System.currentTimeMillis());
			double t = Double.parseDouble(st.nextToken());
			str += "\t" + MathUtil.d2(t);
			while (st.hasMoreTokens()) {
				t = Double.parseDouble(st.nextToken());
				t *= 500.0 / 1024.0;
				str += "\t" + MathUtil.d2(t);
			}
			out.println(str);
		}
	}
	
	public static void main(String[] args) throws Exception {
//		System.setProperty("java.library.path", "/home/slavian/.bin/");
//		-Djava.library.path=/home/slavian/.bin/
//		-Djava.library.path=D:\prg\rxtx
		TemperatureSensorTest test = new TemperatureSensorTest();
		try {
			test.setup();
			test.doIt();
		} finally {
			test.close();
		}
	}
}
