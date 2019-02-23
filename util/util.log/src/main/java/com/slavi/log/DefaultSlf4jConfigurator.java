package com.slavi.log;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class DefaultSlf4jConfigurator extends BasicConfigurator {
	@Override
	public void configure(LoggerContext loggerContext) {
		super.configure(loggerContext);
		Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.setLevel(Level.INFO);
	}
}
