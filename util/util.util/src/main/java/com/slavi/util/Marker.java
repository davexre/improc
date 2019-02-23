package com.slavi.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used for tracking the memory and CPU used for specific blocks
 * of code.
 * <p>
 * It's main purpose is to help debugging and locating bottleneck points in
 * the code. Calls to Marker.mark may be nested.</p>
 * <p>Usage:</p>
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
 * <p>The output of this code is:</p>
 * <code>
 *   Set block marker "Lengthy job", memory used 23.4 M
 *   ...
 *   Set block marker "Inner lengthy job", memory used 24.0 M
 *   ...
 *   Block "Inner lengthy job" elapsed 1 minute 2.3 seconds, memory used 25.2 M, memory delta 1.2 M
 *   ...
 *   Block "Lengthy job" elapsed 1 hour 23 minutes 45.6 seconds, memory used 34.5 M, memory delta 11.1 M
 * </code>
 * <p>Usage with try-with-resources</p>
 * <code>
 *   try (Marker m = new Marker("optional makrer name")) {
 *     ...
 *   }
 * </code>
 */
public class Marker implements AutoCloseable {

	static class TinyGarbage {
		public void finalize() {
			garbageCounter.incrementAndGet();
			new TinyGarbage();
		}
	}

	static final Stack<Marker> marks;

	static final AtomicInteger garbageCounter;

	static final AtomicInteger markerId = new AtomicInteger();

	static final Logger log = LoggerFactory.getLogger(Marker.class);

	static {
		marks = new Stack();
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
		mark("Marker");
	}

	/**
	 * Puts a marker in the marker stack. Every call to this method should have
	 * a corresponding call to {@link #release()}. Calls to this method
	 * may be nested.
	 * @param markName	the name of the marker
	 */
	public synchronized static void mark(String markName) {
		Marker marker = new Marker(markName);
		log.info("Set block marker {}, memory used {}", markName, Util.getFormatBytes(marker.memoryUsedStart));
		marks.push(marker);
	}

	public synchronized static Marker release() {
		if (marks.empty())
			throw new RuntimeException(
					"TimeStiatistics: Called release() without a matching call to mark()");
		Marker m = marks.pop();
		m.stop();
		log.info(m.toString());
		return m;
	}

	public synchronized static void releaseAndMark() {
		release();
		mark();
	}

	public synchronized static void releaseAndMark(String markName) {
		release();
		mark(markName);
	}

	public String name;

	public final int garbageCounterMark;

	public int garbageCounterDelta;

	public final long start;

	public long end = 0;

	public final long memoryUsedStart;

	public long memoryUsedEnd;

	public Marker() {
		this("Marker");
	}

	public Marker(String name) {
		if (name == null)
			this.name = Integer.toString(markerId.getAndIncrement());
		else
			this.name = name + " (" + Integer.toString(markerId.getAndIncrement()) + ")";
		MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		memoryUsedStart = memoryUsage.getUsed();
		garbageCounterMark = garbageCounter.get();
		start = System.currentTimeMillis();
	}

	public void stop() {
		if (end == 0L) {
			garbageCounterDelta = garbageCounter.get() - garbageCounterMark;
			end = System.currentTimeMillis();
			MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
			memoryUsedEnd = memoryUsage.getUsed();
		}
	}

	StringBuilder format() {
		StringBuilder sb = new StringBuilder("Block ");
		sb.append(name).append(" elapsed ").append(Util.getFormatedMilliseconds(end - start));
		if (garbageCounterDelta == 0)
			sb.append(", GC not invoked");
		else
			sb.append(", GC invoked ").append(garbageCounterDelta).append(" times");
		return sb;
	}

	public String toString() {
		return format()
			.append(", memory used ").append(Util.getFormatBytes(memoryUsedEnd))
			.append(", memory delta ").append(Util.getFormatBytes(memoryUsedEnd - memoryUsedStart))
			.toString();
	}

	@Override
	public void close() throws Exception {
		if (end == 0L) {
			stop();
			log.info(format().toString());
		}
	}
}
