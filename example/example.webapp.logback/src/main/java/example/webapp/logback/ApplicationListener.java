package example.webapp.logback;

import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

@WebListener
public class ApplicationListener implements ServletContextListener {
	static final Logger log = LoggerFactory.getLogger(ApplicationListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			ServletContext servletContext = sce.getServletContext();
			String tempDir = String.valueOf(servletContext.getAttribute(ServletContext.TEMPDIR));
			URL url = servletContext.getResource("/WEB-INF/logback.xml");
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			loggerContext.reset();
			loggerContext.putProperty("tempDir", tempDir);

			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			configurator.doConfigure(url);
			log.info("Log output dir is " + tempDir);
		} catch (Exception e) {
			log.error("Error configuring logback", e);
		}
		JUL.initialise();
		SystemOut.initialise();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}
