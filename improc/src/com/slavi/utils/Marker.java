package com.slavi.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Stack;

/**
 * This class is used for tracking the memory and CPU used for specific blocks
 * of code.
 * <p>
 * It's main purpose is to help debugging and locating bottleneck points in
 * the code. Calls to Marker.mark may be nested.  
 * <p>
 * Usage:
 * <p>
 * <pre>
 *   Marker.mark("Lengthy job");
 *   ... // some lengthy job
 *   Marker.mark("Inner lengthy job");
 *   ...
 *   Marker.release();
 *   ...
 *   Marker.release();
 *   ...
 * </pre>
 * <p>The output of this code is:
 * <code>
 *   Set block marker "Lengthy job", memory used 23.4 M
 *   ...
 *   Set block marker "Inner lengthy job", memory used 24.0 M
 *   ...
 *   Block "Inner lengthy job" elapsed 1 minute 2.3 seconds, memory used 25.2 M, memory delta 1.2 M
 *   ...
 *   Block "Lengthy job" elapsed 1 hour 23 minutes 45.6 seconds, memory used 34.5 M, memory delta 11.1 M
 * </code>
 */
public class Marker {

	protected static class InternalMarker {
		public String id;

		public long mark;
		
		public long memoryUsed;
	}

	protected static final Stack marks = new Stack();

	private static int markerId = 1;
	
	/**
	 * Puts a marker in the marker stack with the default name "Marker 1", "Marker 2", ..., etc.
	 */
	public synchronized static void mark() {
		mark("Marker " + Integer.toString(markerId++));
	}

	/**
	 * Puts a marker in the marker stack. Every call to this method should have 
	 * a correspondin call to {@link #release()}. Calls to this method 
	 * may be nested.
	 * @param markName	the name of the marker 
	 */
	public synchronized static void mark(String markName) {
		InternalMarker marker = new InternalMarker();
		marker.id = markName;
		marker.mark = System.currentTimeMillis();
		MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		marker.memoryUsed = memoryUsage.getUsed();

		marks.push(marker);
		System.out.println("Set block marker \"" + markName + "\", memory used " + Utl.getFormatBytes(marker.memoryUsed));
	}

	public synchronized static void release() {
		long now = System.currentTimeMillis();
		if (marks.empty())
			throw new RuntimeException(
					"TimeStiatistics: Called release() without a matching call to mark()");
		InternalMarker m = (InternalMarker)marks.pop();
		MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		System.out.println("Block \"" + m.id + "\" elapsed "
				+ Utl.getFormatedMilliseconds(now - m.mark) + 
				", memory used " + Utl.getFormatBytes(memoryUsage.getUsed()) + 
				", memory delta " + Utl.getFormatBytes(memoryUsage.getUsed() - m.memoryUsed));
	}
}
