package com.test;

public class EnumTest {

	public enum MyEnum {
		ONE(1), TWO(2), THREE(3);
		
		public int v;
		MyEnum(int i) {
			v = i;
		}
	}
	
	public static void main(String[] args) {
		for (MyEnum i : MyEnum.values()) {
			i.v++;
			System.out.println(i + " " + i.v);
		}
	}
}
