package example.webapp.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.spi.ContextAwareBase;

public class LogbackConfigurator extends ContextAwareBase implements Configurator {
	@Override
	public void configure(LoggerContext loggerContext) {
		ApplicationListener.setLoggerContext(loggerContext);
	}
}
