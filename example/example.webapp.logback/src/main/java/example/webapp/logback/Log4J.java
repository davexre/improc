package example.webapp.logback;

import org.apache.log4j.Logger;

public class Log4J {
	static Logger log = Logger.getLogger(Log4J.class);
	
	public static void doLog() {
		log.debug("Log4J: This is a debug message");
		log.error("Log4J: This is a error message");
		log.info ("Log4J: This is an info message");
	}
}
