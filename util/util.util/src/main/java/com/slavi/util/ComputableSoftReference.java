package com.slavi.util;

import java.lang.ref.SoftReference;

public abstract class ComputableSoftReference<T> {

	SoftReference<T> r = null;

	public T get() throws Exception {
		T t = null;
		synchronized(this) {
			if (r != null) {
				t = r.get();
				if (t == null)
					r = null;
			}
		}
		if (t == null) {
			t = compute();
			synchronized(this) {
				if (r == null)
					r = new SoftReference<T>(t);
			}
		}
		return t;
	}

	protected abstract T compute() throws Exception;
}
