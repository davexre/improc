package com.slavi.improc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.slavi.image.DWindowedImage;
import com.slavi.image.PDImageMapBuffer;
import com.slavi.improc.parallel.ExecutePDLowe;
import com.slavi.improc.parallel.ExecutionProfile;
import com.slavi.improc.singletreaded.DLoweDetector;
import com.slavi.improc.singletreaded.DLoweDetector.Hook;
import com.slavi.util.Utl;
import com.slavi.util.concurrent.SteppedParallelTaskExecutor;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FileStamp;
import com.slavi.util.tree.KDNodeSaver;

public class KeyPointListSaver extends KDNodeSaver<KeyPoint> {
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

	private static class ListenerImpl implements Hook {
		public KeyPointList scalePointList;
		
		public ListenerImpl(KeyPointList spl) {
			this.scalePointList = spl;
		}
		
		public synchronized void keyPointCreated(KeyPoint scalePoint) {
			scalePoint.keyPointList = scalePointList;
			scalePointList.add(scalePoint);
		}		
	}
	
	public static void updateKeyPointFileIfNecessary(
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			File image) throws Exception {
		doUpdateKeyPointFileIfNecessary(rootImagesDir, rootKeyPointFileDir, image);
	}
	
	public static File getFile(AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			File image) {
		return new File(Utl.chageFileExtension(
			rootKeyPointFileDir.getFullPath(
				rootImagesDir.getRelativePath(image, false)), "spf"));
	}
	
	public static KeyPointList buildKeyPointFileSingleThreaded(File image) throws Exception {
		DImageMap img = new DImageMap(image);
		KeyPointList result = new KeyPointList();
		result.imageSizeX = img.getSizeX();
		result.imageSizeY = img.getSizeY();
		DLoweDetector d = new DLoweDetector();
		d.hook = new ListenerImpl(result);
		d.DetectFeatures(img, 3, 32);
		return result;
	}
	
	public static KeyPointList buildKeyPointFileMultiThreaded(File image) throws Exception {
		DWindowedImage img = new PDImageMapBuffer(image);
		final KeyPointList result = new KeyPointList();
		result.imageSizeX = img.maxX() + 1;
		result.imageSizeY = img.maxY() + 1;

		result.imageSizeX = img.maxX() + 1;
		result.imageSizeY = img.maxY() + 1;

		Hook hook = new Hook() {
			public synchronized void keyPointCreated(KeyPoint scalePoint) {
				scalePoint.keyPointList = result;
				result.add(scalePoint);
			}		
		};
		ExecutionProfile profile = ExecutionProfile.suggestExecutionProfile(img.getExtent());
		ExecutePDLowe execPDLowe = new ExecutePDLowe(img, hook, profile);

		ExecutorService exec = Executors.newFixedThreadPool(profile.parallelTasks);
		Future<Void> ft = new SteppedParallelTaskExecutor<Void>(exec, profile.parallelTasks, execPDLowe).start();
		try {
			ft.get();
		} finally {
			exec.shutdown();
		}		
		return result;
	}
	
	private static KeyPointList doUpdateKeyPointFileIfNecessary(
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
		
//		KeyPointList result = buildKeyPointFileSingleThreaded(image);
		KeyPointList result = buildKeyPointFileMultiThreaded(image);

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
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			File image) throws Exception {
		KeyPointList result = doUpdateKeyPointFileIfNecessary(rootImagesDir, rootKeyPointFileDir, image);
		if (result != null)
			return result;

		File kplFile = getFile(rootImagesDir, rootKeyPointFileDir, image);
		BufferedReader fin = new BufferedReader(new FileReader(kplFile));
		fin.readLine(); // Skip header.
		result = KeyPointList.fromTextStream(fin, rootImagesDir);
		return result;
	}
	
	/*
	public static KeyPointList loadAutoPanoFile(String finName) throws JDOMException, IOException {
		KeyPointList result = new KeyPointList();
		Element root = XMLHelper.readXML(new File(finName));
		result.imageFileStamp = new FileStamp(root.getChildText("ImageFile"));
		result.imageSizeX = Integer.parseInt(root.getChildText("XDim"));
		result.imageSizeY = Integer.parseInt(root.getChildText("YDim"));
		
		java.util.List kpl = root.getChild("Arr").getChildren("KeypointN");
		
		for (int counter = 0; counter < kpl.size(); counter++) {
			Element key = (Element)kpl.get(counter);
			KeyPoint sp = new KeyPoint();
			sp.doubleX = Double.parseDouble(key.getChildText("X"));
			sp.doubleY = Double.parseDouble(key.getChildText("Y"));
			sp.imgX = (int)sp.doubleX;
			sp.imgY = (int)sp.doubleY;
			sp.adjS = 0;
			sp.degree = Double.parseDouble(key.getChildText("Orientation"));
			sp.level = Integer.parseInt(key.getChildText("Level"));
			sp.kpScale = Double.parseDouble(key.getChildText("Scale"));
			
			List descr = key.getChild("Descriptor").getChildren("int");
			for (int i = 0; i < KeyPoint.descriptorSize; i++) {
				for (int j = 0; j < KeyPoint.descriptorSize; j++) {
					for (int k = 0; k < KeyPoint.numDirections; k++) {
						int index = 
							i * KeyPoint.descriptorSize * KeyPoint.numDirections +
							j * KeyPoint.numDirections + k;
						sp.setItem(i, j, k, Byte.parseByte(
							((Element)descr.get(index)).getText()));
					}
				}
			}
			result.kdtree.add(sp);
		}
		return result;
	}
	*/
}
