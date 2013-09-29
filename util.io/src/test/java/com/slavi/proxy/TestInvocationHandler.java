package com.slavi.proxy;

import java.lang.reflect.*;

public class TestInvocationHandler implements InvocationHandler {
	private Object testImpl;

	public TestInvocationHandler(Object impl) {
		this.testImpl = impl;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (Object.class == method.getDeclaringClass()) {
			String name = method.getName();
			if ("equals".equals(name)) {
				return proxy == args[0];
			} else if ("hashCode".equals(name)) {
				return System.identityHashCode(proxy);
			} else if ("toString".equals(name)) {
				return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy))
						+ ", with InvocationHandler " + this;
			} else {
				throw new IllegalStateException(String.valueOf(method));
			}
		}
		return method.invoke(testImpl, args);
	}
}