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
import com.slavi.util.ui.SwtUtil;

public class TestImageCanvas {

	public static class ImageCanvas extends Composite {
		ImageData idata;
		Image image;
		int atX = 0; 
		int atY = 0;
		int imgWidth = 0;
		int imgHeight = 0;
		double zoom = 1.0;
		
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
							if (atX > 0) 
								atX = 0;
							if (atY > 0) 
								atY = 0;
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
						zoom += 5 * zoom * event.count / 100.0;
						if (zoom < 0.01)
							zoom = 0.01;
						if (zoom > 100)
							zoom = 100;
						System.out.println(zoom);
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
				gc.drawImage(image, 0, 0, imgWidth, imgHeight, atX, atY, (int) (imgWidth * zoom), (int) (imgHeight * zoom));
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
		
		SwtUtil.openTaskManager(null, true);
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
