package com.test.scripting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.slavi.util.Util;

public class TestRegExp {

	public static void dumpMatcher(Matcher m) {
		int count = m.groupCount();
		for (int i = 0; i <= count; i++) {
			System.out.println(" grp[" + i + "]=" + m.group(i));
		}
	}

	public static void main(String[] args) {
//		String regexp = "a+([DFG]|(QW))+z+";
//		String regexp = "\\b\\w+(?<!s)\\b";
		String regexp = "(?=(\\d+))[\\d\\w]+\\1";
//		String regexp = "\\b\\w+[^s]\\b";
//		String regexp = "\\$\\{([\\$[^(\\$\\{)(\\})]]+)\\}";
//		String regexp = "\\$\\{(([^}](.*\\$\\{.*){0}?)+)\\}";
		String vals[][] = {
				{ "123x12", "12" },
				{ "456x56", "56x56" },
//				{ "sssaaaQWzzzsss", "aaaQWzzz" },
//				{ "sssaaaQDWzzzsss", "" },
		};

		System.out.println("Pattern is: " + regexp);
		Pattern pattern = Pattern.compile(regexp);
		for (String pair[] : vals) {
			String val = pair[0];
			String expected = pair[1];

			String grp = "";
			Matcher m = pattern.matcher(val);
			if (m.find()) {
//				System.out.println(m);
				dumpMatcher(m);
				grp = Util.trimNZ(m.group());
			}
			if (grp.equals(expected)) {
				System.out.println("matched:   " + grp + " value: " + val);
			} else {
				System.out.println("UNMATCHED: " + grp + " value: " + val);
			}
		}
	}

	public static void main2(String[] args) {
		String regexp = "\\$\\{(([^}](?!\\$\\{))+)\\}";
//		String regexp = "\\$\\{([\\$[^(\\$\\{)(\\})]]+)\\}";
//		String regexp = "\\$\\{(([^}](.*\\$\\{.*){0}?)+)\\}";
		String vals[][] = {
				{ "aaa${qwe.zxc}zzz", "qwe.zxc" },
				{ "aaa${qwe$zxc}zzz", "qwe$zxc" },
				{ "aaa${qwe${zxc}}zzz", "zxc" },
				{ "aaa${qwe${z{xc}}zzz", "z{xc" },
				{ "aaa${qwe}${zxc}zzz", "qwe" },
		};

		System.out.println("Pattern is: " + regexp);
		Pattern pattern = Pattern.compile(regexp);
		for (String pair[] : vals) {
			String val = pair[0];
			String expected = pair[1];

			String grp = "";
			Matcher m = pattern.matcher(val);
			if (m.find()) {
//				System.out.println(m);
				dumpMatcher(m);
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
