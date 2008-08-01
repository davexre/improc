package com.slavi.img;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.slavi.img.DLoweDetector.Hook;
import com.slavi.parallel.img.DWindowedImage;
import com.slavi.parallel.img.PDImageMapBuffer;
import com.slavi.parallel.img.PDLoweDetector;
import com.slavi.parallel.img.PDLoweDetector.ExecutionProfile;
import com.slavi.tree.KDNodeSaver;
import com.slavi.tree.KDTree;
import com.slavi.utils.AbsoluteToRelativePathMaker;
import com.slavi.utils.FileStamp;
import com.slavi.utils.Marker;
import com.slavi.utils.Utl;

public class KeyPointList {
	public static final String fileHeader = "KeyPoint file version 1.2";
	
	public FileStamp imageFileStamp = null;
	
	public static class KeyPointTree extends KDTree<KeyPoint> {

		public KeyPointTree() {
			super(KeyPoint.featureVectorLinearSize);
		}

		public boolean canFindDistanceBetween(KeyPoint fromNode, KeyPoint toNode) {
			return fromNode != toNode;
		}

		public double getValue(KeyPoint node, int dimensionIndex) {
			return node.getValue(dimensionIndex);
		}
	}
	
	public static class KeyPointListSaver extends KDNodeSaver<KeyPoint> {
		public KDTree<KeyPoint> getTree(int dimensions) {
			if (dimensions != KeyPoint.featureVectorLinearSize)
				throw new IllegalArgumentException("Invalid dimension"); 
			return new KeyPointTree();
		}

		public KeyPoint nodeFromString(String source) {
			return KeyPoint.fromString(source);
		}

		public String nodeToString(KeyPoint node) {
			return node.toString();
		}
	}
	
	public KeyPointTree kdtree = new KeyPointTree();

	public int imageSizeX;

	public int imageSizeY;
	
	private static KeyPointList fromTextStream(BufferedReader fin, AbsoluteToRelativePathMaker rootImagesDir) throws IOException {
		KeyPointList r = new KeyPointList();
		r.imageFileStamp = FileStamp.fromString(fin.readLine(), rootImagesDir);
		StringTokenizer st = new StringTokenizer(fin.readLine(), "\t");
		r.imageSizeX = Integer.parseInt(st.nextToken());
		r.imageSizeY = Integer.parseInt(st.nextToken());
		r.kdtree = new KeyPointTree(); 
		new KeyPointListSaver().fromTextStream(r.kdtree, fin);
		return r;
	}

	private void toTextStream(PrintWriter fou) {
		fou.println(imageFileStamp.toString());
		fou.println(imageSizeX + "\t" + imageSizeY);
		new KeyPointListSaver().toTextStream(kdtree, fou);
	}

	private static class ListenerImpl implements Hook {
		public KeyPointList scalePointList;
		
		public ListenerImpl(KeyPointList spl) {
			this.scalePointList = spl;
		}
		public synchronized void keyPointCreated(KeyPoint scalePoint) {
			scalePointList.kdtree.add(scalePoint);
		}		
	}
	
	public static void updateKeyPointFileIfNecessary(
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			File image) throws IOException {
		doUpdateKeyPointFileIfNecessary(rootImagesDir, rootKeyPointFileDir, image);
	}
	
	public static File getFile(AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			File image) {
		return new File(Utl.chageFileExtension(
			rootKeyPointFileDir.getFullPath(
				rootImagesDir.getRelativePath(image, false)), "spf"));
	}
	
	private static KeyPointList buildKeyPointFileSingleThreaded(File kplFile, File image) throws IOException {
		DImageMap img = new DImageMap(image);
		KeyPointList result = new KeyPointList();
		result.imageSizeX = img.getSizeX();
		result.imageSizeY = img.getSizeY();
		DLoweDetector d = new DLoweDetector();
		d.hook = new ListenerImpl(result);
		d.DetectFeatures(img, 3, 32);
		return result;
	}
	
	private static KeyPointList buildKeyPointFileMultiThreaded(File kplFile, File image) throws IOException {
		DWindowedImage img = new PDImageMapBuffer(image);
		KeyPointList result = new KeyPointList();
		result.imageSizeX = img.maxX() + 1;
		result.imageSizeY = img.maxY() + 1;

		Hook hook = new ListenerImpl(result);
		
		int scale = 1;
		while (true) {
			ExecutionProfile profile = PDLoweDetector.makeTasks(img, scale, hook);
//			ExecutorService exec = Executors.newSingleThreadExecutor();
//			ExecutorService exec = Executors.newFixedThreadPool(profile.parallelTasks);
			System.out.println(profile);
			System.out.println("---------------------");
			for (Runnable task : profile.tasks) {
				System.out.println(task);
				System.out.println();
			}
			for (Runnable task : profile.tasks)
				task.run();
				
//			for (Runnable task : profile.tasks)
//				exec.execute(task);
//			exec.shutdown();
//			while (!exec.isTerminated()) {
//				try {
//					exec.awaitTermination(1000, TimeUnit.SECONDS);
//				} catch (InterruptedException e) {
//				}
//			}
//			DImageMap tmp = new DImageMap(img.getSizeX() >> 1, img.getSizeY() >> 1);
//			img.scaleHalf(tmp);
			scale *= 2;
			img = profile.nextLevelBlurredImage;
			if (img.maxX() / 2 <= 64) 
				break;
		};		
		return result;
	}
	
	private static KeyPointList doUpdateKeyPointFileIfNecessary(
			AbsoluteToRelativePathMaker rootImagesDir,
			AbsoluteToRelativePathMaker rootKeyPointFileDir,
			File image) throws IOException {
		File kplFile = getFile(rootImagesDir, rootKeyPointFileDir, image);
		
		try {
			if (kplFile.isFile()) {
				BufferedReader fin = new BufferedReader(new FileReader(kplFile));
				if (fileHeader.equals(fin.readLine())) {
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
		
//		KeyPointList result = buildKeyPointFileSingleThreaded(kplFile, image);
		KeyPointList result = buildKeyPointFileMultiThreaded(kplFile, image);

		String relativeImageName = rootImagesDir.getRelativePath(image, false);
		result.imageFileStamp = new FileStamp(relativeImageName, rootImagesDir);
		kplFile.getParentFile().mkdirs();
		PrintWriter fou = new PrintWriter(kplFile);
		fou.println(fileHeader);
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
			File image) throws IOException {
		KeyPointList result = doUpdateKeyPointFileIfNecessary(rootImagesDir, rootKeyPointFileDir, image);
		if (result != null)
			return result;

		File kplFile = getFile(rootImagesDir, rootKeyPointFileDir, image);
		BufferedReader fin = new BufferedReader(new FileReader(kplFile));
		fin.readLine(); // Skip header.
		result = new KeyPointList();
		result = KeyPointList.fromTextStream(fin, rootImagesDir);
		return result;
	}
	
	public HashMap<Integer, KeyPoint> makeMap() {
		HashMap<Integer, KeyPoint> result = new HashMap<Integer, KeyPoint>(kdtree.getSize());
		for (KeyPoint i : kdtree) {
			result.put(i.id, i);
		}
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
	
	public void compareToList(KeyPointList dest) {
		ArrayList points = kdtree.toList();
		ArrayList destPoints = dest.kdtree.toList();
		
		int matchedCount1 = 0;
		for (int i = points.size() - 1; i >= 0; i--) {
			KeyPoint sp1 = (KeyPoint)points.get(i);
			boolean matchingFound = false;
			for (int j = destPoints.size() - 1; j >= 0; j--) {
				KeyPoint sp2 = (KeyPoint)destPoints.get(j);
				if (sp1.equals(sp2)) {
					matchingFound = true;
					matchedCount1++;
					break;
				}
			}
			if (!matchingFound)
				System.out.println("Point No " + i + " from 1-st list has no match in 2-nd list");
		}

		int matchedCount2 = 0;
		for (int j = destPoints.size() - 1; j >= 0; j--) {
			KeyPoint sp2 = (KeyPoint)destPoints.get(j);
			boolean matchingFound = false;
			for (int i = points.size() - 1; i >= 0; i--) {
				KeyPoint sp1 = (KeyPoint)points.get(i);
				if (sp1.equals(sp2)) {
					matchingFound = true;
					matchedCount2++;
					break;
				}
			}
			if (!matchingFound)
				System.out.println("Point No " + j + " from 2-nd list has no match in 1-st list");
		}
		
		System.out.println("Matched 1-st list against 2-nd list: " + matchedCount1 + "/" + points.size());
		System.out.println("Matched 2-nd list against 1-st list: " + matchedCount2 + "/" + destPoints.size());
	}
	
	public static void main(String[] args) throws IOException {
		File kplFile = new File(Const.tempDir + "/ttt.kpl");
		File image = new File(Const.sourceImage);
		System.out.println("Source image is " + Const.sourceImage);
		
		Marker.mark("Single threaded");
		KeyPointList l1 = buildKeyPointFileSingleThreaded(kplFile, image);
		Marker.release();
		Marker.mark("Multithreaded");
		KeyPointList l2 = buildKeyPointFileMultiThreaded(kplFile, image);
		Marker.release();
		
		System.out.println("----------------------- Comparing -----------------");
		l1.compareToList(l2);

	}
}
