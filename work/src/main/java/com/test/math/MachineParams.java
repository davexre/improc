package com.test.math;

public class MachineParams {

	public static double DLAMC1() {
		float a, c, one, two;
		int i;
		one = (float)1.0;
		two = (float)2.0;
		a =  one;
		c = one;
		i = 0;
		while (c == one) {
			a *= two;
			c = a + one;
			c -= a;
			i++;
		}		
		System.out.println("I=" + i);
		System.out.println("A=" + a);
		System.out.println("C=" + c);
		return c;
	}
	
	public static void main(String[] args) {
		DLAMC1();
	}

}
