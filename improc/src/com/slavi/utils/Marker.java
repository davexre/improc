package com.slavi.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Locale;
import java.util.Stack;

public class Marker {

	protected static class InternalMarker {
		public String id;

		public long mark;
		
		public long memoryUsed;
	}

	protected static final Stack marks = new Stack();

	private static int markerId = 1;
	
	public synchronized static void mark() {
		mark("Marker " + Integer.toString(markerId++));
	}

	public synchronized static void mark(String markName) {
		InternalMarker marker = new InternalMarker();
		marker.id = markName;
		marker.mark = System.currentTimeMillis();
		MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		marker.memoryUsed = memoryUsage.getUsed();

		marks.push(marker);
		System.out.println("Set block marker \"" + markName + "\", memory used " + formatBytes(marker.memoryUsed));
	}

	public static String formatMillis(long millis) {
		final long[] divisors = { (1000 * 60 * 60 * 24), (1000 * 60 * 60),
				(1000 * 60), (1000) };
		final String[][] texts = { { " day ", " days " },
				{ " hour ", " hours " }, { " minute ", " minutes " },
				{ " second", " seconds" } };
		String s = new String("");
		for (int i = 0; i < 3; i++) {
			long tmp = millis / divisors[i];
			millis %= divisors[i];
			if (tmp > 0)
				s += Long.toString(tmp) + texts[i][tmp == 1 ? 0 : 1];
		}
		return s + String.format(Locale.US, "%1$.3f", new Object[] { new Double((double) (millis) / divisors[3]) } )
				+ texts[3][1];
	}
	
	public static String formatBytes(long sizeInBytes) {
		String dim = "bytes";
		double size = sizeInBytes;
		if (Math.abs(size) >= 1000.0) {
			dim = "K";
			size /= 1000.0;
		}
		if (Math.abs(size) >= 800.0) {
			dim = "M";
			size /= 1000.0;
		}
		if (Math.abs(size) >= 800.0) {
			dim = "G";
			size /= 1000.0;
		}
		if (Math.abs(size) >= 800.0) {
			dim = "T";
			size /= 1000.0;
		}
		if (Math.floor(size) == size) 
			return String.format("%d %s", new Object[] { new Integer((int)size), dim } );
		return String.format(Locale.US, "%.1f %s", new Object[] { new Double(size), dim } );
	}

	public synchronized static void release() {
		long now = System.currentTimeMillis();
		if (marks.empty())
			throw new RuntimeException(
					"TimeStiatistics: Called release() without a matching call to mark()");
		InternalMarker m = (InternalMarker)marks.pop();
		MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		System.out.println("Block \"" + m.id + "\" elapsed "
				+ formatMillis(now - m.mark) + 
				", memory used " + formatBytes(memoryUsage.getUsed()) + 
				", memory delta " + formatBytes(memoryUsage.getUsed() - m.memoryUsed));
	}
}
