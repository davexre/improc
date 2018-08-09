package com.slavi.example.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.joran.spi.JoranException;

public class SLF4J {

	Logger log = LoggerFactory.getLogger(getClass());
	Logger log2 = LoggerFactory.getLogger("dummyLogger");

	void doSomething() throws JoranException {
		log.debug("Logger 1 message 1");
		log2.info("Logger 2 message 1");
		LoggerContext lc = ContextSelectorStaticBinder.getSingleton().getContextSelector().getDefaultLoggerContext();
		lc.reset();
		new ContextInitializer(lc).configureByResource(SLF4J.class.getResource("/logback_alternative.xml"));

		log.debug("Logger 1 message 2");
		log2.info("Logger 2 message 2");
	}

	public static void main(String[] args) throws Exception {
		new SLF4J().doSomething();
	}
}
