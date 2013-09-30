package com.slavi.util.io;

import java.beans.IntrospectionException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import sun.reflect.ReflectionFactory;

public class ObjectToXML {
	private static class CompareByIndex implements Comparator<Element> {
		public int compare(Element o1, Element o2) {
			int i1 = Integer.parseInt(com.slavi.util.Util.trimNZ(o1.getAttributeValue("index")));
			int i2 = Integer.parseInt(com.slavi.util.Util.trimNZ(o2.getAttributeValue("index")));
			return Integer.compare(i1, i2);
		}
	}
	
	public static class Read implements ObjectRead {
		public ArrayList readObjectIds = new ArrayList();

		List<Element> itemElements;

		boolean setToNullMissingProperties;

		public Read(Element root) {
			this(root, true);
		}
		
		public Read(Element root, boolean setToNullMissingProperties) {
			itemElements = root.getChildren("object");
			Collections.sort(itemElements, new CompareByIndex());
			this.setToNullMissingProperties = setToNullMissingProperties;
		}
		
		public void loadPropertiesToObject(Element element, Object object) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, SecurityException, NoSuchMethodException, InvocationTargetException {
			if (object == null) {
				return;
			}
			if (element == null) {
				return;
			}
			if (setToNullMissingProperties && (element.getChildren().size() == 0)) {
				return;
			}
			Class currClass = object.getClass();
			while ((currClass != null) && (element != null)) {
				Field fields[] = currClass.getDeclaredFields();
				Arrays.sort(fields, new Comparator<Field>() {
					public int compare(Field f1, Field f2) {
						return f1.getName().compareTo(f2.getName());
					}
				});
				for (Field field : fields) {
					int modifiers = field.getModifiers();
//					if (field.isSynthetic())
//						continue;
//					if (Modifier.isTransient(modifiers))
//						continue;
					if (Modifier.isStatic(modifiers))
						continue;
					Class fieldType = field.getType();
					field.setAccessible(true);
					
					String fieldName = field.getName();
					Element fieldElement = element;
					if (fieldName.startsWith("this$")) {
						fieldName = "this_" + fieldName.substring("this$".length());
						fieldElement = element.getChild("this");
						if (fieldElement != null)
							fieldElement = fieldElement.getChild(fieldName);
					} else {
						fieldElement = element.getChild(fieldName);
					}

					String classStr = "";
					if (fieldElement != null) {
						classStr = com.slavi.util.Util.trimNZ(fieldElement.getAttributeValue("class"));
					}
					if (classStr == "")
						classStr = Utils.computeClassTag(fieldType);
					Object value = xmlToObject(fieldElement, classStr);
					
					if (value == null) {
						if (fieldType.isPrimitive() || (!setToNullMissingProperties))
							continue;
					}
					field.set(object, value);
				}
				currClass = currClass.getSuperclass();
				element = element.getChild("parent");
			}
		}
		
		public Object xmlToObject(Element element) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
			if (element == null)
				return null;
			String objectClassName = com.slavi.util.Util.trimNZ(element.getAttributeValue("class"));
			if (objectClassName == "")
				return null;
			return xmlToObject(element, objectClassName);
		}
		
		public Object xmlToObject(Element element, String objectClassName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
			if (element == null)
				return null;
			objectClassName = com.slavi.util.Util.trimNZ(objectClassName);
			if (objectClassName == "")
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
				String arraySizeStr = com.slavi.util.Util.trimNZ(element.getAttributeValue("size"));
				if (arraySizeStr == null)
					return null;
				int arraySize = Integer.parseInt(arraySizeStr);
				if (arraySize < 0)
					return null;
				Object array = Array.newInstance(arrayType, arraySize);
				readObjectIds.add(array);

				boolean arrayItemsNeedClassTag = Utils.isClassTagNeeded(objectClass);
				List<Element> itemElements = element.getChildren("item");
				Collections.sort(itemElements, new CompareByIndex());

				for (int i = 0; i < itemElements.size(); i++) {
					Element itemElement = itemElements.get(i);
					int index = Integer.parseInt(com.slavi.util.Util.trimNZ(itemElement.getAttributeValue("index")));
					
					Object item;
					if (arrayItemsNeedClassTag)
						item = xmlToObject(itemElement);
					else
						item = xmlToObject(itemElement, arrayItemType);
					if (item == null) {
						if (arrayType.isPrimitive() || (!setToNullMissingProperties))
							continue;
					}
					Array.set(array, index, item);
				}
				
				return array;
			}
			
			String sval = element.getText();
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
			if (objectClass == Class.class) {
				Class r = Utils.getClassFromClassTag(sval);
				return r;
			}

			/*
			 * Creating new object WITHOUT invoking a constructor.
			 * Ideas borrowed from http://www.javaspecialists.eu/archive/Issue175.html
			 */
			ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
			Constructor objDef = Object.class.getDeclaredConstructor();
			Constructor intConstr = rf.newConstructorForSerialization(objectClass, objDef);
			Object object = intConstr.newInstance();
			
			readObjectIds.add(object);
			loadPropertiesToObject(element, object);
			return object;
		}

		int readCounter = 0;
		int itemCounter = 0;

		public Object read() throws Exception {
			readCounter++;
			Element el = itemElements.get(itemCounter);
			int i = Integer.parseInt(com.slavi.util.Util.trimNZ(el.getAttributeValue("index")));
			if (i != readCounter)
				return null;
			itemCounter++;
			return xmlToObject(el);
		}
	}
	
	public static class Write implements ObjectWrite {
		public Map<Object, Integer> writeObjectIds = new HashMap<Object, Integer>();
		
		Element root;
		
		public Write(Element root) {
			this.root = root;
		}
		
		public void objectToXML(Element element, Object object) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			objectToXML(element, object, true);
		}
		
		public void objectToXML(Element element, Object object, boolean needsClassTag) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			if (object == null)
				return;
			Integer objectId = writeObjectIds.get(object);
			if (objectId != null) {
				element.setAttribute("class", "$ref$" + objectId);
				return;
			}
			Class objectClass = object.getClass();
			String objectClassName = Utils.computeClassTag(objectClass);
			if (Utils.primitiveClasses.containsKey(objectClassName) || objectClass.isEnum()) {
				if (needsClassTag) {
					element.setAttribute("class", objectClassName);
				}
				element.setText(object.toString());
			} else if (objectClass == Class.class) {
				if (needsClassTag) {
					element.setAttribute("class", objectClassName); // "java.lang.Class"
				}
				String value = Utils.computeClassTag((Class) object);
				element.setText(value);
			} else if (objectClass.getComponentType() != null) {
				// array
				if (needsClassTag) {
					element.setAttribute("class", objectClassName);
				}
				writeObjectIds.put(object, writeObjectIds.size() + 1);
				boolean arrayItemsNeedClassTag = Utils.isClassTagNeeded(objectClass.getComponentType());
				int length = Array.getLength(object);
				element.setAttribute("size", Integer.toString(length));
				for (int i = 0; i < length; i++) {
					Object o = Array.get(object, i);
					if (o != null) {
						Element itemElement = new Element("item");
						itemElement.setAttribute("index", Integer.toString(i));
						objectToXML(itemElement, o, arrayItemsNeedClassTag);
						element.addContent(itemElement);
					}
				}
			} else {
				if (needsClassTag) {
					element.setAttribute("class", objectClassName);
				}
				writeObjectIds.put(object, writeObjectIds.size() + 1);
				Class currClass = objectClass;
				Element currElement = element;
				while (currClass != null) {
					Field fields[] = currClass.getDeclaredFields();
					Arrays.sort(fields, new Comparator<Field>() {
						public int compare(Field f1, Field f2) {
							return f1.getName().compareTo(f2.getName());
						}
					});
					for (Field field : fields) {
						int modifiers = field.getModifiers();
//						if (field.isSynthetic())
//							continue;
//						if (Modifier.isTransient(modifiers))
//							continue;
						if (Modifier.isStatic(modifiers))
							continue;
						Class fieldType = field.getType();
						field.setAccessible(true);
						boolean fieldItemNeedClassTag = Utils.isClassTagNeeded(fieldType);
						Object value = field.get(object);
						if (value != null) {
							String fieldName = field.getName();
							Element tmpElement = currElement;
							if (fieldName.startsWith("this$")) {
								fieldName = "this_" + fieldName.substring("this$".length());
								tmpElement = currElement.getChild("this");
								if (tmpElement == null) {
									tmpElement = new Element("this");
									currElement.addContent(tmpElement);
								}
							}
							Element itemElement = new Element(fieldName);
							objectToXML(itemElement, value, fieldItemNeedClassTag);
							tmpElement.addContent(itemElement);
						}
					}
					currClass = currClass.getSuperclass();
					Element next = new Element("parent");
					currElement.addContent(next);
					currElement = next;
				}
				while (currElement != element) {
					if (currElement.getChildren().size() != 0) {
						break;
					}
					currElement = currElement.getParentElement();
					currElement.removeChild("parent");
				}
			}
		}

		int writeCounter = 0;

		public void write(Object object) throws Exception {
			Element itemElement = new Element("object");
			itemElement.setAttribute("index", Integer.toString(++writeCounter));
			objectToXML(itemElement, object);
			root.addContent(itemElement);
		}
	}
}
