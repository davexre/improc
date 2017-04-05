package com.slavi.test.mock;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.sf.cglib.beans.ImmutableBean;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

// http://mydailyjava.blogspot.bg/2013/11/cglib-missing-manual.html
public class TestMock {

	public static class MockMe implements Serializable {
//		public String publicField = "publicField";
		String name = "MockMe";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getSomeInt() {
			System.out.println("SomeInt");
			return 5;
		}
	}

	public static class MyHandler implements MethodInterceptor {
/*
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName() == "getName" && method.getParameterCount() == 0)
				return "WTF?";
			return method.invoke(o, args);
		}
*/
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			if (method.getName() == "getName" && method.getParameterCount() == 0)
				return "WTF?";
			System.out.println("--> " + method.getName());
			return proxy.invokeSuper(obj, args);
		}
	}
	/*
	public static class MyCallback extends CallbackHelper {
		Object o;
		public MyCallback(Object o) {
			super(arg0, arg1)
			this.o = o;
		}
		@Override
		protected Object getCallback(Method method) {
			// TODO Auto-generated method stub
			return null;
		}
	}*/

	void doIt() throws Exception {
		MockMe m = new MockMe();

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(m.getClass());
		enhancer.setCallback(new MyHandler());
		//enhancer.setCallbackFilter(new MyCallback());
		System.out.println(m.getName());
		m = (MockMe) enhancer.create();
		System.out.println(m.getName());
//		System.out.println(m.publicField);
		System.out.println(m.getSomeInt());
		System.out.println(m.getClass().getName());
		/*
		m = new MockMe();
		m = (MockMe) ImmutableBean.create(m);
		System.out.println(m.getClass().getName());
		m = new MockMe();
		m = (MockMe) ImmutableBean.create(m);
		System.out.println(m.getClass().getName());
		 */


/*		m = (MockMe) Proxy.newProxyInstance(m.getClass().getClassLoader(), new Class[] { m.getClass() }, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				System.out.println("Invoked " + method.getName());
				return null;
			}
		});*/

		//MockMe mocked = Mockito.mock(MockMe.class);
		//System.out.println(mocked.getName());
	}

	public static void main(String[] args) throws Exception {
		new TestMock().doIt();
		System.out.println("Done.");
	}
}
