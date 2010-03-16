package com.test.scripting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.slavi.util.Util;

public class TestRegExp {

	public static void main(String[] args) {
		String regexp = "\\$\\{([\\$[^(\\$\\{)(\\})]]+)\\}";
//		String regexp = "\\$\\{([\\{\\$[^\\}]&&([^(\\$\\{)]]+)\\}";
		String vals[][] = {
				{ "aaa${qwe.zxc}zzz", "qwe.zxc" },
				{ "aaa${qwe$zxc}zzz", "qwe$zxc" },
				{ "aaa${qwe${zxc}}zzz", "zxc" },
				{ "aaa${qwe${z$xc}}zzz", "z$xc" },
		};
		
		System.out.println("Pattern is: " + regexp);
		Pattern pattern = Pattern.compile(regexp);
		for (String pair[] : vals) {
			String val = pair[0];
			String expected = pair[1];
			
			String grp = "";
			Matcher m = pattern.matcher(val);
			if (m.find()) {
				grp = Util.trimNZ(m.group(1));
			}
			if (grp.equals(expected)) {
				System.out.println("matched:   " + grp + " value: " + val);
			} else {
				System.out.println("UNMATCHED: " + grp + " value: " + val);
			}
		}
	}
	
	public static void main1(String[] args) {
//		String regexp = "\\$\\{([\\$[^(\\$\\{)(\\})]]+)\\}";
		String regexp = "BC(((BC){0}+)+)D";
		String vals[][] = {
				{ "aaaBCqwe.zxcDzzz", "qwe.zxc" },
				{ "aaaBCqweBzxcDzzz", "qweBzxc" },
				{ "aaaBCqweBCzxcDDzzz", "zxc" },
				{ "aaaBCqweBCzCxcDDzzz", "zCxc" },
		};
		
		System.out.println("Pattern is: " + regexp);
		Pattern pattern = Pattern.compile(regexp);
		for (String pair[] : vals) {
			String val = pair[0];
			String expected = pair[1];
			
			String grp = "";
			Matcher m = pattern.matcher(val);
			if (m.find()) {
				grp = Util.trimNZ(m.group(1));
			}
			if (grp.equals(expected)) {
				System.out.println("matched:   " + grp + " value: " + val);
			} else {
				System.out.println("UNMATCHED: " + grp + " value: " + val);
			}
		}
	}
}
