package com.slavi.dxf;

import java.io.BufferedReader;

public class DxfRead {

	BufferedReader r;
	
	int code;
	String val;
	
	void readPair() throws Exception {
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
					
				}
				break;
			}
		}		
	}

	void readSection() throws Exception {
		// YdxfGet
		readPair();
		if ("HEADER".equals(val)) {
			readHeader();
		} else if ("TABLES".equals(val)) {
			
		} else if ("BLOCKS".equals(val)) {
			
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
