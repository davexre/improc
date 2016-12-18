package example.webapp.logback;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

@WebListener
public class ApplicationListener implements ServletContextListener {
	static Object synch = new Object();
	static ServletContext servletContext = null;
	static LoggerContext loggerContext = null;
	static boolean loggerInitialized = false;
	
	static final Logger log = LoggerFactory.getLogger(ApplicationListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		JUL.initialise();
		SystemOut.initialise();
		synchronized (synch) {
			servletContext = sce.getServletContext();
			updateLogger();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		synchronized (synch) {
			servletContext = null;
		}
	}
	
	public static ServletContext getServletContext() {
		synchronized (synch) {
			return servletContext;
		}
	}

	public static void setLoggerContext(LoggerContext loggerContext) {
		synchronized (synch) {
			ApplicationListener.loggerContext = loggerContext;
			updateLogger();
		}
	}
	
	private static void updateLogger() {
		synchronized (synch) {
			if (loggerInitialized || servletContext == null || loggerContext == null) {
				return;
			}
			try {
				String tempDir = String.valueOf(servletContext.getAttribute(ServletContext.TEMPDIR));
				URL url = servletContext.getResource("/WEB-INF/logback.xml");
				JoranConfigurator configurator = new JoranConfigurator();
				loggerContext.putProperty("tempDir", tempDir);
				configurator.setContext(loggerContext);
				configurator.doConfigure(url);
				loggerInitialized = true;
				log.info("Log output dir is " + tempDir);
			} catch (MalformedURLException | JoranException e) {
				e.printStackTrace();
			}
		}
	}
}
