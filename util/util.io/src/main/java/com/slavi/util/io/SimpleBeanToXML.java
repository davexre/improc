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

import org.jdom2.Element;

public class SimpleBeanToXML {
	public static class Read implements ObjectRead {
		Element root;
		
		boolean setToNullMissingProperties;
		
		public Read(Element root) {
			this(root, true);
		}
		
		public Read(Element root, boolean setToNullMissingProperties) {
			this.root = root;
			this.setToNullMissingProperties = setToNullMissingProperties;
		}
		
		int readCounter = 0;

		public Object read() throws Exception {
			String objectName = "object." + Integer.toString(++readCounter);
			Element el = root.getChild(objectName);
			if (el == null)
				return null;
			String className = el.getAttributeValue("class");
			if (className == null)
				return null;
			Class clazz = Class.forName(className);
			return xmlToObject(el, clazz, setToNullMissingProperties);
		}
	}

	public static class Write implements ObjectWrite {
		Element root;
		
		boolean setToNullMissingProperties;

		public Write(Element root) {
			this(root, true);
		}
		
		public Write(Element root, boolean setToNullMissingProperties) {
			this.root = root;
			this.setToNullMissingProperties = setToNullMissingProperties;
		}
		
		int writeCounter = 0;

		public void write(Object object) throws Exception {
			String objectName = "object." + Integer.toString(++writeCounter);
			if (object == null)
				return;
			Element el = new Element(objectName);
			el.setAttribute("class", object.getClass().getName());
			root.addContent(el);
			objectToXml(el, object);
		}
	}

	private static Object xmlToObjectArray(Element root, Class arrayType, boolean setToNullMissingProperties) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException {
		// array
		String arraySizeStr = root.getAttributeValue("size");
		if (arraySizeStr == null)
			return null;
		int arraySize = Integer.parseInt(arraySizeStr);
		if (arraySize < 0)
			return null;
		
		Object array = Array.newInstance(arrayType, arraySize);
		for (Object i : root.getChildren("item")) {
			Element e = (Element) i;
			int index = Integer.parseInt(e.getAttributeValue("index"));
			Object item = xmlToObject(e, arrayType, setToNullMissingProperties);
			if (item == null) {
				if (arrayType.isPrimitive() || (!setToNullMissingProperties))
					continue;
			}
			
			Array.set(array, index, item);
		}
		return array;
	}
	
	public static <T> T xmlToObject(Element root, Class<T> objectClass, boolean setToNullMissingProperties) throws IntrospectionException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (root == null)
			return null;
		String sval = root.getTextTrim();
		if ((objectClass == boolean.class) ||
			(objectClass == Boolean.class)) {
			return (T) new Boolean(sval);
		} else if (
				(objectClass == byte.class) ||
				(objectClass == Byte.class)) {
				return (T) new Byte(sval);
		} else if (objectClass == Character.class) {
			if (sval.length() > 0)
				return (T) new Character(sval.charAt(0));
		} else if ( 
				(objectClass == double.class) ||
				(objectClass == Double.class)) {
				return (T) new Double(sval);
		} else if ((objectClass.isEnum()) || (objectClass == Enum.class)) {
				return (T) Enum.valueOf((Class<Enum>) objectClass, sval);
		} else if (
				(objectClass == float.class) ||
				(objectClass == Float.class)) {
				return (T) new Float(sval);
		} else if (
				(objectClass == int.class) ||
				(objectClass == Integer.class)) {
				return (T) new Integer(sval);
		} else if (
				(objectClass == long.class) ||
				(objectClass == Long.class)) {
				return (T) new Long(sval);
		} else if (
				(objectClass == short.class) ||
				(objectClass == Short.class)) {
				return (T) new Short(sval);
		} else if (objectClass == String.class) {
			return (T) sval;
		} else if (objectClass.getComponentType() != null) {
			// array
			Class arrayType = objectClass.getComponentType();
			return (T) xmlToObjectArray(root, arrayType, setToNullMissingProperties);
		} else if (Serializable.class.isAssignableFrom(objectClass)) {
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
				Element item = root.getChild(pd.getName());
				Object o = null;
				if (item != null) {
					o = xmlToObject(item, propertyType, setToNullMissingProperties);
				}
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
				Element properties = root.getChild(pd.getName());
				Object items = null;
				if (properties != null) {
					items = xmlToObject(properties, propertyType, setToNullMissingProperties);
				}
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

	public static void objectToXml(Element root, Object object) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
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
			root.setText(object.toString());
		} else if (objectClass.getComponentType() != null) {
			// array
			int length = Array.getLength(object);
			root.setAttribute("size", Integer.toString(length));
			for (int i = 0; i < length; i++) {
				Object o = Array.get(object, i);
				if (o == null)
					continue;
				Element item = new Element("item");
				item.setAttribute("index", Integer.toString(i));
				objectToXml(item, o);
				root.addContent(item);
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
					if (array == null)
						continue;
					Element item = new Element(pd.getName());
					objectToXml(item, array);
					root.addContent(item);
					continue;
				} else {
					if (pd.getWriteMethod() == null)
						continue;
					Object value = read.invoke(object, (Object[]) null);
					if (value == null)
						continue;
					Element item = new Element(pd.getName());
					objectToXml(item, value);
					root.addContent(item);
				}
			}
		}
	}
	
}
