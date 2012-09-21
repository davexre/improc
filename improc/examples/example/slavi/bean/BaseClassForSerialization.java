package example.slavi.bean;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import sun.reflect.ReflectionFactory;

import com.slavi.util.Util;

public class BaseClassForSerialization implements Serializable {
	private static final AtomicInteger counter = new AtomicInteger();
	
	public static interface Dumpable extends Serializable {
		public void dump(String prefix);
	}

	public static interface NestedDumpable extends Serializable {
		public void nestedDump(String prefix);
	}

	public static class Class1 implements Dumpable {
		public String stringProperty;
		
		public Class1() {
			stringProperty = "Class1 " + Integer.toString(counter.getAndIncrement());
			System.out.println("new Class1() " + stringProperty);
		}
		
		public void dump(String prefix) {
			System.out.println(prefix + "Class1.dump() " + stringProperty);
		}
		
		public class Class1Inner implements NestedDumpable {
			public int intProperty;
			
			public Class1Inner() {
				intProperty = counter.getAndIncrement();
				System.out.println("new Class1Inner() " + intProperty);
			}
			
			public void nestedDump(String prefix) {
				System.out.println(prefix + "Class1Inner.nestedDump() " + intProperty);
				dump(prefix + "Class1Inner.");
			}
		}

		public static class Class1StaticInner implements Dumpable{
			public String stringProperty;

			public Class1StaticInner() {
				stringProperty = "Class1StaticInner " + Integer.toString(counter.getAndIncrement());
				System.out.println("new Class1StaticInner() " + stringProperty);
			}
			
			public void dump(String prefix) {
				System.out.println(prefix + "Class1StaticInner.dump() " + stringProperty);
			}
		}
		
		public class Class1InnerClassExtended extends Class1Inner {
			private int privateInt;
			
			public Class1InnerClassExtended() {
				privateInt = counter.getAndIncrement();
				System.out.println("new Class1InnerClassExtended() " + privateInt);
			}
			
			public void nestedDump(String prefix) {
				System.out.println(prefix + "Class1InnerClassExtended.nestedDump() " + privateInt);
				dump(prefix + "Class1InnerClassExtended.");
			}

			public class Class1InnerClassExtendedInner implements Dumpable {
				protected int protectedInt;
				
				public Class1InnerClassExtendedInner() {
					protectedInt = counter.getAndIncrement();
					System.out.println("new Class1InnerClassExtendedInner() " + protectedInt);
				}
				
				public void dump(String prefix) {
					System.out.println(prefix + "Class1InnerClassExtendedInner.dump() " + protectedInt);
					nestedDump(prefix + "Class1Inner.");
				}
			}
		}
	}
	
	public static class StaticClassExtendsNonStaticInner extends Class1.Class1Inner {
		public final int finalInt;
		
		public StaticClassExtendsNonStaticInner(Class1 class1) {
			class1.super();
			finalInt = counter.getAndIncrement();
			System.out.println("new StaticClassExtendsNonStaticInner() " + finalInt);
		}
		
		public void nestedDump(String prefix) {
			System.out.println(prefix + "StaticClassExtendsNonStaticInner.nestedDump() " + finalInt);
			super.nestedDump(prefix + "StaticClassExtendsNonStaticInner.");
		}
	}

	public class Class2 implements Dumpable {
		public String stringProperty = "Class2 DEFAULT VALUE";
		
		public Class2() {
			stringProperty = "Class2 " + Integer.toString(counter.getAndIncrement());
			System.out.println("new Class2() " + stringProperty);
		}
		
		public void dump(String prefix) {
			System.out.println(prefix + "Class2.dump() " + stringProperty);
		}

		public class Class2InnerExtendsNonStatic extends Class1.Class1Inner {
			private final int finalInt;
			
			public Class2InnerExtendsNonStatic(Class1 class1) {
				class1.super();
				finalInt = counter.getAndIncrement();
				System.out.println("new Class2InnerExtendsNonStatic() " + finalInt);
			}
			
			public void nestedDump(String prefix) {
				System.out.println(prefix + "Class2InnerExtendsNonStatic.nestedDump() " + finalInt);
				super.nestedDump(prefix + "Class2InnerExtendsNonStatic.");
			}
		}
	}
	
	static final Class1 staticClass1 = new Class1(); 
	
	public static class StaticClassExtendsNonStaticWithConstructorHack extends Class1.Class1Inner {
		public final String finalStringProperty;
		
		public StaticClassExtendsNonStaticWithConstructorHack(String value) {
			staticClass1.super();
			finalStringProperty = value;
			System.out.println("new StaticClassExtendsNonStaticWithConstructorHack() " + finalStringProperty);
		}

		public void nestedDump(String prefix) {
			System.out.println(prefix + "StaticClassExtendsNonStaticWithConstructorHack.nestedDump() " + finalStringProperty);
			super.nestedDump(prefix + "StaticClassExtendsNonStaticWithConstructorHack.");
		}
	}

	public ArrayList<Serializable> getTestObjects() {
		ArrayList<Serializable> r = new ArrayList<Serializable>();
		System.out.println("*** Creating object 0");
		final Class1 class1 = new Class1();
		System.out.println("*** Creating object 1");
		Class1.Class1Inner class1Inner = class1.new Class1Inner();
		System.out.println("*** Creating object 2");
		Class1.Class1StaticInner class1StaticInner = new Class1.Class1StaticInner();
		System.out.println("*** Creating object 3");
		Class1.Class1InnerClassExtended class1InnerClassExtended = class1.new Class1InnerClassExtended();
		System.out.println("*** Creating object 4");
		Class1.Class1InnerClassExtended.Class1InnerClassExtendedInner class1InnerClassExtendedInner = class1InnerClassExtended.new Class1InnerClassExtendedInner();
		System.out.println("*** Creating object 5");
		StaticClassExtendsNonStaticInner staticClassExtendsNonStaticInner = new StaticClassExtendsNonStaticInner(class1);
		System.out.println("*** Creating object 6");
		final Class2 class2 = new Class2();
		System.out.println("*** Creating object 7");
		Class2.Class2InnerExtendsNonStatic class2InnerExtendsNonStatic = class2.new Class2InnerExtendsNonStatic(class1);
		System.out.println("*** Creating object 8");
		StaticClassExtendsNonStaticWithConstructorHack staticClassExtendsNonStaticWithConstructorHack = new StaticClassExtendsNonStaticWithConstructorHack("final dummy string");
		System.out.println("*** Creating object 9");
		Class1 anonymous1 = new Class1() {
			public void dump(String prefix) {
				System.out.println(prefix + "anonymous.dump()");
				super.dump(prefix + "anonymous.");
			}
		};

		r.add(class1);
		r.add(class1Inner);
		r.add(class1StaticInner);
		r.add(class1InnerClassExtended);
		r.add(class1InnerClassExtendedInner);
		r.add(staticClassExtendsNonStaticInner);
		r.add(class2);
		r.add(staticClassExtendsNonStaticWithConstructorHack);
		r.add(anonymous1);
		r.add(class2InnerExtendsNonStatic);
		return r;
	}
	
	
	
	
	
	static void dumpObjectSyntheticFields(String prefix, Object o) throws Exception {
		Class c = o.getClass();
		while (c != null) {
			for (Field f : c.getDeclaredFields()) {
				if (!f.isSynthetic())
					continue;
				System.out.println(prefix + "." + f.getName() + " -> " + f.getType());
				f.setAccessible(true);
				Object value = f.get(o);
				dumpObjectSyntheticFields(prefix + "." + f.getName(), value);
			}
			prefix += ".super";
			c = c.getSuperclass();
		}
	}
	
	static Serializable dumpObjectClassData(Serializable o) throws Exception {
		o = Util.deepCopy(o);
		Class c = o.getClass();
		System.out.println("=======");
		System.out.println("CLASS " + c);
		System.out.println("isStatic " + Modifier.isStatic(c.getModifiers()));
		
		System.out.println("ENCLOSING " + c.getEnclosingClass());
		System.out.println("DECLARING " + c.getDeclaringClass());
		System.out.println("SUPER     " + c.getSuperclass());
//		System.out.println("ENC ctr " + c.getEnclosingConstructor());
		
		for (Class i : c.getClasses()) {
			System.out.println("classes " + i);
		}
		
		for (Class i : c.getDeclaredClasses()) {
			System.out.println("declared class " + i);
		}
		
		dumpObjectSyntheticFields("this", o);

		Class tmpc = c;
		String pref = "this.";
		while (tmpc != null) {
			for (Constructor ctr : c.getConstructors()) {
				System.out.println(pref + ctr);
			}
			pref += "this.";
			tmpc = tmpc.getSuperclass();
		}
		
//		System.out.println(c.isMemberClass());
//		System.out.println(c.isLocalClass());
//		System.out.println(c.isAnonymousClass());
		
		return o;
	}
	
	public static void dumpObjects(ArrayList<Serializable> objects) {
		for (int i = 0; i < objects.size(); i++) {
			Serializable o = objects.get(i);
			System.out.println("*** Dumping Object " + Integer.toString(i));
			if (o == null)
				System.out.println("NULL OBJECT");
			if (o instanceof Dumpable)
				((Dumpable) o).dump("Dumpable " + Integer.toString(i) + ".");
			if (o instanceof NestedDumpable)
				((NestedDumpable) o).nestedDump("NestedDumpable " + Integer.toString(i) + ".");
			if ((o != null) &&
				(!(o instanceof Dumpable)) && 
				(!(o instanceof NestedDumpable))) {
				System.out.println("??? BUG " + o.getClass());
			}
		}
	}
	
	void doIt() throws Exception {
		staticClass1.stringProperty = "final StaticClass1 string property value";
		ArrayList<Serializable> objects = getTestObjects();
		ArrayList<Serializable> objects2 = new ArrayList<Serializable>();
		for (int i = 0; i < objects.size(); i++) {
			Serializable o = objects.get(i);
			System.out.println("*** Dumping Object " + Integer.toString(i));
			objects2.add(dumpObjectClassData(o));
		}
		staticClass1.stringProperty = "final StaticClass1 VALUE MODIFIED";
		dumpObjects(objects2);
	}
	
	public static void main(String[] args) throws Exception {
		new BaseClassForSerialization().doIt();
//		ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
//		Constructor objDef = Object.class.getDeclaredConstructor();
//		Class<StaticClassExtendsNonStaticWithConstructorHack> clazz = StaticClassExtendsNonStaticWithConstructorHack.class;
//		Constructor intConstr = rf.newConstructorForSerialization(clazz, objDef);
//		StaticClassExtendsNonStaticWithConstructorHack cl7 = clazz.cast(intConstr.newInstance());
//		System.out.println(cl7.getStringProperty());
	}
}
