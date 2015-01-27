package com.slavi.adruino;

import com.slavi.math.MathUtil;

public class Temperature {

	public static class Heater {
		public boolean heaterOn = false;
		
		public void setHeaterOn(boolean state) {
			this.heaterOn = state;
		}

		public boolean isHeaterOn() {
			return heaterOn;
		}
	}
	
	public static abstract class HeatSensor {
		Heater heater;
		
		public HeatSensor() {
			this.heater = new Heater();
		}
		
		double val = 101;
		
		public double getValue() {
			return val;
		}
		
		public Heater getHeater() {
			return heater;
		}

		public abstract void update(double timeMillis);
	}
	
	public static class HeatSensor1 extends HeatSensor {
		public double a1 = 3.4;
		public double b1 = 4.1;
		
		public double a2 = 2.2;
		public double b2 = 1.3;

		public void update(double timeMillis) {
			double t1 = timeMillis / 1000.0;
			double t2 = t1*t1;
			
			if (heater.isHeaterOn()) {
				val += a1 * t2 + b1 * t1;
			} else {
				val -= a2 * t2 + b2 * t1;
			}
		}
	}
	
	public static class HeatSensor2 extends HeatSensor {
		public double dT = 0;
		public double T_per_Sec = 0.8;

		public void update(double timeMillis) {
			double t1 = timeMillis / 1000.0;
			if (heater.isHeaterOn()) {
				dT += T_per_Sec * t1;
			} else {
				dT -= T_per_Sec * t1 * 0.5;
			}
			val += dT;
		}
	}
	
	public static class Controller {
		Heater heater;
		HeatSensor sensor;
		
		double minT = 100.5;
		double maxT = 101.1;
		
		public Controller() {
			sensor = new HeatSensor2();
			heater = sensor.getHeater();
			prevT = sensor.getValue();
		}
		
		double prevT = 0;
		
		double timeBelow = 0;
		double timeAbove = 0;
		double timeOk = 0;
		
		public void update(double timeMillis) {
			sensor.update(timeMillis);
			
			double T = sensor.getValue();
			double dT = T - prevT;

			String status;
			if (T < minT) {
				status = "Below";
				timeBelow += timeMillis;
			} else if (T > maxT) {
				status = "ABOVE";
				timeAbove += timeMillis;
			} else {
				status = "ok   ";
				timeOk += timeMillis;
			}
			System.out.println((heater.isHeaterOn() ? "ON  " : "off ") + status + "\t" + MathUtil.d2(T) + "\t" +
					MathUtil.d2(dT) + "\t" +
					MathUtil.d2(((HeatSensor2) sensor).dT));

			if (T < minT) {
				heater.setHeaterOn(true);
			} else if (T > maxT) {
				heater.setHeaterOn(false);
			}
			if (T+dT >= maxT)
				heater.setHeaterOn(false);
			else if (T+dT <= minT)
				heater.setHeaterOn(true);
			
			prevT = T;
		}
		
		public String toString() {
			double scale = 100 / (timeBelow + timeAbove + timeOk);
			return 
					"Below: " + MathUtil.d2(timeBelow * scale) + ", " +
					"Above: " + MathUtil.d2(timeAbove * scale) + ", " +
					"ok:    " + MathUtil.d2(timeOk * scale);
		}
	}
	
	void doIt() throws Exception {
		Controller c = new Controller();
		
		for (int i = 0; i < 1000; i++) {
			double timeMillis = 10 + Math.random() * 10;
			c.update(timeMillis);
		}
		System.out.println(c.toString());
	}

	public static void main(String[] args) throws Exception {
		new Temperature().doIt();
		System.out.println("Done.");
	}
}
