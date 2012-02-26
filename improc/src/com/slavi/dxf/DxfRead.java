package com.slavi.dxf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.slavi.util.PropertyUtil;

public class DxfRead {

	LineNumberReader r;
	
	boolean hasPushedPair = false;
	int code;
	String val;
	
	Properties header = new Properties();

	public DxfRead(InputStream in) {
		r = new LineNumberReader(new InputStreamReader(in));
	}
	
	public void blockRead(Map<Integer, String> block, ArrayList<Map<Integer, String>> blockEntities, Map<Integer, String> blockData) {
		System.out.println("Block :" + block.get(2) + " blockData:" + blockData.size());
	}

	public void entityRead(Map<Integer, String> entity) {
		System.out.println("Entity:" + entity.get(0));
	}

	public void tableRead(Map<Integer, String> tableDesc, ArrayList<Map<Integer, String>> table) {
		System.out.println("Table :" + tableDesc.get(2));
	}

	public void err(String desc) {
		System.out.println(r.getLineNumber() + ": " + code + "/" + val + ": " + desc);
	}

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
			code = Integer.parseInt(r.readLine().trim());
			val = r.readLine().trim();
		} else {
			code = 0;
			val = null;
		}
	}

	void readHeader() throws Exception {
		String lastName = "";
		while (r.ready()) {
			readPair();
			if (code == 0) {
				if ("ENDSEC".equals(val)) {
					return;
				}
				err("ENDSEC expected reading header");
				continue;
			} else if (code == 9) {
				lastName = val;
			} else {
				header.setProperty(lastName + "." + Integer.toString(code), val);
			}
		}
	}
	
	Map<Integer, String> readCodeValueMap() throws Exception {
		Map<Integer, String> result = new HashMap<Integer, String>();
		while (r.ready()) {
			readPair();
			if (code == 0) {
				pushPair();
				break;
			}
			String val1 = result.get(code);
			if (val1 == null)
				val1 = val;
			else 
				val1 = val1 + ":" + val;
			result.put(code, val1);
		}
		return result;
	}
	
	ArrayList<Map<Integer, String>> readTable() throws Exception {
		ArrayList<Map<Integer, String>> result = new ArrayList<Map<Integer,String>>();
		while (r.ready()) {
			readPair();
			if (code != 0)
				continue;
			if ("ENDTAB".equals(val)) {
				break;
			} else {
				String tableType = val;
				Map<Integer, String> tableItem = readCodeValueMap();
				tableItem.put(0, tableType);
				result.add(tableItem);
			}
		}		
		return result;
	}

	void readTables() throws Exception {
		while (r.ready()) {
			readPair();
			if (code != 0) {
				err("Code 0 expected reading tables");
				continue;
			}
			if ("ENDSEC".equals(val)) {
				return;
			} else if ("TABLE".equals(val)) {
				Map<Integer, String> tableDesc = readCodeValueMap();
				ArrayList<Map<Integer, String>> table = readTable();
				tableRead(tableDesc, table);
			} else {
				err("ENDSEC or TABLE expected");
			}
		}
	}

	Map<Integer, String> readEntity() throws Exception {
		String entityType = val;
		Map<Integer, String> result = readCodeValueMap();
		result.put(0, entityType);
		return result;
	}

	void readEntities() throws Exception {
		while (r.ready()) {
			readPair();
			if (code != 0) {
				err("Code 0 expected reading entities");
				continue;
			}
			if ("ENDSEC".equals(val)) {
				break;
			} else {
				Map<Integer, String> entity = readEntity();
				entityRead(entity);
			}
		}
	}
	
	void readBlocks() throws Exception {
		Map<Integer, String> block = null;
		ArrayList<Map<Integer, String>> blockEntities = null;
		while (r.ready()) {
			readPair();
			if (code != 0) {
				err("ENDSEC/BLOCK/ENDBLK with code 0 expected");
				continue;
			}
			if ("ENDSEC".equals(val)) {
				break;
			} else if ("BLOCK".equals(val)) {
				block = readCodeValueMap();
				blockEntities = new ArrayList<Map<Integer, String>>();
			} else if ("ENDBLK".equals(val)) {
				Map<Integer, String> blockData = readCodeValueMap();
				blockRead(block, blockEntities, blockData);
				block = null;
				blockEntities = null;
			} else {
				if (blockEntities != null)
					blockEntities.add(readEntity());
			}
		}
	}

	void readSection() throws Exception {
		readPair();
		if ("HEADER".equals(val)) {
			readHeader();
		} else if ("TABLES".equals(val)) {
			readTables();
		} else if ("BLOCKS".equals(val)) {
			readBlocks();
		} else if ("ENTITIES".equals(val)) {
			readEntities();
		} else {
			err("Invalid section");
			while (r.ready()) {
				readPair();
				if (code != 0) {
					continue;
				}
				if ("ENDSEC".equals(val)) {
					break;
				}
			}
		}
	}
	
	public void readDxf() throws Exception {
		while (r.ready()) {
			readPair();
			if (code != 0) {
				err("SECTION or EOF with code 0 expected");
				continue;
			}
			if ("EOF".equals(val)) {
				break;
			} else if ("SECTION".equals(val)) {
				readSection();
			} else {
				err("SECTION or EOF expected");
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		String dxfRoot = "/home/slavian/S/java/workspace/ycad/dat/";
		DxfRead dxfRead = new DxfRead(new FileInputStream(new File(dxfRoot, "hexhouse.dxf")));
		ArrayList<String> lines = PropertyUtil.propertiesToSortedStringList(dxfRead.header);
		for (String line : lines) {
			System.out.println(line);
		}
	}
}
