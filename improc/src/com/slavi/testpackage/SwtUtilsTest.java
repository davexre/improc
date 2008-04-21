package com.slavi.testpackage;

import com.slavi.utils.SwtUtl;

public class SwtUtilsTest {

	void openFileTest() {
		System.out.println(SwtUtl.openFile(null, "Some title", null, null));
		System.out.println(SwtUtl.openFile(null, "Some title", null, null));
		System.out.println(SwtUtl.openFile(null, "Some title", null, null));
	}
	
	public static void main(String[] args) {
		SwtUtilsTest test = new SwtUtilsTest();
		test.openFileTest();
	}
}
