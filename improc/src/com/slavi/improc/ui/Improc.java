package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.slavi.improc.KeyPointBigTree;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.CalculatePanoramaParams;
import com.slavi.improc.myadjust.GeneratePanoramas;
import com.slavi.improc.myadjust.ValidateKeyPointPairList;
import com.slavi.util.Marker;
import com.slavi.util.Util;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FindFileIterator;
import com.slavi.util.ui.SwtUtil;

public class Improc {
	
	Shell parent;
	
	public int countPairs(ArrayList<KeyPointPairList> kppl) {
		int count = 0;
		for (KeyPointPairList i : kppl) {
			count += i.items.size();
		}
		return count;
	}
	
	public void doTheJob(ExecutorService exec) throws Exception {
		parent = new Shell((Shell) null, SWT.NONE);
		parent.setBounds(-10, -10, 1, 1); // fixes a bug in SWT
		parent.open();
		
		Settings settings = Settings.getSettings(parent);
		if (settings == null)
			return;
		SwtUtil.openTaskManager(parent, true);
		System.out.println("Making panorama using " + settings.adjustMethodClassName);
		Marker.mark("Image Processing");

		AbsoluteToRelativePathMaker imagesRoot = new AbsoluteToRelativePathMaker(settings.imagesRootStr);
		AbsoluteToRelativePathMaker keyPointFileRoot = new AbsoluteToRelativePathMaker(settings.keyPointFileRootStr);
		
		FindFileIterator imagesIterator = FindFileIterator.makeWithWildcard(imagesRoot.getFullPath(settings.imagesRelativePathStr + "/*.jpg"), true, true);
		ArrayList<String> images = SwtUtil.openWaitDialog(parent, "Searching for images", new EnumerateImageFiles(imagesIterator), -1);
		Collections.sort(images);
		SwtUtil.openWaitDialog(parent, "Generating key point files", 
				new GenerateKeyPointFiles(exec, images, imagesRoot, keyPointFileRoot), images.size() - 1);
	
/*		if (true) {
			for (String image : images) {
				KeyPointList l = KeyPointListSaver.readKeyPointFile(exec, imagesRoot, keyPointFileRoot, new File(image));
				SafeImage im = new SafeImage(new FileInputStream(l.imageFileStamp.getFile()));
				for (int i = 0; i < im.sizeX; i++)
					for (int j = 0; j < im.sizeY; j++) {
						int col = im.getRGB(i, j);
						col = DWindowedImageUtils.getGrayColor(col);
						im.setRGB(i, j, col);
					}
				for (KeyPoint p : l.items) {
					int color = SafeImage.colors[p.dogLevel % SafeImage.colors.length];
					im.drawCross((int)p.doubleX, (int)p.doubleY, color, -1);
				}
				String fou = settings.outputDirStr + "/" + l.imageFileStamp.getFile().getName();
				System.out.println("Writing file " + fou);
				im.save(fou);
			}
			return;
		}
*/		
		ArrayList<KeyPointPairList> kppl;
	
		if (true) {
			KeyPointBigTree bigTree = SwtUtil.openWaitDialog(parent, "Generating key point BIG tree", 
					new GenerateKeyPointPairBigTree(exec, images, imagesRoot, keyPointFileRoot), 
					images.size() - 1);
			bigTree.balance();
			System.out.println("Tree size  : " + bigTree.getSize());
			System.out.println("Tree depth : " + bigTree.getTreeDepth());
			System.out.println("Tree size          : " + bigTree.getSize());
			System.out.println("Tree depth         : " + bigTree.getTreeDepth());
			System.out.println("Perfect tree depth : " + bigTree.getPerfectTreeDepth());
			
			System.out.println("---------- Generating key point pairs from BIG tree");
			kppl = SwtUtil.openWaitDialog(parent, "Generating key point pairs from BIG tree", 
					new GenerateKeyPointPairsFromBigTree(exec, bigTree),
					images.size() - 1);
			bigTree = null;
		} else {
			kppl = SwtUtil.openWaitDialog(parent, "Generating key point pairs NO big tree", 
					new GenerateKeyPointPairs(exec, images, imagesRoot, keyPointFileRoot),
					images.size() - 1);
			
			images = null;
		}		
		System.out.println("Key point pairs lists to be validated: " + kppl.size());
		
		System.out.println("---------- Validating key point pairs");
		ArrayList<KeyPointPairList> validkppl = SwtUtil.openWaitDialog(parent, "Validating key point pairs", 
				new ValidateKeyPointPairList(exec, kppl),
				kppl.size() - 1);
		kppl = null;
/*
		System.out.println("---------- point pair lists ------------");
		for (int i = 0; i < validkppl.size(); i++) {
			KeyPointPairList pairList = validkppl.get(i);
			int goodCount = 0;
			for (KeyPointPair kp : pairList.items) {
				if (!kp.validatePairBad)
					goodCount++;
			}
			System.out.printf("%3d\t%11s\t%s\t%s\n", i, (goodCount + "/" + pairList.items.size()),
					pairList.source.imageFileStamp.getFile().getName(),
					pairList.target.imageFileStamp.getFile().getName() +
					"\tangle=" +MathUtil.rad2degStr(pairList.angle) + 
					"\tscale=" +MathUtil.d4(pairList.scale) + 
					"\tdX=" +MathUtil.d4(pairList.translateX) + 
					"\tdY=" +MathUtil.d4(pairList.translateY) 
					);
		}
*/		
		System.out.println("---------- Calculating panorama parameters");
/*		if ("com.slavi.improc.myadjust.zyz7params.Stereo_7ParamsLearner".equals(settings.adjustMethodClassName)) {
			ArrayList<ArrayList<KeyPointPairList>> panos = SwtUtil.openWaitDialog(parent, "Calculating panorama parameters", 
					new CalculatePanoramaParams(exec, settings.adjustMethodClassName, 
							validkppl, keyPointFileRoot, settings.outputDirStr,
							settings.pinPoints, settings.useColorMasks, settings.useImageMaxWeight), -1);
			
		} else */ {
			ArrayList<ArrayList<KeyPointPairList>> panos = SwtUtil.openWaitDialog(parent, "Calculating panorama parameters", 
					new CalculatePanoramaParams(exec, settings.adjustMethodClassName, 
							validkppl, keyPointFileRoot, settings.outputDirStr,
							settings.pinPoints, settings.useColorMasks, settings.useImageMaxWeight), -1);

			System.out.println("---------- Generating panorama images");
			SwtUtil.openWaitDialog(parent, "Generating panorama images", 
					new GeneratePanoramas(exec, settings.adjustMethodClassName, panos, settings.outputDirStr,
							settings.pinPoints, settings.useColorMasks, settings.useImageMaxWeight), 100);
		}
		
		Marker.release();
		System.out.println("Done.");
	}
	
	public static void main(String[] args) throws Exception {
		Runtime runtime = Runtime.getRuntime();
		int numberOfProcessors = runtime.availableProcessors();
//		ExecutorService exec = Executors.newFixedThreadPool(numberOfProcessors);
		ExecutorService exec = Util.newBlockingThreadPoolExecutor(numberOfProcessors);
//		ExecutorService exec = Util.newBlockingThreadPoolExecutor(1);

		Improc application = new Improc();
		try {
			application.doTheJob(exec);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			exec.shutdown();
			application.parent.close();
		}
		SwtUtil.closeTaskManager();
	}
}
