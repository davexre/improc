package com.slavi.img.imagefilter;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.slavi.utils.SwtUtl;

public class ImageFilter {
	Display display;
	Shell shell;
	
	Canvas imageCanvas;
	Label lblCurFile;
	
	int outputImageSizeX;
	int outputImageSizeY;
	BufferedImage outputImage;

	BufferedImage sourceImage;

	BufferedImageFilter imageFilter;

	public ImageFilter(BufferedImageFilter imageFilter) {
		this.imageFilter = imageFilter;
	}
	
	public void updateFilter() {
		if (sourceImage == null)
			outputImage = null;
		else
			outputImage = imageFilter.getFilteredImage();
		if (outputImage == null) {
			outputImageSizeX = outputImageSizeY = 0;
		} else {
			outputImageSizeX = outputImage.getWidth();
			outputImageSizeY = outputImage.getHeight();
		}
		if (imageCanvas != null)
			imageCanvas.repaint();
	}

	public void setSourceImage(BufferedImage image) {
		sourceImage = image;
		imageFilter.setImage(sourceImage);
		updateFilter();
	}
	
	public Shell open(Display dpy) {
		// Create a window and set its title.
		this.display = dpy;
		shell = new Shell(display);
		shell.setText("Image process - " + imageFilter.getFilterName());
		shell.setMinimumSize(600, 300);
		// Create the "File" group
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		shell.setLayout(layout);
				
		Group menuGroup = new Group(shell, SWT.NONE);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.minimumWidth = 100;
		gridData.minimumHeight = 80;
		gridData.widthHint = 200;
		gridData.heightHint = 90;
		menuGroup.setLayoutData(gridData);
		layout = new GridLayout();
		layout.numColumns = 1;
		menuGroup.setLayout(layout);
		menuGroup.setText("File");

		Composite composite = new Composite(menuGroup, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		composite.setLayout(rowLayout);
		
		Button btnOpen = new Button(composite, SWT.NONE);
		btnOpen.setText("&Open");
		btnOpen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String fin = SwtUtl.openFile(shell, "Select an image", null, SwtUtl.imageFileFilter);
				if (fin == null)
					return;
				try {
					BufferedImage src = ImageIO.read(new File(fin));
					lblCurFile.setText(fin);
					setSourceImage(src);
				} catch (IOException e1) {
					SwtUtl.msgboxError(shell, "Error opening file " + fin);
				}
			}
		});

		Button btnSave = new Button(composite, SWT.NONE);
		btnSave.setText("&Save");
		btnSave.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String fou = SwtUtl.saveFile(shell, null, null, new File(lblCurFile.getText()).getName(), SwtUtl.imageFileFilter);
				if (fou == null)
					return;
				try {
					ImageIO.write(outputImage, "png", new File(fou));
				} catch (IOException e1) {
					SwtUtl.msgboxError(shell, "Error saving image " + fou);
				}
			}
		});
		
		Label separator = new Label(menuGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		
		lblCurFile = new Label(menuGroup, SWT.NONE | SWT.WRAP);
		lblCurFile.setLayoutData(new GridData(GridData.FILL_BOTH));
		lblCurFile.setText("No file selected");
		
		// Create the "Image" group
		Group imageGroup = new Group(shell, SWT.NONE);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.verticalSpan = 2;
		imageGroup.setLayoutData(gridData);
		imageGroup.setLayout(new FillLayout());
		imageGroup.setText("Image");
		composite = new Composite(imageGroup, SWT.EMBEDDED);
		composite.setLayout(new FillLayout());
		final Frame imageFrame = SWT_AWT.new_Frame(composite);
		imageCanvas = new Canvas() {
			private static final long serialVersionUID = 1L;
			public void paint (Graphics g) {
				int cx = imageCanvas.getWidth() - 1;
				int cy = imageCanvas.getHeight() - 1;
				g.clearRect(0, 0, cx, cy);
				if ((outputImageSizeX == 0) || (outputImageSizeX == 0)) 
					return;
				int sx = cx;
				int sy = outputImageSizeY * sx / outputImageSizeX;
				if (sy > cy) {
					sy = cy;
					sx = outputImageSizeX * sy / outputImageSizeY;
				}
				g.drawImage(outputImage,
						(cx - sx) / 2, (cy - sy) / 2, (cx + sx) / 2, (cy + sy) / 2,
						0, 0, outputImageSizeX, outputImageSizeY,
						imageFrame);
			}
		};
		imageFrame.add(imageCanvas);
				
		// Create the "Filter parameters" group
		Group filterGroup = new Group(shell, SWT.NONE);
		gridData = new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL);
		filterGroup.setLayoutData(gridData);
		filterGroup.setText("Filter parameters");
		filterGroup.setLayout(new FillLayout());
		imageFilter.createFilterWidgets(filterGroup, this);
		
		shell.pack();
		shell.open();
		
		return shell;
	}
}
