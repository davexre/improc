package example.webapp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import example.webapp.logging.JUL;
import example.webapp.logging.SystemOut;

@WebListener
public class ApplicationListener implements ServletContextListener {
/*
	// Tried log4j-jul but it is not working. Will use jul-to-slf4j instead.
	@PostConstruct
	private void initialize() {
		System.setProperty("java.util.logging.manager", org.apache.logging.log4j.jul.LogManager.class.getName());
	}
*/
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		JUL.initialise();
		SystemOut.initialise();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}
