package com.slavi.imagefilter;

import java.awt.image.BufferedImage;

import org.eclipse.swt.widgets.Composite;

public interface BufferedImageFilter {
	public String getFilterName();
	
	public void createFilterWidgets(Composite parent, ImageFilter imageSketch);
	
	public void setImage(BufferedImage image);
	
	public BufferedImage getFilteredImage();
}
