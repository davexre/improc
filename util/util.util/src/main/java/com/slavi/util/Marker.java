package com.slavi.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

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

	public static class State {
		public String id;

		public int garbageCounterMark;
		
		public int garbageCounterDelta;

		public long start;
		
		public long end;
		
		public long memoryUsedStart;

		public long memoryUsedEnd;
		
		public String toString() {
			return "Block \"" + id + "\" elapsed " + 
					Util.getFormatedMilliseconds(end - start) + 
					",\n  memory used " + Util.getFormatBytes(memoryUsedEnd) + 
					", memory delta " + Util.getFormatBytes(memoryUsedEnd - memoryUsedStart) +
					(garbageCounterDelta == 0 ? 
							",\n  garbage collection NOT invoked" : 
							",\n  garbage collection invoked " + Integer.toString(garbageCounterDelta) + " times");
		}
	}

	static class TinyGarbage {
		public void finalize() {
			garbageCounter.incrementAndGet();
			new TinyGarbage();
		}
	}
	
	static final Stack<State> marks;

	static final AtomicInteger garbageCounter;
	
	static int markerId;
	
	static {
		marks = new Stack<State>();
		markerId = 1;
		garbageCounter = new AtomicInteger(0);
		new TinyGarbage();
	}
	
	/**
	 * Returns the internally maintained garbage collection counter. 
	 */
	public static int getGarbageCollectionCounter() {
		return garbageCounter.get();
	}
	
	/**
	 * Puts a marker in the marker stack with the default name "Marker 1", "Marker 2", ..., etc.
	 */
	public synchronized static void mark() {
		mark("Marker " + Integer.toString(markerId++));
	}

	/**
	 * Puts a marker in the marker stack. Every call to this method should have 
	 * a corresponding call to {@link #release()}. Calls to this method 
	 * may be nested.
	 * @param markName	the name of the marker 
	 */
	public synchronized static void mark(String markName) {
		State marker = new State();
		marker.id = markName;
		marker.start = System.currentTimeMillis();
		MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		marker.memoryUsedStart = memoryUsage.getUsed();
		marker.garbageCounterMark = garbageCounter.get();

		marks.push(marker);
		System.out.println("Set block marker \"" + markName + "\", memory used " + Util.getFormatBytes(marker.memoryUsedStart));
	}

	public synchronized static State release() {
		long now = System.currentTimeMillis();
		if (marks.empty())
			throw new RuntimeException(
					"TimeStiatistics: Called release() without a matching call to mark()");
		State m = marks.pop();
		m.garbageCounterDelta = garbageCounter.get() - m.garbageCounterMark;
		m.end = now;
		MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		m.memoryUsedEnd = memoryUsage.getUsed();
		System.out.println(m.toString());
		return m;
	}
}