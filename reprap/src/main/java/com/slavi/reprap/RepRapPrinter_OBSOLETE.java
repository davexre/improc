package com.slavi.reprap;


public class RepRapPrinter_OBSOLETE {
	
	public void cmd(String cmd) throws Exception {
		System.out.println(cmd);
	}
	
	
	
	public void stopMotor() throws Exception {
		cmd("M103 ;extruder off");
	}
	
	public void stopValve() throws Exception {
		cmd("M126 P1 ;valve open");
	}
	
	public void selectExtruder(int materialIndex) throws Exception {
		cmd("T" + materialIndex + "; select new extruder");
		double pwm = 0.5; // getExtruder().getPWM();
		if(pwm >= 0)
			cmd("M113 S" + pwm + "; set extruder PWM");
		else
			cmd("M113; set extruder to use pot for PWM");
	}
	
	public void homeToZeroXYE() throws Exception {
		stopValve();
		cmd("G28 X0 Y0 Z0 ;set x,y,z 0");
	}
	
	public void finishedLayer(/*LayerRules lc*/) throws Exception {
		homeToZeroXYE();
	}
	
	public void setBedTemperature(double temperature) throws Exception {
		cmd("M140 S" + temperature + " ;set bed temperature and return");
	}
	
	public void stabilise() throws Exception
	{
		cmd("M116 ;wait for stability then return");
	}

}
