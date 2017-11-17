package com.slavi.improc.ui;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.image.DWindowedImageUtils;
import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.SafeImage;
import com.slavi.util.concurrent.TaskSet;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.swt.SwtUtil;

public class WriteImagesWithKeyPoint implements Callable<Void> {

	ExecutorService exec;	
	List<KeyPointList> images;
	AbsoluteToRelativePathMaker imagesRoot;
	String outputDir;

	public WriteImagesWithKeyPoint(
			ExecutorService exec,
			List<KeyPointList> images,
			AbsoluteToRelativePathMaker imagesRoot,
			String outputDir) {
		this.exec = exec;
		this.images = images;
		this.imagesRoot = imagesRoot;
		this.outputDir = outputDir;
	}
	
	AtomicInteger counter = new AtomicInteger(0);
	public Void call() throws Exception {
		class WriteImage implements Runnable {
			KeyPointList l;
			
			WriteImage(KeyPointList l) {
				this.l = l;
			}
			public void run() {
				try {
					SafeImage im = new SafeImage(new FileInputStream(l.imageFileStamp.getFile()));
					for (int i = 0; i < im.imageSizeX; i++)
						for (int j = 0; j < im.imageSizeY; j++) {
							int col = im.getRGB(i, j);
							col = DWindowedImageUtils.getGrayColor(col);
							im.setRGB(i, j, col);
						}
					for (KeyPoint p : l.items) {
						int color = SafeImage.colors[p.dogLevel % SafeImage.colors.length];
						im.drawCross((int)p.getDoubleX(), (int)p.getDoubleY(), color, -1);
					}
					String fou = outputDir + "/" + imagesRoot.getRelativePath(l.imageFileStamp.getFile());
					new File(fou).getParentFile().mkdirs();
					im.save(fou);
				} catch (Exception e) {
					throw new CompletionException(e);
				} finally {
					SwtUtil.activeWaitDialogSetStatus("Writing images...", counter.getAndIncrement());
				}
			}
		}
		
		TaskSet ts = new TaskSet(exec);
		for (int i = 0; i < images.size(); i++) {
			KeyPointList image = images.get(i);
			ts.add(new WriteImage(image));
		}
		ts.run().get();
		return null;
	}
}
