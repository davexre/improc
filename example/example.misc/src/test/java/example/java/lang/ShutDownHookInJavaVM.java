package example.java.lang;

public class ShutDownHookInJavaVM {
	public static void main(String[] args) {
		System.out.println("Starting main program");
		Runtime rt = Runtime.getRuntime();
		System.out.println("Main: adding shutdown hook");
		rt.addShutdownHook(new Thread() {
			public void run() {
				// In real life this might close a Connection or something.
				System.out.println("Running my shutdown hook");
			}
		});
		System.out.println("Main: calling Runtime.exit()");
		System.exit(0);
	}
}
