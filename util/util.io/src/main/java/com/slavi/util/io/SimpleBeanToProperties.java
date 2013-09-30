package com.slavi.util.io;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

public class SimpleBeanToProperties {
	public static class Read implements ObjectRead {
		Properties properties;
		
		boolean setToNullMissingProperties;
		
		public Read(Properties properties) {
			this(properties, true);
		}
		
		public Read(Properties properties, boolean setToNullMissingProperties) {
			this.properties = properties;
			this.setToNullMissingProperties = setToNullMissingProperties;
		}
		
		int readCounter = 0;

		public Object read() throws Exception {
			String prefix = "$object$" + Integer.toString(++readCounter);
			String className = properties.getProperty(prefix + ".$class");
			if (className == null)
				return null;
			Class clazz = Class.forName(className);
			return propertiesToObject(properties, prefix, clazz, setToNullMissingProperties);
		}
	}

	public static class Write implements ObjectWrite {
		Properties properties;
		
		public Write(Properties properties) {
			this.properties = properties;
		}
		
		int writeCounter = 0;

		public void write(Object object) throws Exception {
			String prefix = "$object$" + Integer.toString(++writeCounter);
			if (object == null)
				return;
			properties.setProperty(prefix + ".$class", object.getClass().getName());
			objectToProperties(properties, prefix, object);
		}
	}

	private static Object propertiesToObjectArray(Properties properties, String prefix, Class arrayType, boolean setToNullMissingProperties) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException {
		// array
		String arraySizeStr = properties.getProperty(Utils.getChildPrefix(prefix, "size"));
		if (arraySizeStr == null)
			return null;
		int arraySize = Integer.parseInt(arraySizeStr);
		if (arraySize < 0)
			return null;
		
		Object array = Array.newInstance(arrayType, arraySize);
		for (int i = 0; i < arraySize; i++) {
			String itemPrefix = Utils.getChildPrefix(prefix, Integer.toString(i));
			Object item = propertiesToObject(properties, itemPrefix, arrayType, setToNullMissingProperties);
			if (item == null) {
				if (arrayType.isPrimitive() || (!setToNullMissingProperties))
					continue;
			}
			Array.set(array, i, item);
		}
		return array;
	}
	
	public static <T> T propertiesToObject(Properties properties, String prefix, Class<T> objectClass, boolean setToNullMissingProperties) throws IntrospectionException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String sval = properties.getProperty(prefix);
		if ((objectClass == boolean.class) ||
			(objectClass == Boolean.class)) {
			if (sval != null)
				return (T) new Boolean(sval);
		} else if (
				(objectClass == byte.class) ||
				(objectClass == Byte.class)) {
			if (sval != null)
				return (T) new Byte(sval);
		} else if (objectClass == Character.class) {
			if ((sval != null) && (sval.length() > 0))
				return (T) new Character(sval.charAt(0));
		} else if ( 
				(objectClass == double.class) ||
				(objectClass == Double.class)) {
			if (sval != null)
				return (T) new Double(sval);
		} else if ((objectClass.isEnum()) || (objectClass == Enum.class)) {
			if (sval != null)
				return (T) Enum.valueOf((Class<Enum>) objectClass, sval);
		} else if (
				(objectClass == float.class) ||
				(objectClass == Float.class)) {
			if (sval != null)
				return (T) new Float(sval);
		} else if (
				(objectClass == int.class) ||
				(objectClass == Integer.class)) {
			if (sval != null)
				return (T) new Integer(sval);
		} else if (
				(objectClass == long.class) ||
				(objectClass == Long.class)) {
			if (sval != null)
				return (T) new Long(sval);
		} else if (
				(objectClass == short.class) ||
				(objectClass == Short.class)) {
			if (sval != null)
				return (T) new Short(sval);
		} else if (objectClass == String.class) {
			if (sval != null)
				return (T) sval;
		} else if (objectClass.getComponentType() != null) {
			// array
			Class arrayType = objectClass.getComponentType();
			return (T) propertiesToObjectArray(properties, prefix, arrayType, setToNullMissingProperties);
		} else if (Serializable.class.isAssignableFrom(objectClass)) {
			if (!Utils.hasPropertiesStartingWith(properties, prefix) && setToNullMissingProperties) {
				return null;
			}
			Serializable object = (Serializable) objectClass.getConstructor((Class[]) null).newInstance((Object[]) null);
			
			BeanInfo beanInfo = Introspector.getBeanInfo(objectClass);
			PropertyDescriptor pds[] = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				if (pd instanceof IndexedPropertyDescriptor) {
					continue;
				}
				
				Method write = pd.getWriteMethod();
				if (write == null)
					continue;

				Class propertyType = pd.getPropertyType();
				String propertyPrefix = Utils.getChildPrefix(prefix, pd.getName());
				Object o = propertiesToObject(properties, propertyPrefix, propertyType, setToNullMissingProperties);
				if (o == null) {
					if (propertyType.isPrimitive() || (!setToNullMissingProperties))
						continue;
				}
				write.invoke(object, new Object[] { o });
			}
			
			for (PropertyDescriptor pd : pds) {
				if (!(pd instanceof IndexedPropertyDescriptor)) {
					continue;
				}
				Method read = pd.getReadMethod();
				if (read == null)
					continue;

				IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
				if ((ipd.getWriteMethod() == null) && (ipd.getIndexedWriteMethod() == null))
					continue;
				
				Class propertyType = pd.getPropertyType();
				String propertyPrefix = Utils.getChildPrefix(prefix, pd.getName());
				Object items = propertiesToObject(properties, propertyPrefix, propertyType, setToNullMissingProperties);

				if (ipd.getWriteMethod() != null) {
					Method write = ipd.getWriteMethod();
					write.invoke(object, new Object[] { items });
				} else if (items != null) {
					Method write = ipd.getIndexedWriteMethod();
					int length = Array.getLength(items);
					for (int i = 0; i < length; i++) {
						Object item = Array.get(items, i);
						write.invoke(object, new Object[] { i, item });
					}
				}
			}
			
			return (T) object;
		}
		return null;
	}
	
	public static void objectToProperties(Properties properties, String prefix, Object object) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (object == null)
			return;
		Class objectClass = object.getClass();
		if (
			(objectClass == boolean.class) ||
			(objectClass == Boolean.class) ||
			(objectClass == byte.class) ||
			(objectClass == Byte.class) ||
			(objectClass == Character.class) ||
			(objectClass == double.class) ||
			(objectClass == Double.class) ||
			(objectClass == Enum.class) ||
			(objectClass.isEnum()) ||
			(objectClass == float.class) ||
			(objectClass == Float.class) ||
			(objectClass == int.class) ||
			(objectClass == Integer.class) ||
			(objectClass == long.class) ||
			(objectClass == Long.class) ||
			(objectClass == short.class) ||
			(objectClass == Short.class) ||
			(objectClass == String.class)) {
			properties.setProperty(prefix, object.toString());
		} else if (objectClass.getComponentType() != null) {
			// array
			int length = Array.getLength(object);
			properties.setProperty(Utils.getChildPrefix(prefix, "size"), Integer.toString(length));
			for (int i = 0; i < length; i++) {
				Object o = Array.get(object, i);
				String itemPrefix = Utils.getChildPrefix(prefix, Integer.toString(i));
				objectToProperties(properties, itemPrefix, o);
			}
		} else if (Serializable.class.isAssignableFrom(objectClass)) {
			BeanInfo beanInfo = Introspector.getBeanInfo(objectClass);
			PropertyDescriptor pds[] = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				Method read = pd.getReadMethod();
				if (read == null)
					continue;
				
				if (pd instanceof IndexedPropertyDescriptor) {
					IndexedPropertyDescriptor indexedProperty = (IndexedPropertyDescriptor) pd;
					if ((pd.getWriteMethod() == null) && (indexedProperty.getIndexedWriteMethod() == null))
						continue;
					
					Object array = read.invoke(object, (Object[]) null);
					objectToProperties(properties, Utils.getChildPrefix(prefix, indexedProperty.getName()), array);
					continue;
				} else {
					if (pd.getWriteMethod() == null)
						continue;
					Object value = read.invoke(object, (Object[]) null);
					objectToProperties(properties, Utils.getChildPrefix(prefix, pd.getName()), value);
				}
			}
		}
	}
}
