package com.slavi.improc.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.slavi.improc.AdjAffine;
import com.slavi.improc.PanoList;
import com.slavi.improc.PanoPairList;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.ui.SwtUtl;

public class GeneratePanoramaFiles implements Callable<Void> {

	AbsoluteToRelativePathMaker keyPointPairFileRoot;
	PanoList panoList;
	
	public GeneratePanoramaFiles(PanoList panoList, AbsoluteToRelativePathMaker keyPointPairFileRoot) {
		this.panoList = panoList;
		this.keyPointPairFileRoot = keyPointPairFileRoot;
	}
	
	public Void call() throws Exception {
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
		return null;
	}
}
