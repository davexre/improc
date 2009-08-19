package com.test.improc;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

import com.slavi.image.DImageWrapper;
import com.slavi.image.DWindowedImage;
import com.slavi.image.PDImageMapBuffer;
import com.slavi.improc.parallel.ExecutionProfile;
import com.slavi.improc.parallel.PDLoweDetector;
import com.slavi.improc.parallel.PDLoweDetector.Hook;
import com.slavi.util.concurrent.SteppedParallelTask;

public class ExecutePDLowe2 implements SteppedParallelTask<Void> {
	int scale;
	Hook hook;
	DWindowedImage nextLevelBlurredImage;
	
	public ExecutePDLowe2(DWindowedImage source, Hook hook) {
		this.nextLevelBlurredImage = source;
		source = null;
		this.hook = hook;
		this.scale = 1;
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
		for (int i = nextLevelBlurredImageExt.width - 1; i >= 0; i--) {
			int atX = i << 1;
			for (int j = nextLevelBlurredImageExt.height - 1; j >= 0; j--) {
				int atY = j << 1;
				double col = (
					source.getPixel(atX, atY) + 
					source.getPixel(atX, atY + 1) + 
					source.getPixel(atX + 1, atY + 1) + 
					source.getPixel(atX + 1, atY)) / 4.0;
				nextLevelBlurredImage.setPixel(i, j, col);
			}
		}
		
//		nextLevelBlurredImage = new ImageWriteTracker(nextLevelBlurredImage, false, true);

		ExecutionProfile suggestedProfile = ExecutionProfile.suggestExecutionProfile(srcExtent);

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
				PDLoweDetector2 task = new PDLoweDetector2(srcW, destR, scale, PDLoweDetector.defaultScaleSpaceLevels);
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
