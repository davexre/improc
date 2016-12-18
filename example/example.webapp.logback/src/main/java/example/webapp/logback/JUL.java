package example.webapp.logback;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;

public class JUL {
	public static void initialise() {
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
		Logger.getGlobal().setLevel(Level.FINEST);
	}

	static Logger log = Logger.getLogger(JUL.class.getName());
	
	public static void doLog() {
		log.info   ("JUL: This is an info message");
		log.fine   ("JUL: This is a fine message");
		log.warning("JUL: This is a warning message");
		log.severe ("JUL: This is a severe message");
	}
}
