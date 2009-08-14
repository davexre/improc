package com.slavi.improc.parallel;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

import com.slavi.image.DWindowedImage;
import com.slavi.image.PDImageMapBuffer;
import com.slavi.improc.parallel.PDLoweDetector.Hook;
import com.slavi.image.DImageWrapper;
import com.slavi.util.concurrent.SteppedParallelTask;

public class ExecutePDLowe implements SteppedParallelTask<Void> {
	int scale;
	Hook hook;
	ExecutionProfile suggestedProfile;
	DWindowedImage nextLevelBlurredImage;
	
	public ExecutePDLowe(DWindowedImage source, Hook hook, ExecutionProfile suggestedProfile) {
		this.nextLevelBlurredImage = source;
		source = null;
		this.hook = hook;
		this.scale = 1;
		this.suggestedProfile = suggestedProfile;
	}

	public Queue<Callable<Void>> getNextStepTasks() throws Exception {
		if ((nextLevelBlurredImage == null) || (nextLevelBlurredImage.maxX() <= 32)) 
			return null;
		
		LinkedList<Callable<Void>> result = new LinkedList<Callable<Void>>();
		DWindowedImage source = nextLevelBlurredImage;
		Rectangle srcExtent = source.getExtent();
		Rectangle nextLevelBlurredImageExt = new Rectangle(
				srcExtent.x >> 1,
				srcExtent.y >> 1,
				srcExtent.width >> 1,
				srcExtent.height >> 1);
//		DWindowedImageUtils.toImageFile(nextLevelBlurredImage, Const.workDir + "/dlowe_nextlevel_" + scale + "p.png");
		nextLevelBlurredImage = new PDImageMapBuffer(nextLevelBlurredImageExt);
//		nextLevelBlurredImage = new ImageWriteTracker(nextLevelBlurredImage, false, true);

		int dx = (int)Math.ceil((double)srcExtent.width / 
				Math.ceil((double)srcExtent.width / (double)suggestedProfile.destWindowSizeX));
		int dy = (int)Math.ceil((double)srcExtent.height / 
				Math.ceil((double)srcExtent.height / (double)suggestedProfile.destWindowSizeY));
		
		Rectangle rect = new Rectangle(dx, dy);
		rect = PDLoweDetector.getNeededSourceExtent(rect, PDLoweDetector.defaultScaleSpaceLevels);
		
		for (int sminx = 0; sminx < srcExtent.width; sminx += dx) {
			for (int sminy = 0; sminy < srcExtent.height; sminy += dy) {
				Rectangle srcR = new Rectangle(sminx + rect.x, sminy + rect.y, rect.width, rect.height);
				srcR = srcR.intersection(srcExtent);
				Rectangle destR = new Rectangle(sminx, sminy, dx, dy);
				destR = destR.intersection(srcExtent);
				if (srcR.isEmpty() || destR.isEmpty()) {
					throw new RuntimeException("empty");
				}
				DImageWrapper srcW = new DImageWrapper(source, srcR);
				PDLoweDetector task = new PDLoweDetector(srcW, destR, nextLevelBlurredImage, scale, PDLoweDetector.defaultScaleSpaceLevels);
				task.hook = hook;
				result.add(task);
			}
		}
		scale *= 2;
		return result;
	}

	public void onError(Callable<Void> task, Throwable e) {
	}

	public void onFinally() throws Exception {
	}

	public void onPrepare() throws Exception {
	}

	public void onSubtaskFinished(Callable<Void> subtask, Void result) throws Exception {
	}
}
