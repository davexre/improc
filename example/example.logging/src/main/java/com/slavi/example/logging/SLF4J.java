package com.slavi.example.logging;

import static org.slf4j.event.EventConstants.DEBUG_INT;
import static org.slf4j.event.EventConstants.ERROR_INT;
import static org.slf4j.event.EventConstants.INFO_INT;
import static org.slf4j.event.EventConstants.TRACE_INT;
import static org.slf4j.event.EventConstants.WARN_INT;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.joran.spi.JoranException;

public class SLF4J {

	Logger log = LoggerFactory.getLogger(getClass());
	Logger log2 = LoggerFactory.getLogger("dummyLogger");

	static final Map<org.slf4j.event.Level, ch.qos.logback.classic.Level> levelMap = new HashMap<>();

	static {
		levelMap.put(org.slf4j.event.Level.ERROR, ch.qos.logback.classic.Level.ERROR);
		levelMap.put(org.slf4j.event.Level.WARN,  ch.qos.logback.classic.Level.WARN);
		levelMap.put(org.slf4j.event.Level.INFO,  ch.qos.logback.classic.Level.INFO);
		levelMap.put(org.slf4j.event.Level.DEBUG, ch.qos.logback.classic.Level.DEBUG);
		levelMap.put(org.slf4j.event.Level.TRACE, ch.qos.logback.classic.Level.TRACE);
	}

	public static void setLogLevel(Logger log, org.slf4j.event.Level level) {
		if (log instanceof ch.qos.logback.classic.Logger) {
			((ch.qos.logback.classic.Logger) log).setLevel(
				levelMap.getOrDefault(level, ch.qos.logback.classic.Level.OFF));
		}
	}

	void doSomething() throws JoranException {
		log.debug("Logger 1 message 1");
		log2.info("Logger 2 message 1");
		LoggerContext lc = ContextSelectorStaticBinder.getSingleton().getContextSelector().getDefaultLoggerContext();
		lc.reset();
		new ContextInitializer(lc).configureByResource(SLF4J.class.getResource("/logback_alternative.xml"));

		log.debug("Logger 1 message 2");
		log2.info("Logger 2 message 2");
		setLogLevel(log, null);
		log.debug("Logger 1 message 3");
		setLogLevel(log, org.slf4j.event.Level.TRACE);
		log.debug("Logger 1 message 4");

	}

	public static void main(String[] args) throws Exception {
		new SLF4J().doSomething();
	}
}
