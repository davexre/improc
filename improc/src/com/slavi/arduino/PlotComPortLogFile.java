package com.slavi.arduino;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ext.InteractiveChart;

import com.slavi.util.Util;
import com.slavi.util.ui.SwtUtil;

public class PlotComPortLogFile {

	static final String finName = "comport_2.log";
	
	Shell shell;
	Chart chart;
	
	public void createWidgets() {
        shell = new Shell();
        shell.setLayout(new FillLayout(SWT.HORIZONTAL));
        Composite parent = shell;
        
        chart = new InteractiveChart(parent, SWT.NONE) {
            public void save(String filename, int format) {
                Point size = getSize();
                Image image = new Image(Display.getDefault(), size.x, size.y);
                GC gc = new GC(image);
                gc.copyArea(image, 0, 0);
                gc.dispose();

                ImageData data = image.getImageData();
                ImageLoader loader = new ImageLoader();
                loader.data = new ImageData[] { data };
                loader.save(filename, format);
                image.dispose();
            }
        };
        chart.getTitle().setText(finName);
        ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, finName);
//        lineSeries.setYSeries(data);
        lineSeries.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        lineSeries.enableArea(true);
        lineSeries.setSymbolType(PlotSymbolType.NONE);
//        chart.getAxisSet().adjustRange();
        chart.getLegend().setVisible(false);
        for (IAxis axis : chart.getAxisSet().getAxes()) {
        	axis.getTitle().setVisible(false);
//        	axis.getTick().setVisible(false);
//        	axis.setRange(new Range(0, 3));
        }
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
	
	private void setFile(BufferedReader fin) throws Exception {
		ArrayList<Integer> yAxis = new ArrayList<Integer>();
		ArrayList<Integer> xAxis = new ArrayList<Integer>();
		int curFreq = -1;
		int maxValue = -1;
		while (fin.ready()) {
			String line = Util.trimNZ(fin.readLine());
			StringTokenizer st = new StringTokenizer(line, "\t"); 
			if (st.countTokens() < 8)
				continue;
			String time = st.nextToken();
			int freq = Integer.parseInt(st.nextToken());
			int curPresure = Integer.parseInt(st.nextToken());
			int maxPresure = Integer.parseInt(st.nextToken());
			double temperature1 = Double.parseDouble(st.nextToken());
			double temperature2 = Double.parseDouble(st.nextToken());
			boolean isPlaying = st.nextToken().equals("1");
			boolean aborting = st.nextToken().equals("1");
			
			int plotValue = maxPresure;
			yAxis.add((int)curPresure);
			xAxis.add(curFreq);
			
			if (curFreq == freq) {
				if (maxValue < plotValue) {
					maxValue = plotValue;
				}
			} else {
				if (curFreq > 0) {
//					yAxis.add(maxValue);
//					xAxis.add(curFreq);
				}
				curFreq = freq;
				maxValue = plotValue;
			}
		}
		yAxis.add(Integer.valueOf(maxValue));
		xAxis.add(Integer.valueOf(curFreq));
		double dataY[] = new double[yAxis.size()];
		double dataX[] = new double[yAxis.size()];
		for (int i = 0; i < yAxis.size(); i++) {
			dataX[i] = xAxis.get(i);
			dataY[i] = yAxis.get(i);
		}
//		chart.getSeriesSet().getSeries()[0].setXSeries(dataX);
		chart.getSeriesSet().getSeries()[0].setYSeries(dataY);
		chart.getAxisSet().adjustRange();
	}

	public static void main(String[] args) throws Exception {
		String fName = System.getProperty("user.home") + "/" + finName;
		BufferedReader fin = new BufferedReader(new FileReader(fName));

		PlotComPortLogFile t = new PlotComPortLogFile();
		t.createWidgets();
		t.setFile(fin);
		fin.close();
		t.open();

		System.out.println("Done.");
	}
}
