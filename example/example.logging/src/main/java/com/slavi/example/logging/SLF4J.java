package com.slavi.example.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4J {

	Logger log = LoggerFactory.getLogger(getClass());
	
	void doSomething() {
		log.debug("This is a debug message");
	}
	
	public static void main(String[] args) {
		new SLF4J().doSomething();
	}
}
