package example.webapp.logback;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		SystemOut.initialise();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}
