package com.slavi.improc.parallel;

import java.awt.Rectangle;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import com.slavi.image.DImageWrapper;
import com.slavi.image.DWindowedImage;
import com.slavi.image.ImageWriteTracker;
import com.slavi.image.PDImageMapBuffer;
import com.slavi.improc.singletreaded.DLoweDetector.Hook;

public class ExecutePDLowe {
	public static ExecutionProfile makeTasks(DWindowedImage source, int scale, Hook hook) {
		return makeTasks(source, scale, hook, ExecutionProfile.suggestExecutionProfile(source.getExtent()));
	}
	
	public static ExecutionProfile makeOneTaskProfile(Rectangle srcExtent) {
		ExecutionProfile result = new ExecutionProfile();
//		result.sourceExtentX = srcExtent.width;
//		result.sourceExtentY = srcExtent.height;
		
		Runtime runtime = Runtime.getRuntime();
		result.numberOfProcessors = runtime.availableProcessors();
		long usedMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		result.availableMemory = runtime.maxMemory() - usedMemory;
		
		result.parallelTasks = 1;
		result.destWindowSizeX = srcExtent.width;
		result.destWindowSizeY = srcExtent.height;
		return result;
	}
	
	public static ExecutionProfile makeTasks(DWindowedImage source, int scale, Hook hook, ExecutionProfile suggestedProfile) {
		Rectangle srcExtent = source.getExtent();
		ExecutionProfile profile = suggestedProfile;
		profile.tasks = new ArrayList<Runnable>();
		Rectangle nextLevelBlurredImageExt = new Rectangle(
				(srcExtent.x + 1) >> 1,
				(srcExtent.y + 1) >> 1,
				(srcExtent.width + 1) >> 1,
				(srcExtent.height + 1) >> 1);
		profile.nextLevelBlurredImage = new PDImageMapBuffer(nextLevelBlurredImageExt);
		profile.nextLevelBlurredImage = new ImageWriteTracker(profile.nextLevelBlurredImage, false, true);
		
		Rectangle rect = new Rectangle(suggestedProfile.destWindowSizeX, suggestedProfile.destWindowSizeY);
		rect = PDLoweDetector.getNeededSourceExtent(rect, PDLoweDetector.defaultScaleSpaceLevels);
		
		for (int sminx = 0; sminx < srcExtent.width; sminx += suggestedProfile.destWindowSizeX) {
			for (int sminy = 0; sminy < srcExtent.height; sminy += suggestedProfile.destWindowSizeY) {
				Rectangle srcR = new Rectangle(sminx + rect.x, sminy + rect.y, rect.width, rect.height);
				srcR = srcR.intersection(srcExtent);
				Rectangle destR = new Rectangle(sminx, sminy, suggestedProfile.destWindowSizeX, suggestedProfile.destWindowSizeY);
				destR = destR.intersection(srcExtent);
				if (srcR.isEmpty() || destR.isEmpty()) {
					throw new RuntimeException("empty");
				}
				DImageWrapper srcW = new DImageWrapper(source, srcR);
				PDLoweDetector task = new PDLoweDetector(srcW, destR, profile.nextLevelBlurredImage, scale, PDLoweDetector.defaultScaleSpaceLevels);
				task.hook = hook;
				profile.tasks.add(task);
			}
		}
		return profile;
	}
}
