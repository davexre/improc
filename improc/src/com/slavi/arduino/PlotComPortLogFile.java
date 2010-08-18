package com.slavi.arduino;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;

import com.slavi.util.Util;
import com.slavi.util.ui.SwtUtil;

public class PlotComPortLogFile {

	Shell shell;
	Chart chart;

	public void createWidgets() {
        shell = new Shell();
        shell.setLayout(new FillLayout(SWT.HORIZONTAL));
        Composite parent = shell;
        
        chart = new Chart(parent, SWT.NONE);
        chart.getTitle().setText("comport.log");
        ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "comport.log");
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
		ArrayList<Integer> peaks = new ArrayList<Integer>();
		ArrayList<Integer> freqs = new ArrayList<Integer>();
		int curFreq = -1;
		int maxValue = -1;
		while (fin.ready()) {
			String line = Util.trimNZ(fin.readLine());
			StringTokenizer st = new StringTokenizer(line, ":"); 
			if (st.countTokens() < 6)
				continue;
			String time = st.nextToken();
			int curPresure = Integer.parseInt(st.nextToken());
			boolean isPlaying = st.nextToken().equals("1");
			boolean aborting = st.nextToken().equals("1");
			int freq = Integer.parseInt(st.nextToken());
			int maxPresure = Integer.parseInt(st.nextToken());
			int presureThreshold = Integer.parseInt(st.nextToken());
			
			int plotValue = curPresure;
			
			if (curFreq == freq) {
				if (maxValue < plotValue) {
					maxValue = plotValue;
				}
			} else {
				if (curFreq > 0) {
					peaks.add(Integer.valueOf(maxValue));
					freqs.add(Integer.valueOf(curFreq));
				}
				curFreq = freq;
				maxValue = plotValue;
			}
		}
		peaks.add(Integer.valueOf(maxValue));
		freqs.add(Integer.valueOf(curFreq));
		double dataY[] = new double[peaks.size()];
		double dataX[] = new double[peaks.size()];
		for (int i = 0; i < peaks.size(); i++) {
			dataX[i] = freqs.get(i);
			dataY[i] = peaks.get(i);
		}
		chart.getSeriesSet().getSeries()[0].setXSeries(dataX);
		chart.getSeriesSet().getSeries()[0].setYSeries(dataY);
		chart.getAxisSet().adjustRange();
	}

	public static void main(String[] args) throws Exception {
		String finName = System.getProperty("user.home") + "/comport.log";
		BufferedReader fin = new BufferedReader(new FileReader(finName));

		PlotComPortLogFile t = new PlotComPortLogFile();
		t.createWidgets();
		t.setFile(fin);
		fin.close();
		t.open();

		System.out.println("Done.");
	}
}
