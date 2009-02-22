package com.slavi.improc;

import java.util.ArrayList;

import com.slavi.improc.pano.ImageData;

public class PanoPairList {
	public ArrayList<PanoPair> items;
	
	public PanoPairTransformer transform = null;
	
	public String sourceImage;
	
	public String targetImage;
	
	public int sourceImageSizeX;
	
	public int sourceImageSizeY;
	
	public int targetImageSizeX;
	
	public int targetImageSizeY; 
	
	public ImageData source;
	
	public ImageData target;
	
	public PanoPairList() {
		items = new ArrayList<PanoPair>();
	}
}
