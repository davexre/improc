package com.slavi.dxf;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DxfRead {

	Map<Integer, String> vportCodeName = new HashMap<Integer, String>();
	Map<Integer, String> ltypeCodeName = new HashMap<Integer, String>();
	Map<Integer, String> layerCodeName = new HashMap<Integer, String>();
	Map<Integer, String> styleCodeName = new HashMap<Integer, String>();

	ArrayList<Properties> vports = new ArrayList<Properties>();
	ArrayList<Properties> ltypes = new ArrayList<Properties>();
	ArrayList<Properties> layers = new ArrayList<Properties>();
	ArrayList<Properties> styles = new ArrayList<Properties>();
	
	public DxfRead() {
		vportCodeName.put(2, "name");
		vportCodeName.put(5, "handle");
		vportCodeName.put(10, "llx");
		vportCodeName.put(11, "urx");
		vportCodeName.put(12, "vcpx");
		vportCodeName.put(13, "sbpx");
		vportCodeName.put(14, "ssx");
		vportCodeName.put(15, "gsx");
		vportCodeName.put(16, "vdir.x");
		vportCodeName.put(17, "vtgt.x");
		
		vportCodeName.put(20, "lly");
		vportCodeName.put(21, "ury");
		vportCodeName.put(22, "vcpy");
		vportCodeName.put(23, "sbpy");
		vportCodeName.put(24, "ssy");
		vportCodeName.put(25, "gsy");
		vportCodeName.put(26, "vdir.y");
		vportCodeName.put(27, "vtgt.y");

		vportCodeName.put(36, "vdir.z");
		vportCodeName.put(37, "vtgt.z");

		vportCodeName.put(40, "vheight");
		vportCodeName.put(41, "vaspect");
		vportCodeName.put(42, "lensLength");
		vportCodeName.put(43, "fcp");
		vportCodeName.put(44, "bcp");
		
		vportCodeName.put(50, "snaprotang");
		vportCodeName.put(51, "viewtwistang_deg");
		
		vportCodeName.put(68, "status");
		vportCodeName.put(69, "id");
		vportCodeName.put(70, "flags");
		vportCodeName.put(71, "viewmode");
		vportCodeName.put(72, "circleZoomPercent");
		vportCodeName.put(73, "fastZoomSetting");
		vportCodeName.put(74, "ucsicon");
		vportCodeName.put(75, "snaponoff");
		vportCodeName.put(76, "gridonoff");
		vportCodeName.put(77, "snapStyle");
		vportCodeName.put(78, "snapIsopair");
		
		vportCodeName.put(100, "subClassMarker");
		
		///////////////////////
		
		ltypeCodeName.put(2, "name");
		ltypeCodeName.put(3, "desc");
		ltypeCodeName.put(4, "namelist");
		ltypeCodeName.put(5, "handle");
		
		ltypeCodeName.put(40, "patternlen");
		ltypeCodeName.put(44, "xlist");
		ltypeCodeName.put(45, "ylist");
		ltypeCodeName.put(46, "scalelist");
		ltypeCodeName.put(49, "lenlist");
		ltypeCodeName.put(50, "rotlist");
		
		ltypeCodeName.put(70, "flags");
		ltypeCodeName.put(72, "aligncode");
		ltypeCodeName.put(73, "dashlencount");

		///////////////////////
		
		layerCodeName.put(2, "name");
		layerCodeName.put(5, "handle");
		layerCodeName.put(6, "ltype");
		layerCodeName.put(62, "aci");
		layerCodeName.put(70, "flags");

		///////////////////////

		styleCodeName.put(2, "name");
		styleCodeName.put(5, "handle");
		styleCodeName.put(6, "ltype");
		styleCodeName.put(62, "aci");
		styleCodeName.put(70, "flags");
	}

	BufferedReader r;
	
	boolean hasPushedPair = false;
	int code;
	String val;
	
	Properties prop = new Properties();
	
	void pushPair() throws Exception {
		if (hasPushedPair) {
			throw new Exception("Only one pair can be pushed");
		}
		hasPushedPair = true;
	}
	
	void readPair() throws Exception {
		if (hasPushedPair) {
			hasPushedPair = false;
			return;
		}
		if (r.ready()) {
			code = Integer.parseInt(r.readLine());
			val = r.readLine().trim();
		} else {
			code = 0;
			val = "";
		}
	}
	
	void readHeader() throws Exception {
		while (r.ready()) {
			readPair();
			switch (code) {
			case 0:
				if ("ENDSEC".equals(val)) {
					return;
				}
				break;
				
			case 9:
				if ("$ACADVER".equals(val)) {
					readPair();
					prop.setProperty("$ACADVER", val);
				} else if ("$AUNITS".equals(val)) {
					readPair();
					prop.setProperty("$AUNITS", val);
				} else if ("$CECOLOR".equals(val)) {
					readPair();
					prop.setProperty("$CECOLOR", val);
				} else if ("$CELTYPE".equals(val)) {
					readPair();
					prop.setProperty("$CELTYPE", val);
				} else if ("$CLAYER".equals(val)) {
					readPair();
					prop.setProperty("$CLAYER", val);
				} else if ("$EXTMIN".equals(val)) {
					readPair();
					while (code != 0 && code != 9) {
						switch (code) {
						case 10:
							prop.setProperty("$EXTMIN.X", val);
							break;
						case 20:
							prop.setProperty("$EXTMIN.Y", val);
							break;
						case 30:
							prop.setProperty("$EXTMIN.Z", val);
							break;
						}
						readPair();
					}
				} else if ("$EXTMAX".equals(val)) {
					readPair();
					while (code != 0 && code != 9) {
						switch (code) {
						case 10:
							prop.setProperty("$EXTMAX.X", val);
							break;
						case 20:
							prop.setProperty("$EXTMAX.Y", val);
							break;
						case 30:
							prop.setProperty("$EXTMAX.Z", val);
							break;
						}
						readPair();
					}
				} else if ("$FILLMODE".equals(val)) {
					readPair();
					prop.setProperty("$FILLMODE", val);
				} else if ("$LIMMIN".equals(val)) {
					readPair();
					while (code != 0 && code != 9) {
						switch (code) {
						case 10:
							prop.setProperty("$LIMMIN.X", val);
							break;
						case 20:
							prop.setProperty("$LIMMIN.Y", val);
							break;
						}
						readPair();
					}
				} else if ("$LIMMAX".equals(val)) {
					readPair();
					while (code != 0 && code != 9) {
						switch (code) {
						case 10:
							prop.setProperty("$LIMMAX.X", val);
							break;
						case 20:
							prop.setProperty("$LIMMAX.Y", val);
							break;
						}
						readPair();
					}
				} else if ("$LTSCALE".equals(val)) {
					readPair();
					prop.setProperty("$LTSCALE", val);
				} else if ("$SPLFRAME".equals(val)) {
					readPair();
					prop.setProperty("$SPLFRAME", val);
				} else if ("$TILEMODE".equals(val)) {
					readPair();
					prop.setProperty("$TILEMODE", val);
				}
				break;
			}
		}		
	}

	void readVPort() throws Exception {
		Properties vport = new Properties();
		readPair();
		while (r.ready()) {
			if (code == 0) {
				vports.add(vport);
				return;
			}
			String propName = vportCodeName.get(code);
			if (propName != null) {
				vport.setProperty(propName, val);
			}
		}
	}
	
	void readVPorts() throws Exception {
		readPair();
		while (r.ready()) {
			if (code != 0)
				continue;
			if ("ENDTAB".equals(val)) {
				break;
			} else if ("VPORT".equals(val)) {
				readVPort();
			}
		}		
	}
	
	void readLType() throws Exception {
		Properties ltype = new Properties();
		readPair();
		while (r.ready()) {
			if (code == 0) {
				ltypes.add(ltype);
				return;
			}
			String propName = ltypeCodeName.get(code);
			if (propName != null) {
				ltype.setProperty(propName, val);
			}
		}
	}
	
	void readLTypes()  throws Exception {
		readPair();
		while (r.ready()) {
			if (code != 0)
				continue;
			if ("ENDTAB".equals(val)) {
				break;
			} else if ("LTYPE".equals(val)) {
				readLType();
			}
		}		
	}
	
	void readLayer() throws Exception {
		Properties layer = new Properties();
		readPair();
		while (r.ready()) {
			if (code == 0) {
				layers.add(layer);
				return;
			}
			String propName = layerCodeName.get(code);
			if (propName != null) {
				layer.setProperty(propName, val);
			}
		}
	}
	
	void readLayers() throws Exception {
		readPair();
		while (r.ready()) {
			if (code != 0)
				continue;
			if ("ENDTAB".equals(val)) {
				break;
			} else if ("LAYER".equals(val)) {
				readLayer();
			}
		}		
	}
	
	void readStyle() throws Exception {
		Properties style = new Properties();
		readPair();
		while (r.ready()) {
			if (code == 0) {
				styles.add(style);
				return;
			}
			String propName = styleCodeName.get(code);
			if (propName != null) {
				style.setProperty(propName, val);
			}
		}
	}
	
	void readStyles() throws Exception {
		readPair();
		while (r.ready()) {
			if (code != 0)
				continue;
			if ("ENDTAB".equals(val)) {
				break;
			} else if ("STYLE".equals(val)) {
				readStyle();
			}
		}		
	}
	
	void readTables() throws Exception {
		while (r.ready()) {
			readPair();
			if (code != 0)
				continue;
			if ("ENDSEC".equals(val)) {
				return;
			} else if ("TABLE".equals(val)) {
				if (code != 2)
					continue;
				if ("VPORT".equals(val)) {
					readVPorts();
				} else if ("LTYPE".equals(val)) {
					readLTypes();
				} else if ("LAYER".equals(val)) {
					readLayers();
				} else if ("STYLE".equals(val)) {
					readStyles();
				}
			}
		}
	}

	/*
	 3DFACE
	 ATTDEF
	 ATTRIB
	 ARC
	 BLOCK
	 CIRCLE
	 DIMENSION
	 ELLIPSE
	 ENDBLK
	 INSERT
	 	
	 */
	
	
	void readEntity() throws Exception {
		Map<Integer, String> entity = new HashMap<Integer, String>();
		readPair();
		while (r.ready()) {
			if (code == 0)
				break;
			entity.put(code, val);
		}
	}
	
	void readEntities() throws Exception {
		readPair();
		while (r.ready()) {
			if (code != 0)
				continue;
			if ("ENDSEC".equals(val)) {
				break;
			} else if ("3DFACE".equals(val)) {
				
			} else if ("ATTDEF".equals(val)) {
				
			} else if ("ATTRIB".equals(val)) {
				
			} else if ("ARC".equals(val)) {
				
			} else if ("BLOCK".equals(val)) {
				
			} else if ("CIRCLE".equals(val)) {
				
			} else if ("DIMENSION".equals(val)) {
				
			} else if ("ELLIPSE".equals(val)) {
				
			} else if ("ENDBLK".equals(val)) {
				
			} else if ("INSERT".equals(val)) {
				
			} else if ("LINE".equals(val)) {
				
			} else if ("LWPOLYLINE".equals(val)) {
				
			} else if ("MTEXT".equals(val)) {
				
			} else if ("POINT".equals(val)) {
				
			} else if ("POLYLINE".equals(val)) {
				
			} else if ("SEQEND".equals(val)) {
				
			} else if ("SOLID".equals(val)) {
				
			} else if ("TEXT".equals(val)) {
				
			} else if ("TRACE".equals(val)) {
				
			} else if ("VERTEX".equals(val)) {
				
			} else if ("VIEWPORT".equals(val)) {
				
			}
		}
	}
	
	void readBlocks() throws Exception {
		readPair();
	}
	
	void readSection() throws Exception {
		// YdxfGet
		readPair();
		if ("HEADER".equals(val)) {
			readHeader();
		} else if ("TABLES".equals(val)) {
			readTables();
		} else if ("BLOCKS".equals(val)) {
			readBlocks();
		} else if ("ENTITIES".equals(val)) {

		}
	}
	
	void readDxf() throws Exception {
		while (r.ready()) {
			readPair();
			if (code != 0)
				continue;
			if ("EOF".equals(val)) {
				break;
			} else if ("SECTION".equals(val)) {
				readSection();
			}
		}
	}
	
	void doIt() {
		
		
	}
	
	public static void main(String[] args) {
		new DxfRead().doIt();
	}
}
