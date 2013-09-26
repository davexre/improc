package com.slavi.reprap.plotter;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import com.slavi.dxf.DxfRead;

public class PlotterDxfTest {
	double maxCircleSegmentLength = 5000;

	ArrayList<Path2D> paths = new ArrayList<Path2D>();
	
	static double getDouble(Map<Integer, String> entity, int key) {
		return Double.parseDouble(entity.get(key));
	}
	
	class InternalTestDxfRead extends DxfRead {
		public InternalTestDxfRead(InputStream in) {
			super(in);
		}
		
		public void blockRead(Map<Integer, String> block, ArrayList<Map<Integer, String>> blockEntities, Map<Integer, String> blockData) {
			System.out.println("Block :" + block.get(2) + " blockData:" + blockData.size());
		}

		public void entityRead(Map<Integer, String> entity) {
			String entytyType = entity.get(0);
			if ("LINE".equals(entytyType)) {
				Path2D path = new Path2D.Double();
				path.moveTo(getDouble(entity, 10), getDouble(entity, 20));
				path.lineTo(getDouble(entity, 11), getDouble(entity, 21));
				paths.add(path);
			} else if ("POINT".equals(entytyType)) {
				// skip it
			} else if ("ATTDEF".equals(entytyType)) {
				// skip it
			} else if ("TEXT".equals(entytyType)) {
				// skip it
			} else if ("INSERT".equals(entytyType)) {
				// skip it
			} else if ("ARC".equals(entytyType)) {
				// skip it
			} else {
				System.out.println("Entity:" + entity.get(0));
			}
		}

		public void tableRead(Map<Integer, String> tableDesc, ArrayList<Map<Integer, String>> table) {
//			System.out.println("Table :" + tableDesc.get(2));
		}

		public void err(int lineNumber, String desc) {
			System.err.println(lineNumber + ": " + code + "/" + val + ": " + desc);
		}
	}

	public void doIt() throws Exception {
		String dxfRoot = "/home/slavian/java/workspace4test/ycad/dat";
		InputStream fin = new FileInputStream(new File(dxfRoot, "hexhouse.dxf"));
		try {
			DxfRead dxfRead = new InternalTestDxfRead(fin);
			dxfRead.readDxf();
		} finally {
			fin.close();
		}

//		RepRapPlotter reprap = new RepRapPlotter();
		try {
			//reprap.start("/dev/ttyUSB0");
			double coords[] = new double[6];
			for (Path2D i : paths) {
				PathIterator pIter = i.getPathIterator(null);
				while (!pIter.isDone()) {
					int type = pIter.currentSegment(coords);
					switch (type) {
					case PathIterator.SEG_MOVETO:
//						reprap.moveTo(coords[0], coords[1], true);
						break;
					case PathIterator.SEG_LINETO:
//						reprap.moveTo(coords[0], coords[1], false);
						break;
					}
					pIter.next();
				}
			}
		} finally {
			//reprap.stop();
		}
	}
	
	public static void main(String[] args) throws Exception {
		new PlotterDxfTest().doIt();
		System.out.println("Done.");
	}
}
