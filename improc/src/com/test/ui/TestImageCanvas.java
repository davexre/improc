package com.test.ui;

import org.eclipse.swt.SWT;
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
		ImageData idata;
		Image image;
		double atX = 0; 
		double atY = 0;
		int imgWidth = 0;
		int imgHeight = 0;
		double zoom = 30.0;
		
		public ImageCanvas(Composite parent, int style) {
			super(parent, style | SWT.DOUBLE_BUFFERED );
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
						idata = null;
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
//							if (atX > 0) 
//								atX = 0;
//							if (atY > 0) 
//								atY = 0;
							dragOriginX = event.x;
							dragOriginY = event.y;
							redraw();
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
						redraw();
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
		}
		
		public void paint(GC gc, int width, int height) {
			if (image != null) {
				double x1 = Math.max(0.0, (imgWidth / 2.0 - (atX + width / 2.0) / zoom)); 
				double y1 = Math.max(0.0, (imgHeight / 2.0 - (atY + height / 2.0) / zoom)); 
				double X1 = ((x1 - imgWidth / 2.0) * zoom + atX + width / 2.0); 
				double Y1 = ((y1 - imgHeight / 2.0) * zoom + atY + height / 2.0); 
				
				double x2 = Math.min(imgWidth, 1+ (imgWidth / 2.0 - (atX - width / 2.0) / zoom));
				double y2 = Math.min(imgHeight, 1+ (imgHeight / 2.0 - (atY - height / 2.0) / zoom)); 
				double X2 = ((x2 - imgWidth / 2.0) * zoom + atX + width / 2.0); 
				double Y2 = ((y2 - imgHeight / 2.0) * zoom + atY + height / 2.0); 
				System.out.println(
//						zoom
//						atX + "\t" + atY + "\t" +
						x1 + "\t" +	y1 + "\t" + x2 + "\t" + y2 + "\t|" +
						X1 + "\t" +	Y1 + "\t" + X2 + "\t" + Y2
						);
				int w = (int) (x2 - x1); 
				int h = (int) (y2 - y1);
				int W = (int) (X2 - X1);
				int H = (int) (Y2 - Y1);
				if ((w > 0) && (h > 0) && (W > 0) && (H > 0)) {
					gc.drawImage(image, 
							(int) x1, (int) y1, w, h,
							(int) X1, (int) Y1, W, H);
				}
				gc.drawLine(0, height / 2, width, height / 2);
				gc.drawLine(width / 2, 0, width / 2, height);
//				gc.drawImage(image, 0, 0, imgWidth, imgHeight, atX, atY, (int) (imgWidth * zoom), (int) (imgHeight * zoom));
			}
		}
		
		public void setImageData(ImageData idata) {
			if (this.image != null) {
				this.image.dispose();
			}
			this.idata = idata;
			image = new Image(getDisplay(), idata);
			Rectangle rect = image.getBounds();
			imgWidth = rect.width;
			imgHeight = rect.height;
		}
	}
	

	public static void main(String[] args) throws Exception {
		ImageData idata = new ImageData(Const.sourceImage);
//		BufferedImage src = ImageIO.read(new File(Const.sourceImage)); 
//		src.getData().getDataBuffer().
		
//		SwtUtil.openTaskManager(null, true);
		Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		shell.setText("Have a nice day!");
		ImageCanvas c = new ImageCanvas(shell, SWT.NONE);
		c.setImageData(idata);
		shell.pack();
		shell.setSize(300, 300);
		shell.open();
		Display display = shell.getDisplay();
		while(!shell.isDisposed()){
			if(!display.readAndDispatch())
				display.sleep();
		}
	}
}
