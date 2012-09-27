package com.slavi.io;

import java.beans.IntrospectionException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ObjectToProperties {
	public static class Read implements ObjectRead {
		public ArrayList readObjectIds = new ArrayList();

		Properties properties;
		
		boolean setToNullMissingProperties;

		public Read(Properties properties) {
			this(properties, true);
		}
		
		public Read(Properties properties, boolean setToNullMissingProperties) {
			this.properties = properties;
			this.setToNullMissingProperties = setToNullMissingProperties;
		}
		
		public void loadPropertiesToObject(String prefix, Object object) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException {
			if (object == null) {
				return;
			}
			if (setToNullMissingProperties && (!Utils.hasPropertiesStartingWith(properties, prefix))) {
				return;
			}
			Class currClass = object.getClass();
			while (currClass != null) {
				Field fields[] = currClass.getDeclaredFields();
				Arrays.sort(fields, new Comparator<Field>() {
					public int compare(Field f1, Field f2) {
						return f1.getName().compareTo(f2.getName());
					}
				});
				for (Field field : fields) {
					int modifiers = field.getModifiers();
					if (field.isSynthetic())
						continue;
					if (Modifier.isTransient(modifiers))
						continue;
					if (Modifier.isStatic(modifiers))
						continue;
					Class fieldType = field.getType();
					field.setAccessible(true);
					String fieldPrefix = Utils.getChildPrefix(prefix, field.getName());

					String classStr = properties.getProperty(Utils.getChildPrefix(fieldPrefix, "$class"));
					if (classStr == null)
						classStr = Utils.computeClassTag(fieldType);
					Object value = propertiesToObject(fieldPrefix, classStr);
					if (value == null) {
						if (fieldType.isPrimitive() || (!setToNullMissingProperties))
							continue;
					}
					field.set(object, value);
				}
				currClass = currClass.getSuperclass();
			}
		}
		
		public Object propertiesToObject(String prefix) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
			String objectClassName = properties.getProperty(Utils.getChildPrefix(prefix, "$class"));
			if (objectClassName == null)
				return null;
			return propertiesToObject(prefix, objectClassName);
		}
		
		public Object propertiesToObject(String prefix, String objectClassName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
			if (objectClassName == null)
				return null;
			if (objectClassName.startsWith("$ref$")) {
				int index = Integer.parseInt(objectClassName.substring("$ref$".length()));
				Object o = readObjectIds.get(index - 1);
				return o;
			}
			Class objectClass = Utils.getClassFromClassTag(objectClassName);
			if (objectClass.getComponentType() != null) {
				// array
				Class arrayType = objectClass.getComponentType();
				String arrayItemType = objectClassName.substring(1, objectClassName.length() - 1);
				String arraySizeStr = properties.getProperty(Utils.getChildPrefix(prefix, "$size"));
				if (arraySizeStr == null)
					return null;
				int arraySize = Integer.parseInt(arraySizeStr);
				if (arraySize < 0)
					return null;
				Object array = Array.newInstance(arrayType, arraySize);
				readObjectIds.add(array);

				boolean arrayItemsNeedClassTag = Utils.isClassTagNeeded(objectClass);
				for (int i = 0; i < arraySize; i++) {
					String itemPrefix = Utils.getChildPrefix(prefix, Integer.toString(i));
					Object item;
					if (arrayItemsNeedClassTag)
						item = propertiesToObject(itemPrefix);
					else
						item = propertiesToObject(itemPrefix, arrayItemType);
					if (item == null) {
						if (arrayType.isPrimitive() || (!setToNullMissingProperties))
							continue;
					}
					Array.set(array, i, item);
				}
				return array;
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

			if (!Utils.hasPropertiesStartingWith(properties, prefix) && setToNullMissingProperties) {
				return null;
			}
			Object object = objectClass.newInstance();
			readObjectIds.add(object);
			loadPropertiesToObject(prefix, object);
			return object;
		}

		int readCounter = 0;

		public Object read() throws Exception {
			String prefix = "$object$" + Integer.toString(++readCounter);
			return propertiesToObject(prefix);
		}
	}
	
	public static class Write implements ObjectWrite {
		public Map<Object, Integer> writeObjectIds = new HashMap<Object, Integer>();
		
		Properties properties;
		
		public Write(Properties properties) {
			this.properties = properties;
		}
		
		public void objectToProperties(String prefix, Object object) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			objectToProperties(prefix, object, true);
		}
		
		public void objectToProperties(String prefix, Object object, boolean needsClassTag) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			if (object == null)
				return;
			Integer objectId = writeObjectIds.get(object);
			if (objectId != null) {
				properties.setProperty(Utils.getChildPrefix(prefix, "$class"), "$ref$" + objectId);
				return;
			}
			Class objectClass = object.getClass();
			String objectClassName = Utils.computeClassTag(objectClass);
			if (Utils.primitiveClasses.containsKey(objectClassName) || objectClass.isEnum()) {
				if (needsClassTag) {
					properties.setProperty(Utils.getChildPrefix(prefix, "$class"), objectClassName);
				}
				properties.setProperty(prefix, object.toString());
			} else if (objectClass.getComponentType() != null) {
				// array
				if (needsClassTag) {
					properties.setProperty(Utils.getChildPrefix(prefix, "$class"), objectClassName);
				}
				writeObjectIds.put(object, writeObjectIds.size() + 1);
				boolean arrayItemsNeedClassTag = Utils.isClassTagNeeded(objectClass.getComponentType());
				int length = Array.getLength(object);
				properties.setProperty(Utils.getChildPrefix(prefix, "$size"), Integer.toString(length));
				for (int i = 0; i < length; i++) {
					Object o = Array.get(object, i);
					String itemPrefix = Utils.getChildPrefix(prefix, Integer.toString(i));
					objectToProperties(itemPrefix, o, arrayItemsNeedClassTag);
				}
			} else {
				if (needsClassTag) {
					properties.setProperty(Utils.getChildPrefix(prefix, "$class"), objectClassName);
				}
				writeObjectIds.put(object, writeObjectIds.size() + 1);
				Class currClass = objectClass;
				while (currClass != null) {
					Field fields[] = currClass.getDeclaredFields();
					Arrays.sort(fields, new Comparator<Field>() {
						public int compare(Field f1, Field f2) {
							return f1.getName().compareTo(f2.getName());
						}
					});
					for (Field field : fields) {
						int modifiers = field.getModifiers();
						if (field.isSynthetic())
							continue;
						if (Modifier.isTransient(modifiers))
							continue;
						if (Modifier.isStatic(modifiers))
							continue;
						Class fieldType = field.getType();
						field.setAccessible(true);
						boolean fieldItemNeedClassTag = Utils.isClassTagNeeded(fieldType);
						Object value = field.get(object);
						objectToProperties(Utils.getChildPrefix(prefix, field.getName()), value, fieldItemNeedClassTag);
					}
					currClass = currClass.getSuperclass();
				}
			}
		}

		int writeCounter = 0;

		public void write(Object object) throws Exception {
			String prefix = "$object$" + Integer.toString(++writeCounter);
			objectToProperties(prefix, object);
		}
	}
}
