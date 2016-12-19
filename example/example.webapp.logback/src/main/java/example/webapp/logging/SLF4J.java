package example.webapp.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4J {
	static Logger log = LoggerFactory.getLogger(SLF4J.class);
	
	public static void doLog() {
		log.debug("SLF4J: This is a debug message");
		log.error("SLF4J: This is a error message");
		log.info ("SLF4J: This is an info message");
	}
}
