package com.slavi.img;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.slavi.utils.AbsoluteToRelativePathMaker;
import com.slavi.utils.FindFileIterator;
import com.slavi.utils.SwtUtl;

public class Improc {

	ArrayList<String>images;
	
	class CountImageFiles implements Runnable {
		public void run() {
			try {
				FindFileIterator imagesIterator = FindFileIterator.makeWithWildcard(imagesRoot.getFullPath("*.jpg"), true, true);
				int numberOfImages = 0;
				while (imagesIterator.hasNext()) {
					if (Thread.interrupted()) {
						numberOfImages = 0;
						break;
					}
					numberOfImages++;
					images.add(imagesIterator.next().getPath());
					SwtUtl.activeWaitDialogSetStatus("Found " + numberOfImages + " images", 0);
				}
			} catch (Exception e) {
				System.err.println("CountImageFiles FAILED!");
				e.printStackTrace();
				SwtUtl.activeWaitDialogAbortTask();
			}
		}
	}
	
	class GenerateKeyPointFiles implements Runnable {
		public void run() {
			try {
				for (int i = 0; i < images.size(); i++) {
					if (Thread.interrupted()) {
						break;
					}
					String fileName = images.get(i); 
					String statusMessage = (i + 1) + "/" + images.size() + " " + fileName;
					System.out.print(statusMessage);
					SwtUtl.activeWaitDialogSetStatus(statusMessage, i);
//					KeyPointList.updateKeyPointFileIfNecessary(imagesRoot, keyPointFileRoot, new File(fileName));
					KeyPointList kpl = KeyPointList.readKeyPointFile(imagesRoot, keyPointFileRoot, new File(fileName));
					System.out.println(" (" + kpl.kdtree.getSize() + " key points)");
				}
			} catch (Throwable e) {
				System.err.println("GenerateKeyPointFiles FAILED!");
				e.printStackTrace();
				SwtUtl.activeWaitDialogAbortTask();
			}
		}
	}

	class GenerateKeyPointPairFiles implements Runnable {
		public void run() {
			try {
				for (int i = 0, pairsCount = 0; i < images.size(); i++) {
					for (int j = i + 1; j < images.size(); j++, pairsCount++) {
						if (Thread.interrupted()) {
							break;
						}
						String fileName1 = images.get(i); 
						String fileName2 = images.get(j); 
						File image1 = new File(fileName1);
						File image2 = new File(fileName2);
						File kpplFile = KeyPointPairList.getFile(imagesRoot, keyPointFileRoot, keyPointPairFileRoot, image1, image2); 
						String statusMessage = (i + 1) + "/" + (j + 1) + "/" + images.size() + " " + kpplFile.getPath();
						System.out.print(statusMessage);
						SwtUtl.activeWaitDialogSetStatus(statusMessage, pairsCount);
//						KeyPointPairList.updateKeyPointPairFileIfNecessary(imagesRoot, keyPointFileRoot, keyPointPairFileRoot, image1, image2);
						KeyPointPairList kppl = KeyPointPairList.readKeyPointPairFile(imagesRoot, keyPointFileRoot, keyPointPairFileRoot, image1, image2);
						System.out.println(" (" + kppl.items.size() + " key point pairs)");
					}
				}
			} catch (Exception e) {
				System.err.println("GenerateKeyPointPairFiles FAILED!");
				e.printStackTrace();
				SwtUtl.activeWaitDialogAbortTask();
			}
		}
	}

	PanoList panoList;
	
	class GeneratePanoPairFiles implements Runnable {
		public void run() {
			panoList = new PanoList();
			try {
				for (int i = 0, pairsCount = 0; i < images.size(); i++) {
					for (int j = i + 1; j < images.size(); j++, pairsCount++) {
						if (Thread.interrupted()) {
							break;
						}
						String fileName1 = images.get(i); 
						String fileName2 = images.get(j);
						File image1 = new File(fileName1);
						File image2 = new File(fileName2);
						File kpplFile = PanoPairList.getFile(imagesRoot, keyPointFileRoot, keyPointPairFileRoot, image1, image2); 
						String statusMessage = (i + 1) + "/" + (j + 1) + "/" + images.size() + " " + kpplFile.getPath();;
						System.out.print(statusMessage);
						SwtUtl.activeWaitDialogSetStatus(statusMessage, pairsCount);
						PanoPairList ppl = PanoPairList.readPanoPairFile(imagesRoot, keyPointFileRoot, keyPointPairFileRoot, image1, image2);
						panoList.addItem(ppl);
						System.out.println(" (" + ppl.items.size() + " pano paris)");
					}
				}
			} catch (Exception e) {
				System.err.println("GeneratePanoPairFiles FAILED!");
				e.printStackTrace();
				SwtUtl.activeWaitDialogAbortTask();
			}
		}
	}

	class GeneratePanoramaFiles implements Runnable {
		public void run() {
			try {
				int panoCount = 1;
				int maxItems = panoList.items.size();
				AdjAffine adjustAffine = new AdjAffine();
				while (!Thread.interrupted()) {
					ArrayList<PanoPairList>chain = panoList.getImageChain();
					if (chain == null)
						break;
					File fou = keyPointPairFileRoot.getFullPathFile("Pano" + panoCount + ".pto");

					String statusMessage = fou.getAbsolutePath();
					System.out.print(statusMessage);
					SwtUtl.activeWaitDialogSetStatus(statusMessage, maxItems - panoList.items.size());

					PanoList.writeToPtoFile(fou, chain);
					
					adjustAffine.initWithPanoList(chain);
					adjustAffine.doTheJob();
					
					panoCount++;
					System.out.println(" (" + chain.size() + " images in chain)");
				}
			} catch (Exception e) {
				System.err.println("GeneratePanoramaFiles FAILED!");
				e.printStackTrace();
				SwtUtl.activeWaitDialogAbortTask();
			}
		}
	}
	
	AbsoluteToRelativePathMaker userHomeRoot;
	AbsoluteToRelativePathMaker imagesRoot;
	AbsoluteToRelativePathMaker keyPointFileRoot;
	AbsoluteToRelativePathMaker keyPointPairFileRoot;

	public void doTheJob() {
		Properties properties = new Properties();
		String userHomeRootStr = System.getProperty("user.home");
		AbsoluteToRelativePathMaker userHomeRootBase = new AbsoluteToRelativePathMaker(userHomeRootStr);

		String propertiesFile = userHomeRootBase.getFullPath("ImageProcess.xproperties");
		try {
			properties.loadFromXML(new FileInputStream(propertiesFile));
		} catch (Exception e) {
		}

		Settings settings = new Settings(null);
		if (!settings.open(properties))
			return;
		
		String imagesRootStr = properties.getProperty("ImagesRoot", userHomeRootStr);
		String keyPointFileRootStr = properties.getProperty("KeyPointFileRoot", userHomeRootStr);
		String keyPointPairFileRootStr = properties.getProperty("KeyPointPairFileRoot", userHomeRootStr);
		
		try {
			properties.storeToXML(new FileOutputStream(propertiesFile), "Image Process configuration file");
		} catch (Exception e) {
		}
		properties = null;
		settings = null;
		
		imagesRoot = new AbsoluteToRelativePathMaker(imagesRootStr);
		keyPointFileRoot = new AbsoluteToRelativePathMaker(keyPointFileRootStr);
		keyPointPairFileRoot = new AbsoluteToRelativePathMaker(keyPointPairFileRootStr);
		
		images = new ArrayList<String>();
		if (!SwtUtl.openWaitDialog("Searching for images", 
				new CountImageFiles(), -1)) {
			System.err.println("CountImageFiles aborted");
			return;
		}
		
		if (!SwtUtl.openWaitDialog("Generating key point files", 
				new GenerateKeyPointFiles(), images.size() - 1)) {
			System.err.println("GenerateKeyPointFiles aborted");
			return;
		}
			
		System.out.println("---------- Generating key point pair files");

		if (!SwtUtl.openWaitDialog("Generating key point pair files", 
				new GenerateKeyPointPairFiles(), (images.size() - 1) * images.size() - 1)) {
			System.err.println("GenerateKeyPointPairFiles aborted");
			return;
		}
		
		System.out.println("---------- Generating pano pair files");

		if (!SwtUtl.openWaitDialog("Generating pano pair files", 
				new GeneratePanoPairFiles(), (images.size() - 1) * images.size() - 1)) {
			System.err.println("GeneratePanoPairFiles aborted");
			return;
		}
		
		System.out.println("---------- Generating panorama (PTO) files");

		if (!SwtUtl.openWaitDialog("Generating panorama (PTO) files", 
				new GeneratePanoramaFiles(), panoList.items.size())) {
			System.err.println("GeneratePanoramaFiles aborted");
			return;
		}
		System.out.println("Done.");
	}
	
	public static void main(String[] args) {
		Improc application = new Improc();
		application.doTheJob();
	}
}
