package com.slavi.arduino;

import java.util.Random;

import com.slavi.math.MathUtil;

public class SmoothTest {

	int maxVal = 1024;
	int maxCounter = 10000;
	int counter = 0;
	Random random = new Random();
	boolean play = true;
	
	int getNextVal() {
		double r = (counter++) * 2.0 * Math.PI / maxCounter;
		if (counter >= maxCounter) {
			counter = 0;
			play = !play;
		}
		int result = (int) ((1.0 + Math.sin(r)) * maxVal / 2.0);
		return play ? result : 0;
		//return random.nextInt(400);
	}
	
	int smooth(int data, double smoothFactor, double smoothVal) {
		return (int) (data * (1.0 - smoothFactor) + smoothVal * smoothFactor);
	}
	
	void myWait() {
		int max = getNextVal();
		for (int i = 0; i < max; i++)
			for (int j = 0; j < max; j++) ;
	}
	
	static class RPS {
		final int RPS_COUNT_PER_SECOND = 5;
		long counters[] = new long[RPS_COUNT_PER_SECOND];
		long started[] = new long[RPS_COUNT_PER_SECOND];
		long lastTime;
		int deltaTime;
		byte curCounter;
		long rps;

		void initialize() {
			lastTime = System.currentTimeMillis();
			for (byte i = 0; i < RPS_COUNT_PER_SECOND; i++) {
				counters[i] = 0;
				started[i] = lastTime;
			}
			curCounter = 0;
			deltaTime = 1000 / RPS_COUNT_PER_SECOND;
		}
		
		void update() {
			long now = System.currentTimeMillis();
			for (byte i = 0; i < RPS_COUNT_PER_SECOND; i++) {
				counters[i]++;
			}
			if (now - lastTime >= deltaTime) {
				rps = (counters[curCounter] * 1000) / (now - started[curCounter]);
				lastTime = now;
				started[curCounter] = now;
				counters[curCounter++] = 0;
				if (curCounter >= RPS_COUNT_PER_SECOND) {
					curCounter = 0;
				}
			}
		}		
	}

	static RPS rps = new RPS();
	
	double smooth(int data, double smoothedVal) {
		int scale = (int) rps.rps / 4;
		smoothedVal = (data + smoothedVal * scale) / (scale + 1);
		return smoothedVal;
	}
	
	long lastTime;
	double sval;
	void doIt() throws Exception {
		rps.initialize();
		lastTime = System.currentTimeMillis();
		sval = 0;
		while (true) {
			rps.update();
			int val = getNextVal();
			sval = smooth(val, sval);
			long now = System.currentTimeMillis();
			if (now - lastTime > 500) {
				lastTime = now;
				System.out.println(val + "\t" + MathUtil.d2(sval) + "\t" + rps.rps);
			}
			Thread.sleep(1);
//			myWait();
		}
		
/*		
		for (int i = 0; i < 1000; i++) {
			int val = getNextVal();
			smoothVal = smooth(val, 0.95, smoothVal);
//			if (i % 500 == 0)
				System.out.println(val + "\t" + smoothVal);
		}*/
	}
	
	public static void main(String[] args) throws Exception {
		SmoothTest t = new SmoothTest();
		t.doIt();
	}
}
