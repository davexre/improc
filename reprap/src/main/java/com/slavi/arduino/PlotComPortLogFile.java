package com.slavi.arduino;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
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
import org.swtchart.IAxis.Position;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ext.InteractiveChart;

import com.slavi.util.Util;
import com.slavi.util.swt.SwtUtil;

public class PlotComPortLogFile {

	static boolean labelFrequency = true;

	static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
//        chart.getTitle().setText(finName);
//        chart.getLegend().setVisible(false);
        chart.getTitle().setVisible(false);
		chart.getAxisSet().getXAxes()[0].getTitle().setText(labelFrequency ? "frequency (Hz)" : "hour");
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

	public static class MeasurementData {
		Date time = null;
		public int frequency = 0;
//		public boolean isPlaying = false;
//		public boolean aborting = false;
		public int pressure = 0;
		public double cellTemperature = 0;
		public double mosfetTemperature = 0;
		public double ambientTemperature = 0;
		public double current = 0;
		public int maxPressure = 0;
		public double maxCurrent = 0;
	}

	static ArrayList<MeasurementData> readData(BufferedReader fin) throws Exception {
		ArrayList<MeasurementData> r = new ArrayList<MeasurementData>();
		double curFreq = -1;
		MeasurementData d = null;
		while (fin.ready()) {
			String line = Util.trimNZ(fin.readLine());
			if ("".equals(line) || line.startsWith("*"))
				continue;
			StringTokenizer st = new StringTokenizer(line, "\t");
			if (st.countTokens() < 11)
				continue;
			Date time = df.parse(st.nextToken());
			int frequency = Integer.parseInt(st.nextToken());
			/*boolean isPlaying =*/ st.nextToken().equals("1");
			/*boolean aborting = */ st.nextToken().equals("1");
			int pressure = Integer.parseInt(st.nextToken());
			double cellTemperature = Double.parseDouble(st.nextToken());
			double mosfetTemperature = Double.parseDouble(st.nextToken());
			double ambientTemperature = Double.parseDouble(st.nextToken());
			double current = Double.parseDouble(st.nextToken());
			int maxPressure = Integer.parseInt(st.nextToken());
			double maxCurrent = Double.parseDouble(st.nextToken());

			if (curFreq != frequency) {
				if (curFreq > 0) {
					r.add(d);
				}
				curFreq = frequency;
				d = new MeasurementData();
				d.time = time;
				d.frequency = frequency;
			}
			d.pressure = Math.max(d.pressure, pressure);
			d.cellTemperature = Math.max(d.cellTemperature, cellTemperature);
			d.mosfetTemperature = Math.max(d.mosfetTemperature, mosfetTemperature);
			d.ambientTemperature = Math.max(d.ambientTemperature, ambientTemperature);
			d.current = Math.max(d.current, current);
			d.maxPressure = Math.max(d.maxPressure, maxPressure);
			d.maxCurrent = Math.max(d.maxCurrent, maxCurrent);
		}
		if (d != null)
			r.add(d);

		Collections.sort(r, new Comparator<MeasurementData>() {
			public int compare(MeasurementData o1, MeasurementData o2) {
				if (labelFrequency)
					return (o1.frequency < o2.frequency ? -1 : (o1.frequency > o2.frequency ? 1 : 0));
				return o1.time.compareTo(o2.time);
			}
		});
		return r;
	}

	private ILineSeries makeSeries(String label, int color, boolean createNewYSeries) {
		Color systemColor = Display.getDefault().getSystemColor(color);
		ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, label);
		lineSeries.setLineColor(systemColor);
		lineSeries.enableArea(false);
		lineSeries.setSymbolType(PlotSymbolType.NONE);

		IAxis yAxis;
		if ((chart.getSeriesSet().getSeries().length == 0) || (!createNewYSeries)) {
			yAxis = chart.getAxisSet().getYAxes()[0];
		} else {
			int axisId = chart.getAxisSet().createYAxis();
			yAxis = chart.getAxisSet().getYAxis(axisId);
			yAxis.setPosition(Position.Secondary);
		}

		yAxis.getTitle().setText(label);
		yAxis.getTick().setForeground(systemColor);
		yAxis.getTitle().setForeground(systemColor);
		lineSeries.setYAxisId(yAxis.getId());
        if (createNewYSeries) {
        }
        return lineSeries;
	}

	int colors[] = {
			SWT.COLOR_BLUE,
			SWT.COLOR_RED,
			SWT.COLOR_DARK_GREEN,
			SWT.COLOR_MAGENTA
	};
	int curColor = 0;

	int getNextColor() {
		return colors[(curColor++) % colors.length];
	}

	private void readFile(File fin) throws Exception {
		String finName = fin.getName();
		InputStream inStream = new FileInputStream(fin);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
		ArrayList<MeasurementData> data = readData(reader);
		reader.close();
		Date mindate = new Date(data.get(0).time.getTime());
		mindate.setHours(0);
		mindate.setMinutes(0);
		mindate.setSeconds(0);

		double dataX[] = new double[data.size()];
		double dataY[] = new double[data.size()];
//		double dataY2[] = new double[data.size()];

		for (int i = 0; i < data.size(); i++) {
			MeasurementData d = data.get(i);
			if (labelFrequency) {
				dataX[i] = d.frequency;
			} else {
				double delta = d.time.getTime() - mindate.getTime(); // delta in millis
				dataX[i] = delta / (1000 * 60 * 60);
			}
			dataY[i] = d.maxCurrent;
//			dataY2[i] = d.maxCurrent - d.current;
		}

		int col = getNextColor();
		ILineSeries lineSeries;
		lineSeries = makeSeries("current (A) " + finName, col, false);
		lineSeries.setXSeries(dataX);
		lineSeries.setYSeries(dataY);
/*
		lineSeries = makeSeries("pressure (rel)" + finName, getNextColor(), true);
		lineSeries.setXSeries(dataX);
		lineSeries.setYSeries(dataY2);*/
	}

	public void doIt() throws Exception {
		createWidgets();

//		readFile(new File(getClass().getResource("comport/comport_output_09.txt").toURI()));
//		readFile(new File(getClass().getResource("comport/comport_output_04.txt").toURI()));
//		readFile(new File(System.getProperty("user.home") + "/comport_output_12.txt"));
		readFile(new File(System.getProperty("user.home") + "/comport_output_11.txt"));

        chart.getAxisSet().adjustRange();
		open();
	}

	public static void main(String[] args) throws Exception {
		new PlotComPortLogFile().doIt();
		System.out.println("Done.");
	}
}
