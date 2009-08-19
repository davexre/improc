package com.slavi.improc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.slavi.image.DWindowedImage;
import com.slavi.image.PDImageMapBuffer;
import com.slavi.improc.parallel.ExecutionProfile;
import com.slavi.improc.parallel.PDLoweDetector.Hook;
import com.slavi.io.txt.TXTKDTree;
import com.slavi.util.Util;
import com.slavi.util.concurrent.SteppedParallelTaskExecutor;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FileStamp;
import com.test.improc.ExecutePDLowe2;

public class KeyPointListSaver extends TXTKDTree<KeyPoint> {
	final KeyPointList keyPointList;

	public KeyPointListSaver(KeyPointList keyPointList) {
		this.keyPointList = keyPointList;
	}

	public KeyPoint nodeFromString(String source) {
		KeyPoint result = KeyPoint.fromString(source);
		result.keyPointList = keyPointList;
		return result;
	}

	public String nodeToString(KeyPoint node) {
		return node.toString();
	}

	public static class ListenerImpl implements Hook {
		public KeyPointList scalePointList;
		
		public ListenerImpl(KeyPointList spl) {
			this.scalePointList = spl;
		}
		
		public synchronized void keyPointCreated(KeyPoint scalePoint) {
			scalePointList.items.add(scalePoint);
		}		
	}
	
	public static void updateKeyPointFileIfNecessary(
			ExecutorService exec,
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			File image) throws Exception {
		doUpdateKeyPointFileIfNecessary(exec, rootImagesDir, rootKeyPointFileDir, image);
	}
	
	public static File getFile(AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			File image) {
		return new File(Util.changeFileExtension(
			rootKeyPointFileDir.getFullPath(
				rootImagesDir.getRelativePath(image, false)), "spf"));
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
	
	private static KeyPointList doUpdateKeyPointFileIfNecessary(
			ExecutorService exec,
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			File image) throws Exception {
		File kplFile = getFile(rootImagesDir, rootKeyPointFileDir, image);
		
		try {
			if (kplFile.isFile()) {
				BufferedReader fin = new BufferedReader(new FileReader(kplFile));
				if (KeyPointList.fileHeader.equals(fin.readLine())) {
					FileStamp fs = FileStamp.fromString(fin.readLine(), rootImagesDir);
					if (!fs.isModified()) {
						if (fs.getFile().getCanonicalPath().equals(image.getCanonicalPath())) {
							// The image file is not modified, so don't build
							return null;
						}
					}
				}
			}
		} catch (IOException e) {
		}
		
		KeyPointList result = buildKeyPointFileMultiThreaded(exec, image);

		String relativeImageName = rootImagesDir.getRelativePath(image, false);
		result.imageFileStamp = new FileStamp(relativeImageName, rootImagesDir);
		kplFile.getParentFile().mkdirs();
		PrintWriter fou = new PrintWriter(kplFile);
		fou.println(KeyPointList.fileHeader);
		result.toTextStream(fou);
		if (fou.checkError()) {
			fou.close();
			throw new IOException("Write to file failed.");
		}
		fou.close();
		return result;
	}
	
	public static KeyPointList readKeyPointFile(
			ExecutorService exec,
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			File image) throws Exception {
		KeyPointList result = doUpdateKeyPointFileIfNecessary(exec, rootImagesDir, rootKeyPointFileDir, image);
		if (result != null)
			return result;

		File kplFile = getFile(rootImagesDir, rootKeyPointFileDir, image);
		BufferedReader fin = new BufferedReader(new FileReader(kplFile));
		fin.readLine(); // Skip header.
		result = KeyPointList.fromTextStream(fin, rootImagesDir);
		return result;
	}
}
