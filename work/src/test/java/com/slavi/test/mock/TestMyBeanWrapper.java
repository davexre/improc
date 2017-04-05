package com.slavi.test.mock;

import org.objectweb.asm.ClassVisitor;

import net.sf.cglib.core.AbstractClassGenerator;
import net.sf.cglib.core.ReflectUtils;

public class TestMyBeanWrapper {

	public static class MyClassGenerator extends AbstractClassGenerator {
		private static final Source SOURCE = new Source(MyClassGenerator.class.getName());
		private static final Class[] OBJECT_CLASSES = { Object.class };

		Object bean;
		Class target;

		public MyClassGenerator(Object bean) {
			super(SOURCE);
			this.bean = bean;
			target = bean.getClass();
		}

		public void generateClass(ClassVisitor v) throws Exception {

		}

		protected ClassLoader getDefaultClassLoader() {
			return target.getClassLoader();
		}

		protected Object firstInstance(Class type) throws Exception {
			return ReflectUtils.newInstance(type, OBJECT_CLASSES, new Object[]{ bean });
		}

		protected Object nextInstance(Object instance) throws Exception {
			return firstInstance(instance.getClass());
		}
	}
}
