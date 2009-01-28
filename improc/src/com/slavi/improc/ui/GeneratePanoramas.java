package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;

import com.slavi.improc.PanoPairList;
import com.slavi.improc.pano.ImageData;
import com.slavi.math.matrix.Matrix;

public class GeneratePanoramas implements Callable<Void> {

	ArrayList<ArrayList<PanoPairList>> panoChains;
	
	public GeneratePanoramas(ArrayList<ArrayList<PanoPairList>> panoChains) {
		this.panoChains = panoChains;
	}
	
	public Void call() throws Exception {
		for (ArrayList<PanoPairList> panoChain : panoChains) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			processOne(panoChain);
		}
		return null;
	}

	/////////////////////
	
	// variables to optimize
	boolean yaw = true;
	boolean pitch = true;
	boolean roll = true;
	boolean hfov = false;
	boolean a = false;
	boolean b = false;
	boolean c = false;
	boolean d = false;
	boolean e = false;
	boolean X = false;
	boolean Y = false;
	boolean Z = false;

	int countVariablesToOptimize() {
		int result = 0;
		if (yaw) result++;
		if (pitch) result++;
		if (roll) result++;
		if (hfov) result++;
		if (a) result++;
		if (b) result++;
		if (c) result++;
		if (d) result++;
		if (e) result++;
		if (X) result++;
		if (Y) result++;
		if (Z) result++;
		return result;
	}

	boolean needInitialAvgFov = true;
	double initialAvgFov = 0.0;
	int numParam; // Number of parameters to optimize
	int numControlPoints;
	Map<String, ImageData> images;
	
	private void processOne(ArrayList<PanoPairList> panoChain) {
		numParam = countVariablesToOptimize() * panoChain.size();
		numControlPoints = 0;
		for (PanoPairList panoList : panoChain) {
			numControlPoints += panoList.items.size();
			if (!images.containsKey(panoList.sourceImage)) {
				ImageData data = new ImageData();
				data.name = panoList.sourceImage;
				data.width = panoList.sourceImageSizeX;
				data.height = panoList.sourceImageSizeY;
				data.hfov = 38;
			}
		}		
		
		
		// Initialize optimization params
		needInitialAvgFov = true;
		int n = numParam;
		int m = numControlPoints;
		Matrix x = new Matrix(n, 1);
		Matrix fvec = new Matrix(m, 1);
		// Set LM params using global preferences structure
		// Change to cover range 0....1 (roughly)
		int j = 0; // Counter for optimization parameters
		for (PanoPairList panoList : panoChain) {
//			if(yaw) x.setItem(j++, 0, panoList.yaw);

		}
		
	}
}
