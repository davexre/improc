package com.test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class TestURLEncoder {

	public static void main(String[] args) throws UnsupportedEncodingException {
		String s = "asd:QWE!@$#@%$#%^&&%*\"\"\\//:; това е на кирилица";
		String en = URLEncoder.encode(s, "UTF-8");
		String de = URLDecoder.decode(en, "UTF-8");
		System.out.println(s);
		System.out.println(de);
		System.out.println(en);
	}
}
