package com.slavi.improc.old;

import java.util.ArrayList;

import com.slavi.improc.KeyPointList;
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
	
	public KeyPointList source;
	
	public KeyPointList target;
	
	public ImageData sourceImageData;
	public ImageData targetImageData;
	
	public PanoPairList() {
		items = new ArrayList<PanoPair>();
	}
}
