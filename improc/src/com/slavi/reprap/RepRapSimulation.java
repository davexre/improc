package com.slavi.reprap;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;

import javax.media.j3d.BranchGroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.j3d.renderer.java3d.loaders.STLLoader;

import com.slavi.util.ui.SwtUtil;
import com.sun.j3d.loaders.Scene;

public class RepRapSimulation implements RepRapPrinter, Runnable {

	Display display;
	Shell shell;
	Canvas canvas;	
	BufferedImage biShow;
	BufferedImage biDraw;
	
	public class PointIterator {
		PrintLayer layer;
		PathIterator iter;
		int objectType;
		int segmentType;
		double coords[] = new double[6];
		
		public PointIterator(PrintLayer layer) {
			this.layer = layer;
			objectType = 0;
			segmentType = 0;
			iter = RepRapRoutines.reorderPath(startNearHere, layer.outline.getPathIterator(null)).getPathIterator(null);
		}
		
		public boolean getNext() {
			while (iter.isDone()) {
				switch (objectType) {
				case 0:
					iter = RepRapRoutines.reorderPath(startNearHere, layer.outfillsHatch.getPathIterator(null)).getPathIterator(null);
					break;
				case 1:
					iter = RepRapRoutines.reorderPath(startNearHere, layer.infillsHatch.getPathIterator(null)).getPathIterator(null);
					break;
				case 2:
					iter = RepRapRoutines.reorderPath(startNearHere, layer.supportHatch.getPathIterator(null)).getPathIterator(null);
					break;
				default:
					return false;
				}
				objectType++;
			}
			segmentType = iter.currentSegment(coords);
			iter.next();
			return true;
		}
	}
	
	Point2D.Double startNearHere;

	public void startPrinting(Rectangle2D bounds) throws Exception {
		startNearHere = new Point2D.Double(0, 0);
	}

	public void printLayer(PrintLayer layer) throws Exception {
		System.out.println("Printing layer " + layer.layerNumber);
		
		if (layer.layerNumber < 3)
			return;
		
		Graphics2D g = (Graphics2D) biDraw.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, biDraw.getWidth(), biDraw.getHeight());
//		long startTime = System.currentTimeMillis();
//		double drawnLength = 0;
		
		PointIterator iter = new PointIterator(layer);
		iter.getNext();
		int toX = (int) iter.coords[0];
		int toY = (int) iter.coords[1];
		int lastMoveToX = toX;
		int lastMoveToY = toY;
		while (true) {
			g.dispose();
			BufferedImage tmp = biShow;
			biShow = biDraw;
			biDraw = tmp;
			canvas.repaint();

			Thread.sleep(20);
			if (!iter.getNext()) {
				break;
			}			
			
			g = (Graphics2D) biDraw.getGraphics();
			g.drawImage(biShow, 0, 0, biShow.getWidth(), biShow.getHeight(), null);

			switch (iter.objectType) {
			case 0:
				g.setColor(Color.blue);		// outline
				break;
			case 1:
				g.setColor(Color.green);	// outfills
				break;
			case 2:
				g.setColor(Color.yellow);	// infills
				break;
			case 3:
				g.setColor(Color.gray);		// support
				break;
			default:
				g.setColor(Color.red);		// error
				break;
			}
			
			int fromX = toX;
			int fromY = toY;
			switch (iter.segmentType) {
			case PathIterator.SEG_LINETO:
				toX = (int) iter.coords[0];
				toY = (int) iter.coords[1];
				break;
			case PathIterator.SEG_CLOSE:
				toX = lastMoveToX;
				toY = lastMoveToY;
				break;
			case PathIterator.SEG_MOVETO:
				lastMoveToX = toX = (int) iter.coords[0];
				lastMoveToY = toY = (int) iter.coords[1];
				g.setColor(Color.cyan);		// non-printing move
				break;
			default:
				continue;
			}
			g.drawLine(fromX, fromY, toX, toY);
		}

		Thread.sleep(500);
	}

	public void stopPrinting() throws Exception {
	}
	
	PrintJob job;
	public void run() {
		try {
			job.print(this);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	void preparePrinting() throws Exception {
		String fname = "pulley-4.5-6-8-40.stl";
		URL fin = getClass().getResource(fname);
		STLLoader loader = new STLLoader();
		Scene scene = loader.load(fin);
		BranchGroup stl = scene.getSceneGroup();
		ArrayList objects = new ArrayList();
		objects.add(stl);
		job = new PrintJob();
		job.initialize(objects);
	}
	
	Thread thread;
	void start() throws Exception {
		biShow = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		biDraw = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);

		display = new Display();
		shell = new Shell(display);
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		shell.setMinimumSize(600, 300);
		Composite composite = new Composite(shell, SWT.EMBEDDED);
		composite.setLayout(new FillLayout());
		Frame imageFrame = SWT_AWT.new_Frame(composite);
		canvas = new Canvas() {
			public void paint (Graphics g) {
				g.drawImage(biDraw, 0, 0, biDraw.getWidth(), biDraw.getHeight(), null); 
			}
			
			public void update(Graphics g) {
				paint(g);
			}
		};
		
		imageFrame.add(canvas);

		shell.pack();
		SwtUtil.centerShell(shell);
		shell.setMaximized(true);
		shell.open();

		preparePrinting();
		thread = new Thread(this);
		thread.start();
		
		while (!shell.isDisposed())
			if (!display.readAndDispatch()) display.sleep();
		thread.interrupt();
		display.dispose();
	}
	
	public static void main(String[] args) throws Exception {
		RepRapSimulation app = new RepRapSimulation();
		app.start();
	}

}
