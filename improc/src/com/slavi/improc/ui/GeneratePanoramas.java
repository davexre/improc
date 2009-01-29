package com.slavi.improc.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.slavi.improc.PanoList;
import com.slavi.improc.PanoPairList;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.ui.SwtUtil;

public class GeneratePanoramas implements Callable<Void> {

	PanoList panoList;
	AbsoluteToRelativePathMaker keyPointPairFileRoot;
	
	public GeneratePanoramas(PanoList panoList, AbsoluteToRelativePathMaker keyPointPairFileRoot) {
		this.panoList = panoList;
		this.keyPointPairFileRoot = keyPointPairFileRoot;
	}
	
	public Void call() throws Exception {
		int panoCount = 1;
		int maxItems = panoList.items.size();
//		AdjAffine adjustAffine = new AdjAffine();
		while (true) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			ArrayList<PanoPairList>chain = panoList.getImageChain();
			if (chain == null)
				break;
			File fou = keyPointPairFileRoot.getFullPathFile("Pano" + panoCount + ".pto");

			String statusMessage = fou.getAbsolutePath();
			SwtUtil.activeWaitDialogSetStatus(statusMessage, maxItems - panoList.items.size());

			PanoAdjust adjust = new PanoAdjust();
			adjust.processOne(chain);
			
			panoCount++;
		}
		return null;
	}
}
