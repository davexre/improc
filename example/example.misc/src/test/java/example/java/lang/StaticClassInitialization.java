package example.java.lang;

public class StaticClassInitialization {

	public static class ClassA {
		static {
			System.out.println("Class A static initialization.");
		}
	}
	
	public static class ClassB {
		public static final String someConstant = "ClassB.Constant";
		static {
			System.out.println("Class B static initialization.");
		}
		
		public static String getSomeString() {
			return "ClassB.getSomeString";
		}
	}
	
	public static class ClassC {
		public static ClassA classA = new ClassA();
		public static ClassB classB;
		
		static {
			System.out.println("Class C static initialization.");
		}
	}
	
	static {
		System.out.println("Static anonymous called.");
	}
	
	{
		System.out.println("Non-static anonymous called.");
	}
	
	public static void main(String[] args) {
		System.out.println("Main started.");
		StaticClassInitialization a = new StaticClassInitialization();
		StaticClassInitialization b = new StaticClassInitialization();
		ClassC classC = new ClassC();
		System.out.println(ClassB.class.getName());
		System.out.println(ClassB.someConstant);
		System.out.println(ClassB.getSomeString());
		System.out.println("Main finished.");
	}
}
