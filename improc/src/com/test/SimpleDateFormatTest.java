package com.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateFormatTest {

	public static void main(String[] args) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH/mm/ss");
		String s = df.format(System.currentTimeMillis());
		Date d = df.parse(s);
		String s2 = df.format(d);
		System.out.println(s);
		System.out.println(s2);
	}
}
