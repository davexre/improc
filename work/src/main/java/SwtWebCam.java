import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.slavi.image.DWindowedImage;
import com.slavi.image.DWindowedImageUtils;
import com.slavi.image.PDImageMapBuffer;
import com.slavi.improc.parallel.PComputeDirection;
import com.slavi.improc.parallel.PComputeMagnitude;
import com.slavi.math.MathUtil;
import com.slavi.util.ui.SwtUtil;

public class SwtWebCam {
	
	Webcam webcam;
	Shell shell;
	BufferedImage image;
	public static final String[] EmptyStringArray = {};
	
	PaletteData paletteData;
	ImageData imageData;
	Object synchImage = new Object();
	Canvas canvas;
	long lastPaintMillis;
	long countPaints;
	long countWebcamPaints;
	
	void processImage(BufferedImage image) {
		DWindowedImage img = new PDImageMapBuffer(image);
		DWindowedImage magnitude = new PDImageMapBuffer(img.getExtent());
		PComputeMagnitude.computeMagnitude(img, magnitude);
		//PComputeDirection.computeDirection(img, magnitude);
		BufferedImage process = DWindowedImageUtils.toImage(magnitude);
		image.getGraphics().drawImage(process, 0, 0, null);
	}
	
	void doIt() throws Exception {
		List<Webcam> webcams = Webcam.getWebcams();
		if (webcams.isEmpty())
			throw new Exception("No web camera found");
		shell = SwtUtil.makeShell(null, SWT.RESIZE | SWT.DIALOG_TRIM);
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
		Composite parent = new Composite(shell, SWT.NONE);
		parent.setLayout(new FillLayout());
		canvas = new Canvas(parent, SWT.NONE);

		final int pixel[] = new int[3];
		lastPaintMillis = 0;
		countPaints = 0;
		countWebcamPaints = 0;
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle r = e.gc.getClipping();
				e.gc.setBackground(new Color(e.gc.getDevice(), 0, 0, 0));
				e.gc.fillRectangle(r);
				String fps = "";
				synchronized (synchImage) {
					if (image == null)
						return;
					processImage(image);
					Raster raster = image.getRaster();
					for (int x = 0; x < image.getWidth(); x++) {
						for (int y = 0; y < image.getHeight(); y++) {
							raster.getPixel(x, y, pixel);
							int pixelValue = (pixel[0] << 16) | (pixel[1] << 8) | (pixel[2]);
							imageData.setPixel(x, y, pixelValue & 0xffffff);
						}
					}
					countPaints++;
					if (lastPaintMillis == 0) {
						lastPaintMillis = System.currentTimeMillis();
					} else {
						double seconds = System.currentTimeMillis() - lastPaintMillis;
						seconds /= 1000.0;
						fps = MathUtil.d2(countPaints / seconds) + " (" + Integer.toString((int)seconds) + " seconds) " +
								MathUtil.d2(webcam.getFPS());
					}
				}
				Image img = new Image(e.gc.getDevice(), imageData);
				e.gc.drawImage(img, 0, 0);
				e.gc.drawText(fps, 10, 100);
				img.dispose();
			}
		});
		shell.setLayout(new FillLayout());
		shell.pack();
		shell.setSize(200, 200);
		
		SwtUtil.centerShell(shell);
		shell.open();
		final Display display = shell.getDisplay();

		final AtomicBoolean refreshNeeded = new AtomicBoolean(false);
		webcam.addWebcamListener(new WebcamListener() {
			public void webcamOpen(WebcamEvent we) {
				Webcam webcam = we.getSource();
				Dimension size = webcam.getViewSize();
				synchronized (synchImage) {
					image = new BufferedImage((int)size.getWidth(), (int)size.getHeight(), BufferedImage.TYPE_INT_RGB);
					imageData = new ImageData((int)size.getWidth(), (int)size.getHeight(), 24, paletteData);
				}
			}
			
			public void webcamImageObtained(WebcamEvent we) {
				Webcam webcam = we.getSource();
				BufferedImage camImage = webcam.getImage();
				synchronized (synchImage) {
					countWebcamPaints++;
					image.getGraphics().drawImage(camImage, 0, 0, null);
				}
				boolean alreadySet = refreshNeeded.getAndSet(true);
				if (!alreadySet && !display.isDisposed()) {
					display.asyncExec(new Runnable() {
						public void run() {
							refreshNeeded.set(false);
							if (!canvas.isDisposed())
								canvas.redraw();
						}
					});
				}
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
