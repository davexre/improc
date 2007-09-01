package com.slavi.img;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.slavi.utils.AbsoluteToRelativePathMaker;
import com.slavi.utils.FindFileIterator;
import com.slavi.utils.UiUtils;

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
					UiUtils.activeWaitDialogSetStatus("Found " + numberOfImages + " images", 0);
				}
			} catch (Exception e) {
				System.err.println("CountImageFiles FAILED!");
				e.printStackTrace();
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
					System.out.println(statusMessage);
					UiUtils.activeWaitDialogSetStatus(statusMessage, i);
					KeyPointList.updateKeyPointFileIfNecessary(imagesRoot, keyPointFileRoot, new File(fileName));
				}
			} catch (Throwable e) {
				System.err.println("GenerateKeyPointFiles FAILED!");
				e.printStackTrace();
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
						System.out.println(statusMessage);
						UiUtils.activeWaitDialogSetStatus(statusMessage, pairsCount);
						KeyPointPairList.updateKeyPointPairFileIfNecessary(imagesRoot, keyPointFileRoot, keyPointPairFileRoot, image1, image2);
					}
				}
			} catch (Exception e) {
				System.err.println("GenerateKeyPointPairFiles FAILED!");
				e.printStackTrace();
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
						System.out.println(statusMessage);
						UiUtils.activeWaitDialogSetStatus(statusMessage, pairsCount);
						panoList.addItem(PanoPairList.readPanoPairFile(imagesRoot, keyPointFileRoot, keyPointPairFileRoot, image1, image2));
					}
				}
			} catch (Exception e) {
				System.err.println("GeneratePanoPairFiles FAILED!");
				e.printStackTrace();
			}
		}
	}

	class GeneratePanoramaFiles implements Runnable {
		public void run() {
			try {
				int panoCount = 1;
				int maxItems = panoList.items.size();
				while (!Thread.interrupted()) {
					ArrayList<PanoPairList>chain = panoList.getImageChain();
					if (chain == null)
						break;
					File fou = keyPointPairFileRoot.getFullPathFile("Pano" + panoCount + ".pto");

					String statusMessage = fou.getAbsolutePath();
					System.out.println(statusMessage);
					UiUtils.activeWaitDialogSetStatus(statusMessage, maxItems - panoList.items.size());

					PanoList.writeToPtoFile(fou, chain);
					panoCount++;
				}
			} catch (Exception e) {
				System.err.println("GeneratePanoramaFiles FAILED!");
				e.printStackTrace();
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
		AbsoluteToRelativePathMaker userHomdeRootBase = new AbsoluteToRelativePathMaker(userHomeRootStr);

		String propertiesFile = userHomdeRootBase.getFullPath("ImageProcess.xproperties");
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
		if (!UiUtils.openWaitDialog("Searching for images", 
				new CountImageFiles(), -1)) {
			System.err.println("CountImageFiles aborted");
			return;
		}
		
		if (!UiUtils.openWaitDialog("Generating key point files", 
				new GenerateKeyPointFiles(), images.size() - 1)) {
			System.err.println("GenerateKeyPointFiles aborted");
			return;
		}
		if (true)
			return;
			
		System.out.println("----------");

		if (!UiUtils.openWaitDialog("Generating key point pair files", 
				new GenerateKeyPointPairFiles(), (images.size() - 1) * images.size() - 1)) {
			System.err.println("GenerateKeyPointPairFiles aborted");
			return;
		}
		
		System.out.println("----------");

		if (!UiUtils.openWaitDialog("Generating pano pair files", 
				new GeneratePanoPairFiles(), (images.size() - 1) * images.size() - 1)) {
			System.err.println("GeneratePanoPairFiles aborted");
			return;
		}
		
		System.out.println("----------");

		if (!UiUtils.openWaitDialog("Generating panorama (PTO) files", 
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
