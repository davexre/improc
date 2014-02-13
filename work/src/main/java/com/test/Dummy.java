package com.test;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Dummy implements Serializable {

	void doIt() throws Exception {
		String str = "http://velobg.org/wp-admin/edit.php?post_type=page";
		System.out.println(URLEncoder.encode(str));
	}

	public static void main(String[] args) throws Exception {
		new Dummy().doIt();
		System.out.println("Done.");
	}
}
