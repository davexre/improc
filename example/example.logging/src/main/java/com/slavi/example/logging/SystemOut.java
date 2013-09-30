package com.slavi.example.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.lidalia.sysoutslf4j.context.LogLevel;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

public class SystemOut {

	public static void main(String[] args) throws Exception {
		SysOutOverSLF4J.sendSystemOutAndErrToSLF4J(LogLevel.INFO, LogLevel.ERROR);
		String logName = SystemOut.class.getName();
		
		System.out.println("This comes from System.out");
		System.err.println("This comes from System.err");
		Logger log = LoggerFactory.getLogger(logName);
		log.debug("This is info from SLF4J.Logger.Debug");
	}
}
