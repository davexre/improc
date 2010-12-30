package com.slavi.improc;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import com.slavi.image.DImageWrapper;
import com.slavi.image.DWindowedImage;
import com.slavi.image.PDImageMapBuffer;
import com.slavi.improc.parallel.ExecutionProfile;
import com.slavi.improc.parallel.PDLoweDetector;
import com.slavi.improc.parallel.PDLoweDetector.Hook;
import com.slavi.io.txt.TXTKDTree;
import com.slavi.util.Util;
import com.slavi.util.concurrent.TaskSetExecutor;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FileStamp;
import com.test.improc.PDLoweDetector2;

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
				rootImagesDir.getRelativePath(image, false)), ".spf.z"));
	}
	
	public static KeyPointList buildKeyPointFileMultiThreaded(ExecutorService exec, File image) throws Exception {
		final KeyPointList result = new KeyPointList();
		BufferedImage bi = ImageIO.read(image);
		result.imageSizeX = bi.getWidth();
		result.imageSizeY = bi.getHeight();
		result.makeHistogram(bi);
		DWindowedImage img = new PDImageMapBuffer(bi);
		bi = null;
		result.cameraOriginX = result.imageSizeX / 2.0;
		result.cameraOriginY = result.imageSizeY / 2.0;
		result.cameraScale = 1.0 / Math.max(result.imageSizeX, result.imageSizeY);
		result.scaleZ = KeyPointList.defaultCameraFOV_to_ScaleZ;

		Hook hook = new Hook() {
			public synchronized void keyPointCreated(KeyPoint scalePoint) {
				result.items.add(scalePoint);
			}		
		};
		
		int scale = 1;
		TaskSetExecutor ts = new TaskSetExecutor(exec);
		try {
			while (img.maxX() > 32) {
				DWindowedImage source = img;
				Rectangle srcExtent = source.getExtent();
				Rectangle nextLevelBlurredImageExt = new Rectangle(
						srcExtent.x >> 1,
						srcExtent.y >> 1,
						srcExtent.width >> 1,
						srcExtent.height >> 1);
	//			DWindowedImageUtils.toImageFile(img, Const.workDir + "/dlowe_nextlevel_" + scale + "p.png");
				img = new PDImageMapBuffer(nextLevelBlurredImageExt);
				for (int i = nextLevelBlurredImageExt.width - 1; i >= 0; i--) {
					int atX = i << 1;
					for (int j = nextLevelBlurredImageExt.height - 1; j >= 0; j--) {
						int atY = j << 1;
						double col = (
							source.getPixel(atX, atY) + 
							source.getPixel(atX, atY + 1) + 
							source.getPixel(atX + 1, atY + 1) + 
							source.getPixel(atX + 1, atY)) / 4.0;
						img.setPixel(i, j, col);
					}
				}
				
	//			nextLevelBlurredImage = new ImageWriteTracker(nextLevelBlurredImage, false, true);
	
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
						ts.add(task);
					}
				}
				scale *= 2;
			}
		} finally {
			ts.addFinished();
		}
		ts.get();
		return result;
	}
	
	private static KeyPointList doUpdateKeyPointFileIfNecessary(
			ExecutorService exec,
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			File image) throws Exception {
		File kplFile = getFile(rootImagesDir, rootKeyPointFileDir, image);

		if (kplFile.isFile()) {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(kplFile));
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null) {
				if ("KeyPointFile.txt".equals(entry.getName()))
					break;
			}
			if (entry != null) {
				BufferedReader fin = new BufferedReader(new InputStreamReader(zis));
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
			zis.close();
		}
		
		KeyPointList result = buildKeyPointFileMultiThreaded(exec, image);

		String relativeImageName = rootImagesDir.getRelativePath(image, false);
		result.imageFileStamp = new FileStamp(relativeImageName, rootImagesDir);
		kplFile.getParentFile().mkdirs();
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(kplFile));
		zos.putNextEntry(new ZipEntry("KeyPointFile.txt"));
		PrintWriter out = new PrintWriter(zos);
		out.println(KeyPointList.fileHeader);
		result.toTextStream(out);
		if (out.checkError()) {
			out.close();
			throw new IOException("Write to file failed.");
		}
		out.flush();
		zos.closeEntry();
		zos.close();
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
		if (kplFile.isFile()) {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(kplFile));
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null) {
				if ("KeyPointFile.txt".equals(entry.getName()))
					break;
			}
			if (entry != null) {
				BufferedReader fin = new BufferedReader(new InputStreamReader(zis));
				fin.readLine(); // Skip header.
				result = KeyPointList.fromTextStream(fin, rootImagesDir);
			}
			zis.close();
		}
		return result;
	}
}
