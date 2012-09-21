package com.slavi.io;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Utils {
	public static final Map<String, Class> primitiveClasses;
	
	private static void addPrimitiveClass(Class clazz) {
		primitiveClasses.put(clazz.getName(), clazz);
	}
	
	static {
		primitiveClasses = new HashMap<String, Class>();
		addPrimitiveClass(boolean.class);
		addPrimitiveClass(Boolean.class);
		addPrimitiveClass(byte.class);
		addPrimitiveClass(Byte.class);
		addPrimitiveClass(char.class);
		addPrimitiveClass(Character.class);
		addPrimitiveClass(double.class);
		addPrimitiveClass(Double.class);
		addPrimitiveClass(float.class);
		addPrimitiveClass(Float.class);
		addPrimitiveClass(int.class);
		addPrimitiveClass(Integer.class);
		addPrimitiveClass(long.class);
		addPrimitiveClass(Long.class);
		addPrimitiveClass(short.class);
		addPrimitiveClass(Short.class);
		addPrimitiveClass(String.class);
		addPrimitiveClass(Enum.class);
	}
	
	public static boolean hasPropertiesStartingWith(Properties properties, String prefix) {
		prefix = getChildPrefix(prefix, "");
		for (Object key : properties.keySet()) {
			if (((String) key).startsWith(prefix))
				return true;
		}
		return false;
	}
	
	public static String getChildPrefix(String parentPrefix, String childName) {
		if ("".equals(parentPrefix))
			return childName;
		if (parentPrefix.endsWith("."))
			return parentPrefix + childName;
		return parentPrefix + "." + childName;
	}
}
