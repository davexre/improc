package com.slavi.example.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4J {

	Logger log = LoggerFactory.getLogger(getClass());
	Logger log2 = LoggerFactory.getLogger("dummyLogger");
	
	void doSomething() {
		log.debug("This is a debug message");
		log2.info("This is a Dummy debug message");
	}
	
	public static void main(String[] args) {
		new SLF4J().doSomething();
	}
}
