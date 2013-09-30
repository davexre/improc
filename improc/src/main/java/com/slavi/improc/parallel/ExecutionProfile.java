package com.slavi.improc.parallel;

import java.awt.Rectangle;
import java.lang.management.ManagementFactory;

import com.slavi.util.Util;

public class ExecutionProfile {
	public int parallelTasks;
	public int numberOfProcessors;
	public long availableMemory;
//	public int sourceExtentX;
//	public int sourceExtentY;
	public int destWindowSizeX;
	public int destWindowSizeY;
//	public List<Runnable> tasks;
//	public DWindowedImage nextLevelBlurredImage;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Parallel tasks          :");		sb.append(parallelTasks);
//		sb.append("\nTotal number of tasks   :");   sb.append(tasks == null ? 0 : tasks.size());
		sb.append("\nNumber of processors    :");	sb.append(numberOfProcessors);
		sb.append("\nAvailable memory        :");	sb.append(Util.getFormatBytes(availableMemory));
//		sb.append("\nSource extent X         :");	sb.append(sourceExtentX);
//		sb.append("\nSource extent Y         :");	sb.append(sourceExtentY);
		sb.append("\nWindow extent X         :");	sb.append(destWindowSizeX);
		sb.append("\nWindow extent Y         :");	sb.append(destWindowSizeY);
		return sb.toString();
	}
	
	public static ExecutionProfile suggestExecutionProfile(Rectangle srcExtent) {
		return suggestExecutionProfile(srcExtent, 0.4, 100);
	}

	/*
	 * Intermediate image buffers needed as in method #doIt()
	 * blurred0, blurred1, blurred2, 
	 * magnitude, direction,
	 * DOGs[0], DOGs[1], DOGs[2]
	 */
	private static final int numberOfImageBuffers = 9;

	/*
	 * Pixel is defined as double. Size of double = 8 bytes.
	 */
	private static final int sizeOfSingleImageMapPixel = 8;
	
	private static final long requiredMemoryPerPixel = numberOfImageBuffers * sizeOfSingleImageMapPixel; 
	
	/**
	 * Creates an execution profile taking into account the number of CPUs and available memory. 
	 * @param srcExtent					Minimum size (in pixels) for a task window (ex. 100)
	 * @param maxMemoryUsageAllowed		Value is in % of the available memory whereas 0.9 means 90% (ex. 0.7)
	 */
	public static ExecutionProfile suggestExecutionProfile(Rectangle srcExtent, double maxMemoryUsageAllowed, int userSpecifiedMinSizeOfTaskWindow) {
		ExecutionProfile result = new ExecutionProfile();
//		result.sourceExtentX = srcExtent.width;
//		result.sourceExtentY = srcExtent.height;
		
		maxMemoryUsageAllowed = Math.max(0.1, Math.min(0.9, maxMemoryUsageAllowed));
		userSpecifiedMinSizeOfTaskWindow = Math.max(100, userSpecifiedMinSizeOfTaskWindow);
		
		Runtime runtime = Runtime.getRuntime();
		result.numberOfProcessors = runtime.availableProcessors();
		long usedMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		result.availableMemory = runtime.maxMemory() - usedMemory;
		
		result.parallelTasks = Math.max(result.numberOfProcessors, 1);
		long memPerParallelTask = (long)(result.availableMemory * maxMemoryUsageAllowed) / result.parallelTasks;
		
		int tasksForExtent = (int) Math.ceil( 
			(double)(srcExtent.width * srcExtent.height * requiredMemoryPerPixel) / 
			(double) memPerParallelTask);
		tasksForExtent = Math.max(tasksForExtent, result.parallelTasks);
		int divisionsX = Math.max(1, (int)Math.round(
				Math.sqrt((double) tasksForExtent * srcExtent.width / (double) srcExtent.height)));
		int divisionsY = (int)Math.ceil((double)tasksForExtent / divisionsX);
		
		result.destWindowSizeX = (int)Math.max(userSpecifiedMinSizeOfTaskWindow, 
				Math.ceil((double)srcExtent.width / divisionsX));
		result.destWindowSizeY = (int)Math.max(userSpecifiedMinSizeOfTaskWindow, 
				Math.ceil((double)srcExtent.height / divisionsY));
		return result;
	}

}
