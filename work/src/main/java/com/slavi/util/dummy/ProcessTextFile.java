package com.slavi.util.dummy;

import java.io.InputStreamReader;
import java.io.LineNumberReader;

import com.slavi.util.CsvTokenizer;

public class ProcessTextFile {

	void doIt() throws Exception {
		CsvTokenizer t = new CsvTokenizer();
		try (
			LineNumberReader fin = new LineNumberReader(new InputStreamReader(getClass().getResourceAsStream("ProcessTextFile.txt")));
		) {
			String line, s;
			StringBuilder b = new StringBuilder();
			while ((line = fin.readLine()) != null) {
				t.setLine(line);
				b.setLength(0);
				while ((s = t.nextColumn()) != null) {
					if (
						t.getColumn() == 4 ||
						t.getColumn() == 13
					) {
						s = t.unquote(s);
					}
					b.append(s);
					b.append(",");
				}
				System.out.println(b.toString());
			}
		}
	}

	void doIt2() throws Exception {
		LineNumberReader r = new LineNumberReader(new InputStreamReader(getClass().getResourceAsStream("/com/slavi/derbi/northwind/data/Employees.csv")));
		r.readLine();
		CsvTokenizer t = new CsvTokenizer();
		//t.pattern = Pattern.compile("(('([^'])*')|[^,']*)*");
		String s = "aaa,'qqq''zzz',`53, qwe`,asd,sss\"sss\"sss,,ddd,\"fff\"),";
		t.setLine(s);
//		String s = "'a'),,a";
//		t.setLine(r.readLine());
//		System.out.println(s);
		while ((s = t.nextColumn()) != null) {
			System.out.println(t.unquote(s));
		}
	}

	public static void main(String[] args) throws Exception {
		new ProcessTextFile().doIt2();
		System.out.println("Done.");
	}
}
