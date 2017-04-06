package com.slavi.adruino;

import com.slavi.ann.NNet;
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
		
		public HeatSensor(Heater heater) {
			this.heater = heater;
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

		public HeatSensor1(Heater heater) {
			super(heater);
		}
		
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

		public HeatSensor2(Heater heater) {
			super(heater);
		}
		
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
	
	public static abstract class Controller {
		Heater heater;
		HeatSensor sensor;
		
		double minT = 100.5;
		double maxT = 101.1;
		
		public Controller(HeatSensor sensor) {
			this.sensor = sensor;
			heater = sensor.getHeater();
			currentT = prevT = sensor.getValue();
		}
		
		double currentT = 0;
		double prevT = 0;
		
		double timeBelow = 0;
		double timeAbove = 0;
		double timeOk = 0;
		
		public String stateToString() {
			double dT = currentT - prevT;
			String status;
			if (currentT < minT) {
				status = "Below";
			} else if (currentT > maxT) {
				status = "ABOVE";
			} else {
				status = "ok   ";
			}
			return (heater.isHeaterOn() ? "ON  " : "off ") + status + "    T=" + MathUtil.d2(currentT) + "    dT=" + MathUtil.d2(dT);
		}
		
		public void updateStatistics(double timeMillis) {
			if (currentT < minT) {
				timeBelow += timeMillis;
			} else if (currentT > maxT) {
				timeAbove += timeMillis;
			} else {
				timeOk += timeMillis;
			}
			System.out.println(stateToString());
		}
		
		public abstract void updateController(double timeMillis);
		
		public void update(double timeMillis) {
			sensor.update(timeMillis);
			currentT = sensor.getValue();
			updateStatistics(timeMillis);
			updateController(timeMillis);
			prevT = currentT;
		}
		
		public String toString() {
			double scale = 100 / (timeBelow + timeAbove + timeOk);
			return 
					"Below: " + MathUtil.d2(timeBelow * scale) + ", " +
					"Above: " + MathUtil.d2(timeAbove * scale) + ", " +
					"ok:    " + MathUtil.d2(timeOk * scale);
		}
	}
	
	public static class SimpleController extends Controller {
		
		public SimpleController(HeatSensor sensor) {
			super(sensor);
		}
		
		public void updateController(double timeMillis) {
			double dT = currentT - prevT;
			if (currentT < minT) {
				heater.setHeaterOn(true);
			} else if (currentT > maxT) {
				heater.setHeaterOn(false);
			}
			if (currentT+dT >= maxT)
				heater.setHeaterOn(false);
			else if (currentT+dT <= minT)
				heater.setHeaterOn(true);
		}
	}
	
	public static class AdvController extends SimpleController {
		NNet nnet;
		
		double inputPattern[];
		
		int historyItems = 4;
		
		boolean learning = true;

		public AdvController(HeatSensor sensor) {
			super(sensor);
			inputPattern = new double[historyItems + 1];
			nnet = new NNet(inputPattern.length, 1);
			for (int i = 0; i < inputPattern.length; i++)
				inputPattern[i] = prevT;
			nnet.setLearningRate(0.9);
		}
		
		double err[] = new double[1];
		
		public void updateController(double timeMillis) {
			double status = 0.5;
			if (currentT < minT) {
				status = 0;
			} else if (currentT > maxT) {
				status = 1;
			}
			nnet.feedForward(inputPattern);
			double r = nnet.getOutput()[0];
			err[0] = r - status;
			nnet.backPropagate(err);

			System.arraycopy(inputPattern, 0, inputPattern, 1, inputPattern.length - 1);
			inputPattern[0] = currentT;
			nnet.feedForward(inputPattern);

			if (learning) {
				super.updateController(timeMillis);
			} else {
				r = nnet.getOutput()[0];
				if (r >= 0.75)
					heater.setHeaterOn(true);
				else if (r <= 0.25)
					heater.setHeaterOn(false);
			}
		}
		
		public String stateToString() {
			return super.stateToString() + 
					"    Output=" + MathUtil.d2(nnet.getOutput()[0]) +
					"    err=" + MathUtil.d2(err[0]);
		}
	}
	
	void doIt() throws Exception {
		Controller c = new AdvController(new HeatSensor2(new Heater()));
		
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
