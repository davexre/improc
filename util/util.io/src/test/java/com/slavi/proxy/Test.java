package com.slavi.proxy;

import java.lang.reflect.Proxy;
import java.util.Properties;

import org.junit.Assert;

import com.slavi.util.PropertyUtil;
import com.slavi.util.io.ObjectToPropertiesInputStream;
import com.slavi.util.io.ObjectToPropertiesOutputStream;

public class Test {
	
	static void dumpTestIF(TestIF t) {
		System.out.printf("t.hello(Duke): %s%n", t.hello("Duke"));
		System.out.printf("t.toString(): %s%n", t);
		System.out.printf("t.hashCode(): %H%n", t);
		System.out.printf("t.equals(t): %B%n", t.equals(t));
		System.out.printf("t.equals(new Object()): %B%n", t.equals(new Object()));
		System.out.printf("t.equals(null): %B%n", t.equals(null));
	}
	
	public static void main(String... args) throws Exception {
		TestIF t = (TestIF) Proxy.newProxyInstance(TestIF.class.getClassLoader(), new Class<?>[] { TestIF.class },
				new TestInvocationHandler(new TestImpl()));
		dumpTestIF(t);

		Properties properties = new Properties();
		ObjectToPropertiesOutputStream oos = new ObjectToPropertiesOutputStream(properties, "", true);
		oos.writeObject(t);
		oos.close();
		String s1 = PropertyUtil.propertiesToString(properties);
		
		System.out.println(s1);
		ObjectToPropertiesInputStream ois = new ObjectToPropertiesInputStream(properties, "");
		TestIF t2 = (TestIF) ois.readObject();
		ois.close();
		
		properties.clear();
		oos = new ObjectToPropertiesOutputStream(properties, "", true);
		oos.writeObject(t2);
		oos.close();
		String s2 = PropertyUtil.propertiesToString(properties);
		
		Assert.assertEquals("", s1, s2);
		dumpTestIF(t2);
	}
}
