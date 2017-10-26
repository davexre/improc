import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.slavi.imagefilter.SwtDisplayImage;
import com.slavi.math.MathUtil;
import com.slavi.util.swt.SwtUtil;

public class SwtWebCam {
	
	Webcam webcam;
	Shell shell;
	SwtDisplayImage displayImage;
	public static final String[] EmptyStringArray = {};
	
	PaletteData paletteData;
	Object synchImage = new Object();
	BufferedImage image = null;
	
	long lastPaintMillis;
	long countPaints;
	long countWebcamPaints;
	
	BufferedImage processImage(BufferedImage image) {
		return image;
/*		DWindowedImage img = new PDImageMapBuffer(image);
		DWindowedImage magnitude = new PDImageMapBuffer(img.getExtent());
		PComputeMagnitude.computeMagnitude(img, magnitude);
		//PComputeDirection.computeDirection(img, magnitude);
		BufferedImage process = DWindowedImageUtils.toImage(magnitude);
		//image.getGraphics().drawImage(process, 0, 0, null);
		return process;*/
	}
	
	void updateImage() {
		String fps = "";
		BufferedImage img;
		synchronized (synchImage) {
			img = image;
			image = null;
			countPaints++;
			if (lastPaintMillis == 0) {
				lastPaintMillis = System.currentTimeMillis();
			} else {
				double seconds = System.currentTimeMillis() - lastPaintMillis;
				seconds /= 1000.0;
				fps = 
						MathUtil.d2(countPaints / seconds) + 
						" " + MathUtil.d2(countWebcamPaints / seconds) + 
						" (" + Integer.toString((int)seconds) + " seconds) " +
						MathUtil.d2(webcam.getFPS());
			}
		}
		if (img != null) {
			img = processImage(img);
			((Graphics2D) img.getGraphics()).drawString(fps, 10, 100);
			displayImage.setBufferedImage(img);
		}
	}
	
	void doIt() throws Exception {
		List<Webcam> webcams = Webcam.getWebcams();
		if (webcams.isEmpty())
			throw new Exception("No web camera found");
		shell = SwtUtil.makeShell(null, SWT.RESIZE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.CLOSE);
		if (webcams.size() == 1) {
			webcam = webcams.get(0);
		} else {
			ArrayList<String> items = new ArrayList<>();
			for (Webcam i : webcams) {
				items.add(i.getName());
			}
			String camera = SwtUtil.optionBox(shell, "Select webcam", "Select a webcamera", Webcam.getDefault().getName(), items.toArray(EmptyStringArray));
			int selected = items.indexOf(camera);
			if (selected < 0)
				return;
			webcam = webcams.get(selected);
		}
		
		Dimension dimensions[] = webcam.getViewSizes();
		ArrayList<String> items = new ArrayList<>();
		for (Dimension i : dimensions) {
			items.add(i.width + " x " + i.height);
		}
		String dimension = SwtUtil.optionBox(shell, "Select resolution", "Select a webcamera resolution",items.get(0), items.toArray(EmptyStringArray));
		int selected = items.indexOf(dimension);
		if (selected < 0)
			return;
		Dimension resolution = dimensions[selected];
		webcam.setViewSize(resolution);
		
		paletteData = new PaletteData(0xFF0000, 0xFF00, 0xFF);
		displayImage = new SwtDisplayImage(shell, SWT.NONE);

		lastPaintMillis = 0;
		countPaints = 0;
		countWebcamPaints = 0;
		
		shell.setLayout(new FillLayout());
		shell.pack();
		shell.setSize(resolution.width + 50, resolution.height + 50);
		
		SwtUtil.centerShell(shell);
		shell.open();
		final Display display = shell.getDisplay();

		webcam.addWebcamListener(new WebcamListener() {
			public void webcamOpen(WebcamEvent we) {
			}
			
			public void webcamImageObtained(WebcamEvent we) {
				Webcam webcam = we.getSource();
				BufferedImage bimage = webcam.getImage();
				synchronized (synchImage) {
					countWebcamPaints++;
					image = bimage;
				}
				display.asyncExec(new Runnable() {
					public void run() {
						updateImage();
					}
				});
			}
			
			public void webcamDisposed(WebcamEvent we) {
			}
			
			public void webcamClosed(WebcamEvent we) {
			}
		});
		
		if (!webcam.open(true))
			shell.dispose();
		while(!shell.isDisposed()){
			if (Thread.currentThread().isInterrupted()) {
				shell.dispose();
			} else if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		webcam.close();
	}

	public static void main(String[] args) throws Exception {
		new SwtWebCam().doIt();
		System.out.println("Done.");
	}
}
