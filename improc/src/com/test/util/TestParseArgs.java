package com.test.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.slavi.util.ParseArgs;

public class TestParseArgs {
	public static void main(String[] args) throws IOException {
//		String str = "\"qqq=\"asd qw\\eqwe\\\"zxc rty.fgh\" zzz";
//		String expr = "(\".*\")|(\\w\\w*)";
//		String expr = "(\".*(a{0}?).*\")|(\\w++)";
//		String expr = "(\"((\\\\\")|[^\"(\\\\\")])+\")|(\\w++)";

		BufferedReader fin = new BufferedReader(new InputStreamReader(
				ParseArgs.class.getResourceAsStream("TestParseArgs.txt")));
		String expr = fin.readLine();
		String str = fin.readLine();
		fin.close();
		
		System.out.println(str);
		System.out.println("-------");
		int i = 1;

		Pattern p = Pattern.compile(expr);
		Matcher m = p.matcher(str);
		while (m.find()) {
			System.out.printf("%5d|%s|\n", i++, str.substring(m.start(), m.end()));
		}

		
//		Pattern p = Pattern.compile(expr);
//		Matcher m = p.matcher(str);
//		int index = 0;
//		while (m.find()) {
//			System.out.println(str.substring(index, m.start()));
//			index = m.end();
//		}

//		String res[] = str.split(expr);
//		for (String s : res)
//			System.out.printf("%5d|%s|\n", i++, s);
		
		System.out.println("-------");
	}

}
