package com.test;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Locale;

public class Dummy implements Serializable {

	void doIt() throws Exception {
		String str = "http://velobg.org/wp-admin/edit.php?post_type=page";
		System.out.println(URLEncoder.encode(str));
	}

	public static void main(String[] args) throws Exception {
//		new Dummy().doIt();
		System.out.println(System.getProperty( "os.arch" ).toLowerCase( Locale.US ));
		System.out.println("Done.");
	}
}
