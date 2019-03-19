package com.slavi.util;

import java.lang.ref.SoftReference;

public abstract class ComputableSoftReference<T> {

	SoftReference<T> ref = null;

	public T get() throws Exception {
		T t = null;
		synchronized(this) {
			if (ref != null) {
				t = ref.get();
				if (t == null)
					ref = null;
			}
		}
		if (t == null) {
			t = compute();
			synchronized(this) {
				if (ref != null) {
					T tt = ref.get();
					if (tt == null)
						ref = new SoftReference<T>(t);
					else
						t = tt;
				} else {
					ref = new SoftReference<T>(t);
				}
			}
		}
		return t;
	}

	protected abstract T compute() throws Exception;
}
