package com.slavi.improc.pano;

import com.slavi.improc.pano.AlignInfo.PTRect;
import com.slavi.improc.pano.AlignInfo.cPrefs;

public class Image {
	public static enum ImageFormat {
		Rectilinear,
		Panorama,
		FisheyeCirc,
		FisheyeFF,
		Equirectangular,
		SphericalCP,
		ShpericalTP,
		Mirror,
		Orthographic,
		Cubic
	}
	
	public long width = 0;
	public long height = 0;
	public long bytesPerLine = 0;
	public long bitsPerPixel = 0;	// Must be 24 or 32
	public long dataSize = 0;
	public byte[][] data = null;
	public long dataformat = 0;		// rgb, Lab etc
	public ImageFormat format = ImageFormat.Rectilinear;	// Projection: rectilinear etc
	public double hfov = 0;
	public double yaw = 0;
	public double pitch = 0;
	public double roll = 0;
	public cPrefs cP = new cPrefs();		// How to correct the image
	public String name = "";
	public PTRect selection = null;
}
