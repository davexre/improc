package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VA_Message.java

public class VA_Message {

	public static boolean check_length(String s) {
		int i = s.indexOf('|');
		String s1 = s.substring(0, i);
		int j = Integer.parseInt(s1);
		return s.length() == j + i;
	}

	public static String get_message(String s) {
		int i = s.indexOf('|');
		String s1 = s.substring(i + 1, s.length());
		return s1;
	}

	public static String compose_message(String s) {
		String s1 = (s.length() + 1) + "|" + s;
		return s1;
	}

	public static String compose_cmd(String s, String s1) {
		String s2 = s + "|" + s1;
		return s2;
	}

	public static String get_cmd(String s) {
		int i = s.indexOf('|');
		String s1;
		if (i > 0)
			s1 = s.substring(0, i);
		else
			s1 = s;
		return s1;
	}

	public static String get_arg(String s) {
		int i = s.indexOf('|');
		String s1 = s.substring(i + 1, s.length());
		return s1;
	}

	public VA_Message() {
	}
}
