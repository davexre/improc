package com.test;

import java.io.Serializable;

import com.slavi.util.Util;

public class Dummy implements Serializable {
	
	String str;
	
	class Kuku implements Serializable {
		String strKuku;
		
		public String getStr() {
			return str;
		}
	}
	
	public static void main(String[] args) throws Exception {
		Dummy dummy1 = new Dummy();
		dummy1.str = "dummy1";
		Kuku kuku1 = dummy1.new Kuku();
		kuku1.strKuku = "kuku1";
		
		Kuku kuku2 = Util.deepCopy(kuku1);
		System.out.println(kuku2.strKuku);
		System.out.println(kuku1 == kuku2);
		dummy1.str = "changed1";
		System.out.println(kuku2.getStr());
	}
}
