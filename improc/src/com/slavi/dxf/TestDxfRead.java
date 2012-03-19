package com.slavi.dxf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

public class TestDxfRead {

	static class InternalTestDxfRead extends DxfRead {
		public InternalTestDxfRead(InputStream in) {
			super(in);
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

		public void err(int lineNumber, String desc) {
			System.err.println(lineNumber + ": " + code + "/" + val + ": " + desc);
		}
	}
	
	public static void main(String[] args) throws Exception {
		String dxfRoot = "/home/slavian/java/workspace4test/ycad/dat";
		DxfRead dxfRead = new InternalTestDxfRead(new FileInputStream(new File(dxfRoot, "hexhouse.dxf")));
		dxfRead.readDxf();
//		ArrayList<String> lines = PropertyUtil.propertiesToSortedStringList(dxfRead.getHeader());
//		for (String line : lines) {
//			System.out.println(line);
//		}
	}
}
