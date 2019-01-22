package com.slavi.jaxb;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

import com.slavi.tools.dtd.parser.DTDParser;
import com.slavi.tools.test.parser.DTDEntityParser;

public class MyDtdParser {


	public void doIt(String[] args) throws Exception {
		String fname = "/home/spetrov/git/ibetms/docs/misc/IxultRequest.dtd";

		String fin = FileUtils.readFileToString(new File(fname), Charset.forName("UTF8"));
		DTDEntityParser p1 = new DTDEntityParser(new StringReader(fin));
		p1.parse();
		System.out.println(p1.entities);
/*
		try (InputStream is = new FileInputStream(fname)) {
			DTDParser parser = new DTDParser(new StringReader(fin));
			parser.dtd();
			System.out.println(parser.entities);
		}
*/	}

	public static void main(String[] args) throws Exception {
		new MyDtdParser().doIt(args);
		System.out.println("Done.");
	}
}
