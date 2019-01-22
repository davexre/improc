package com.slavi.lang;

public class TestEnumValueOf {

	static enum MyEnum {
		Abc, Bcd, Cde;
	}

	public static void main(String[] args) {
		System.out.println(MyEnum.valueOf("qqqq"));
	}
}
