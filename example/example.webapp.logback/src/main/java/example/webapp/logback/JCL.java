package example.webapp.logback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JCL {
	static Log log = LogFactory.getLog(JCL.class);

	public static void doLog() {
		log.debug("JCL: This is a debug message");
		log.error("JCL: This is a error message");
		log.info ("JCL: This is an info message");
	}
}
