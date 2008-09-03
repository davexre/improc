package com.slavi.imagefilter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;

import com.slavi.improc.DImageMap;
import com.slavi.math.adjust.Statistics;
import com.slavi.util.Const;

public class SketchFilter implements BufferedImageFilter {
	DImageMap theImage;
	
	int threshold1 = 50;
	
	int threshold2 = 100;
		
	public int getThreshold1() {
		return threshold1;
	}

	public void setThreshold1(int threshold1) {
		this.threshold1 = threshold1;
	}

	public int getThreshold2() {
		return threshold2;
	}

	public void setThreshold2(int threshold2) {
		this.threshold2 = threshold2;
	}

	public static int getGrayColor(BufferedImage src, int x, int y) {
		int color = src.getRGB(x, y);
		int b = (color      ) & 0x0ff;
		int g = (color >>  8) & 0x0ff;
		int r = (color >> 16) & 0x0ff;
		return (r + b + g) / 3;
	}

	public void setImage(BufferedImage image) {
		if (image == null)
			theImage = null;
		else
			theImage = new DImageMap(image);
		Statistics stat = theImage.calcStatistics();
		System.out.println(stat);
	}
	
	private static final double borderColorValue = 0;
	
	public BufferedImage getFilteredImage() {
		if (theImage == null)
			return null;
	
		DImageMap dest = new DImageMap(theImage.getSizeX(), theImage.getSizeY());
		
		for (int i = theImage.getSizeX() - 1; i >= 0; i--) {
			dest.setPixel(i, 0, borderColorValue);
			dest.setPixel(i, theImage.getSizeY() - 1, borderColorValue);
		}
		for (int j = theImage.getSizeY() - 1; j >= 0; j--) {
			dest.setPixel(0, j, borderColorValue);
			dest.setPixel(theImage.getSizeX() - 1, j, borderColorValue);
		}
		
		for (int i = theImage.getSizeX() - 2; i > 0; i--)
			for (int j = theImage.getSizeY() - 2; j > 0; j--) {
				double curColor = theImage.getPixel(i, j);
				double d = 0;

				d = Math.max(d, Math.abs(curColor - theImage.getPixel(i - 1, j - 1)));
				d = Math.max(d, Math.abs(curColor - theImage.getPixel(i - 1, j    )));
				d = Math.max(d, Math.abs(curColor - theImage.getPixel(i - 1, j + 1)));
				
				d = Math.max(d, Math.abs(curColor - theImage.getPixel(i    , j - 1)));
				d = Math.max(d, Math.abs(curColor - theImage.getPixel(i    , j + 1)));
				
				d = Math.max(d, Math.abs(curColor - theImage.getPixel(i + 1, j - 1)));
				d = Math.max(d, Math.abs(curColor - theImage.getPixel(i + 1, j    )));
				d = Math.max(d, Math.abs(curColor - theImage.getPixel(i + 1, j + 1)));
				
				dest.setPixel(i, j, 1-d);
			}
				
/*		for (int i = theImage.getSizeX() - 2; i > 0; i--)
			for (int j = theImage.getSizeY() - 2; j > 0; j--) {
				
				d *= 255;
				
				int newColor = 0xffffff;
				if (d > t1) newColor = 0x500000;
				if (d > t2) newColor = 0xa00000;
				r.setRGB(i, j, newColor);
				
			}
*/
		
		BufferedImage r = dest.toImage();
		return r;
/*		int w = theImage.getSizeX();
		int h = theImage.getSizeY();
		BufferedImage r = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		w--;
		h--;
		int t1 = Math.min(threshold1, threshold2);
		int t2 = Math.max(threshold1, threshold2);
		double rows[][] = new double[3][w + 2];
		for (int i = w - 1; i >= 0; i--) 
			rows[1][i+1] = rows[2][i+1] = theImage.getPixel(i, 0);
		rows[1][0] = rows[2][0] = rows[1][1];
		rows[1][w+1] = rows[2][w+1] = rows[1][w];
		Marker.mark();
		
		for (int j = 1; j <= h; j++) {
			double tmp[] = rows[0];
			rows[0] = rows[1];
			rows[1] = rows[2];
			rows[2] = tmp;
			if (j == h)
				rows[2] = rows[1];
			else {
				for (int i = w - 1; i >= 0; i--) 
					tmp[i+1] = theImage.getPixel(i, j);
				tmp[0] = tmp[1];
				tmp[w+1] = tmp[w];
			}
			
			for (int i = w; i > 0; i--) {
				double curColor = rows[1][i];
				double d = 0;

				d = Math.max(d, Math.abs(curColor - rows[0][i-1]));
				d = Math.max(d, Math.abs(curColor - rows[0][i  ]));
				d = Math.max(d, Math.abs(curColor - rows[0][i+1]));
				
				d = Math.max(d, Math.abs(curColor - rows[1][i-1]));
				d = Math.max(d, Math.abs(curColor - rows[1][i+1]));

				d = Math.max(d, Math.abs(curColor - rows[2][i-1]));
				d = Math.max(d, Math.abs(curColor - rows[2][i  ]));
				d = Math.max(d, Math.abs(curColor - rows[2][i+1]));
				
				d *= 255;
				
				int newColor = 0xffffff;
				if (d > t1) newColor = 0x500000;
				if (d > t2) newColor = 0xa00000;
				r.setRGB(i, j, newColor);
			}
		}
		Marker.release();
		return r;
*/	}
	
	Label lblT1;
	
	Label lblT2;
	
	Slider sliderT1;
	
	Slider sliderT2;
	
	ImageFilter imageSketchApp;
	
	public void createFilterWidgets(Composite parent, ImageFilter imageSketch){
		imageSketchApp = imageSketch;
		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new GridLayout(2, false));
		
		Label label = new Label(root, SWT.NONE);
		label.setText("Threshold 1");
		
		sliderT1 = new Slider(root, SWT.HORIZONTAL);
		sliderT1.setMinimum(1);
		sliderT1.setMaximum(255);
		sliderT1.setSelection(threshold1);
		sliderT1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				lblT1.setText(Integer.toString(sliderT1.getSelection()));
				threshold1 = sliderT1.getSelection();
			}
		});
		
		label = new Label(root, SWT.NONE);
		lblT1 = new Label(root, SWT.CENTER);
		lblT1.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		lblT1.setText(Integer.toString(threshold1));
		
		label = new Label(root, SWT.NONE);
		label.setText("Threshold 2");
		sliderT2 = new Slider(root, SWT.HORIZONTAL);
		sliderT2.setMinimum(1);
		sliderT2.setMaximum(255);
		sliderT2.setSelection(threshold2);
		sliderT2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				lblT2.setText(Integer.toString(sliderT2.getSelection()));
				threshold2 = sliderT2.getSelection();
			}
		});
		
		label = new Label(root, SWT.NONE);
		lblT2 = new Label(root, SWT.CENTER);
		lblT2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		lblT2.setText(Integer.toString(threshold2));
		
		Button btn = new Button(root, SWT.NONE);
		btn.setText("GO");
		btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				imageSketchApp.updateFilter();
			}
		});
	}

	public String getFilterName() {
		return "Sketch-like B&W";
	}

	public static void main(String[] args) throws IOException {
		BufferedImage src = ImageIO.read(new File(Const.sourceImage)); 

		Display display = new Display();
		ImageFilter imageSketch = new ImageFilter(new SketchFilter());
		Shell shell = imageSketch.open(display);
		imageSketch.setSourceImage(src);
		
		while (!shell.isDisposed())
			if (!display.readAndDispatch()) display.sleep();
		display.dispose();
	}
}
