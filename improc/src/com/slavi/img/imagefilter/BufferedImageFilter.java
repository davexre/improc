package com.slavi.img.imagefilter;

import java.awt.image.BufferedImage;

import org.eclipse.swt.widgets.Composite;

public interface BufferedImageFilter {
	public String getFilterName();
	
	public void createFilterWidgets(Composite parent, ImageFilter imageSketch);
	
	public BufferedImage getFilteredImage(BufferedImage image);
}
