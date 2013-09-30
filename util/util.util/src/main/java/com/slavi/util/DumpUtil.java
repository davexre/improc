package com.slavi.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class contains utility methods for debugging purposes. 
 */
public class DumpUtil {
	
	/**
	 * This method displays the name of the method that invoked
	 * the method, that is calling showWhoIsCallingMe.
	 * <p>Example:
	 * <pre>
	 * package edu.test;
	 * public class DumpTest {
	 *   void method1() {
	 *     method2();
	 *   }
	 * 
	 *   void method2() {
	 *     showWhoIsCallingMe();
	 *   }
	 * }
	 * </pre>
	 * <p>The output of the code above will be:
	 * <pre>===== Called from edu.test.DumpTest.method1() =====</pre>
	 */
	public static void showWhoIsCallingMe() {
		StackTraceElement ste[] = Thread.currentThread().getStackTrace();
		StackTraceElement s = ste[3];
		System.out.println("===== Called from " + s.toString() + " =====");
	}
	
	/**
	 * Reads all public fields of the object displaying their 
	 * names and values on the system console.
	 * @param object		the object to be displayed
	 * @see #showObject(Object, boolean, boolean)
	 */
	public static void showObject(Object object) {
		showObject(object, false, false);
	}
	
	/**
	 * Reads all fields and methods of the object displaying their 
	 * names and values on the system console.
	 * @param object		the object to be displayed.
	 * @param showHidden	if false the hidden (private) fields and 
	 * 						methods will not be displayed.
	 * @param showMethods	if false the method names will not be displayed.
	 */
	public static void showObject(Object object, boolean showHidden, boolean showMethods) {
		if (object == null) {
			System.out.println("Show object: Object is null.");
			return;
		}
		Class<?> c = object.getClass();
		System.out.println("Show object:" + c.getName());
		System.out.println("  toString()=" + object.toString());
		ArrayList<String> lst = new ArrayList<String>();
		Class<?> loopC = c;
		while (loopC != null) {
			Field[] fields = loopC.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				if (showHidden || Modifier.isPublic(f.getModifiers())) {
					boolean isAccessible = f.isAccessible();
					Object fldVal = null;
					try {
						f.setAccessible(true);
						fldVal = f.get(object);
					} catch (Exception e) {
						//e.printStackTrace();
					}
					lst.add(f.getName() + ":" + f.getType().getName() + "=" + fldVal);
					try {
						f.setAccessible(isAccessible);
					} catch (Exception e) {
					}
				}
			}
			loopC = loopC.getSuperclass();
		}
		Collections.sort(lst, String.CASE_INSENSITIVE_ORDER);
		for (int i = 0; i < lst.size(); i++)
			System.out.println("  " + lst.get(i));
		
		if (!showMethods) 
			return;
		loopC = c;
		lst.clear();
		while (loopC != null) {
			Method[] methods = loopC.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				Method m = methods[i];
				if ((!m.isAccessible()) && (!showHidden))
					continue;
				if (m.getGenericParameterTypes().length != 0) 
					continue;
				Class<?> retClass = m.getReturnType();  
				if (retClass == Void.TYPE) 
					continue;
				if (!((m.getName().indexOf("get") == 0) || (m.getName().indexOf("is") == 0)))
					continue;
				Object methodVal = null;
				try {
					m.setAccessible(true);
					methodVal = m.invoke(object, (Object[]) null);
				} catch (Exception e) {
					//e.printStackTrace();
				}
				lst.add(m.getName() + "():" + retClass.getName() + "=" + methodVal);
			}
			loopC = loopC.getSuperclass();
		}
		Collections.sort(lst, String.CASE_INSENSITIVE_ORDER);
		for (int i = 0; i < lst.size(); i++)
			System.out.println("  " + lst.get(i));
	}
	
//	public static void showHttpParameterValues(HttpServletRequest request)
//	{
//		System.out.println("===== Start new debug SESSION =====");
//		Enumeration names = request.getParameterNames();
//		while (names.hasMoreElements()) {
//			String name = (String) names.nextElement();
//			String values[] = request.getParameterValues(name);
//			if (values != null) {
//				for (int i = 0; i < values.length; i++) {
//					System.out.println(name
//							+ ((values.length > 1) ? " (" + i + "): " : ": ")
//							+ values[i]);
//				}
//			}
//		}
//	}
}
