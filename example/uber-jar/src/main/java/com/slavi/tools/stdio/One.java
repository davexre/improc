package com.slavi.tools.stdio;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class One {
	static Logger log = LoggerFactory.getLogger(com.slavi.tools.stdio.One.class);
	
	public static void main(String[] args) throws Exception {
		try (
				LineNumberReader bi = new LineNumberReader(new InputStreamReader(new BufferedInputStream(System.in)));
			) {
			while (true) {
				String s = bi.readLine();
				if (s == null)
					break;
				System.out.println(bi.getLineNumber() + ": " + s.toUpperCase());
			}
		}
	}
}
