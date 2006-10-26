package com.slavi.example;

import java.util.Properties;

public class SystemPropertiesExample {

	public static void main(String[] args) {
		Properties prop = System.getProperties();
		prop.list(System.out);
	}
}
