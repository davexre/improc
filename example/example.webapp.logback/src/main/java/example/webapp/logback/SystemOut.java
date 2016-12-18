package example.webapp.logback;

import uk.org.lidalia.sysoutslf4j.context.LogLevel;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

public class SystemOut {
	public static void initialise() {
		SysOutOverSLF4J.sendSystemOutAndErrToSLF4J(LogLevel.INFO, LogLevel.ERROR);
	}
	
	public static void doLog() {
		System.out.println("This comes from System.out");
		System.err.println("This comes from System.err");
	}
}
