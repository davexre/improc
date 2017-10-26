package com.test.ui;

import com.slavi.util.swt.SwtUtil;

public class TestInputBox {
	public static void main(String[] args) {
		System.out.println(SwtUtil.inputBox(null, "Enter a string", "Title", "default value"));
	}
}
