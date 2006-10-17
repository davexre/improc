package com.slavi.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;


public class DumpUtils {
	
	public String asd;
	
	public static void showWhoIsCallingMe() {
		StackTraceElement ste[] = Thread.currentThread().getStackTrace();
		StackTraceElement s = ste[3];
		System.out.println("===== Called from " + s.toString() + " =====");
	}

	public static void showObject(Object o) {
		showObject(o, false, false);
	}
	
	public static void showObject(Object o, boolean showHidden, boolean showMethods) {
		if (o == null) {
			System.out.println("Show object: Object is null.");
			return;
		}
		Class c = o.getClass();
		System.out.println("Show object:" + c.getName());
		System.out.println("  toString()=" + o.toString());
		ArrayList lst = new ArrayList();
		Class loopC = c;
		while (loopC != null) {
			Field[] fields = loopC.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				Object fldVal = null;
				if ((!f.isAccessible()) && (!showHidden))
					continue;
				try {
					f.setAccessible(true);
					fldVal = f.get(o);
				} catch (Exception e) {
					//e.printStackTrace();
				}
				lst.add(f.getName() + ":" + f.getType().getName() + "=" + fldVal);
			}
			loopC = loopC.getSuperclass();
		}
		Collections.sort(lst, String.CASE_INSENSITIVE_ORDER);
		for (int i = 0; i < lst.size(); i++)
			System.out.println("  " + (String)lst.get(i));
		
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
				Class retClass = m.getReturnType();  
				if (retClass == Void.TYPE) 
					continue;
				if (!((m.getName().indexOf("get") == 0) || (m.getName().indexOf("is") == 0)))
					continue;
				Object methodVal = null;
				try {
					m.setAccessible(true);
					methodVal = m.invoke(o, (Object[]) null);
				} catch (Exception e) {
					//e.printStackTrace();
				}
				lst.add(m.getName() + "():" + retClass.getName() + "=" + methodVal);
			}
			loopC = loopC.getSuperclass();
		}
		Collections.sort(lst, String.CASE_INSENSITIVE_ORDER);
		for (int i = 0; i < lst.size(); i++)
			System.out.println("  " + (String)lst.get(i));
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
	
	public static void main(String[] args) {
		DumpUtils du = new DumpUtils();
		DumpUtils.showObject(du);
	}
}
