package com.test.improc;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.slavi.image.DWindowedImage;
import com.slavi.image.PDImageMapBuffer;
import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointListSaver;
import com.slavi.improc.parallel.ExecutePDLowe;
import com.slavi.improc.parallel.ExecutionProfile;
import com.slavi.improc.parallel.PDLoweDetector.Hook;
import com.slavi.util.concurrent.SteppedParallelTaskExecutor;
import com.slavi.util.file.AbsoluteToRelativePathMaker;

public class TestDLowe {

	
	public static class MyThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setPriority(Thread.MIN_PRIORITY);
			return thread;
		}
	}

	public static KeyPointList buildKeyPointFileMultiThreaded(ExecutorService exec, File image) throws Exception {
		DWindowedImage img = new PDImageMapBuffer(image);
		final KeyPointList result = new KeyPointList();
		result.imageSizeX = img.maxX() + 1;
		result.imageSizeY = img.maxY() + 1;
		result.cameraOriginX = result.imageSizeX / 2.0;
		result.cameraOriginY = result.imageSizeY / 2.0;
		result.cameraScale = 1.0 / Math.max(result.imageSizeX, result.imageSizeY);
		result.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;

		Hook hook = new Hook() {
			public synchronized void keyPointCreated(KeyPoint scalePoint) {
				result.items.add(scalePoint);
			}		
		};
		ExecutionProfile profile = ExecutionProfile.suggestExecutionProfile(img.getExtent());
		ExecutePDLowe2 execPDLowe = new ExecutePDLowe2(img, hook);

		Future<Void> ft = new SteppedParallelTaskExecutor<Void>(exec, profile.parallelTasks, execPDLowe).start();
		ft.get();
		return result;
	}
	
	void doTheJob(ExecutorService exec) throws Exception {
		String imagesRootStr = "D:\\Users\\S\\Java\\Images\\Image data\\Beli plast";
		String keyPointFileRootStr = "D:\\Users\\S\\Java\\Images\\work";
		String fileName = "HPIM0319.jpg";

		
		AbsoluteToRelativePathMaker imagesRoot = new AbsoluteToRelativePathMaker(imagesRootStr);
		AbsoluteToRelativePathMaker keyPointFileRoot = new AbsoluteToRelativePathMaker(keyPointFileRootStr);
		File image = imagesRoot.getFullPathFile(fileName);
		System.out.println("Reading KP OLD");
		KeyPointList kpl1 = KeyPointListSaver.readKeyPointFile(exec, imagesRoot, keyPointFileRoot, image);
		System.out.println("Building KP NEW");
		KeyPointList kpl2 = buildKeyPointFileMultiThreaded(exec, image);
		kpl1.compareToList(kpl2);
	}
	
	public static void main(String[] args) throws Exception {
		Runtime runtime = Runtime.getRuntime();
		int numberOfProcessors = runtime.availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(numberOfProcessors + 1,
				new MyThreadFactory());

		try {
			TestDLowe application = new TestDLowe();
			application.doTheJob(exec);
		} finally {
			exec.shutdown();
		}
		System.out.println("Done.");
	}
}
