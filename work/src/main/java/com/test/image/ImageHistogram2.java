package com.test.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import com.slavi.util.ColorConversion;
import com.slavi.util.Const;
import com.slavi.util.Marker;
import com.slavi.util.file.FindFileIterator;
import com.slavi.util.swt.SwtUtil;

public class ImageHistogram2 {

	Shell shell;
	org.eclipse.swt.graphics.Image image;
	List fileList;
	Chart chartR;
	Chart chartG;
	Chart chartB;
	Chart chartL;
	Chart chartS;
	Chart chartV;
	Composite imageRect;
	String fdir = ".";
		
	private void addChartData(Chart chart, String dataName, int color) {
		ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, dataName);
//		lineSeries.setYSeries(data);
		lineSeries.setLineColor(Display.getDefault().getSystemColor(color));
		lineSeries.enableArea(false);
		lineSeries.setSymbolType(PlotSymbolType.NONE);
//		chart.getAxisSet().adjustRange();
	}
	
	private Chart makeChart(Composite parent, String dataName, int color) {
        Chart chart = new Chart(parent, SWT.NONE);
        chart.getTitle().setText(dataName);
        addChartData(chart, dataName, color);
        chart.getLegend().setVisible(false);
        for (IAxis axis : chart.getAxisSet().getAxes()) {
        	axis.getTitle().setVisible(false);
//        	axis.getTick().setVisible(false);
//        	axis.setRange(new Range(0, 3));
        }
        return chart;
	}

	private void setFile(String finName) throws Exception {
        shell.setText(finName);
        File fin = new File(finName);
        InputStream fis = new FileInputStream(fin);
        image = new org.eclipse.swt.graphics.Image(shell.getDisplay(), fis);
        fis.close();

		BufferedImage bi = ImageIO.read(fin);
		double r[] = new double[256];
		double g[] = new double[256];
		double b[] = new double[256];
		double l[] = new double[256];
		double s[] = new double[256];
		double v[] = new double[256];
		
		double DRGB[] = new double[3];
		double HSL[] = new double[3];
//		double HSV[] = new double[3];

		final int sizeX = bi.getWidth() - 1;
		final int sizeY = bi.getHeight() - 1;
		
		int ffcount = 0;
		Marker.mark("calc");
		for (int j = sizeY; j >= 0; j--) {
			for (int i = sizeX; i >= 0; i--) {
				int color = bi.getRGB(i, j);
				if (color == 0xffffff)
					ffcount++;
				ColorConversion.RGB.fromRGB(color, DRGB);
				r[(int) Math.round(DRGB[0] * 255.0)]++;
				g[(int) Math.round(DRGB[1] * 255.0)]++;
				b[(int) Math.round(DRGB[2] * 255.0)]++;
				ColorConversion.HSL.fromDRGB(DRGB, HSL);
				s[(int) Math.round(HSL[1] * 255.0)]++;
				l[(int) Math.round(HSL[2] * 255.0)]++;
//				ColorConversion.HSV.fromDRGB(DRGB, HSV);
//				v[(int) Math.round(HSV[2] * 255.0)]++;
			}
		}
		
		// calc cumul
		{
			double cumul[] = s;
			double sum = 0;
			for (double d : cumul) {
				sum += d;
			}
			double c = 0;
			for (int i = 0; i < cumul.length; i++) {
				c += cumul[i];
				s[i] = c / sum;
			}
		}
		{
			double cumul[] = l;
			double sum = 0;
			for (double d : cumul) {
				sum += d;
			}
			double c = 0;
			for (int i = 0; i < cumul.length; i++) {
				c += cumul[i];
				v[i] = c / sum;
			}
		}
		
		Marker.release();
		System.out.println("FF=" + ffcount);
		System.out.println("R[255] = " + r[255]);
		System.out.println("G[255] = " + g[255]);
		System.out.println("B[255] = " + b[255]);
		System.out.println("L[255] = " + l[255]);
		System.out.println("S[255] = " + s[255]);
		System.out.println("V[255] = " + v[255]);
		System.out.println("Pixels = " + bi.getWidth() * bi.getHeight());
		
		setChartData(chartR, 0, r);
		setChartData(chartR, 1, g);
		setChartData(chartR, 2, b);

		
		setChartData(chartG, 0, g);
		setChartData(chartB, 0, b);
		setChartData(chartL, 0, l);
		setChartData(chartV, 0, v);
		setChartData(chartS, 0, s);
		imageRect.redraw();
	}
	
	private void setChartData(Chart chart, int seriesIndex, double data[]) {
		chart.getSeriesSet().getSeries()[seriesIndex].setYSeries(data);
		chart.getAxisSet().adjustRange();
		chart.redraw();
	}
	
	private void setFiles(String finDir) {
		fdir = finDir;
		FindFileIterator ff = FindFileIterator.makeWithWildcard(finDir + "/*.jpg", true, true);
		fileList.removeAll();
		ArrayList<String> files = new ArrayList<String>();
		while (ff.hasNext()) {
			files.add(ff.next().getAbsolutePath());
		}
		Collections.sort(files);
		for (String file : files)
			fileList.add(file);
	}
	
	public void createWidgets() {
        shell = new Shell();
        shell.setLayout(new FillLayout(SWT.HORIZONTAL));

        // create a chart
        Composite parent;
        
        parent = new Composite(shell, SWT.NONE);
        parent.setLayout(new FillLayout(SWT.VERTICAL));

        Button button = new Button(parent, SWT.PUSH);
		button.setText("Browse");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String result = SwtUtil.browseForFolder(shell, "Images root folder", fdir);
				if (result != null)
					setFiles(result); 
			}
		});
        
        fileList = new List(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        fileList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				try {
					String sel[] = fileList.getSelection();
					if (sel.length > 0)
						setFile(sel[0]);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
        });
        
        imageRect = new Composite(parent, SWT.NONE);
        imageRect.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (image == null)
					return;
				Rectangle r = image.getBounds();
				double scale = Math.min((double) e.width / r.width, (double) e.height / r.height);
				int destW = (int) (r.width * scale);
				int destH = (int) (r.height * scale);
				e.gc.drawImage(image, 0, 0, r.width, r.height, 
						(e.width - destW) / 2 , (e.height - destH) / 2 , destW, destH);
			}
        });
        
        parent = new Composite(shell, SWT.NONE);
        parent.setLayout(new FillLayout(SWT.VERTICAL));
        
        chartR = makeChart(parent, "Red", SWT.COLOR_RED);
        addChartData(chartR, "Green", SWT.COLOR_GREEN);
        addChartData(chartR, "Blue", SWT.COLOR_BLUE);
        
        
        chartG = makeChart(parent, "Green", SWT.COLOR_GREEN);
        chartB = makeChart(parent, "Blue", SWT.COLOR_BLUE);

        parent = new Composite(shell, SWT.NONE);
        parent.setLayout(new FillLayout(SWT.VERTICAL));

        chartL = makeChart(parent, "Light", SWT.COLOR_YELLOW);
        chartV = makeChart(parent, "Value", SWT.COLOR_CYAN);
        chartS = makeChart(parent, "Saturation", SWT.COLOR_DARK_MAGENTA);

        shell.pack();
        shell.setSize(1200, 1000);
	}
	
	public void open() throws Exception {
        SwtUtil.centerShell(shell);
        shell.open();
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        shell.dispose();
	}
	
	public static void main(String[] args) throws Exception {
		ImageHistogram2 t = new ImageHistogram2();
//		String finDir = "D:/Users/S/Java/Images/Image data/Evgeni panorama/1/";
//		String finDir = "D:/Users/S/Java/Images/Image data/Beli plast";
//		String finDir = "/home/slavian/S/java/Images/Image data/20090801 Vodopad Skaklia/Skaklia 2";
//		String finDir = "/home/slavian/S/temp/1";
		String finDir = Const.workDir;
		
		t.createWidgets();
		t.setFiles(finDir);
		t.open();
		System.out.println("Done.");
	}

}
