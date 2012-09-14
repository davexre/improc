package com.slavi.io;

import java.beans.IntrospectionException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ObjectToProperties {
	static Map<String, Class> primitiveClasses;
	
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
	
	private static boolean hasPropertiesStartingWith(Properties properties, String prefix) {
		prefix = getChildPrefix(prefix, "");
		for (Object key : properties.keySet()) {
			if (((String) key).startsWith(prefix))
				return true;
		}
		return false;
	}
	
	private static String getChildPrefix(String parentPrefix, String childName) {
		if ("".equals(parentPrefix))
			return childName;
		if (parentPrefix.endsWith("."))
			return parentPrefix + childName;
		return parentPrefix + "." + childName;
	}

	public static Object propertiesToObject(Properties properties, String prefix, boolean setToNullMissingProperties) throws IntrospectionException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		String objectClassName = properties.getProperty(getChildPrefix(prefix, "$class"));
		if (objectClassName == null)
			return null;
		return propertiesToObject(properties, prefix, objectClassName, setToNullMissingProperties);
	}

	private static void internalPropertiesToObject(Properties properties, String prefix, Object object, boolean setToNullMissingProperties) throws IntrospectionException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		if (!hasPropertiesStartingWith(properties, prefix) && setToNullMissingProperties) {
			return;
		}
		Class currClass = object.getClass();
		while (currClass != null) {
			for (Field field : currClass.getDeclaredFields()) {
				int modifiers = field.getModifiers();
				if (Modifier.isTransient(modifiers))
					continue;
				if (Modifier.isStatic(modifiers))
					continue;
				Class fieldType = field.getType();
				field.setAccessible(true);
				if (Modifier.isFinal(modifiers)) {
					if (primitiveClasses.containsKey(fieldType) || fieldType.isEnum())
						continue;

					Object value = field.get(object);
					if (value == null)
						continue;
					if (fieldType.getComponentType() != null) {
						// array
						String fieldClassName = "[" + fieldType.getComponentType().getName() + "]";
						Object value2 = propertiesToObject(properties, getChildPrefix(prefix, field.getName()), fieldClassName, setToNullMissingProperties);
						int size = Array.getLength(value);
						int size2 = Array.getLength(value2);
						for (int i = Math.min(size, size2) - 1; i >= 0; i--) {
							Object o = Array.get(value2, i);
							Array.set(value, i, o);
						}
					} else {
						internalPropertiesToObject(properties, getChildPrefix(prefix, field.getName()), value, setToNullMissingProperties);
					}
					continue;
				}
				boolean fieldItemNeedClassTag = true;
				if (primitiveClasses.containsKey(fieldType) || fieldType.isEnum() || Modifier.isFinal(fieldType.getModifiers())) {
					fieldItemNeedClassTag = false;
				}
				Object value;
				if (fieldItemNeedClassTag)
					value = propertiesToObject(properties, getChildPrefix(prefix, field.getName()), setToNullMissingProperties);
				else {
					String fieldClassName;
					if (fieldType.getComponentType() != null) {
						// array
						fieldClassName = "[" + fieldType.getComponentType().getName() + "]";
					} else {
						fieldClassName = fieldType.getName();
					}
					value = propertiesToObject(properties, getChildPrefix(prefix, field.getName()), fieldClassName, setToNullMissingProperties);
				}

				if (value == null) {
					if (fieldType.isPrimitive() || (!setToNullMissingProperties))
						continue;
				}
				field.setAccessible(true);
				field.set(object, value);
			}
			currClass = currClass.getSuperclass();
		}
	}
	
	private static Object propertiesToObject(Properties properties, String prefix, String objectClassName, boolean setToNullMissingProperties) throws IntrospectionException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		if (objectClassName == null)
			return null;
		if (objectClassName.startsWith("[") && objectClassName.endsWith("]")) {
			// array
			objectClassName = objectClassName.substring(1, objectClassName.length() - 1);
			Class arrayType = primitiveClasses.get(objectClassName);
			boolean arrayItemsNeedClassTag = false;
			if (arrayType == null) {
				arrayItemsNeedClassTag = true;
				arrayType = Class.forName(objectClassName);
			}
			if (arrayType.isEnum() || Modifier.isFinal(arrayType.getModifiers()))
				arrayItemsNeedClassTag = false;
			
			String arraySizeStr = properties.getProperty(getChildPrefix(prefix, "$size"));
			if (arraySizeStr == null)
				return null;
			int arraySize = Integer.parseInt(arraySizeStr);
			if (arraySize < 0)
				return null;
			
			Object array = Array.newInstance(arrayType, arraySize);
			for (int i = 0; i < arraySize; i++) {
				String itemPrefix = getChildPrefix(prefix, Integer.toString(i));
				Object item;
				if (arrayItemsNeedClassTag)
					item = propertiesToObject(properties, itemPrefix, setToNullMissingProperties);
				else
					item = propertiesToObject(properties, itemPrefix, objectClassName, setToNullMissingProperties);
				if (item == null) {
					if (arrayType.isPrimitive() || (!setToNullMissingProperties))
						continue;
				}
				Array.set(array, i, item);
			}
			return array;
		}
		
		Class objectClass = primitiveClasses.get(objectClassName);
		if (objectClass == null) {
			objectClass = Class.forName(objectClassName);
		}
		
		String sval = properties.getProperty(prefix);
		if ((objectClass == boolean.class) ||
			(objectClass == Boolean.class)) {
			return (sval == null) ? null : new Boolean(sval);
		}
		if ((objectClass == byte.class) ||
			(objectClass == Byte.class)) {
			return (sval == null) ? null : new Byte(sval);
		}
		if ((objectClass == char.class) ||
			(objectClass == Character.class)) {
			return ((sval != null) && (sval.length() > 0)) ? null : new Character(sval.charAt(0));
		}
		if ((objectClass == double.class) ||
			(objectClass == Double.class)) {
			return (sval == null) ? null : new Double(sval);
		}
		if ((objectClass.isEnum()) || (objectClass == Enum.class)) {
			return (sval == null) ? null : Enum.valueOf((Class<Enum>) objectClass, sval);
		}
		if ((objectClass == float.class) ||
			(objectClass == Float.class)) {
			return (sval == null) ? null : new Float(sval);
		}
		if ((objectClass == int.class) ||
			(objectClass == Integer.class)) {
			return (sval == null) ? null : new Integer(sval);
		}
		if ((objectClass == long.class) ||
			(objectClass == Long.class)) {
			return (sval == null) ? null : new Long(sval);
		}
		if ((objectClass == short.class) ||
			(objectClass == Short.class)) {
			return (sval == null) ? null : new Short(sval);
		}
		if (objectClass == String.class) {
			return (sval == null) ? null : sval;
		}

		if (!hasPropertiesStartingWith(properties, prefix) && setToNullMissingProperties) {
			return null;
		}
		Object object = objectClass.newInstance();
		internalPropertiesToObject(properties, prefix, object, setToNullMissingProperties);
		return object;
	}

	public static void objectToProperties(Properties properties, String prefix, Object object) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		objectToProperties(properties, prefix, object, true);
	}
	
	private static void objectToProperties(Properties properties, String prefix, Object object, boolean needsClassTag) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (object == null)
			return;
		Class objectClass = object.getClass();
		if (primitiveClasses.containsKey(objectClass.getName()) || objectClass.isEnum()) {
			if (needsClassTag)
				properties.setProperty(getChildPrefix(prefix, "$class"), objectClass.getName());
			properties.setProperty(prefix, object.toString());
		} else if (objectClass.getComponentType() != null) {
			// array
			Class arrayType = objectClass.getComponentType();
			if (needsClassTag)
				properties.setProperty(getChildPrefix(prefix, "$class"), "[" + arrayType.getName() + "]");
			boolean arrayItemsNeedClassTag = true;
			if (primitiveClasses.containsKey(arrayType) || arrayType.isEnum() || Modifier.isFinal(arrayType.getModifiers())) {
				arrayItemsNeedClassTag = false;
			}
			int length = Array.getLength(object);
			properties.setProperty(getChildPrefix(prefix, "$size"), Integer.toString(length));
			for (int i = 0; i < length; i++) {
				Object o = Array.get(object, i);
				String itemPrefix = getChildPrefix(prefix, Integer.toString(i));
				objectToProperties(properties, itemPrefix, o, arrayItemsNeedClassTag);
			}
		} else {
			if (needsClassTag)
				properties.setProperty(getChildPrefix(prefix, "$class"), objectClass.getName());
			Class currClass = objectClass;
			while (currClass != null) {
				for (Field field : currClass.getDeclaredFields()) {
					int modifiers = field.getModifiers();
					if (Modifier.isTransient(modifiers))
						continue;
					if (Modifier.isStatic(modifiers))
						continue;
					Class fieldType = field.getType();
					field.setAccessible(true);
					boolean fieldItemNeedClassTag = true;
					if (Modifier.isFinal(modifiers)) {
						if (primitiveClasses.containsKey(fieldType) || fieldType.isEnum())
							continue;
						fieldItemNeedClassTag = false;
					}
					if (primitiveClasses.containsKey(fieldType) || fieldType.isEnum() || Modifier.isFinal(fieldType.getModifiers())) {
						fieldItemNeedClassTag = false;
					}
					Object value = field.get(object);
					objectToProperties(properties, getChildPrefix(prefix, field.getName()), value, fieldItemNeedClassTag);
				}
				currClass = currClass.getSuperclass();
			}
		}
	}
}
