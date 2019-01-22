package com.slavi.lang;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slavi.dtd.entity.parser.EntityParser;
import com.slavi.util.Util;
import com.slavi.util.io.CommentAwareLineNumberReader;
import com.slavi.util.io.LineReader;

public class TestEntityParserMain {

	void doIt() throws Exception {
		try (Reader is = new FileReader("/home/spetrov/git/ibetms/bin/docs/misc/RemoteControlResponse.dtd")) {
//		try (Reader is = new InputStreamReader(getClass().getResourceAsStream("TestParserMain.dtd"))) {
			EntityParser p = new EntityParser(is);
			p.parse();
			ObjectMapper m = Util.jsonMapper();
			System.out.println(m.writeValueAsString(p.entities));
		}
	}

	void doIt1() throws Exception {
		ArrayList<String> err = new ArrayList();
		try (LineReader lr = new CommentAwareLineNumberReader(new InputStreamReader(getClass().getResourceAsStream("TestParserMain files list.txt")))) {
			String l;
			while ((l = lr.readLine()) != null) {
				System.out.println("\n---------- " + l);
				try (Reader is = new FileReader(l)) {
					EntityParser p = new EntityParser(is);
					p.parse();
					ObjectMapper m = Util.jsonMapper();
					System.out.println(m.writeValueAsString(p.entities));
//				} catch (Exception e) {
//					err.add(l);
				}
			}
		}
		for (String i : err)
			System.out.println(i);
	}

	public static void main(String[] args) throws Exception {
		new TestEntityParserMain().doIt();
		System.out.println("Done.");
	}
}
