package anoncvs;

import java.awt.Rectangle;

public class Dummy {
	private static final int numberOfImageBuffers = 8;
	/*
	 * Pixel is defined as double. Size of double = 8 bytes.
	 */
	private static final int sizeOfSingleImageMapPixel = 8;

	private static final long requiredMemoryPerPixel = numberOfImageBuffers * sizeOfSingleImageMapPixel; 

	public static void main(String[] args) {
		Rectangle srcExtent = new Rectangle(100, 2000);
		double maxMemoryUsageAllowed = 0.7; // Value is in % if the available memory
		
//		Runtime runtime = Runtime.getRuntime();
//		long memoryAvailable = runtime.maxMemory() + runtime.totalMemory() - runtime.freeMemory();
//		int numberOfProcessors = runtime.availableProcessors();
		
		int userSpecifiedMinSizeOfTaskWindow = 120;
		long memoryAvailable = 1000 * 10 * 1;
		int numberOfProcessors = 2;
		
		int numberOfParallelTasks = Math.max(numberOfProcessors + 1, 2);
		long memPerParallelTask = (long)(memoryAvailable * maxMemoryUsageAllowed) / numberOfParallelTasks;
		int tasksForExtent = (int) Math.max(numberOfParallelTasks, Math.ceil( 
			(double)(srcExtent.width * srcExtent.height * requiredMemoryPerPixel) / 
			(double) memPerParallelTask));
		int divisionsX = Math.max(1, (int)Math.round(
				Math.sqrt(tasksForExtent * srcExtent.width / srcExtent.height)));
		int divisionsY = (int)Math.ceil((double)tasksForExtent / divisionsX);
		
		int minSizeOfTaskWindow = Math.min(userSpecifiedMinSizeOfTaskWindow,
				(int)Math.sqrt((double)memoryAvailable / requiredMemoryPerPixel));
		minSizeOfTaskWindow = Math.max(100, minSizeOfTaskWindow);
		
		divisionsX = Math.max(1, Math.min(divisionsX, srcExtent.width / minSizeOfTaskWindow));
		divisionsY = Math.max(1, Math.min(divisionsY, srcExtent.height / minSizeOfTaskWindow));
		
		int divSizeX = srcExtent.width / divisionsX;
		int divSizeY = srcExtent.height / divisionsY;
		
		System.out.format("Tasks      %10d\n", tasksForExtent);
		System.out.format("MinDivSize %10d\n", minSizeOfTaskWindow);
		System.out.format("DivisionsX %10d\n", divisionsX);
		System.out.format("DivisionsY %10d\n", divisionsY);
		System.out.format("Num divs   %10d\n", divisionsX * divisionsY);
		System.out.format("DivSizeX   %10d\n", divSizeX);
		System.out.format("DivSizeY   %10d\n", divSizeY);
		System.out.format("Parallel   %10d\n", numberOfParallelTasks);
		System.out.format("MemPerDiv  %10d\n", divSizeX * divSizeY * requiredMemoryPerPixel);
		System.out.format("maxMem     %10d\n", numberOfParallelTasks * divSizeX * divSizeY * requiredMemoryPerPixel);
	}
}
