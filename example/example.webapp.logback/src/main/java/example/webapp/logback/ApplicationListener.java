package example.webapp.logback;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

@WebListener
public class ApplicationListener implements ServletContextListener {
	static Object synch = new Object();
	static ServletContext servletContext = null;
	static LoggerContext loggerContext = null;
	static boolean loggerInitialized = false;
	
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
				URL url = servletContext.getResource("/WEB-INF/logback.xml");
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(loggerContext);
				configurator.doConfigure(url);
				loggerInitialized = true;
			} catch (MalformedURLException | JoranException e) {
				e.printStackTrace();
			}
		}
	}
}
