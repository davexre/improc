package com.slavi.example.logging;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;

public class JUL {

	public static void main(String[] args) {
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
		Logger.getLogger("global").setLevel(Level.FINEST);
		
		String logName = JUL.class.getName();
		Logger log = Logger.getLogger(logName);
		log.info("This is info from JUL.Logger.Info");
	}
}
