package example.webapp.logback;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class JUL {
	static Logger log = Logger.getLogger(JUL.class.getName());

	public static void initialise() {
		LogManager.getLogManager(). reset();
		Logger.getGlobal().setLevel(Level.FINEST);
	}
	
	public static void doLog() {
		log.info   ("JUL: This is an info message");
		log.fine   ("JUL: This is a fine message");
		log.warning("JUL: This is a warning message");
		log.severe ("JUL: This is a severe message");
	}
}
