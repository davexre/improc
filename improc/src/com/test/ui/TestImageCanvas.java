package com.test.ui;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.eclipse.swt.widgets.Composite;

import com.slavi.util.Const;

public class TestImageCanvas {

	public static class ImageCanvas extends Composite {

		public ImageCanvas(Composite parent, int style) {
			super(parent, style);
		}

	}
	
	
	public static void main(String[] args) throws Exception {
		BufferedImage src = ImageIO.read(new File(Const.sourceImage)); 

		
	}
}
