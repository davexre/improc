package com.slavi.improc.myadjust.render;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.SafeImage;
import com.slavi.improc.myadjust.GeneratePanoramas;
import com.slavi.util.swt.SwtUtil;

public abstract class AbstractParallelRender implements Callable<Void> {
	GeneratePanoramas parent;
	
	AtomicInteger rowsProcessed;
	Map<KeyPointList, SafeImage> imageData;
	SafeImage outImageColor;
	SafeImage outImageColor2;
	SafeImage outImageMask;

	public AbstractParallelRender(GeneratePanoramas parent) {
		this.parent = parent;
		rowsProcessed = parent.rowsProcessed;
		imageData = parent.imageData;
		outImageColor = parent.outImageColor;
		outImageColor2 = parent.outImageColor2;
		outImageMask = parent.outImageMask;		
	}

	public abstract void renderRow(int row) throws Exception;
	
	public Void call() throws Exception {
		for (int row = rowsProcessed.getAndIncrement(); row < outImageColor.imageSizeY; row = rowsProcessed.getAndIncrement()) {
			renderRow(row);
			if (row % 10 == 0) {
				SwtUtil.activeWaitDialogSetStatus(null, (100 * row) / outImageColor.imageSizeY);
			}				
		}
		return null;
	}
}
