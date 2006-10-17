package com.test;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;

public class MemoryTest {

	public static void show() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
		System.out.println("Used memory: " + (memoryUsage.getUsed() / 1000000) + " Mb");
		System.out.println(memoryUsage);
	}
	
	public static void occupyMem(int count) {
		if (count == 0)
			return;
		double a[] = new double[1000000];
		a[0] = 5;
		System.out.println(a[0]);
		occupyMem(count-1);
	}
	
	public static void main(String[] args) {
//		show();
//		occupyMem(3);
//		show();
		
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		System.out.println(os.getAvailableProcessors());
	}

}
