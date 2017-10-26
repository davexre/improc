package com.slavi.imagefilter;

import java.awt.image.BufferedImage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.slavi.util.swt.SwtUtil;

public class SwtDisplayImage extends Composite implements Listener {

	Image image = null;
	int imageWidth = -1;
	int imageHeight = -1;
	
	double zoom = 1.0;
	Point origin = new Point(0, 0);
	
	public static enum ZoomBehaviour {
		zoomToFit,
		customZoom
	}

	ZoomBehaviour zoomBehaviour = ZoomBehaviour.zoomToFit;
	
	public SwtDisplayImage(Composite parent, int style) {
		super(parent, style);

		addListener(SWT.Paint, this);
		addListener(SWT.MouseUp, this);
		addListener(SWT.MouseDown, this);
		addListener(SWT.MouseMove, this);
		addListener(SWT.MouseWheel, this);
		addListener(SWT.Resize, this);
	}

	public void setImage(Image image) {
		if (this.image != null)
			this.image.dispose();
		this.image = image;
		if (image == null) {
			imageWidth = -1;
			imageHeight = -1;
		} else {
			Rectangle bounds = image.getBounds();
			imageWidth = bounds.width;
			imageHeight = bounds.height;
		}
		if (zoomBehaviour == ZoomBehaviour.zoomToFit)
			zoomToFit();
		redraw();
	}
	
	public Image getImage() {
		return this.image;
	}
	
	public void setBufferedImage(BufferedImage bImage) {
		if (bImage == null) {
			setImage(null);
		} else {
			ImageData imageData =  SwtUtil.copyAwtImage(bImage, null);
			setImage(new Image(getDisplay(), imageData));
		}
	}
	
	public void dispose() {
		setImage(null);
		super.dispose();
	}
	
	public double getZoom() {
		return zoom;
	}
	
	public Point getOrigin() {
		return new Point(origin.x, origin.y);
	}

	void fixOrigin() {
		Point size = getSize();
		switch (zoomBehaviour) {
		case zoomToFit:
			//center();
			origin.x = (int) (size.x - zoom * imageWidth) / 2;
			origin.y = (int) (size.y - zoom * imageHeight) / 2;
			break;
		case customZoom:
			if (origin.x > size.x - 10)
				origin.x = size.x - 10;
			if (origin.y > size.y - 10)
				origin.y = size.y - 10;
			double t = imageWidth * zoom;
			if (t + origin.x < 10)
				origin.x = (int) (10.0 - t);
			t = imageHeight * zoom;
			if (t + origin.y < 10)
				origin.y = (int) (10.0 - t);
			break;
		}
	}
	
	public void setOrigin(int x, int y) {
		this.origin.x = x;
		this.origin.y = y;
		fixOrigin();
		redraw();
	}
	
	public void setZoom(double zoom) {
		if ((zoom > 0) && (zoom < 10) &&
			(this.imageWidth * zoom >= 10) &&
			(this.imageHeight * zoom >= 10)) {
			this.zoom = zoom;
		}
		if (zoomBehaviour == ZoomBehaviour.zoomToFit)
			center();
		else
			fixOrigin();
		redraw();
	}

	public void center() {
		Point size = getSize();
		setOrigin(
			(int) (size.x - zoom * imageWidth) / 2,
			(int) (size.y - zoom * imageHeight) / 2);
		redraw();
	}
	
	public void zoomToFit() {
		Point size = getSize();
		setZoom(Math.min(
				(double) size.x / (double) imageWidth,
				(double) size.y / (double) imageHeight));
		center();
	}
	
	public ZoomBehaviour getZoomBehaviour() {
		return this.zoomBehaviour;
	}
	
	public void setZoomBehaviour(ZoomBehaviour zoomBehaviour) {
		if (zoomBehaviour == null || zoomBehaviour == this.zoomBehaviour)
			return;
		this.zoomBehaviour = zoomBehaviour;
		if (zoomBehaviour == ZoomBehaviour.zoomToFit)
			zoomToFit(); // This method will make invoke redraw;
		else
			redraw();
	}
	
	boolean isDragging = false;
	int dragOriginX = 0;
	int dragOriginY = 0;
	
	public void handleEvent(Event event) {
		switch (event.type) {
		case SWT.Paint:
			if (image == null)
				return;
			int dx = (int) (zoom * imageWidth);
			int dy = (int) (zoom * imageHeight);
			event.gc.drawImage(image, 0, 0, imageWidth, imageHeight,
					origin.x, origin.y, dx, dy);
			break;
		case SWT.MouseUp:
			switch (event.button) {
			case 1:
			case 2:
				isDragging = false;
				Cursor cursor = getDisplay().getSystemCursor(SWT.CURSOR_ARROW);
				setCursor(cursor);
				break;
			case 3:
				zoomBehaviour = ZoomBehaviour.zoomToFit;
				zoomToFit();
				break;
			}
			break;
		case SWT.MouseDown:
			switch (event.button) {
			case 1:
			case 2:
				isDragging = true;
				dragOriginX = event.x;
				dragOriginY = event.y;
				Cursor cursor = getDisplay().getSystemCursor(SWT.CURSOR_HAND);
				setCursor(cursor);
				break;
			}
			break;
		case SWT.MouseMove:
			if (isDragging) {
				zoomBehaviour = ZoomBehaviour.customZoom;
				setOrigin(
					origin.x + event.x - dragOriginX,
					origin.y + event.y - dragOriginY);
				dragOriginX = event.x;
				dragOriginY = event.y;
			}
			break;
		case SWT.MouseWheel:
			zoomBehaviour = ZoomBehaviour.customZoom;
			double x = (event.x - origin.x) / zoom;
			double y = (event.y - origin.y) / zoom;
			double newZoom = getZoom() * (1.0 + event.count / 15.0);
			setZoom(newZoom);
			setOrigin(
				(int) (event.x - x * zoom),
				(int) (event.y - y * zoom));
			break;
		case SWT.Resize:
			if (zoomBehaviour == ZoomBehaviour.zoomToFit) {
				zoomToFit();
			}
			break;
		default:
			break;
			//System.out.println("Listener: " + event);
		}
	}
}
