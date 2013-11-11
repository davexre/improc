package com.slavi.example.logging;

import org.apache.log4j.Logger;

public class Log4J {

	Logger log = Logger.getLogger(getClass());
	
	void doSomething() {
		log.debug("This is a debug message");
		log.error("This is a error message");
		log.info ("This is an info message");
	}
	
	public static void main(String[] args) {
		new Log4J().doSomething();
	}
}
