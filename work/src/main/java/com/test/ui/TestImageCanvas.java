package com.test.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.slavi.util.Const;

public class TestImageCanvas {

	public static class ImageCanvas extends Composite {
		Image image;
		double atX = 0; 
		double atY = 0;
		int imgWidth = 0;
		int imgHeight = 0;
		double zoom = 0.1;
		ArrayList<ImageCanvas> notify; 
		
		public ImageCanvas(Composite parent, int style, ImageCanvas linkWith) {
			super(parent, style /*| SWT.DOUBLE_BUFFERED */);
			if (linkWith == null) {
				notify = new ArrayList<ImageCanvas>();
			} else {
				notify = linkWith.notify;
			}
			final ImageCanvas current = this;
			Listener listener = new Listener() {
				boolean isDragging = false;
				int dragOriginX;
				int dragOriginY;
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.Paint: {
				    	Rectangle rect = getClientArea();
				    	paint(event.gc, rect.width, rect.height);
				    	break;
					}
					case SWT.Dispose: {
						if ((image != null) && (!image.isDisposed())) {
							image.dispose();
						}
						image = null;
						break;
					}
					case SWT.DragDetect: {
						isDragging = event.button == 1; 
						dragOriginX = event.x;
						dragOriginY = event.y;
						break;
					}
					case SWT.MouseMove: {
						if (isDragging) {
							atX += event.x - dragOriginX;
							atY += event.y - dragOriginY;
							dragOriginX = event.x;
							dragOriginY = event.y;
							internalNotifyListeners(current);
						}
						break;
					}
					case SWT.MouseDown: {
						break;
					}
					case SWT.MouseUp: {
						isDragging = false;
						break;
					}
					case SWT.MouseWheel: {
						double newZoom = zoom + 5.0 * zoom * event.count / 100.0;
						if (newZoom < 0.01)
							newZoom = 0.01;
						if (newZoom > 100)
							newZoom = 100;
						
						atX *= newZoom / zoom;
						atY *= newZoom / zoom;
						
						zoom = newZoom;
						internalNotifyListeners(current);
						break;
					}
					}					
				}
			};
			
			addListener(SWT.Paint, listener);
			addListener(SWT.Dispose, listener);
			addListener(SWT.DragDetect, listener);
			addListener(SWT.MouseMove, listener);
			addListener(SWT.MouseDown, listener);
			addListener(SWT.MouseUp, listener);
			addListener(SWT.MouseWheel, listener);
			addCanvasListener(this);
		}

		public void addCanvasListener(ImageCanvas imageCanvas) {
			if (!notify.contains(imageCanvas))
				notify.add(imageCanvas);
		}

		private void internalNotifyListeners(ImageCanvas canvas) {
			for (ImageCanvas i : notify) {
				i.internalUpdate(canvas);
			}
		}
		
		private void internalUpdate(ImageCanvas canvas) {
			atX = canvas.atX;
			atY = canvas.atY;
			zoom = canvas.zoom;
			redraw();
		}
		
		public void paint(GC gc, int width, int height) {
			if (image != null) {
				double srcTLX = Math.max(0.0, (imgWidth / 2.0 - (atX + width / 2.0) / zoom)); 
				double srcTLY = Math.max(0.0, (imgHeight / 2.0 - (atY + height / 2.0) / zoom)); 
				double destTLX = ((srcTLX - imgWidth / 2.0) * zoom + atX + width / 2.0); 
				double destTLY = ((srcTLY - imgHeight / 2.0) * zoom + atY + height / 2.0); 
				
				double srcBRX = Math.min(imgWidth, 1+ (imgWidth / 2.0 - (atX - width / 2.0) / zoom));
				double srcBRY = Math.min(imgHeight, 1+ (imgHeight / 2.0 - (atY - height / 2.0) / zoom)); 
				double destBRX = ((srcBRX - imgWidth / 2.0) * zoom + atX + width / 2.0); 
				double destBRY = ((srcBRY - imgHeight / 2.0) * zoom + atY + height / 2.0); 

				int srcWidth = (int) (srcBRX - srcTLX); 
				int srcHeight = (int) (srcBRY - srcTLY);
				int destWidth = (int) (destBRX - destTLX);
				int destHeight = (int) (destBRY - destTLY);
				if ((srcWidth > 0) && (srcHeight > 0) && (destWidth > 0) && (destHeight > 0)) {
					gc.drawImage(image, 
							(int) srcTLX, (int) srcTLY, srcWidth, srcHeight,
							(int) destTLX, (int) destTLY, destWidth, destHeight);
				}

				Color oldColor = gc.getForeground();
				Color white = new Color(gc.getDevice(), 255, 255, 255);
				gc.setForeground(white);
				gc.drawLine(0, height / 2, width, height / 2);
				gc.drawLine(width / 2, 0, width / 2, height);
				gc.setForeground(oldColor);
				white.dispose();
				
			}
		}
		
		public void setImageData(ImageData idata) {
			if (this.image != null) {
				this.image.dispose();
			}
			image = new Image(getDisplay(), idata);
			Rectangle rect = image.getBounds();
			imgWidth = rect.width;
			imgHeight = rect.height;
		}
	}
	
	public static class TestDialog {
		Shell shell;
		ImageCanvas c1;
		ImageCanvas c2;
		
		public void setImageData(ImageData idata) {
			c1.setImageData(idata);
			c2.setImageData(idata);
		}
		
		public void createWidgets() {
			shell = new Shell();
			shell.setLayout(new FillLayout());
			shell.setText("Have a nice day!");

			c1 = new ImageCanvas(shell, SWT.NONE, null);
			c2 = new ImageCanvas(shell, SWT.NONE, c1);
			
			shell.pack();
			shell.setSize(300, 300);
		}
		
		public void doIt() throws Exception {
			createWidgets();

//			ImageData idata = new ImageData(Const.sourceImage);
			BufferedImage bi = ImageIO.read(new File(Const.sourceImage));
			ImageData idata = ConvertBufferedImageToSWT_Image.convertToSWT(bi);
			setImageData(idata);
			
			shell.open();
			Display display = shell.getDisplay();
			while(!shell.isDisposed()){
				if(!display.readAndDispatch())
					display.sleep();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		TestImageCanvas.TestDialog test = new TestImageCanvas.TestDialog();
		test.doIt();
	}
}
